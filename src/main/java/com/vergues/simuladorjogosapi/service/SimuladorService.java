package com.vergues.simuladorjogosapi.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.vergues.simuladorjogosapi.model.Escalacao;
import com.vergues.simuladorjogosapi.model.Evento;
import com.vergues.simuladorjogosapi.model.Jogador;
import com.vergues.simuladorjogosapi.model.Jogo;
import com.vergues.simuladorjogosapi.model.Time;

@Service
public class SimuladorService {

    private final Random random = new Random();

    public Jogo simular(
            Time timeCasa,
            Time timeFora) {

        Jogo jogo =
                new Jogo(
                        timeCasa,
                        timeFora
                );

        int forcaCasa =
                calcularForcaPartida(
                        timeCasa,
                        true
                );

        int forcaFora =
                calcularForcaPartida(
                        timeFora,
                        false
                );

        int golsCasa =
                calcularGols(
                        forcaCasa,
                        forcaFora
                );

        int golsFora =
                calcularGols(
                        forcaFora,
                        forcaCasa
                );

        golsCasa =
                aplicarRegrasEspeciais(
                        timeCasa,
                        timeFora,
                        golsCasa,
                        golsFora
                );

        golsFora =
                aplicarRegrasEspeciais(
                        timeFora,
                        timeCasa,
                        golsFora,
                        golsCasa
                );

        jogo.setGolsCasa(golsCasa);
        jogo.setGolsFora(golsFora);

        return jogo;
    }

    public Escalacao gerarEscalacao(
            Time time) {

        List<Jogador> todos =
                new ArrayList<>(
                        time.getJogadores()
                );

        List<Jogador> titulares =
                new ArrayList<>();

        adicionarMelhoresPorPosicao(
                titulares,
                todos,
                "GOL",
                1
        );

        adicionarMelhoresPorPosicao(
                titulares,
                todos,
                "ZAG",
                2
        );

        adicionarLateral(
                titulares,
                todos,
                "LD"
        );

        adicionarLateral(
                titulares,
                todos,
                "LE"
        );

        adicionarMelhoresDoMeio(
                titulares,
                todos,
                3
        );

        adicionarMelhoresOfensivos(
                titulares,
                todos,
                3
        );

        while (titulares.size() < 11
                && !todos.isEmpty()) {

            Jogador melhor =
                    todos.stream()
                            .max(
                                    Comparator.comparingInt(
                                            Jogador::getOverall
                                    )
                            )
                            .orElse(null);

            if (melhor == null) {
                break;
            }

            titulares.add(melhor);
            todos.remove(melhor);
        }

        List<Jogador> reservas =
                new ArrayList<>(todos);

        return new Escalacao(
                titulares,
                reservas
        );
    }

    public List<Evento> gerarEventos(
            Time timeCasa,
            Time timeFora,
            int golsCasa,
            int golsFora) {

        Escalacao escalacaoCasa =
                gerarEscalacao(timeCasa);

        Escalacao escalacaoFora =
                gerarEscalacao(timeFora);

        List<Evento> eventos =
                new ArrayList<>();

        gerarEventosGol(
                eventos,
                timeCasa,
                escalacaoCasa,
                golsCasa
        );

        gerarEventosGol(
                eventos,
                timeFora,
                escalacaoFora,
                golsFora
        );

        gerarFaltas(
                eventos,
                timeCasa,
                escalacaoCasa,
                timeFora,
                escalacaoFora
        );

        gerarCartoesAmarelos(
                eventos,
                timeCasa,
                escalacaoCasa,
                timeFora,
                escalacaoFora
        );

        eventos.sort(
                Comparator.comparingInt(
                        Evento::getMinuto
                )
        );

        return eventos;
    }

    private void adicionarMelhoresPorPosicao(
            List<Jogador> titulares,
            List<Jogador> disponiveis,
            String posicao,
            int quantidade) {

        List<Jogador> candidatos =
                disponiveis.stream()
                        .filter(jogador ->
                                jogador.getPosicao()
                                        .equalsIgnoreCase(
                                                posicao
                                        )
                        )
                        .sorted(
                                Comparator.comparingInt(
                                        Jogador::getOverall
                                ).reversed()
                        )
                        .limit(quantidade)
                        .toList();

        titulares.addAll(candidatos);
        disponiveis.removeAll(candidatos);
    }

    private void adicionarLateral(
            List<Jogador> titulares,
            List<Jogador> disponiveis,
            String posicao) {

        Jogador lateral =
                disponiveis.stream()
                        .filter(jogador ->
                                jogador.getPosicao()
                                        .equalsIgnoreCase(
                                                posicao
                                        )
                        )
                        .max(
                                Comparator.comparingInt(
                                        Jogador::getOverall
                                )
                        )
                        .orElse(null);

        if (lateral != null) {
            titulares.add(lateral);
            disponiveis.remove(lateral);
        }
    }

