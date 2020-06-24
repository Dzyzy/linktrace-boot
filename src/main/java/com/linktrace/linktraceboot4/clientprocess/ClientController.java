package com.linktrace.linktraceboot4.clientprocess;

import com.alibaba.fastjson.JSON;
import com.linktrace.linktraceboot4.Utils;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class ClientController {

    public static List<String> endTraceIdList = new ArrayList<>();

    @RequestMapping("/findTraceId")
    public void filterData(@RequestParam String traceId) {
        if(ClientProcessData.endSpanMap.containsKey(traceId)) {
            try {
                RequestBody body = new FormBody.Builder()
                        .add("traceId", traceId)
                        .add("spanList", JSON.toJSONString(ClientProcessData.endSpanMap.get(traceId)))
                        .add("port", "0")
                        .build();
                Request request = new Request.Builder()
                        .url("http://localhost:8002/Summary")
                        .post(body)
                        .build();
                Response response = Utils.callHttp(request);
                response.close();
                ClientProcessData.endSpanMap.remove(traceId);
            } catch (IOException e) {
                System.out.println("发送失败");
            }
        }
        endTraceIdList.add(traceId);
    }
}
