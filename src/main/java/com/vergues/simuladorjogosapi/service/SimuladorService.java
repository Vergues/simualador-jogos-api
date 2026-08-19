package com.vergues.simuladorjogosapi.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.vergues.simuladorjogosapi.model.Evento;
import com.vergues.simuladorjogosapi.model.Jogo;
import com.vergues.simuladorjogosapi.model.Time;

@Service
public class SimuladorService {

    /*
     * =========================================================
     * RNG DO FLUXO DA PARTIDA
     * =========================================================
     */

    private final Random random =
            new Random();


    /*
     * Quem realmente cria os eventos.
     */
    private final EventoService eventoService;


    /*
     * =========================================================
     * CLÁSSICOS
     * =========================================================
     */

    private static final Set<String> CLASSICOS =
            Set.of(

                    "COR-PAL",
                    "CAM-CRU",
                    "GRE-INT",
                    "FLA-FLU",
                    "SAN-SAO",
                    "FLA-VAS"
            );


    public SimuladorService(
            EventoService eventoService) {

        this.eventoService =
                eventoService;
    }


    /*
     * =========================================================
     * SIMULAÇÃO COMPLETA
     * =========================================================
     */

    public Jogo simular(
            Time timeCasa,
            Time timeFora) {

        Jogo jogo =
                new Jogo(
                        timeCasa,
                        timeFora
                );


        for (
                int minuto = 1;
                minuto <= 90;
                minuto++
        ) {

            processarMinutoAoVivo(
                    jogo,
                    minuto
            );
        }


        jogo.setMinutoAtual(
                90
        );


        jogo.setUltimoMinutoProcessado(
                90
        );


        return jogo;
    }


    /*
     * =========================================================
     * PROCESSAMENTO MINUTO A MINUTO
     * =========================================================
     */

    public void processarMinutoAoVivo(
            Jogo jogo,
            int minuto) {

        /*
         * Primeiro verifica clássico.
         */
        prepararBrigaSeNecessario(
                jogo
        );


        /*
         * Briga tem prioridade sobre
         * qualquer outro evento daquele minuto.
         */
        if (
                jogo.isBrigaProgramada()
                && jogo.getMinutoBriga()
                        == minuto
        ) {

            eventoService.gerarBriga(
                    jogo,
                    minuto
            );


            atualizarPosse(
                    jogo
            );


            return;
        }


        /*
         * =====================================================
         * SUBSTITUIÇÕES
         * =====================================================
         *
         * Tentamos substituições em minutos específicos.
         *
         * Assim não precisamos ficar sorteando
         * substituição todos os 90 minutos.
         */
        processarSubstituicoes(
                jogo,
                minuto
        );


        /*
         * =====================================================
         * EVENTO NORMAL
         * =====================================================
         */

        int sorteio =
                random.nextInt(
                        1000
                );


        /*
         * GOL
         *
         * ~1,7%
         */
        if (sorteio < 17) {

            if (
                    !houveGolRecente(
                            jogo,
                            minuto
                    )
            ) {

                eventoService
                        .gerarGol(
                                jogo,
                                minuto
                        );
            }


            atualizarPosse(
                    jogo
            );


            return;
        }


        /*
         * CARTÃO
         *
         * ~2%
         */
        if (sorteio < 37) {

            eventoService
                    .gerarCartaoAmarelo(
                            jogo,
                            minuto
                    );


            atualizarPosse(
                    jogo
            );


            return;
        }


        /*
         * DEFESA / CHUTE NO GOL
         */
        if (sorteio < 87) {

            eventoService
                    .gerarDefesa(
                            jogo,
                            minuto
                    );


            atualizarPosse(
                    jogo
            );


            return;
        }


        /*
         * FINALIZAÇÃO
         */
        if (sorteio < 157) {

            eventoService
                    .gerarFinalizacao(
                            jogo,
                            minuto
                    );


            atualizarPosse(
                    jogo
            );


            return;
        }


        /*
         * ESCANTEIO
         */
        if (sorteio < 207) {

            eventoService
                    .gerarEscanteio(
                            jogo,
                            minuto
                    );


            atualizarPosse(
                    jogo
            );


            return;
        }


        /*
         * IMPEDIMENTO
         */
        if (sorteio < 230) {

            eventoService
                    .gerarImpedimento(
                            jogo,
                            minuto
                    );


            atualizarPosse(
                    jogo
            );


            return;
        }


        /*
         * =====================================================
         * FALTAS
         * =====================================================
         *
         * Aproximadamente 14% por minuto.
         *
         * Isso gera algo perto de 12 a 15 faltas
         * totais em muitos jogos.
         *
         * Porém só 25% delas aparecem
         * na timeline.
         */
        if (sorteio < 370) {

            boolean mostrar =
                    random.nextInt(100)
                            < 25;


            eventoService
                    .gerarFalta(
                            jogo,
                            minuto,
                            mostrar
                    );


            atualizarPosse(
                    jogo
            );


            return;
        }


        /*
         * Minuto sem evento relevante.
         */
        atualizarPosse(
                jogo
        );
    }


