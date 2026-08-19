package com.vergues.simuladorjogosapi.dto;

import java.time.LocalDateTime;

import com.vergues.simuladorjogosapi.model.Jogo;
import com.vergues.simuladorjogosapi.model.StatusJogo;

public class JogoCalendarioResponse {

    private Long id;

    private String campeonato;
    private String temporada;

    private int rodada;

    private LocalDateTime dataHora;

    private StatusJogo status;

    private int minutoAtual;

    private String timeCasa;
    private String siglaCasa;
    private int golsCasa;

    private String timeFora;
    private String siglaFora;
    private int golsFora;

    public JogoCalendarioResponse() {
    }

    public JogoCalendarioResponse(
            Jogo jogo) {

        this.id =
                jogo.getId();

        this.campeonato =
                jogo.getCampeonato()
                        .getNome();

        this.temporada =
                jogo.getCampeonato()
                        .getTemporada();

        this.rodada =
                jogo.getRodada();

        this.dataHora =
                jogo.getDataHora();

        this.status =
                jogo.getStatus();

        this.minutoAtual =
                jogo.getMinutoAtual();

        this.timeCasa =
                jogo.getTimeCasa()
                        .getNome();

        this.siglaCasa =
                jogo.getTimeCasa()
                        .getSigla();

        this.golsCasa =
                jogo.getGolsCasa();

        this.timeFora =
                jogo.getTimeFora()
                        .getNome();

        this.siglaFora =
                jogo.getTimeFora()
                        .getSigla();

        this.golsFora =
                jogo.getGolsFora();
    }

    public Long getId() {
        return id;
    }

    public String getCampeonato() {
        return campeonato;
    }

    public String getTemporada() {
        return temporada;
    }

    public int getRodada() {
        return rodada;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public StatusJogo getStatus() {
        return status;
    }

    public int getMinutoAtual() {
        return minutoAtual;
    }

    public String getTimeCasa() {
        return timeCasa;
    }

    public String getSiglaCasa() {
        return siglaCasa;
    }

    public int getGolsCasa() {
        return golsCasa;
    }

    public String getTimeFora() {
        return timeFora;
    }

    public String getSiglaFora() {
        return siglaFora;
    }

    public int getGolsFora() {
        return golsFora;
    }
}