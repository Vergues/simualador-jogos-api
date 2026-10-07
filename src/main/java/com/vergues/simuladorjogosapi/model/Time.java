package com.vergues.simuladorjogosapi.model;

import java.util.List;
import java.text.Normalizer;
import java.util.Locale;

public record Time(String nome, String sigla, int forca, List<Jogador> jogadores,
                   String slug, String pais, String escudo) {
    public Time {
        jogadores = List.copyOf(jogadores);
        slug = slug == null ? Normalizer.normalize(nome, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-") : slug;
        pais = pais == null ? "Brasil" : pais;
    }

    public record Jogador(String nome, String posicao, int overall, int ataque,
                          int defesa, int finalizacao, int fisico) {
    }
}
