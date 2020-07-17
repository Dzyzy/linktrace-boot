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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class ClientController {

    public static List<String> endTraceIdList = new ArrayList<>();

    @RequestMapping("/findTraceId")
    public String filterData(@RequestParam String traceId) {
        String json = "";
        if(ClientProcessData.endSpanMap.containsKey(traceId)) {
            json = JSON.toJSONString(ClientProcessData.endSpanMap.get(traceId));
            ClientProcessData.endSpanMap.remove(traceId);
        } else {
            if(endTraceIdList == null) {
                endTraceIdList = new ArrayList<>();
            }
            endTraceIdList.add(traceId);
        }
        return json;
    }
}
