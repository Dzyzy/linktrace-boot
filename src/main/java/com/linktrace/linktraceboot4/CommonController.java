package com.linktrace.linktraceboot4;


import com.linktrace.linktraceboot4.backendprocess.BackendProcessData;
import com.linktrace.linktraceboot4.backendprocess.BackendWorking;
import com.linktrace.linktraceboot4.clientprocess.ClientDataSend;
import com.linktrace.linktraceboot4.clientprocess.ClientProcessData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class CommonController {

    private static final Logger log = LoggerFactory.getLogger(CommonController.class);

    private static Integer DATA_SOURCE_PORT = 0;

    public static Integer getDataSourcePort() {
        return DATA_SOURCE_PORT;
    }

    @RequestMapping("/ready")
    public String ready() {
        log.info("程序开始");
        return "suc";
    }

    @RequestMapping("/setParameter")
    public String setParameter(@RequestParam Integer port) {
        DATA_SOURCE_PORT = port;
        if (Utils.isClientProcess()) {
            Thread thread1 = new Thread(new ClientProcessData(), "ProcessDataThread");
            thread1.start();
            Thread thread2 = new Thread(new ClientDataSend(), "DataJudgmentThread");
            thread2.start();
        } else if(Utils.isBackendProcess()) {
            Thread thread3 = new Thread(new BackendProcessData(), "PorcessDataThread");
            thread3.start();
            Thread thread4 = new Thread(new BackendWorking(), "WorkingThread");
            thread4.start();
        }
        return "suc";
    }
}
