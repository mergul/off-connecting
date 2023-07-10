package com.busra.connecting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@EnableKafka
@EnableKafkaStreams
@ComponentScan(basePackages = {
        "com.busra.connecting.config",
        "com.busra.connecting.config.streams",
        "com.busra.connecting.config.web",
        "com.busra.connecting.config.producers",
        "com.busra.connecting.config.consumers",
        "com.busra.connecting.config.security",
        "com.busra.connecting.model",
        "com.busra.connecting.service",
        "com.busra.connecting.controller"
})
@SpringBootApplication
public class ConnectingApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConnectingApplication.class, args);
	}

}
