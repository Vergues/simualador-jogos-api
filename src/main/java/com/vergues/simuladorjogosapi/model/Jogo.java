package com.vergues.simuladorjogosapi.model;

public class Jogo {

    private Time timeCasa;
    private Time timeFora;

    private int golsCasa;
    private int golsFora;

    public Jogo(Time timeCasa, Time timeFora) {
        this.timeCasa = timeCasa;
        this.timeFora = timeFora;
    }

    public Time getTimeCasa() {
        return timeCasa;
    }

    public Time getTimeFora() {
        return timeFora;
    }

    public int getGolsCasa() {
        return golsCasa;
    }

    public int getGolsFora() {
        return golsFora;
    }

    public void setGolsCasa(int golsCasa) {
        this.golsCasa = golsCasa;
    }

    public void setGolsFora(int golsFora) {
        this.golsFora = golsFora;
    }
}
