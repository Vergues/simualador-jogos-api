package com.vergues.simuladorjogosapi.dto;

import com.vergues.simuladorjogosapi.model.Jogo;

public record AtualizacaoPartidaRequest(Integer minuto, Integer golsMandante,
                                        Integer golsVisitante, Jogo.Status status,
                                        EventoRequest novoEvento) {
    public record EventoRequest(Integer minuto, String tipo, String descricao,
                                String siglaTime, String jogador) { }
}
