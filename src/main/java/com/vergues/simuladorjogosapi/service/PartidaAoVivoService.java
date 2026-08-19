package com.vergues.simuladorjogosapi.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.vergues.simuladorjogosapi.model.Jogo;
import com.vergues.simuladorjogosapi.model.StatusJogo;
import com.vergues.simuladorjogosapi.repository.JogoRepository;

@Service
public class PartidaAoVivoService {

    /*
     * Repository onde os jogos do calendário
     * estão armazenados atualmente.
     *
     * Por enquanto é memória.
     *
     * Depois vamos substituir isso pelo banco.
     */
    private final JogoRepository jogoRepository;


    /*
     * Motor responsável por processar
     * cada minuto de futebol.
     */
    private final SimuladorService simuladorService;


    /*
     * Injeção de dependência feita pelo Spring.
     */
    public PartidaAoVivoService(
            JogoRepository jogoRepository,
            SimuladorService simuladorService) {

        this.jogoRepository =
                jogoRepository;

        this.simuladorService =
                simuladorService;
    }


    /*
     * =========================================================
     * RELÓGIO GLOBAL
     * =========================================================
     *
     * O Spring executa este método
     * automaticamente a cada 5 segundos.
     *
     * Ele verifica TODOS os jogos
     * e atualiza seus estados.
     */
    @Scheduled(fixedRate = 5000)
    public void atualizarPartidas() {

        /*
         * Horário atual da máquina/servidor.
         */
        LocalDateTime agora =
                LocalDateTime.now();


        /*
         * Busca todo o calendário.
         */
        List<Jogo> jogos =
                jogoRepository
                        .listarTodos();


        /*
         * Atualiza cada partida individualmente.
         */
        for (Jogo jogo : jogos) {

            atualizarJogo(
                    jogo,
                    agora
            );
        }
    }


    /*
     * =========================================================
     * ATUALIZA UMA PARTIDA
     * =========================================================
     */
    private void atualizarJogo(
            Jogo jogo,
            LocalDateTime agora) {

        /*
         * Segurança:
         * jogo sem data não pode ser processado.
         */
        if (jogo.getDataHora() == null) {
            return;
        }


        /*
         * =====================================================
         * JOGO AINDA NÃO COMEÇOU
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
         * Calculamos quantos minutos reais
         * passaram desde o horário marcado.
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
         *
         * 0 a 44 minutos reais.
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


            /*
             * Processa todos os minutos
             * que ainda não foram simulados.
             */
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
         *
         * Usamos 15 minutos reais.
         */
        if (minutosReais < 60) {

            jogo.setStatus(
                    StatusJogo.INTERVALO
            );


            jogo.setMinutoAtual(
                    45
            );


            /*
             * Garante que todo o primeiro tempo
             * foi processado.
             */
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
         *
         * De 60 até 104 minutos após
         * o horário marcado.
         */
        if (minutosReais < 105) {

            /*
             * Exemplo:
             *
             * 60 minutos reais = 46'
             * 61 minutos reais = 47'
             */
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
        jogo.setStatus(
                StatusJogo.ENCERRADO
        );


        jogo.setMinutoAtual(
                90
        );


        /*
         * Se a aplicação foi ligada depois do jogo,
         * ela simula automaticamente todos os minutos
         * que ainda não tinham sido processados.
         */
        processarAteMinuto(
                jogo,
                90
        );
    }


    /*
     * =========================================================
     * PROCESSADOR DE MINUTOS PENDENTES
     * =========================================================
     *
     * Isso é importantíssimo.
     *
     * Suponha que a aplicação começou quando
     * o jogo já estava aos 67'.
     *
     * Em vez de simplesmente pular para 67,
     * fazemos:
     *
     * 1
     * 2
     * 3
     * ...
     * 67
     *
     * e geramos todos os eventos da partida.
     */
    private void processarAteMinuto(
            Jogo jogo,
            int minutoDestino) {

        int ultimoProcessado =
                jogo.getUltimoMinutoProcessado();


        /*
         * Começa no minuto seguinte ao último
         * que já foi simulado.
         */
        for (
                int minuto =
                        ultimoProcessado + 1;

                minuto <= minutoDestino;

                minuto++
        ) {

            /*
             * Chama nosso motor.
             */
            simuladorService
                    .processarMinutoAoVivo(
                            jogo,
                            minuto
                    );


            /*
             * Marca esse minuto como concluído
             * para ele nunca ser gerado novamente.
             */
            jogo.setUltimoMinutoProcessado(
                    minuto
            );
        }
    }
}