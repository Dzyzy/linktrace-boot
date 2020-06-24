package com.linktrace.linktraceboot4;

import java.util.List;

public class Trace {
    private String traceId;
    private String prot;
    private String listJson;

    public Trace(String traceId, String prot, String listJson) {
        this.traceId = traceId;
        this.prot = prot;
        this.listJson = listJson;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getProt() {
        return prot;
    }

    public void setProt(String prot) {
        this.prot = prot;
    }

    public String getList() {
        return listJson;
    }

    public void setList(String listJson) {
        this.listJson = listJson;
    }
}
