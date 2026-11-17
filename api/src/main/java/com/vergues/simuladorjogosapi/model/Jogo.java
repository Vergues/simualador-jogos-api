package com.vergues.simuladorjogosapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Entity
@Table(name = "jogos")
public class Jogo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, unique = true)
    public String chave;
    @Column(nullable = false)
    public String campeonato;
    public String competicao;
    public String fase;
    public String grupo;
    public String confronto;
    public int perna;
    public String vencedor;
    public Integer penaltisCasa;
    public Integer penaltisFora;
    public int temporada;
    public int rodada;
    @Column(nullable = false)
    public OffsetDateTime dataHora;
    @Column(nullable = false)
    public String siglaCasa;
    @Column(nullable = false)
    public String siglaFora;
    public int golsCasa;
    public int golsFora;
    public int minutoAtual;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public Status status = Status.AGENDADO;
    @Lob
    @Column(nullable = false)
    public String estadoJson;

    public enum Status { AGENDADO, AO_VIVO, INTERVALO, ENCERRADO }

    // O JSON guarda tudo que é necessário para continuar a partida após um reinício.
    public static class Estado {
        public long semente = ThreadLocalRandom.current().nextLong();
        public int ultimoMinuto;
        public int minutoBriga;
        public Equipe casa = new Equipe();
        public Equipe fora = new Equipe();
        public List<Evento> eventos = new ArrayList<>();
    }

    public static class Equipe {
        // Índices do elenco identificam jogadores dentro de cada equipe, nunca pelo nome.
        public List<Integer> campo = new ArrayList<>();
        public List<Integer> reservas = new ArrayList<>();
        public Map<Integer, Integer> amarelos = new HashMap<>();
        public int substituicoes;
        public int expulsos;
        public int posse = 50;
        public int finalizacoes;
        public int finalizacoesGol;
        public int escanteios;
        public int faltas;
        public int cartoesAmarelos;
        public int impedimentos;
    }

    public record Evento(int minuto, String tipo, String time, String jogador, String descricao,
                         String siglaTime, Integer jogadorId) {
    }
}