    private void adicionarMelhoresDoMeio(
            List<Jogador> titulares,
            List<Jogador> disponiveis,
            int quantidade) {

        List<Jogador> candidatos =
                disponiveis.stream()
                        .filter(jogador ->
                                jogador.getPosicao()
                                        .equalsIgnoreCase("VOL")
                                || jogador.getPosicao()
                                        .equalsIgnoreCase("MC")
                                || jogador.getPosicao()
                                        .equalsIgnoreCase("MEI")
                        )
                        .sorted(
                                Comparator.comparingInt(
                                        Jogador::getOverall
                                ).reversed()
                        )
                        .limit(quantidade)
                        .toList();

        titulares.addAll(candidatos);
        disponiveis.removeAll(candidatos);
    }

    private void adicionarMelhoresOfensivos(
            List<Jogador> titulares,
            List<Jogador> disponiveis,
            int quantidade) {

        List<Jogador> candidatos =
                disponiveis.stream()
                        .filter(jogador ->
                                jogador.getPosicao()
                                        .equalsIgnoreCase("ATA")
                                || jogador.getPosicao()
                                        .equalsIgnoreCase("SA")
                                || jogador.getPosicao()
                                        .equalsIgnoreCase("PD")
                                || jogador.getPosicao()
                                        .equalsIgnoreCase("PE")
                        )
                        .sorted(
                                Comparator.comparingInt(
                                        Jogador::getOverall
                                ).reversed()
                        )
                        .limit(quantidade)
                        .toList();

        titulares.addAll(candidatos);
        disponiveis.removeAll(candidatos);
    }

    private int calcularForcaPartida(
            Time time,
            boolean mandante) {

        int forca = time.getForca();

        if (mandante) {
            forca += 3;
        }

        int variacao =
                random.nextInt(7) - 3;

        return forca + variacao;
    }

    private int calcularGols(
            int forcaAtacante,
            int forcaAdversario) {

        int diferenca =
                forcaAtacante
                - forcaAdversario;

        int chanceBase =
                35 + (diferenca * 2);

        chanceBase =
                Math.max(
                        10,
                        Math.min(
                                85,
                                chanceBase
                        )
                );

        int gols = 0;

        for (int tentativa = 0;
             tentativa < 5;
             tentativa++) {

            int chance =
                    random.nextInt(100);

            if (chance < chanceBase) {
                gols++;
            }

            chanceBase -= 8;

            if (chanceBase < 5) {
                chanceBase = 5;
            }
        }

        return gols;
    }

    private int aplicarRegrasEspeciais(
            Time time,
            Time adversario,
            int golsTime,
            int golsAdversario) {

        String regra =
                time.getRegraEspecial();

        if (regra == null) {
            return golsTime;
        }

        if (regra.equalsIgnoreCase(
                "melhor")) {

            if (!adversario.getSigla()
                    .equalsIgnoreCase("FLA")) {

                if (golsTime
                        <= golsAdversario) {

                    if (random.nextInt(100)
                            < 85) {

                        golsTime =
                                golsAdversario
                                + 1;
                    }
                }
            }
        }

        if (regra.equalsIgnoreCase(
                "elite")) {

            if (!adversario.getSigla()
                    .equalsIgnoreCase("PAL")) {

                if (golsTime
                        <= golsAdversario) {

                    if (random.nextInt(100)
                            < 75) {

                        golsTime =
                                golsAdversario
                                + 1;
                    }
                }
            }
        }

        if (regra.equalsIgnoreCase(
                "lanterna")) {

            if (golsTime > 1
                    && random.nextInt(100)
                    < 80) {

                golsTime = 1;
            }
        }

        return golsTime;
    }

    private void gerarEventosGol(
            List<Evento> eventos,
            Time time,
            Escalacao escalacao,
            int quantidadeGols) {

        for (int i = 0;
             i < quantidadeGols;
             i++) {

            Jogador jogador =
                    escolherArtilheiro(
                            escalacao
                                    .getTitulares()
                    );

            eventos.add(
                    new Evento(
                            gerarMinutoPartida(),
                            "GOL",
                            time.getNome(),
                            jogador.getNome(),
                            "Gol de "
                                    + jogador.getNome()
                                    + " para o "
                                    + time.getNome()
                    )
            );
        }
    }

    private void gerarFaltas(
            List<Evento> eventos,
            Time timeCasa,
            Escalacao escalacaoCasa,
            Time timeFora,
            Escalacao escalacaoFora) {

        int quantidade =
                random.nextInt(9) + 6;

        for (int i = 0;
             i < quantidade;
             i++) {

            boolean casa =
                    random.nextBoolean();

            Time time =
                    casa
                            ? timeCasa
                            : timeFora;

            Escalacao escalacao =
                    casa
                            ? escalacaoCasa
                            : escalacaoFora;

            Jogador jogador =
                    escolherJogadorParaFalta(
                            escalacao
                                    .getTitulares()
                    );

            eventos.add(
                    new Evento(
                            gerarMinutoPartida(),
                            "FALTA",
                            time.getNome(),
                            jogador.getNome(),
                            jogador.getNome()
                                    + " cometeu falta"
                    )
            );
        }
    }

