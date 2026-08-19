package com.vergues.simuladorjogosapi.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.vergues.simuladorjogosapi.model.Jogo;
import com.vergues.simuladorjogosapi.model.StatusJogo;
import com.vergues.simuladorjogosapi.repository.JogoRepository;

@Service
public class PartidaAoVivoService {

    /*
     * =========================================================
     * CALENDARIO ATUAL EM MEMORIA
     * =========================================================
     *
     * Ainda estamos usando temporariamente
     * o JogoRepository atual.
     *
     * No proximo passo ele sera carregado
     * usando o banco.
     */
    private final JogoRepository jogoRepository;


    /*
     * Motor da simulacao.
     */
    private final SimuladorService simuladorService;


    /*
     * Ponte entre o simulador e o H2.
     */
    private final PersistenciaService persistenciaService;


    /*
     * =========================================================
     * CACHE DE PARTIDAS JA VERIFICADAS
     * =========================================================
     *
     * Isso evita consultar o banco a cada
     * 5 segundos para a mesma partida.
     *
     * IMPORTANTE:
     *
     * ele NAO e mais responsavel por impedir
     * duplicidade permanentemente.
     *
     * O banco faz isso agora.
     */
    private final Set<Long> partidasPersistidas =
            new HashSet<>();


    /*
     * =========================================================
     * CONSTRUTOR
     * =========================================================
     */
    public PartidaAoVivoService(
            JogoRepository jogoRepository,
            SimuladorService simuladorService,
            PersistenciaService persistenciaService) {

        this.jogoRepository =
                jogoRepository;

        this.simuladorService =
                simuladorService;

        this.persistenciaService =
                persistenciaService;
    }


    /*
     * =========================================================
     * RELOGIO GLOBAL
     * =========================================================
     */
    @Scheduled(fixedRate = 5000)
    public void atualizarPartidas() {

        LocalDateTime agora =
                LocalDateTime.now();


        List<Jogo> jogos =
                jogoRepository
                        .listarTodos();


        for (Jogo jogo : jogos) {

            atualizarJogo(
                    jogo,
                    agora
            );
        }
    }


    /*
     * =========================================================
     * ATUALIZA UM JOGO
     * =========================================================
     */
    private void atualizarJogo(
            Jogo jogo,
            LocalDateTime agora) {

        /*
         * Sem data, nao processamos.
         */
        if (jogo.getDataHora() == null) {

            return;
        }


        /*
         * =====================================================
         * AGENDADO
         * =====================================================
         */
        if (
                agora.isBefore(
                        jogo.getDataHora()
                )
        ) {

            jogo.setStatus(
                    StatusJogo.AGENDADO
            );

            jogo.setMinutoAtual(
                    0
            );

            return;
        }


        /*
         * Quantos minutos reais passaram
         * desde o inicio previsto.
         */
        long minutosReais =
                Duration.between(
                        jogo.getDataHora(),
                        agora
                ).toMinutes();


        /*
         * =====================================================
         * PRIMEIRO TEMPO
         * =====================================================
         */
        if (minutosReais < 45) {

            int minutoJogo =
                    (int) minutosReais
                    + 1;


            jogo.setStatus(
                    StatusJogo.AO_VIVO
            );

            jogo.setMinutoAtual(
                    minutoJogo
            );


            processarAteMinuto(
                    jogo,
                    minutoJogo
            );


            return;
        }


        /*
         * =====================================================
         * INTERVALO
         * =====================================================
         */
        if (minutosReais < 60) {

            jogo.setStatus(
                    StatusJogo.INTERVALO
            );

            jogo.setMinutoAtual(
                    45
            );


            processarAteMinuto(
                    jogo,
                    45
            );


            return;
        }


        /*
         * =====================================================
         * SEGUNDO TEMPO
         * =====================================================
         */
        if (minutosReais < 105) {

            int minutoJogo =
                    46
                    + (int) (
                            minutosReais - 60
                    );


            jogo.setStatus(
                    StatusJogo.AO_VIVO
            );

            jogo.setMinutoAtual(
                    minutoJogo
            );


            processarAteMinuto(
                    jogo,
                    minutoJogo
            );


            return;
        }


        /*
         * =====================================================
         * FIM DE JOGO
         * =====================================================
         */

        processarAteMinuto(
                jogo,
                90
        );


        jogo.setStatus(
                StatusJogo.ENCERRADO
        );

        jogo.setMinutoAtual(
                90
        );


        /*
         * Agora usamos a persistencia
         * protegida contra duplicidade.
         */
        persistirPartidaSeNecessario(
                jogo
        );
    }


    /*
     * =========================================================
     * PERSISTENCIA SEGURA
     * =========================================================
     */
    private void persistirPartidaSeNecessario(
            Jogo jogo) {

        /*
         * Precisamos de ID para nosso cache.
         */
        if (jogo.getId() == null) {

            return;
        }


        /*
         * Se ja verificamos nesta execucao,
         * nao fazemos mais nada.
         */
        if (
                partidasPersistidas.contains(
                        jogo.getId()
                )
        ) {

            return;
        }


        /*
         * =====================================================
         * CONSULTA O BANCO
         * =====================================================
         *
         * Isso resolve o problema de reiniciar o Spring.
         *
         * Mesmo que o HashSet esteja vazio novamente,
         * consultamos o H2.
         */
        if (
                persistenciaService
                        .partidaJaExiste(
                                jogo
                        )
        ) {

            /*
             * A partida ja existe no H2.
             *
             * Apenas colocamos no cache local.
             */
            partidasPersistidas.add(
                    jogo.getId()
            );


            System.out.println(
                    "Partida ja existe no banco: "
                            + jogo.getTimeCasa()
                                    .getNome()
                            + " x "
                            + jogo.getTimeFora()
                                    .getNome()
            );


            return;
        }


        /*
         * =====================================================
         * PARTIDA AINDA NAO EXISTE
         * =====================================================
         */

        persistenciaService
                .salvarOuAtualizarPartida(
                        jogo
                );


        partidasPersistidas.add(
                jogo.getId()
        );


        System.out.println(
                "Nova partida salva no banco: "
                        + jogo.getTimeCasa()
                                .getNome()
                        + " "
                        + jogo.getGolsCasa()
                        + " x "
                        + jogo.getGolsFora()
                        + " "
                        + jogo.getTimeFora()
                                .getNome()
        );
    }


    /*
     * =========================================================
     * PROCESSAMENTO DOS MINUTOS
     * =========================================================
     */
    private void processarAteMinuto(
            Jogo jogo,
            int minutoDestino) {

        int ultimoProcessado =
                jogo.getUltimoMinutoProcessado();


        for (
                int minuto =
                        ultimoProcessado + 1;

                minuto <= minutoDestino;

                minuto++
        ) {

            simuladorService
                    .processarMinutoAoVivo(
                            jogo,
                            minuto
                    );


            jogo.setUltimoMinutoProcessado(
                    minuto
            );
        }
    }
}	