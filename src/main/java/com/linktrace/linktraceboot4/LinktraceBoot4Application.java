package com.linktrace.linktraceboot4;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class LinktraceBoot4Application {

    public static void main(String[] args) {
        String port = System.getProperty("server.port", "8080");
        SpringApplication.run(LinktraceBoot4Application.class, "--server.port=" + port);
    }

}
