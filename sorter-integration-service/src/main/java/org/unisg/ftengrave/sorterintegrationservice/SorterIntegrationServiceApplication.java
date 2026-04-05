package org.unisg.ftengrave.sorterintegrationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication(scanBasePackages = {
    "org.unisg.ftengrave.sorterintegrationservice",
    "org.unisg.mqttkafkabridge"
})
public class SorterIntegrationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SorterIntegrationServiceApplication.class, args);
    }
}
