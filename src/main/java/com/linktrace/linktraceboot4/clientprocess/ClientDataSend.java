package com.linktrace.linktraceboot4.clientprocess;

import com.alibaba.fastjson.JSON;
import com.linktrace.linktraceboot4.Utils;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;


public class ClientDataSend implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ClientDataSend.class);

    public static boolean mark;
    public static ConcurrentLinkedQueue<List<String>> traceIdQueue;

    public ClientDataSend() {
        mark = false;
        traceIdQueue = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void run() {
        while(true) {
            if(!traceIdQueue.isEmpty()) {
                List<String> list = traceIdQueue.poll();
                String[] strings = list.get(0).split("\\|");
                String traceId = strings[0];
                sendData(traceId, list);
            } else if(mark && traceIdQueue.isEmpty()) {
                break;
            }
        }
    }

    public static void sendData(String traceId, List<String> spanList) {
        try {
            RequestBody body = new FormBody.Builder()
                    .add("traceId", traceId)
                    .add("spanList", JSON.toJSONString(spanList))
                    .add("port", System.getProperty("server.port", "8080"))
                    .build();
            Request request = new Request.Builder()
                    .url("http://localhost:8002/Summary")
                    .post(body)
                    .build();
            Response response =  Utils.callHttp(request);
            response.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
