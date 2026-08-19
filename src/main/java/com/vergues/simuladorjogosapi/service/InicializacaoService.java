package com.vergues.simuladorjogosapi.service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class InicializacaoService implements CommandLineRunner {

    private final BrasileiraoService brasileiraoService;

    public InicializacaoService(
            BrasileiraoService brasileiraoService) {

        this.brasileiraoService =
                brasileiraoService;
    }

    @Override
    public void run(String... args) {

        brasileiraoService
                .gerarCalendario(2026);

        System.out.println(
                "Calendário do Brasileirão 2026 "
                + "gerado automaticamente."
        );
    }
}