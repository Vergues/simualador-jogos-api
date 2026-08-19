package com.vergues.simuladorjogosapi.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.vergues.simuladorjogosapi.model.Campeonato;
import com.vergues.simuladorjogosapi.model.Jogo;
import com.vergues.simuladorjogosapi.model.StatusJogo;
import com.vergues.simuladorjogosapi.model.Time;

@Service
public class CalendarioService {

    public List<Jogo> gerarBrasileirao(
            List<Time> times,
            int ano) {

        if (times.size() != 20) {
            throw new RuntimeException(
                    "O Brasileirão precisa de exatamente 20 times."
            );
        }

        Campeonato brasileirao =
                new Campeonato(
                        1L,
                        "Campeonato Brasileiro",
                        String.valueOf(ano),
                        38
                );

        List<Time> listaTimes =
                new ArrayList<>(times);

        List<Jogo> jogos =
                new ArrayList<>();

        int totalTimes =
                listaTimes.size();

        int totalRodadasTurno =
                totalTimes - 1;

        int jogosPorRodada =
                totalTimes / 2;

        LocalDate dataBase =
                LocalDate.of(
                        ano,
                        4,
                        5
                );

        long idJogo = 1;

        for (int rodada = 1;
             rodada <= totalRodadasTurno;
             rodada++) {

            LocalDate dataRodada =
                    dataBase.plusWeeks(
                            rodada - 1
                    );

            for (int i = 0;
                 i < jogosPorRodada;
                 i++) {

                Time timeA =
                        listaTimes.get(i);

                Time timeB =
                        listaTimes.get(
                                totalTimes
                                        - 1
                                        - i
                        );

                boolean inverterMando =
                        rodada % 2 == 0;

                Time casa =
                        inverterMando
                                ? timeB
                                : timeA;

                Time fora =
                        inverterMando
                                ? timeA
                                : timeB;

                LocalDateTime dataHora =
                        gerarHorarioJogo(
                                dataRodada,
                                i
                        );

                Jogo jogo =
                        new Jogo(
                                casa,
                                fora
                        );

                jogo.setId(idJogo++);
                jogo.setRodada(rodada);
                jogo.setDataHora(dataHora);
                jogo.setStatus(
                        StatusJogo.AGENDADO
                );
                jogo.setCampeonato(
                        brasileirao
                );

                jogos.add(jogo);
            }

            rotacionarTimes(
                    listaTimes
            );
        }

        List<Jogo> returno =
                gerarReturno(
                        jogos,
                        brasileirao,
                        idJogo
                );

        jogos.addAll(returno);

        return jogos;
    }

    private List<Jogo> gerarReturno(
            List<Jogo> primeiroTurno,
            Campeonato campeonato,
            long idInicial) {

        List<Jogo> returno =
                new ArrayList<>();

        long idJogo =
                idInicial;

        for (Jogo jogoTurno : primeiroTurno) {

            Jogo jogoReturno =
                    new Jogo(
                            jogoTurno.getTimeFora(),
                            jogoTurno.getTimeCasa()
                    );

            jogoReturno.setId(
                    idJogo++
            );

            jogoReturno.setRodada(
                    jogoTurno.getRodada()
                            + 19
            );

            jogoReturno.setDataHora(
                    jogoTurno.getDataHora()
                            .plusWeeks(19)
            );

            jogoReturno.setStatus(
                    StatusJogo.AGENDADO
            );

            jogoReturno.setCampeonato(
                    campeonato
            );

            returno.add(
                    jogoReturno
            );
        }

        return returno;
    }

    private void rotacionarTimes(
            List<Time> times) {

        Time fixo =
                times.get(0);

        List<Time> restantes =
                new ArrayList<>(
                        times.subList(
                                1,
                                times.size()
                        )
                );

        Collections.rotate(
                restantes,
                1
        );

        times.clear();

        times.add(fixo);
        times.addAll(restantes);
    }

    private LocalDateTime gerarHorarioJogo(
            LocalDate dataRodada,
            int indiceJogo) {

        int diaExtra =
                indiceJogo / 4;

        int posicaoNoDia =
                indiceJogo % 4;

        LocalTime horario;

        if (posicaoNoDia == 0) {
            horario =
                    LocalTime.of(
                            16,
                            0
                    );

        } else if (posicaoNoDia == 1) {
            horario =
                    LocalTime.of(
                            18,
                            30
                    );

        } else if (posicaoNoDia == 2) {
            horario =
                    LocalTime.of(
                            19,
                            30
                    );

        } else {
            horario =
                    LocalTime.of(
                            21,
                            30
                    );
        }

        return LocalDateTime.of(
                dataRodada.plusDays(
                        diaExtra
                ),
                horario
        );
    }
}