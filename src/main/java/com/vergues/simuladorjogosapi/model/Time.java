package com.vergues.simuladorjogosapi.model;

import java.util.ArrayList;
import java.util.List;

public class Time {

    private String nome;
    private String sigla;
    private int forca;
    private String regraEspecial;

    private List<Jogador> jogadores = new ArrayList<>();

    public Time() {
    }

    public Time(String nome, int forca) {
        this.nome = nome;
        this.forca = forca;
        this.jogadores = new ArrayList<>();
    }

    public void adicionarJogador(Jogador jogador) {
        jogadores.add(jogador);
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getSigla() {
        return sigla;
    }

    public void setSigla(String sigla) {
        this.sigla = sigla;
    }

    public int getForca() {
        return forca;
    }

    public void setForca(int forca) {
        this.forca = forca;
    }

    public String getRegraEspecial() {
        return regraEspecial;
    }

    public void setRegraEspecial(String regraEspecial) {
        this.regraEspecial = regraEspecial;
    }

    public List<Jogador> getJogadores() {
        return jogadores;
    }

    public void setJogadores(List<Jogador> jogadores) {
        this.jogadores = jogadores;
    }
}