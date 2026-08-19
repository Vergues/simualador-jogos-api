package com.vergues.simuladorjogosapi.model;

public class Campeonato {

    private Long id;
    private String nome;
    private String temporada;
    private int totalRodadas;

    public Campeonato() {
    }

    public Campeonato(
            Long id,
            String nome,
            String temporada,
            int totalRodadas) {

        this.id = id;
        this.nome = nome;
        this.temporada = temporada;
        this.totalRodadas = totalRodadas;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTemporada() {
        return temporada;
    }

    public void setTemporada(String temporada) {
        this.temporada = temporada;
    }

    public int getTotalRodadas() {
        return totalRodadas;
    }

    public void setTotalRodadas(int totalRodadas) {
        this.totalRodadas = totalRodadas;
    }
}