package com.vergues.simuladorjogosapi.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vergues.simuladorjogosapi.dto.JogoResponse;
import com.vergues.simuladorjogosapi.model.Evento;
import com.vergues.simuladorjogosapi.model.Jogo;
import com.vergues.simuladorjogosapi.model.Time;
import com.vergues.simuladorjogosapi.repository.TimeRepository;
import com.vergues.simuladorjogosapi.service.SimuladorService;

@RestController
@RequestMapping("/api/jogos")
public class JogoController {

    private final SimuladorService simuladorService;
    private final TimeRepository timeRepository;

    public JogoController(
            SimuladorService simuladorService,
            TimeRepository timeRepository) {

        this.simuladorService =
                simuladorService;

        this.timeRepository =
                timeRepository;
    }

    @GetMapping("/simular")
    public JogoResponse simular(
            @RequestParam String casa,
            @RequestParam String fora) {

        Time timeCasa =
                timeRepository
                        .buscarPorSigla(casa);

        Time timeFora =
                timeRepository
                        .buscarPorSigla(fora);

        if (timeCasa == null) {
            throw new RuntimeException(
                    "Time da casa não encontrado: "
                            + casa
            );
        }

        if (timeFora == null) {
            throw new RuntimeException(
                    "Time de fora não encontrado: "
                            + fora
            );
        }

        Jogo jogo =
                simuladorService.simular(
                        timeCasa,
                        timeFora
                );

        List<Evento> eventos =
                simuladorService.gerarEventos(
                        timeCasa,
                        timeFora,
                        jogo.getGolsCasa(),
                        jogo.getGolsFora()
                );

        return new JogoResponse(
                timeCasa.getNome(),
                timeCasa.getSigla(),
                jogo.getGolsCasa(),
                timeFora.getNome(),
                timeFora.getSigla(),
                jogo.getGolsFora(),
                eventos
        );
    }
}