package com.vergues.simuladorjogosapi.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/*
 * =========================================================
 * ENTIDADE DE PARTIDA
 * =========================================================
 *
 * Representa uma partida armazenada no banco.
 *
 * Essa tabela vai ser a base para:
 *
 * - calendario;
 * - resultados;
 * - classificacao;
 * - historico;
 * - estatisticas da temporada.
 */
@Entity
@Table(name = "partidas")
public class PartidaEntity {

    /*
     * ID interno da partida no banco.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    /*
     * Time mandante.
     *
     * Muitas partidas podem apontar
     * para o mesmo time.
     */
    @ManyToOne
    @JoinColumn(
            name = "time_casa_id",
            nullable = false
    )
    private TimeEntity timeCasa;


    /*
     * Time visitante.
     */
    @ManyToOne
    @JoinColumn(
            name = "time_fora_id",
            nullable = false
    )
    private TimeEntity timeFora;


    /*
     * Placar.
     */
    @Column(nullable = false)
    private int golsCasa;


    @Column(nullable = false)
    private int golsFora;


    /*
     * Rodada do campeonato.
     *
     * Ex:
     * 1
     * 20
     * 38
     */
    @Column(nullable = false)
    private int rodada;


    /*
     * Data e horario da partida.
     */
    @Column(nullable = false)
    private LocalDateTime dataHora;


    /*
     * Status armazenado como texto.
     *
     * Exemplos:
     *
     * AGENDADO
     * AO_VIVO
     * INTERVALO
     * ENCERRADO
     *
     * Poderiamos usar Enum com @Enumerated,
     * mas por enquanto vamos manter simples.
     */
    @Column(
            nullable = false,
            length = 30
    )
    private String status;


    /*
     * Nome do campeonato.
     *
     * Ex:
     * Campeonato Brasileiro
     *
     * Depois podemos transformar isso
     * numa entidade propria de Campeonato.
     */
    @Column(
            nullable = false,
            length = 100
    )
    private String campeonato;


    /*
     * Temporada.
     *
     * Ex:
     * 2026
     */
    @Column(
            nullable = false,
            length = 10
    )
    private String temporada;


    /*
     * Construtor vazio exigido pelo JPA.
     */
    public PartidaEntity() {
    }


    /*
     * Construtor principal.
     */
    public PartidaEntity(
            TimeEntity timeCasa,
            TimeEntity timeFora,
            int golsCasa,
            int golsFora,
            int rodada,
            LocalDateTime dataHora,
            String status,
            String campeonato,
            String temporada) {

        this.timeCasa = timeCasa;
        this.timeFora = timeFora;

        this.golsCasa = golsCasa;
        this.golsFora = golsFora;

        this.rodada = rodada;

        this.dataHora = dataHora;

        this.status = status;

        this.campeonato = campeonato;

        this.temporada = temporada;
    }


    /*
     * =========================================================
     * GETTERS E SETTERS
     * =========================================================
     */

    public Long getId() {
        return id;
    }


    public void setId(
            Long id) {

        this.id = id;
    }


    public TimeEntity getTimeCasa() {
        return timeCasa;
    }


    public void setTimeCasa(
            TimeEntity timeCasa) {

        this.timeCasa = timeCasa;
    }


    public TimeEntity getTimeFora() {
        return timeFora;
    }


    public void setTimeFora(
            TimeEntity timeFora) {

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


    public String getStatus() {
        return status;
    }


    public void setStatus(
            String status) {

        this.status = status;
    }


    public String getCampeonato() {
        return campeonato;
    }


    public void setCampeonato(
            String campeonato) {

        this.campeonato = campeonato;
    }


    public String getTemporada() {
        return temporada;
    }


    public void setTemporada(
            String temporada) {

        this.temporada = temporada;
    }
}