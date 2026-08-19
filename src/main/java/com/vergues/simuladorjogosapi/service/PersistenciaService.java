package com.vergues.simuladorjogosapi.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.vergues.simuladorjogosapi.model.Jogo;
import com.vergues.simuladorjogosapi.model.PartidaEntity;
import com.vergues.simuladorjogosapi.model.Time;
import com.vergues.simuladorjogosapi.model.TimeEntity;
import com.vergues.simuladorjogosapi.repository.PartidaJpaRepository;
import com.vergues.simuladorjogosapi.repository.TimeJpaRepository;

@Service
public class PersistenciaService {

    /*
     * =========================================================
     * REPOSITORIES
     * =========================================================
     */

    private final TimeJpaRepository timeJpaRepository;

    private final PartidaJpaRepository partidaJpaRepository;


    /*
     * =========================================================
     * CONSTRUTOR
     * =========================================================
     */

    public PersistenciaService(
            TimeJpaRepository timeJpaRepository,
            PartidaJpaRepository partidaJpaRepository) {

        this.timeJpaRepository =
                timeJpaRepository;

        this.partidaJpaRepository =
                partidaJpaRepository;
    }


    /*
     * =========================================================
     * SALVAR OU BUSCAR TIME
     * =========================================================
     *
     * Antes de criar um time, procuramos
     * pela sigla.
     *
     * Assim nao teremos:
     *
     * PAL
     * PAL
     * PAL
     *
     * repetidos no banco.
     */
    public TimeEntity salvarOuBuscarTime(
            Time time) {

        Optional<TimeEntity> existente =
                timeJpaRepository
                        .findBySigla(
                                time.getSigla()
                        );


        /*
         * Se ja existe, usamos o registro existente.
         */
        if (existente.isPresent()) {

            TimeEntity entidade =
                    existente.get();


            /*
             * Atualizamos nome e forca caso
             * esses dados mudem futuramente.
             */
            entidade.setNome(
                    time.getNome()
            );

            entidade.setForca(
                    time.getForca()
            );


            return timeJpaRepository
                    .save(
                            entidade
                    );
        }


        /*
         * Caso nao exista, cria um novo time.
         */
        TimeEntity entidade =
                new TimeEntity(
                        time.getNome(),
                        time.getSigla(),
                        time.getForca()
                );


        return timeJpaRepository
                .save(
                        entidade
                );
    }


    /*
     * =========================================================
     * VERIFICA SE UMA PARTIDA JA EXISTE
     * =========================================================
     */
    public boolean partidaJaExiste(
            Jogo jogo) {

        return buscarPartida(
                jogo
        ).isPresent();
    }


    /*
     * =========================================================
     * BUSCA UMA PARTIDA EXISTENTE
     * =========================================================
     *
     * Usamos:
     *
     * temporada
     * rodada
     * time casa
     * time fora
     */
    private Optional<PartidaEntity> buscarPartida(
            Jogo jogo) {

        return partidaJpaRepository
                .findByTemporadaAndRodadaAndTimeCasa_SiglaAndTimeFora_Sigla(
                        "2026",
                        jogo.getRodada(),
                        jogo.getTimeCasa()
                                .getSigla(),
                        jogo.getTimeFora()
                                .getSigla()
                );
    }


    /*
     * =========================================================
     * SALVAR OU ATUALIZAR PARTIDA
     * =========================================================
     *
     * Esse metodo substitui a ideia antiga de
     * simplesmente inserir uma nova partida.
     *
     * Agora:
     *
     * nao existe
     *      ↓
     * INSERT
     *
     * ja existe
     *      ↓
     * UPDATE
     */
    public PartidaEntity salvarOuAtualizarPartida(
            Jogo jogo) {

        /*
         * Garante que os times estejam
         * cadastrados no banco.
         */
        TimeEntity casa =
                salvarOuBuscarTime(
                        jogo.getTimeCasa()
                );

        TimeEntity fora =
                salvarOuBuscarTime(
                        jogo.getTimeFora()
                );


        /*
         * Procuramos se essa partida
         * ja foi cadastrada.
         */
        Optional<PartidaEntity> existente =
                buscarPartida(
                        jogo
                );


        PartidaEntity partida;


        /*
         * =====================================================
         * PARTIDA JA EXISTE
         * =====================================================
         *
         * Atualizamos o mesmo registro.
         */
        if (existente.isPresent()) {

            partida =
                    existente.get();


            partida.setTimeCasa(
                    casa
            );

            partida.setTimeFora(
                    fora
            );

            partida.setGolsCasa(
                    jogo.getGolsCasa()
            );

            partida.setGolsFora(
                    jogo.getGolsFora()
            );

            partida.setRodada(
                    jogo.getRodada()
            );

            partida.setDataHora(
                    jogo.getDataHora()
            );

            partida.setStatus(
                    jogo.getStatus()
                            .name()
            );

            partida.setCampeonato(
                    obterNomeCampeonato(
                            jogo
                    )
            );

            partida.setTemporada(
                    "2026"
            );


            /*
             * JPA percebe que essa entidade possui ID
             * e executa UPDATE, nao INSERT.
             */
            return partidaJpaRepository
                    .save(
                            partida
                    );
        }


        /*
         * =====================================================
         * PARTIDA NOVA
         * =====================================================
         */

        partida =
                new PartidaEntity(
                        casa,
                        fora,
                        jogo.getGolsCasa(),
                        jogo.getGolsFora(),
                        jogo.getRodada(),
                        jogo.getDataHora(),
                        jogo.getStatus().name(),
                        obterNomeCampeonato(
                                jogo
                        ),
                        "2026"
                );


        return partidaJpaRepository
                .save(
                        partida
                );
    }


    /*
     * =========================================================
     * COMPATIBILIDADE
     * =========================================================
     *
     * Mantemos esse metodo porque algum codigo antigo
     * pode ainda chamar salvarPartida().
     *
     * Internamente, ele agora utiliza nossa nova
     * logica segura.
     */
    public PartidaEntity salvarPartida(
            Jogo jogo) {

        return salvarOuAtualizarPartida(
                jogo
        );
    }


    /*
     * =========================================================
     * NOME DO CAMPEONATO
     * =========================================================
     */
    private String obterNomeCampeonato(
            Jogo jogo) {

        if (jogo.getCampeonato() == null) {

            return "Campeonato Brasileiro";
        }


        return jogo.getCampeonato()
                .toString();
    }
}