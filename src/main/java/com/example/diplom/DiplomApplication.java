package com.example.diplom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DiplomApplication {
//boot
    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(DiplomApplication.class, args);
        RequestService requestService = context.getBean(RequestService.class);
        //Monitoring monitoring = context.getBean(Monitoring.class);
    }

}
