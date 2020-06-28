package com.linktrace.linktraceboot4.backendprocess;

import com.linktrace.linktraceboot4.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;



@RestController
public class BackendController {

    private static final Logger log = LoggerFactory.getLogger(BackendController.class);

    public static volatile Integer FINISH_PROCESS_COUNT = 0;

    public static int num = 0;

    //接收过滤节点发过来的traceId
    @RequestMapping("/Summary")
    public void Summary(@RequestParam String mapJson, @RequestParam String port) {
        BackendWorking.traceIdQueue.offer(new Trace(port, mapJson));
    }

    //发送完成后，更改状态
    @RequestMapping("/finish")
    public void finish() {
        log.info("finish");
        FINISH_PROCESS_COUNT++;
    }

}
