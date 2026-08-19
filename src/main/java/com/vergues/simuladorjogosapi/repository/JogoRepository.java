package com.vergues.simuladorjogosapi.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.vergues.simuladorjogosapi.model.Jogo;

@Repository
public class JogoRepository {

    private final List<Jogo> jogos = new ArrayList<>();

    public void salvarTodos(List<Jogo> novosJogos) {
        jogos.clear();
        jogos.addAll(novosJogos);
    }

    public List<Jogo> listarTodos() {
        return jogos;
    }

    public List<Jogo> buscarPorRodada(int rodada) {

        return jogos.stream()
                .filter(jogo ->
                        jogo.getRodada() == rodada
                )
                .toList();
    }

    public List<Jogo> buscarPorData(LocalDate data) {

        return jogos.stream()
                .filter(jogo ->
                        jogo.getDataHora() != null
                        && jogo.getDataHora()
                                .toLocalDate()
                                .equals(data)
                )
                .toList();
    }

    public List<Jogo> buscarProximos(
            LocalDateTime agora,
            int limite) {

        return jogos.stream()
                .filter(jogo ->
                        jogo.getDataHora() != null
                        && jogo.getDataHora()
                                .isAfter(agora)
                )
                .sorted(
                        Comparator.comparing(
                                Jogo::getDataHora
                        )
                )
                .limit(limite)
                .toList();
    }

    public Jogo buscarPorId(Long id) {

        return jogos.stream()
                .filter(jogo ->
                        jogo.getId() != null
                        && jogo.getId().equals(id)
                )
                .findFirst()
                .orElse(null);
    }
}