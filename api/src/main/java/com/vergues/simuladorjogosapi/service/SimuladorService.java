package com.vergues.simuladorjogosapi.service;

import com.vergues.simuladorjogosapi.model.Jogo;
import com.vergues.simuladorjogosapi.model.Time;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;

@Service
public class SimuladorService {
    private static final Set<String> CLASSICOS = Set.of(
            "COR-PAL", "CAM-CRU", "GRE-INT", "FLA-FLU", "SAN-SAO", "FLA-VAS");

    public void atualizar(Jogo jogo, Jogo.Estado estado, Time casa, Time fora, OffsetDateTime agora) {
        if (jogo.status == Jogo.Status.ENCERRADO || agora.isBefore(jogo.dataHora)) {
            return;
        }
        long minutos = Duration.between(jogo.dataHora, agora).toMinutes();
        int destino = minutos < 45 ? (int) minutos + 1
                : minutos < 60 ? 45 : Math.min(90, (int) (minutos - 60) + 46);
        processarAte(jogo, estado, casa, fora, destino);
        jogo.status = minutos >= 105 ? Jogo.Status.ENCERRADO
                : minutos >= 45 && minutos < 60 ? Jogo.Status.INTERVALO : Jogo.Status.AO_VIVO;
    }

    public void simular(Jogo jogo, Jogo.Estado estado, Time casa, Time fora) {
        if (jogo.status != Jogo.Status.ENCERRADO) {
            processarAte(jogo, estado, casa, fora, 90);
            jogo.status = Jogo.Status.ENCERRADO;
        }
    }

    private void processarAte(Jogo jogo, Jogo.Estado estado, Time casa, Time fora, int destino) {
        if (estado.ultimoMinuto == 0) {
            escalar(casa, estado.casa);
            escalar(fora, estado.fora);
            Random sorteio = new Random(estado.semente);
            if (ehClassico(casa.sigla(), fora.sigla()) && sorteio.nextInt(100) < 25) {
                estado.minutoBriga = 25 + sorteio.nextInt(56);
            }
        }
        for (int minuto = estado.ultimoMinuto + 1; minuto <= destino; minuto++) {
            // Cada minuto tem seu sorteio próprio: retomar não depende da ordem de outros jogos.
            Random random = new Random(estado.semente ^ (minuto * 0x9E3779B97F4A7C15L));
            processarMinuto(jogo, estado, casa, fora, minuto, random);
            estado.ultimoMinuto = minuto;
        }
        jogo.minutoAtual = estado.ultimoMinuto;
    }

    private void processarMinuto(Jogo jogo, Jogo.Estado estado, Time casa, Time fora,
                                 int minuto, Random random) {
        if (minuto == estado.minutoBriga) {
            briga(estado, casa, fora, minuto, random);
        } else {
            if (minuto == 60 || minuto == 70 || minuto == 80) {
                for (Time time : List.of(casa, fora)) {
                    Jogo.Equipe equipe = time == casa ? estado.casa : estado.fora;
                    if (random.nextInt(100) < 65) {
                        int quantidade = 1 + random.nextInt(2);
                        for (int i = 0; i < quantidade; i++) {
                            substituir(estado, time, equipe, minuto, random);
                        }
                    }
                }
            }
            evento(jogo, estado, casa, fora, minuto, random);
        }
        int diferenca = forca(casa, estado.casa) - forca(fora, estado.fora);
        estado.casa.posse = Math.clamp(50 + diferenca / 3 + random.nextInt(3) - 1, 32, 68);
        estado.fora.posse = 100 - estado.casa.posse;
    }

