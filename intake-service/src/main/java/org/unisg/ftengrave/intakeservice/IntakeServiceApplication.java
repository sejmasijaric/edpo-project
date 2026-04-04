package org.unisg.ftengrave.intakeservice;

import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableProcessApplication("intake-service")
public class IntakeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntakeServiceApplication.class, args);
    }
}
