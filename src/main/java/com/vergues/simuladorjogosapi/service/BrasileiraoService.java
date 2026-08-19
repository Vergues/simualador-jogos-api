package com.vergues.simuladorjogosapi.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.vergues.simuladorjogosapi.model.Jogo;
import com.vergues.simuladorjogosapi.repository.JogoRepository;
import com.vergues.simuladorjogosapi.repository.TimeRepository;

@Service
public class BrasileiraoService {

    private final CalendarioService calendarioService;
    private final TimeRepository timeRepository;
    private final JogoRepository jogoRepository;

    public BrasileiraoService(
            CalendarioService calendarioService,
            TimeRepository timeRepository,
            JogoRepository jogoRepository) {

        this.calendarioService = calendarioService;
        this.timeRepository = timeRepository;
        this.jogoRepository = jogoRepository;
    }

    public void gerarCalendario(int ano) {

        List<Jogo> jogos =
                calendarioService.gerarBrasileirao(
                        timeRepository.listarTodos(),
                        ano
                );

        jogoRepository.salvarTodos(jogos);
    }

    public List<Jogo> listarTodos() {
        return jogoRepository.listarTodos();
    }

    public List<Jogo> buscarPorRodada(int rodada) {
        return jogoRepository.buscarPorRodada(rodada);
    }

    public List<Jogo> buscarPorData(LocalDate data) {
        return jogoRepository.buscarPorData(data);
    }

    public List<Jogo> buscarJogosDeHoje() {

        LocalDate hoje =
                LocalDate.now();

        return jogoRepository.buscarPorData(hoje);
    }

    public List<Jogo> buscarProximosJogos(
            int limite) {

        LocalDateTime agora =
                LocalDateTime.now();

        return jogoRepository.buscarProximos(
                agora,
                limite
        );
    }

    public Jogo buscarPorId(Long id) {
        return jogoRepository.buscarPorId(id);
    }
}