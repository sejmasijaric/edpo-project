package com.example.frontendspringbootservice;

import org.springframework.boot.SpringApplication;

public class TestFrontendSpringbootServiceApplication {

    public static void main(String[] args) {
        SpringApplication.from(FrontendSpringbootServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
