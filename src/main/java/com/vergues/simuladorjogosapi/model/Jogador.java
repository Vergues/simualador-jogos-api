package com.vergues.simuladorjogosapi.model;

public class Jogador {

    private String nome;
    private String posicao;

    private int overall;
    private int ataque;
    private int defesa;
    private int finalizacao;
    private int passe;
    private int velocidade;
    private int fisico;

    public Jogador() {
    }

    public Jogador(
            String nome,
            String posicao,
            int overall,
            int ataque,
            int defesa,
            int finalizacao,
            int passe,
            int velocidade,
            int fisico) {

        this.nome = nome;
        this.posicao = posicao;
        this.overall = overall;
        this.ataque = ataque;
        this.defesa = defesa;
        this.finalizacao = finalizacao;
        this.passe = passe;
        this.velocidade = velocidade;
        this.fisico = fisico;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getPosicao() {
        return posicao;
    }

    public void setPosicao(String posicao) {
        this.posicao = posicao;
    }

    public int getOverall() {
        return overall;
    }

    public void setOverall(int overall) {
        this.overall = overall;
    }

    public int getAtaque() {
        return ataque;
    }

    public void setAtaque(int ataque) {
        this.ataque = ataque;
    }

    public int getDefesa() {
        return defesa;
    }

    public void setDefesa(int defesa) {
        this.defesa = defesa;
    }

    public int getFinalizacao() {
        return finalizacao;
    }

    public void setFinalizacao(int finalizacao) {
        this.finalizacao = finalizacao;
    }

    public int getPasse() {
        return passe;
    }

    public void setPasse(int passe) {
        this.passe = passe;
    }

    public int getVelocidade() {
        return velocidade;
    }

    public void setVelocidade(int velocidade) {
        this.velocidade = velocidade;
    }

    public int getFisico() {
        return fisico;
    }

    public void setFisico(int fisico) {
        this.fisico = fisico;
    }
}