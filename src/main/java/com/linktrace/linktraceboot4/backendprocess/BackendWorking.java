package com.linktrace.linktraceboot4.backendprocess;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.linktrace.linktraceboot4.Constants;
import com.linktrace.linktraceboot4.Trace;
import com.linktrace.linktraceboot4.Utils;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BackendWorking implements Runnable {

    public static Map<String, TraceIdBatch> traceIdBatchMap = new ConcurrentHashMap<>();
    public static ConcurrentLinkedQueue<Trace> traceIdQueue = new ConcurrentLinkedQueue<>();
    public static boolean mark = false;

    @Override
    public void run() {
        while(true) {
            if(!traceIdQueue.isEmpty()) {
                Trace trace = traceIdQueue.poll();
                String port = trace.getProt();
                Map<String, List<String>> map = JSON.parseObject(trace.getMapJson(), new TypeReference<Map<String, List<String>>>(){});
                Set<String> set = map.keySet();
                for(String traceId : set) {
                    List<String> list = map.get(traceId);
                    TraceIdBatch traceIdBatch = traceIdBatchMap.get(traceId);
                    if(traceIdBatch == null || traceIdBatch.getProcessCount() == 0) {
                        if(traceIdBatch == null) {
                            traceIdBatch = new TraceIdBatch();
                            traceIdBatchMap.put(traceId, traceIdBatch);
                        }
                        traceIdBatch.getTraceIdList().addAll(list);
                        traceIdBatch.setTraceId(traceId);
                        traceIdBatch.setProcessCount(traceIdBatch.getProcessCount() + 1);

                        if(!port.equals("0")) {
                            try {
                                String url = String.format("http://localhost:%s/findTraceId", port.equals(Constants.CLIENT_PROCESS_PORT1) ? Constants.CLIENT_PROCESS_PORT2 : Constants.CLIENT_PROCESS_PORT1);
                                RequestBody body = new FormBody.Builder()
                                        .add("traceId", traceId)
                                        .build();
                                Request request = new Request.Builder()
                                        .url(url)
                                        .post(body)
                                        .build();
                                Response response = Utils.callHttp(request);
                                String str = response.body().string();
                                if(!str.equals("")) {
                                    List<String> l = JSON.parseObject(str,
                                            new TypeReference<List<String>>() {});
                                    traceIdBatch.getTraceIdList().addAll(l);
                                    BackendProcessData.traceIdQueue.offer(traceIdBatch);
                                    traceIdBatchMap.put(traceId, new TraceIdBatch());
                                }
                                response.close();
                            } catch (IOException e) {
                                System.out.println("发送失败");
                            }
                        }
                    } else {
                        traceIdBatch.getTraceIdList().addAll(list);
                        BackendProcessData.traceIdQueue.offer(traceIdBatch);
                        traceIdBatchMap.put(traceId, new TraceIdBatch());
                    }
                }
            } else if(BackendController.FINISH_PROCESS_COUNT >= Constants.PROCESS_COUNT && traceIdQueue.isEmpty()) {
                mark = true;
                break;
            }
        }

    }
}
