package com.linktrace.linktraceboot4;

public class Span {
    private String traceId;
    private long place;

    public Span(String traceId, long place) {
        this.traceId = traceId;
        this.place = place;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public long getPlace() {
        return place;
    }

    public void setPlace(long place) {
        this.place = place;
    }
}
