package com.example.diplom;

import org.apache.coyote.Request;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.management.monitor.Monitor;
@SpringBootApplication
@EnableScheduling
public class DiplomApplication {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(DiplomApplication.class, args);
        RequestService serviceA = context.getBean(RequestService.class);
        Monitoring monitoring = context.getBean(Monitoring.class);
    }

}
