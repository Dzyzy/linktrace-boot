package com.linktrace.linktraceboot4.backendprocess;

import com.alibaba.fastjson.JSON;
import com.linktrace.linktraceboot4.CommonController;
import com.linktrace.linktraceboot4.Constants;
import com.linktrace.linktraceboot4.Utils;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;


public class BackendProcessData implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(BackendController.class);

    public static ConcurrentLinkedQueue<TraceIdBatch> traceIdQueue = new ConcurrentLinkedQueue<>();
    public static Map<String, String> TRACE_CHUCKSUM_MAP = new ConcurrentHashMap<>(20000);

    @Override
    public void run() {
        TraceIdBatch traceIdBatch = null;
        while(!(BackendController.FINISH_PROCESS_COUNT >= Constants.PROCESS_COUNT && traceIdQueue.isEmpty() && BackendWorking.mark)) {
            if(!traceIdQueue.isEmpty()) {
                traceIdBatch = traceIdQueue.poll();
                String spans = traceIdBatch.getTraceIdList().stream().sorted(
                        Comparator.comparing(BackendProcessData::getStartTime)).
                        collect(Collectors.joining("\n"));
                spans += "\n";
                TRACE_CHUCKSUM_MAP.put(traceIdBatch.getTraceId(), Utils.MD5(spans));
            }
        }
        Set<String> set = BackendWorking.traceIdBatchMap.keySet();
        for(String traceId : set) {
            traceIdBatch = BackendWorking.traceIdBatchMap.get(traceId);
            if(traceIdBatch.getProcessCount() > 0) {
                String spans = traceIdBatch.getTraceIdList().stream().sorted(
                        Comparator.comparing(BackendProcessData::getStartTime)).
                        collect(Collectors.joining("\n"));
                spans += "\n";
                TRACE_CHUCKSUM_MAP.put(traceIdBatch.getTraceId(), Utils.MD5(spans));
            }
        }
        sendCheckSum();
        System.out.println(BackendWorking.traceIdQueue.size());
    }

    private void sendCheckSum() {
        try {
            String result = JSON.toJSONString(TRACE_CHUCKSUM_MAP);
            RequestBody body = new FormBody.Builder()
                    .add("result", result).build();
            String url = String.format("http://localhost:%s/api/finished", CommonController.getDataSourcePort());
            Request request = new Request.Builder().url(url).post(body).build();
            Response response = Utils.callHttp(request);
            response.close();
        } catch (Exception e) {
            System.out.println("发送失败");
        }
    }

    public static long getStartTime(String span) {
        if (span != null) {
            String[] cols = span.split("\\|");
            if (cols.length > 8) {
                return Utils.toLong(cols[1], -1);
            }
        }
        return -1;
    }
}
