package com.vergues.simuladorjogosapi.dto;

import java.util.List;

import com.vergues.simuladorjogosapi.model.Evento;

public class JogoResponse {

    private String timeCasa;
    private String siglaCasa;
    private int golsCasa;

    private String timeFora;
    private String siglaFora;
    private int golsFora;

    private List<Evento> eventos;

    public JogoResponse() {
    }

    public JogoResponse(
            String timeCasa,
            String siglaCasa,
            int golsCasa,
            String timeFora,
            String siglaFora,
            int golsFora,
            List<Evento> eventos) {

        this.timeCasa = timeCasa;
        this.siglaCasa = siglaCasa;
        this.golsCasa = golsCasa;
        this.timeFora = timeFora;
        this.siglaFora = siglaFora;
        this.golsFora = golsFora;
        this.eventos = eventos;
    }

    public String getTimeCasa() {
        return timeCasa;
    }

    public void setTimeCasa(String timeCasa) {
        this.timeCasa = timeCasa;
    }

    public String getSiglaCasa() {
        return siglaCasa;
    }

    public void setSiglaCasa(String siglaCasa) {
        this.siglaCasa = siglaCasa;
    }

    public int getGolsCasa() {
        return golsCasa;
    }

    public void setGolsCasa(int golsCasa) {
        this.golsCasa = golsCasa;
    }

    public String getTimeFora() {
        return timeFora;
    }

    public void setTimeFora(String timeFora) {
        this.timeFora = timeFora;
    }

    public String getSiglaFora() {
        return siglaFora;
    }

    public void setSiglaFora(String siglaFora) {
        this.siglaFora = siglaFora;
    }

    public int getGolsFora() {
        return golsFora;
    }

    public void setGolsFora(int golsFora) {
        this.golsFora = golsFora;
    }

    public List<Evento> getEventos() {
        return eventos;
    }

    public void setEventos(List<Evento> eventos) {
        this.eventos = eventos;
    }
}