package com.linktrace.linktraceboot4.clientprocess;


import com.linktrace.linktraceboot4.Span;
import com.linktrace.linktraceboot4.Utils;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ClientProcessData implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ClientProcessData.class);

    //存储多条span
    public static Map<String, List<String>> traceMap;
    //记录符合条件的traceId
    public static Set<String> badTraceIdList;
    //记录指定数据出现的位置
    public static ConcurrentLinkedQueue<Span> traceIdQueue;
    //存储traceId，并去重
    public static Set<String> traceIdSet;
    public static Map<String, List<String>> endSpanMap;
    public static Set<String> endTraceIdList;
    public static long num;
    public static boolean mark;

    public ClientProcessData() {
        traceMap = new HashMap<>(100000);
        badTraceIdList = new HashSet<>(100000);
        traceIdQueue = new ConcurrentLinkedQueue<>();
        traceIdSet = new HashSet<>(20005);
        endSpanMap = new HashMap<>(40005);
        endTraceIdList = new LinkedHashSet<>(40005);
        num = 0;
        mark = false;
    }


    @Override
    public void run() {
        log.info("过滤开始");
        try {
            String path = getPath();
            URL url = new URL(path);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
            InputStream inputStream = connection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                String[] strings = line.split("\\|");
                if (strings != null && strings.length > 1 ) {
                    String traceId = strings[0];
                    //放入队列，判断是否有20000条数据
                    if(!traceIdSet.contains(traceId)) {
                        traceIdQueue.add(new Span(traceId, num));
                    }
                    //放入set中，实现队列中的traceId去重
                    traceIdSet.add(traceId);
                    //把每条数据存入map+list


                    List<String> list = traceMap.get(traceId);
                    if(list == null) {
                        list = new ArrayList<>();
                        traceMap.put(traceId, list);
                    }
                    list.add(line);


                    //如果数据超过了20000条，并且是符合要求的traceId，就发送到汇总节点
                    if(!ClientProcessData.traceIdQueue.isEmpty() && ClientProcessData.num - ClientProcessData.traceIdQueue.peek().getPlace() > 20000) {
                        Span span = ClientProcessData.traceIdQueue.poll();
                        String traceid = span.getTraceId();

                        if(ClientProcessData.badTraceIdList.contains(traceid)) {
                            ClientDataSend.traceIdQueue.offer(traceMap.get(traceid));
                        } else if(ClientController.endTraceIdList.contains(traceid)) {
                            ClientDataSend.traceIdQueue.offer(traceMap.get(traceid));
                            ClientController.endTraceIdList.remove(traceid);
                        } else if(ClientProcessData.endTraceIdList.size() < 20000) {
                            ClientProcessData.endTraceIdList.add(traceid);
                            ClientProcessData.endSpanMap.put(traceid, ClientProcessData.traceMap.get(traceid));
                        }
                        ClientProcessData.badTraceIdList.remove(traceid);
                        ClientProcessData.traceMap.put(traceid, null);
                        ClientProcessData.traceIdSet.remove(traceid);
                    }

//                    if(ClientProcessData.traceIdQueue.size() > 1000) {
//                        while(true) {
//                            Thread.sleep(10);
//                            if(ClientProcessData.traceIdQueue.size() == 0) {
//                                break;
//                            }
//                        }
//                    }

                    //判断是否为符合要求的traceId
                    if (strings.length > 8) {
                        String tags = strings[8];
                        if (tags != null) {
                            if (tags.contains("error=1")) {
                                badTraceIdList.add(traceId);
                            } else if (tags.contains("http.status_code=") && tags.indexOf("http.status_code=200") < 0) {
                                badTraceIdList.add(traceId);
                            }
                        }
                    }
                }
                //记录文件的条数
                num++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //如果队列中还有数据，遍历队列，发送所有的数据
        if(traceMap.size() > 0) {
            while(!traceIdQueue.isEmpty()) {
                Span trace = traceIdQueue.poll();
                String traceid = trace.getTraceId();
                if(badTraceIdList.contains(traceid)) {
                    ClientDataSend.traceIdQueue.offer(traceMap.get(traceid));
                }
            }
        }
        //向汇总节点发送过滤完成的请求
        ClientDataSend.mark = true;
        finish();
        log.info("过滤结束");
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

    //获取静态文件的路径
    private String getPath(){
        String port = System.getProperty("server.port", "8080");
        if ("8000".equals(port)) {
            return "http://localhost:" + 80 + "/trace1.data";
        } else if ("8001".equals(port)){
            return "http://localhost:" + 80 + "/trace2.data";
        } else {
            return null;
        }
    }
}

