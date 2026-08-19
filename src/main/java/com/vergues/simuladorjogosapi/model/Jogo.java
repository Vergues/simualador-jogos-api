package com.vergues.simuladorjogosapi.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Jogo {

    /*
     * =========================================================
     * DADOS BÁSICOS DA PARTIDA
     * =========================================================
     */

    private Long id;

    private Time timeCasa;
    private Time timeFora;

    private int golsCasa;
    private int golsFora;

    private int rodada;

    private LocalDateTime dataHora;

    private StatusJogo status;

    private Campeonato campeonato;


    /*
     * =========================================================
     * CONTROLE DO RELÓGIO
     * =========================================================
     */

    private int minutoAtual;

    /*
     * Evita processarmos o mesmo minuto duas vezes.
     */
    private int ultimoMinutoProcessado;


    /*
     * =========================================================
     * EVENTOS E ESTATÍSTICAS
     * =========================================================
     */

    private List<Evento> eventos;

    private EstatisticasJogo estatisticas;


    /*
     * =========================================================
     * SISTEMA ESPECIAL DE CLÁSSICOS
     * =========================================================
     */

    private boolean brigaAvaliada;

    private boolean brigaProgramada;

    private int minutoBriga;


    /*
     * =========================================================
     * CARTÕES
     * =========================================================
     *
     * Chave:
     * nome do jogador
     *
     * Valor:
     * quantidade de amarelos recebidos.
     */
    private Map<String, Integer> amarelosPorJogador;


    /*
     * Jogadores expulsos não podem mais aparecer
     * em eventos da partida.
     */
    private Set<String> jogadoresExpulsos;


    /*
     * =========================================================
     * SUBSTITUIÇÕES
     * =========================================================
     *
     * jogadoresSubstituidos:
     * jogadores que SAÍRAM.
     *
     * jogadoresQueEntraram:
     * reservas que ENTRARAM.
     */
    private Set<String> jogadoresSubstituidos;

    private Set<String> jogadoresQueEntraram;


    /*
     * Quantidade de substituições já realizadas
     * por cada equipe.
     */
    private int substituicoesCasa;

    private int substituicoesFora;


    /*
     * =========================================================
     * CONSTRUTOR VAZIO
     * =========================================================
     */

    public Jogo() {

        inicializarEstruturas();
    }


    /*
     * =========================================================
     * CONSTRUTOR PRINCIPAL
     * =========================================================
     */

    public Jogo(
            Time timeCasa,
            Time timeFora) {

        inicializarEstruturas();

        this.timeCasa = timeCasa;
        this.timeFora = timeFora;

        this.golsCasa = 0;
        this.golsFora = 0;

        this.minutoAtual = 0;
        this.ultimoMinutoProcessado = 0;

        this.status =
                StatusJogo.AGENDADO;

        this.brigaAvaliada = false;
        this.brigaProgramada = false;

        this.minutoBriga = 0;

        this.substituicoesCasa = 0;
        this.substituicoesFora = 0;
    }


    /*
     * Inicializa todas as coleções.
     *
     * Isso evita NullPointerException.
     */
    private void inicializarEstruturas() {

        this.eventos =
                new ArrayList<>();

        this.estatisticas =
                new EstatisticasJogo();

        this.amarelosPorJogador =
                new HashMap<>();

        this.jogadoresExpulsos =
                new HashSet<>();

        this.jogadoresSubstituidos =
                new HashSet<>();

        this.jogadoresQueEntraram =
                new HashSet<>();
    }


    /*
     * =========================================================
     * MÉTODOS AUXILIARES DE EVENTOS
     * =========================================================
     */

    public void adicionarEvento(
            Evento evento) {

        this.eventos.add(
                evento
        );
    }


    /*
     * =========================================================
     * CONTROLE DE CARTÕES
     * =========================================================
     */

    public int getAmarelosDoJogador(
            Jogador jogador) {

        return amarelosPorJogador
                .getOrDefault(
                        jogador.getNome(),
                        0
                );
    }


    public void adicionarAmarelo(
            Jogador jogador) {

        String nome =
                jogador.getNome();

        int quantidadeAtual =
                getAmarelosDoJogador(
                        jogador
                );

        amarelosPorJogador.put(
                nome,
                quantidadeAtual + 1
        );
    }


    public void expulsarJogador(
            Jogador jogador) {

        jogadoresExpulsos.add(
                jogador.getNome()
        );
    }


    public boolean jogadorExpulso(
            Jogador jogador) {

        return jogadoresExpulsos
                .contains(
                        jogador.getNome()
                );
    }


    /*
     * =========================================================
     * CONTROLE DE SUBSTITUIÇÕES
     * =========================================================
     */

    public void registrarSubstituicao(
            Jogador saindo,
            Jogador entrando) {

        jogadoresSubstituidos.add(
                saindo.getNome()
        );

        jogadoresQueEntraram.add(
                entrando.getNome()
        );
    }


    public boolean jogadorSubstituido(
            Jogador jogador) {

        return jogadoresSubstituidos
                .contains(
                        jogador.getNome()
                );
    }


    public boolean jogadorEntrou(
            Jogador jogador) {

        return jogadoresQueEntraram
                .contains(
                        jogador.getNome()
                );
    }


    /*
     * Um jogador está disponível caso:
     *
     * - não tenha sido expulso;
     * - não tenha sido substituído.
     */
    public boolean jogadorDisponivel(
            Jogador jogador) {

        return !jogadorExpulso(jogador)
                && !jogadorSubstituido(jogador);
    }


    /*
     * =========================================================
     * CONTROLE DE SUBSTITUIÇÕES POR TIME
     * =========================================================
     */

    public int getSubstituicoesDoTime(
            Time time) {

        if (time == timeCasa) {

            return substituicoesCasa;
        }

        return substituicoesFora;
    }


    public void adicionarSubstituicao(
            Time time) {

        if (time == timeCasa) {

            substituicoesCasa++;

        } else {

            substituicoesFora++;
        }
    }


    /*
     * =========================================================
     * GETTERS E SETTERS
     * =========================================================
     */

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public Time getTimeCasa() {
        return timeCasa;
    }

    public void setTimeCasa(
            Time timeCasa) {

        this.timeCasa = timeCasa;
    }


    public Time getTimeFora() {
        return timeFora;
    }

    public void setTimeFora(
            Time timeFora) {

        this.timeFora = timeFora;
    }


    public int getGolsCasa() {
        return golsCasa;
    }

    public void setGolsCasa(
            int golsCasa) {

        this.golsCasa = golsCasa;
    }


    public int getGolsFora() {
        return golsFora;
    }

    public void setGolsFora(
            int golsFora) {

        this.golsFora = golsFora;
    }


    public int getRodada() {
        return rodada;
    }

    public void setRodada(
            int rodada) {

        this.rodada = rodada;
    }


    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(
            LocalDateTime dataHora) {

        this.dataHora = dataHora;
    }


    public StatusJogo getStatus() {
        return status;
    }

    public void setStatus(
            StatusJogo status) {

        this.status = status;
    }


    public Campeonato getCampeonato() {
        return campeonato;
    }

    public void setCampeonato(
            Campeonato campeonato) {

        this.campeonato = campeonato;
    }


    public int getMinutoAtual() {
        return minutoAtual;
    }

    public void setMinutoAtual(
            int minutoAtual) {

        this.minutoAtual = minutoAtual;
    }


    public int getUltimoMinutoProcessado() {
        return ultimoMinutoProcessado;
    }

    public void setUltimoMinutoProcessado(
            int ultimoMinutoProcessado) {

        this.ultimoMinutoProcessado =
                ultimoMinutoProcessado;
    }


    public List<Evento> getEventos() {
        return eventos;
    }

    public void setEventos(
            List<Evento> eventos) {

        this.eventos = eventos;
    }


    public EstatisticasJogo getEstatisticas() {
        return estatisticas;
    }

    public void setEstatisticas(
            EstatisticasJogo estatisticas) {

        this.estatisticas =
                estatisticas;
    }


    public boolean isBrigaAvaliada() {
        return brigaAvaliada;
    }

    public void setBrigaAvaliada(
            boolean brigaAvaliada) {

        this.brigaAvaliada =
                brigaAvaliada;
    }


    public boolean isBrigaProgramada() {
        return brigaProgramada;
    }

    public void setBrigaProgramada(
            boolean brigaProgramada) {

        this.brigaProgramada =
                brigaProgramada;
    }


    public int getMinutoBriga() {
        return minutoBriga;
    }

    public void setMinutoBriga(
            int minutoBriga) {

        this.minutoBriga =
                minutoBriga;
    }


    public Map<String, Integer> getAmarelosPorJogador() {
        return amarelosPorJogador;
    }

    public void setAmarelosPorJogador(
            Map<String, Integer> amarelosPorJogador) {

        this.amarelosPorJogador =
                amarelosPorJogador;
    }


    public Set<String> getJogadoresExpulsos() {
        return jogadoresExpulsos;
    }

    public void setJogadoresExpulsos(
            Set<String> jogadoresExpulsos) {

        this.jogadoresExpulsos =
                jogadoresExpulsos;
    }


    public Set<String> getJogadoresSubstituidos() {
        return jogadoresSubstituidos;
    }

    public void setJogadoresSubstituidos(
            Set<String> jogadoresSubstituidos) {

        this.jogadoresSubstituidos =
                jogadoresSubstituidos;
    }


    public Set<String> getJogadoresQueEntraram() {
        return jogadoresQueEntraram;
    }

    public void setJogadoresQueEntraram(
            Set<String> jogadoresQueEntraram) {

        this.jogadoresQueEntraram =
                jogadoresQueEntraram;
    }


    public int getSubstituicoesCasa() {
        return substituicoesCasa;
    }

    public void setSubstituicoesCasa(
            int substituicoesCasa) {

        this.substituicoesCasa =
                substituicoesCasa;
    }


    public int getSubstituicoesFora() {
        return substituicoesFora;
    }

    public void setSubstituicoesFora(
            int substituicoesFora) {

        this.substituicoesFora =
                substituicoesFora;
    }
}		
