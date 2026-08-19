package com.vergues.simuladorjogosapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/*
 * =========================================================
 * ENTIDADE DE TIME
 * =========================================================
 *
 * Esta classe representa um time salvo no banco de dados.
 *
 * Ela NÃO substitui a classe Time.java que já usamos
 * no simulador.
 *
 * Por enquanto:
 *
 * Time.java
 * -> usado pela lógica da simulação
 *
 * TimeEntity.java
 * -> usado para persistência no banco
 *
 * Depois podemos criar conversões entre os dois.
 */
@Entity
@Table(name = "times")
public class TimeEntity {

    /*
     * ID interno do banco.
     *
     * O banco gera automaticamente:
     *
     * 1
     * 2
     * 3
     * ...
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    /*
     * Nome completo do clube.
     *
     * Ex:
     * Palmeiras
     * Flamengo
     * Cruzeiro
     */
    @Column(nullable = false)
    private String nome;


    /*
     * Sigla do clube.
     *
     * Vamos marcar como UNIQUE porque não queremos:
     *
     * PAL
     * PAL
     *
     * duas vezes no banco.
     */
    @Column(
            nullable = false,
            unique = true,
            length = 10
    )
    private String sigla;


    /*
     * Força usada pelo nosso simulador.
     *
     * Ex:
     *
     * Palmeiras = 95
     * Flamengo = 90
     * Corinthians = 50
     */
    @Column(nullable = false)
    private int forca;


    /*
     * Construtor vazio.
     *
     * O JPA exige um construtor sem argumentos.
     */
    public TimeEntity() {
    }


    /*
     * Construtor que vamos usar
     * para criar times mais facilmente.
     */
    public TimeEntity(
            String nome,
            String sigla,
            int forca) {

        this.nome = nome;
        this.sigla = sigla;
        this.forca = forca;
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


    public String getNome() {
        return nome;
    }


    public void setNome(
            String nome) {

        this.nome = nome;
    }


    public String getSigla() {
        return sigla;
    }


    public void setSigla(
            String sigla) {

        this.sigla = sigla;
    }


    public int getForca() {
        return forca;
    }


    public void setForca(
            int forca) {

        this.forca = forca;
    }
}