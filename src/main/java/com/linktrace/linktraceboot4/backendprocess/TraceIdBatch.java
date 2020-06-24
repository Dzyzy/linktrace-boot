package com.linktrace.linktraceboot4.backendprocess;

import java.util.ArrayList;
import java.util.List;

public class TraceIdBatch {
    private String traceId = null;
    private int processCount = 0;
    private List<String> traceIdList = new ArrayList<>(2005);



    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public int getProcessCount() {
        return processCount;
    }

    public void setProcessCount(int processCount) {
        this.processCount = processCount;
    }

    public List<String> getTraceIdList() {
        return traceIdList;
    }

}
