package com.vergues.simuladorjogosapi.model;

public class Evento {

    private int minuto;
    private String tipo;
    private String time;
    private String jogador;
    private String descricao;

    public Evento() {
    }

    public Evento(
            int minuto,
            String tipo,
            String time,
            String jogador,
            String descricao) {

        this.minuto = minuto;
        this.tipo = tipo;
        this.time = time;
        this.jogador = jogador;
        this.descricao = descricao;
    }

    public int getMinuto() {
        return minuto;
    }

    public void setMinuto(int minuto) {
        this.minuto = minuto;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getJogador() {
        return jogador;
    }

    public void setJogador(String jogador) {
        this.jogador = jogador;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}	
