package com.vergues.simuladorjogosapi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vergues.simuladorjogosapi.model.PartidaEntity;

/*
 * =========================================================
 * REPOSITORIO JPA DE PARTIDAS
 * =========================================================
 *
 * Responsavel por consultar e salvar
 * partidas no banco H2.
 */
public interface PartidaJpaRepository
        extends JpaRepository<PartidaEntity, Long> {


    /*
     * Busca partidas pelo status.
     *
     * Exemplos:
     *
     * AGENDADO
     * AO_VIVO
     * ENCERRADO
     */
    List<PartidaEntity> findByStatus(
            String status
    );


    /*
     * Busca todas as partidas
     * de uma temporada.
     */
    List<PartidaEntity> findByTemporada(
            String temporada
    );


    /*
     * Busca partidas encerradas
     * de uma temporada.
     *
     * Isso vai ser usado posteriormente
     * para calcular a classificacao.
     */
    List<PartidaEntity> findByTemporadaAndStatus(
            String temporada,
            String status
    );


    /*
     * =====================================================
     * IDENTIFICACAO UNICA DE UMA PARTIDA
     * =====================================================
     *
     * Por enquanto vamos considerar uma partida unica por:
     *
     * temporada
     * +
     * rodada
     * +
     * time da casa
     * +
     * time visitante
     *
     * Exemplo:
     *
     * 2026
     * Rodada 10
     * Palmeiras
     * Flamengo
     *
     * Se isso ja existir no banco,
     * NAO criaremos outra linha.
     *
     * Os "_" deixam explicito para o Spring
     * que queremos acessar:
     *
     * timeCasa.sigla
     * timeFora.sigla
     */
    Optional<PartidaEntity>
            findByTemporadaAndRodadaAndTimeCasa_SiglaAndTimeFora_Sigla(
                    String temporada,
                    int rodada,
                    String siglaCasa,
                    String siglaFora
            );


    /*
     * Quando formos carregar o calendario
     * do banco, queremos ele ordenado pela data.
     */
    List<PartidaEntity>
            findByTemporadaOrderByDataHoraAsc(
                    String temporada
            );
}