    private void gerarCartoesAmarelos(
            List<Evento> eventos,
            Time timeCasa,
            Escalacao escalacaoCasa,
            Time timeFora,
            Escalacao escalacaoFora) {

        int quantidade =
                random.nextInt(5);

        for (int i = 0;
             i < quantidade;
             i++) {

            boolean casa =
                    random.nextBoolean();

            Time time =
                    casa
                            ? timeCasa
                            : timeFora;

            Escalacao escalacao =
                    casa
                            ? escalacaoCasa
                            : escalacaoFora;

            Jogador jogador =
                    escolherJogadorParaCartao(
                            escalacao
                                    .getTitulares()
                    );

            eventos.add(
                    new Evento(
                            random.nextInt(80) + 10,
                            "CARTAO_AMARELO",
                            time.getNome(),
                            jogador.getNome(),
                            "Cartão amarelo para "
                                    + jogador.getNome()
                    )
            );
        }
    }

    private Jogador escolherArtilheiro(
            List<Jogador> jogadores) {

        List<Jogador> candidatos =
                jogadores.stream()
                        .filter(jogador ->
                                !jogador.getPosicao()
                                        .equalsIgnoreCase("GOL")
                        )
                        .toList();

        return escolherPorPesoGol(
                candidatos
        );
    }

    private Jogador escolherPorPesoGol(
            List<Jogador> jogadores) {

        int pesoTotal = 0;

        for (Jogador jogador : jogadores) {
            pesoTotal +=
                    calcularPesoGol(jogador);
        }

        int sorteio =
                random.nextInt(pesoTotal);

        int acumulado = 0;

        for (Jogador jogador : jogadores) {

            acumulado +=
                    calcularPesoGol(
                            jogador
                    );

            if (sorteio < acumulado) {
                return jogador;
            }
        }

        return jogadores.get(
                jogadores.size() - 1
        );
    }

    private int calcularPesoGol(
            Jogador jogador) {

        int peso =
                jogador.getFinalizacao() * 3
                + jogador.getAtaque() * 2
                + jogador.getOverall();

        String posicao =
                jogador.getPosicao();

        if (posicao.equalsIgnoreCase("ATA")) {
            peso += 80;
        } else if (posicao.equalsIgnoreCase("SA")) {
            peso += 60;
        } else if (posicao.equalsIgnoreCase("PD")
                || posicao.equalsIgnoreCase("PE")) {
            peso += 45;
        } else if (posicao.equalsIgnoreCase("MEI")) {
            peso += 35;
        } else if (posicao.equalsIgnoreCase("MC")) {
            peso += 10;
        } else if (posicao.equalsIgnoreCase("VOL")) {
            peso -= 30;
        } else if (posicao.equalsIgnoreCase("ZAG")) {
            peso -= 50;
        } else if (posicao.equalsIgnoreCase("LD")
                || posicao.equalsIgnoreCase("LE")) {
            peso -= 25;
        }

        return Math.max(1, peso);
    }

    private Jogador escolherJogadorParaFalta(
            List<Jogador> jogadores) {

        return escolherPorPesoDefensivo(
                jogadores,
                false
        );
    }

    private Jogador escolherJogadorParaCartao(
            List<Jogador> jogadores) {

        return escolherPorPesoDefensivo(
                jogadores,
                true
        );
    }

    private Jogador escolherPorPesoDefensivo(
            List<Jogador> jogadores,
            boolean cartao) {

        int pesoTotal = 0;

        for (Jogador jogador : jogadores) {

            int peso =
                    calcularPesoDefensivo(
                            jogador,
                            cartao
                    );

            pesoTotal += peso;
        }

        int sorteio =
                random.nextInt(pesoTotal);

        int acumulado = 0;

        for (Jogador jogador : jogadores) {

            acumulado +=
                    calcularPesoDefensivo(
                            jogador,
                            cartao
                    );

            if (sorteio < acumulado) {
                return jogador;
            }
        }

        return jogadores.get(
                jogadores.size() - 1
        );
    }

    private int calcularPesoDefensivo(
            Jogador jogador,
            boolean cartao) {

        int peso =
                jogador.getDefesa()
                + jogador.getFisico();

        if (cartao) {

            String posicao =
                    jogador.getPosicao();

            if (posicao.equalsIgnoreCase("ZAG")) {
                peso += 40;
            } else if (posicao.equalsIgnoreCase("VOL")) {
                peso += 35;
            } else if (posicao.equalsIgnoreCase("LD")
                    || posicao.equalsIgnoreCase("LE")) {
                peso += 20;
            }
        }

        return Math.max(1, peso);
    }

    private int gerarMinutoPartida() {
        return random.nextInt(90) + 1;
    }
}