package org.unisg.ftengrave.orderorchestrator;

import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableProcessApplication("order-orchestrator")
public class OrderOrchestratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderOrchestratorApplication.class, args);
    }
}