    /*
     * =========================================================
     * SUBSTITUIÇÕES
     * =========================================================
     */

    private void processarSubstituicoes(
            Jogo jogo,
            int minuto) {

        /*
         * Três janelas principais.
         */
        if (
                minuto != 60
                && minuto != 68
                && minuto != 76
                && minuto != 82
        ) {

            return;
        }


        /*
         * Cada equipe possui 65% de chance
         * de realizar uma substituição
         * naquela janela.
         */
        if (
                random.nextInt(100)
                        < 65
        ) {

            eventoService
                    .gerarSubstituicao(
                            jogo,
                            jogo.getTimeCasa(),
                            minuto
                    );
        }


        if (
                random.nextInt(100)
                        < 65
        ) {

            eventoService
                    .gerarSubstituicao(
                            jogo,
                            jogo.getTimeFora(),
                            minuto
                    );
        }
    }


    /*
     * =========================================================
     * PROTEÇÃO CONTRA GOLS COLADOS
     * =========================================================
     */

    private boolean houveGolRecente(
            Jogo jogo,
            int minutoAtual) {

        return jogo.getEventos()
                .stream()
                .filter(evento ->
                        evento.getTipo()
                                .equalsIgnoreCase(
                                        "GOL"
                                )
                )
                .anyMatch(evento ->
                        Math.abs(
                                evento.getMinuto()
                                - minutoAtual
                        ) <= 1
                );
    }


    /*
     * =========================================================
     * CLÁSSICOS
     * =========================================================
     */

    private void prepararBrigaSeNecessario(
            Jogo jogo) {

        /*
         * Só avalia uma vez.
         */
        if (jogo.isBrigaAvaliada()) {
            return;
        }


        jogo.setBrigaAvaliada(
                true
        );


        if (!ehClassico(jogo)) {
            return;
        }


        /*
         * 25% de chance.
         */
        if (
                random.nextInt(100)
                        < 25
        ) {

            jogo.setBrigaProgramada(
                    true
            );


            jogo.setMinutoBriga(
                    25 + random.nextInt(56)
            );
        }
    }


    private boolean ehClassico(
            Jogo jogo) {

        String casa =
                jogo.getTimeCasa()
                        .getSigla()
                        .toUpperCase();


        String fora =
                jogo.getTimeFora()
                        .getSigla()
                        .toUpperCase();


        String chave;


        if (
                casa.compareTo(
                        fora
                ) < 0
        ) {

            chave =
                    casa
                    + "-"
                    + fora;

        } else {

            chave =
                    fora
                    + "-"
                    + casa;
        }


        return CLASSICOS.contains(
                chave
        );
    }


    /*
     * =========================================================
     * POSSE DE BOLA
     * =========================================================
     *
     * A posse agora considera:
     *
     * - força dos times;
     * - jogadores expulsos.
     */
    private void atualizarPosse(
            Jogo jogo) {

        int forcaCasa =
                calcularForcaEfetiva(
                        jogo,
                        jogo.getTimeCasa()
                );


        int forcaFora =
                calcularForcaEfetiva(
                        jogo,
                        jogo.getTimeFora()
                );


        int diferenca =
                forcaCasa
                - forcaFora;


        int posseCasa =
                50
                + (diferenca / 3);


        /*
         * Pequena variação natural.
         */
        posseCasa +=
                random.nextInt(3)
                - 1;


        /*
         * Limite razoável.
         */
        posseCasa =
                Math.max(
                        32,
                        Math.min(
                                68,
                                posseCasa
                        )
                );


        jogo.getEstatisticas()
                .setPosseCasa(
                        posseCasa
                );


        jogo.getEstatisticas()
                .setPosseFora(
                        100
                        - posseCasa
                );
    }


    /*
     * =========================================================
     * FORÇA EFETIVA
     * =========================================================
     *
     * Cada jogador expulso:
     *
     * -8 de força.
     */
    private int calcularForcaEfetiva(
            Jogo jogo,
            Time time) {

        int expulsos =
                contarExpulsos(
                        jogo,
                        time
                );


        return time.getForca()
                - (expulsos * 8);
    }


    private int contarExpulsos(
            Jogo jogo,
            Time time) {

        int quantidade = 0;


        for (
                var jogador :
                time.getJogadores()
        ) {

            if (
                    jogo.jogadorExpulso(
                            jogador
                    )
            ) {

                quantidade++;
            }
        }


        return quantidade;
    }


    /*
     * =========================================================
     * ENDPOINT ANTIGO
     * =========================================================
     */

    public List<Evento> gerarEventos(
            Time timeCasa,
            Time timeFora,
            int golsCasa,
            int golsFora) {

        return new ArrayList<>();
    }
}