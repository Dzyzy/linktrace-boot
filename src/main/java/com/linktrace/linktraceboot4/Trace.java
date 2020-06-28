package com.linktrace.linktraceboot4;


public class Trace {
    private String prot;
    private String mapJson;

    public Trace(String prot, String mapJson) {
        this.prot = prot;
        this.mapJson = mapJson;
    }


    public String getProt() {
        return prot;
    }

    public void setProt(String prot) {
        this.prot = prot;
    }

    public String getMapJson() {
        return mapJson;
    }

    public void setMapJson(String mapJson) {
        this.mapJson = mapJson;
    }
}
