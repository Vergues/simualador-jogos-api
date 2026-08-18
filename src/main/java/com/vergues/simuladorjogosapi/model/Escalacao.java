package com.vergues.simuladorjogosapi.model;

import java.util.List;

public class Escalacao {

    private List<Jogador> titulares;
    private List<Jogador> reservas;

    public Escalacao() {
    }

    public Escalacao(
            List<Jogador> titulares,
            List<Jogador> reservas) {

        this.titulares = titulares;
        this.reservas = reservas;
    }

    public List<Jogador> getTitulares() {
        return titulares;
    }

    public void setTitulares(List<Jogador> titulares) {
        this.titulares = titulares;
    }

    public List<Jogador> getReservas() {
        return reservas;
    }

    public void setReservas(List<Jogador> reservas) {
        this.reservas = reservas;
    }
}