package org.unisg.ftengrave.manufacturingservice;

import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableProcessApplication("manufacturing-service")
public class ManufacturingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ManufacturingServiceApplication.class, args);
    }
}
