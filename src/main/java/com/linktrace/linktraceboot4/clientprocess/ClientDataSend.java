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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        Map<String, List<String>> map = new HashMap<>(20);
        while(true) {
            if(!traceIdQueue.isEmpty()) {
                List<String> list = traceIdQueue.poll();
                String[] strings = list.get(0).split("\\|");
                String traceId = strings[0];
                map.put(traceId, list);
            } else if(map.size() >= 15) {
                sendData(map, false);
                map = new HashMap<>(20);
            } else if(mark && traceIdQueue.isEmpty()) {
                if(map.size() > 0) {
                    sendData(map, true);
                }
                break;
            }
        }
    }

    //数据全部发送完成，发送请求给汇总节点
    private void finish() {
        try {
            Request request = new Request.Builder()
                    .url("http://localhost:8002/finish")
                    .build();
            Response response = Utils.callHttp(request);
            response.close();
        } catch (IOException e) {
            System.out.println("发送失败2");
        }
    }

    public void sendData(Map<String, List<String>> map, boolean flag) {
        try {
            RequestBody body = new FormBody.Builder()
                    .add("mapJson", JSON.toJSONString(map))
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
        if(flag) {
            finish();
        }
    }

}
