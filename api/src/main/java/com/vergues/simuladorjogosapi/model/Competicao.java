package com.vergues.simuladorjogosapi.model;

import java.util.List;

public record Competicao(String id, String nome, String formato, String descricao,
                         List<String> clubes, String faseInicial, int mesInicio, int diaInicio,
                         int intervaloDias, boolean returno, int grupos, int classificadosPorGrupo,
                         List<Fase> finais) {
    public record Fase(String nome, int mes, int dia, boolean idaVolta) { }

    public static class Classificacao {
        public final String time;
        public final String sigla;
        public final String slug;
        public String grupo;
        public int pontos, jogos, vitorias, empates, derrotas, golsPro, golsContra, saldo;
        public int amarelos, vermelhos;
        public double aproveitamento;

        public Classificacao(Time time) {
            this.time = time.nome();
            this.sigla = time.sigla();
            this.slug = time.slug();
        }

        public void registrar(int pro, int contra) {
            jogos++;
            golsPro += pro;
            golsContra += contra;
            saldo = golsPro - golsContra;
            if (pro > contra) { vitorias++; pontos += 3; }
            else if (pro == contra) { empates++; pontos++; }
            else { derrotas++; }
            aproveitamento = Math.round(1000.0 * pontos / (jogos * 3)) / 10.0;
        }
    }
}
