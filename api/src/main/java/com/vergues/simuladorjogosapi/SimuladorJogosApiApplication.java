package com.vergues.simuladorjogosapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import com.vergues.simuladorjogosapi.service.CampeonatoService;

import java.time.Clock;
import java.time.ZoneId;

@SpringBootApplication
@EnableScheduling
public class SimuladorJogosApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimuladorJogosApiApplication.class, args);
    }

    @Bean
    Clock clock() {
        return Clock.system(ZoneId.of("America/Sao_Paulo"));
    }

    @Bean
    CommandLineRunner inicializar(CampeonatoService campeonato) {
        return args -> campeonato.inicializar();
    }
}