    private void evento(Jogo jogo, Jogo.Estado estado, Time casa, Time fora, int minuto, Random random) {
        int sorteio = random.nextInt(1000);
        if (sorteio >= 370) {
            return;
        }
        int chanceCasa = Math.clamp(53 + forca(casa, estado.casa) - forca(fora, estado.fora), 10, 90);
        boolean disciplinar = sorteio >= 230 || sorteio >= 17 && sorteio < 37;
        boolean mandante = disciplinar
                ? random.nextBoolean() : random.nextInt(100) < chanceCasa;
        Time time = mandante ? casa : fora;
        Jogo.Equipe equipe = mandante ? estado.casa : estado.fora;
        if (equipe.campo.isEmpty()) {
            return;
        }
        if (sorteio < 17) {
            boolean golRecente = estado.eventos.stream().anyMatch(e ->
                    e.tipo().equals("GOL") && minuto - e.minuto() <= 1);
            if (!golRecente) {
                int jogador = finalizador(time, equipe, random);
                if (mandante) {
                    jogo.golsCasa++;
                } else {
                    jogo.golsFora++;
                }
                equipe.finalizacoes++;
                equipe.finalizacoesGol++;
                registrar(estado, minuto, "GOL", time, jogador, "Gol de %s! A bola está na rede!");
            }
        } else if (sorteio < 37) {
            int jogador = defensor(time, equipe, random);
            if (jogador >= 0) {
                amarelo(estado, time, equipe, jogador, minuto);
            }
        } else if (sorteio < 87) {
            Time defesa = mandante ? fora : casa;
            Jogo.Equipe adversario = mandante ? estado.fora : estado.casa;
            int atacante = finalizador(time, equipe, random);
            if (!adversario.campo.isEmpty()) {
                int goleiro = adversario.campo.stream()
                        .filter(i -> defesa.jogadores().get(i).posicao().equals("GOL"))
                        .findFirst().orElse(adversario.campo.getFirst());
                equipe.finalizacoes++;
                equipe.finalizacoesGol++;
                registrar(estado, minuto, "DEFESA", defesa, goleiro,
                        "%s defende o chute de " + time.jogadores().get(atacante).nome() + "!");
            }
        } else if (sorteio < 157) {
            equipe.finalizacoes++;
            registrar(estado, minuto, "FINALIZACAO", time, finalizador(time, equipe, random),
                    "%s finaliza, mas a bola não entra.");
        } else if (sorteio < 207) {
            equipe.escanteios++;
            estado.eventos.add(new Jogo.Evento(minuto, "ESCANTEIO", time.nome(), "",
                    "Escanteio para o " + time.nome() + ".", time.sigla(), null));
        } else if (sorteio < 230) {
            equipe.impedimentos++;
            registrar(estado, minuto, "IMPEDIMENTO", time, finalizador(time, equipe, random),
                    "%s é flagrado em impedimento.");
        } else {
            equipe.faltas++;
            int jogador = defensor(time, equipe, random);
            if (jogador >= 0 && random.nextInt(100) < 25) {
                registrar(estado, minuto, "FALTA", time, jogador, "%s comete uma falta.");
            }
        }
    }

    void substituir(Jogo.Estado estado, Time time, Jogo.Equipe equipe, int minuto, Random random) {
        List<Integer> candidatos = equipe.campo.stream()
                .filter(i -> !time.jogadores().get(i).posicao().equals("GOL")).toList();
        if (equipe.substituicoes >= 5 || candidatos.isEmpty() || equipe.reservas.isEmpty()) {
            return;
        }
        int saindo = candidatos.get(random.nextInt(candidatos.size()));
        List<Integer> compativeis = equipe.reservas.stream()
                .filter(i -> time.jogadores().get(i).posicao()
                        .equals(time.jogadores().get(saindo).posicao())).toList();
        if (compativeis.isEmpty()) {
            compativeis = equipe.reservas.stream()
                    .filter(i -> !time.jogadores().get(i).posicao().equals("GOL")).toList();
        }
        if (compativeis.isEmpty()) {
            return;
        }
        int entrando = compativeis.get(random.nextInt(compativeis.size()));
        equipe.campo.remove(Integer.valueOf(saindo));
        equipe.reservas.remove(Integer.valueOf(entrando));
        equipe.campo.add(entrando);
        equipe.substituicoes++;
        String nome = time.jogadores().get(entrando).nome();
        estado.eventos.add(new Jogo.Evento(minuto, "SUBSTITUICAO", time.nome(), nome,
                "Sai " + time.jogadores().get(saindo).nome() + " e entra " + nome + ".", time.sigla(), entrando));
    }

    void amarelo(Jogo.Estado estado, Time time, Jogo.Equipe equipe, int jogador, int minuto) {
        equipe.cartoesAmarelos++;
        int quantidade = equipe.amarelos.merge(jogador, 1, Integer::sum);
        registrar(estado, minuto, "CARTAO_AMARELO", time, jogador, "%s recebe cartão amarelo.");
        if (quantidade == 2) {
            expulsar(estado, time, equipe, jogador, minuto);
        }
    }

    private void expulsar(Jogo.Estado estado, Time time, Jogo.Equipe equipe, int jogador, int minuto) {
        equipe.campo.remove(Integer.valueOf(jogador));
        equipe.expulsos++;
        registrar(estado, minuto, "CARTAO_VERMELHO", time, jogador, "%s está expulso!");
    }

