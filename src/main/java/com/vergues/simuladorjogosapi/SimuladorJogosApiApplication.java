package com.vergues.simuladorjogosapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SimuladorJogosApiApplication {

    public static void main(String[] args) {

        SpringApplication.run(
                SimuladorJogosApiApplication.class,
                args
        );
    }
}	