    private void briga(Jogo.Estado estado, Time casa, Time fora, int minuto, Random random) {
        estado.eventos.add(new Jogo.Evento(minuto, "BRIGA", "Ambos", "",
                "O clássico esquentou! Confusão entre os jogadores.", null, null));
        int resultado = random.nextInt(100);
        if (resultado < 60) {
            return;
        }
        boolean expulsarCasa = random.nextBoolean();
        for (Time time : List.of(casa, fora)) {
            Jogo.Equipe equipe = time == casa ? estado.casa : estado.fora;
            int jogador = defensor(time, equipe, random);
            if (jogador < 0) {
                continue;
            }
            if (resultado < 90) {
                amarelo(estado, time, equipe, jogador, minuto);
            } else if (resultado >= 98 || (time == casa) == expulsarCasa) {
                expulsar(estado, time, equipe, jogador, minuto);
            }
        }
    }

    private void escalar(Time time, Jogo.Equipe equipe) {
        equipe.reservas = new ArrayList<>(IntStream.range(0, time.jogadores().size()).boxed().toList());
        selecionar(time, equipe, Set.of("GOL"), 1);
        selecionar(time, equipe, Set.of("ZAG"), 2);
        selecionar(time, equipe, Set.of("LD"), 1);
        selecionar(time, equipe, Set.of("LE"), 1);
        selecionar(time, equipe, Set.of("VOL", "MC", "MEI"), 3);
        selecionar(time, equipe, Set.of("ATA", "SA", "PD", "PE"), 3);
        selecionar(time, equipe, Set.of(), 11 - equipe.campo.size());
    }

    private void selecionar(Time time, Jogo.Equipe equipe, Set<String> posicoes, int quantidade) {
        List<Integer> escolhidos = equipe.reservas.stream()
                .filter(i -> posicoes.isEmpty() || posicoes.contains(time.jogadores().get(i).posicao()))
                .sorted(Comparator.comparingInt((Integer i) -> time.jogadores().get(i).overall()).reversed())
                .limit(quantidade).toList();
        equipe.campo.addAll(escolhidos);
        equipe.reservas.removeAll(escolhidos);
    }

    private int finalizador(Time time, Jogo.Equipe equipe, Random random) {
        return sortear(equipe.campo, i -> {
            Time.Jogador jogador = time.jogadores().get(i);
            int peso = jogador.finalizacao() * 3 + jogador.ataque() * 2;
            return switch (jogador.posicao()) {
                case "GOL" -> 1;
                case "ZAG" -> peso / 8;
                case "LD", "LE" -> peso / 5;
                case "VOL" -> peso / 4;
                case "ATA", "SA", "PD", "PE" -> peso + 150;
                default -> peso;
            };
        }, random);
    }

    private int defensor(Time time, Jogo.Equipe equipe, Random random) {
        List<Integer> candidatos = equipe.campo.stream()
                .filter(i -> !time.jogadores().get(i).posicao().equals("GOL")).toList();
        return candidatos.isEmpty() ? -1 : sortear(candidatos, i -> {
            Time.Jogador jogador = time.jogadores().get(i);
            return jogador.defesa() + jogador.fisico();
        }, random);
    }

    private int sortear(List<Integer> candidatos, ToIntFunction<Integer> peso, Random random) {
        int total = candidatos.stream().mapToInt(i -> Math.max(1, peso.applyAsInt(i))).sum();
        int sorteio = random.nextInt(total);
        for (int jogador : candidatos) {
            sorteio -= Math.max(1, peso.applyAsInt(jogador));
            if (sorteio < 0) {
                return jogador;
            }
        }
        return candidatos.getLast();
    }

    private int forca(Time time, Jogo.Equipe equipe) {
        return time.forca() - equipe.expulsos * 8;
    }

    boolean ehClassico(String casa, String fora) {
        String chave = casa.compareTo(fora) < 0 ? casa + "-" + fora : fora + "-" + casa;
        return CLASSICOS.contains(chave);
    }

    private void registrar(Jogo.Estado estado, int minuto, String tipo, Time time,
                           int jogador, String texto) {
        String nome = time.jogadores().get(jogador).nome();
        estado.eventos.add(new Jogo.Evento(minuto, tipo, time.nome(), nome, texto.replace("%s", nome),
                time.sigla(), jogador));
    }
}
