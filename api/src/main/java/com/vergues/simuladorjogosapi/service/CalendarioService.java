package com.vergues.simuladorjogosapi.service;

import com.vergues.simuladorjogosapi.model.Competicao;
import com.vergues.simuladorjogosapi.model.Jogo;
import com.vergues.simuladorjogosapi.model.Time;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class CalendarioService {
    private final ObjectMapper mapper;
    private final Clock clock;

    public CalendarioService(ObjectMapper mapper, Clock clock) {
        this.mapper = mapper;
        this.clock = clock;
    }

    public List<Jogo> inicial(Competicao competicao, int ano, List<Time> clubes, List<Jogo> existentes) {
        List<Jogo> agenda = new ArrayList<>(existentes);
        List<Jogo> novos = new ArrayList<>();
        if (competicao.formato().equals("ELIMINATORIA")) {
            int quantidade = clubes.size();
            int potencia = Integer.highestOneBit(quantidade);
            List<Time> preliminar = quantidade == potencia ? clubes
                    : clubes.subList(quantidade - 2 * (quantidade - potencia), quantidade);
            novos.addAll(eliminatoria(competicao, ano, competicao.finais().getFirst(), preliminar, agenda));
        } else {
            int grupos = Math.max(1, competicao.grupos());
            for (int g = 0; g < grupos; g++) {
                List<Time> participantes = new ArrayList<>();
                for (int i = g; i < clubes.size(); i += grupos) participantes.add(clubes.get(i));
                String grupo = competicao.grupos() == 0 ? null : String.valueOf((char) ('A' + g));
                novos.addAll(liga(competicao, ano, participantes, grupo, agenda));
            }
        }
        return novos;
    }

    public List<Jogo> avancar(Competicao competicao, int ano, List<Time> clubes, List<Jogo> todos) {
        List<Jogo> jogos = todos.stream().filter(j -> j.temporada == ano
                && competicao.id().equals(j.competicao)).toList();
        List<Jogo> finalExistente = jogos.stream().filter(j -> "Final".equals(j.fase)).toList();
        if (!finalExistente.isEmpty()) {
            if (finalExistente.stream().allMatch(j -> j.status == Jogo.Status.ENCERRADO)) decidir(finalExistente);
            return List.of();
        }
        for (int i = 0; i < competicao.finais().size(); i++) {
            Competicao.Fase fase = competicao.finais().get(i);
            List<Jogo> atuais = jogos.stream().filter(j -> fase.nome().equals(j.fase)).toList();
            if (!atuais.isEmpty()) {
                if (atuais.stream().allMatch(j -> j.status == Jogo.Status.ENCERRADO)) decidir(atuais);
                continue;
            }
            List<Time> classificados;
            if (i == 0) {
                if (competicao.formato().equals("ELIMINATORIA")) return List.of();
                List<Jogo> primeira = jogos.stream().filter(j -> competicao.faseInicial().equals(j.fase)).toList();
                if (primeira.isEmpty() || primeira.stream().anyMatch(j -> j.status != Jogo.Status.ENCERRADO)) {
                    return List.of();
                }
                List<Competicao.Classificacao> tabela = classificar(clubes, primeira);
                Map<String, Integer> porGrupo = new LinkedHashMap<>();
                classificados = tabela.stream().filter(c -> porGrupo.merge(c.grupo == null ? "" : c.grupo,
                                1, Integer::sum) <= competicao.classificadosPorGrupo())
                        .map(c -> clubes.stream().filter(t -> t.sigla().equals(c.sigla)).findFirst().orElseThrow()).toList();
            } else {
                String anterior = competicao.finais().get(i - 1).nome();
                List<Jogo> anteriores = jogos.stream().filter(j -> anterior.equals(j.fase)).toList();
                if (anteriores.isEmpty() || anteriores.stream().anyMatch(j -> j.status != Jogo.Status.ENCERRADO)) {
                    return List.of();
                }
                decidir(anteriores);
                List<String> vencedores = anteriores.stream().filter(j -> j.vencedor != null)
                        .map(j -> j.vencedor).toList();
                List<String> siglas = new ArrayList<>();
                if (i == 1 && competicao.formato().equals("ELIMINATORIA")
                        && clubes.size() != Integer.highestOneBit(clubes.size())) {
                    int isentos = 2 * Integer.highestOneBit(clubes.size()) - clubes.size();
                    clubes.subList(0, isentos).forEach(t -> siglas.add(t.sigla()));
                }
                siglas.addAll(vencedores);
                classificados = siglas.stream().map(sigla -> clubes.stream()
                        .filter(t -> t.sigla().equals(sigla)).findFirst().orElseThrow()).toList();
            }
            return eliminatoria(competicao, ano, fase, classificados, new ArrayList<>(todos));
        }
        return List.of();
    }

    private List<Jogo> liga(Competicao c, int ano, List<Time> clubes, String grupo, List<Jogo> agenda) {
        List<Time> ordem = new ArrayList<>(clubes);
        if (ordem.size() % 2 != 0) ordem.add(null);
        int rodadas = ordem.size() - 1;
        List<Jogo> jogos = new ArrayList<>();
        LocalDate inicio = LocalDate.of(ano, c.mesInicio(), c.diaInicio());
        for (int rodada = 1; rodada <= rodadas; rodada++) {
            for (int i = 0; i < ordem.size() / 2; i++) {
                Time casa = ordem.get(rodada % 2 == 0 ? ordem.size() - 1 - i : i);
                Time fora = ordem.get(rodada % 2 == 0 ? i : ordem.size() - 1 - i);
                if (casa == null || fora == null) continue;
                jogos.add(agendar(c, ano, c.faseInicial(), grupo, rodada, casa, fora,
                        inicio.plusDays((long) (rodada - 1) * c.intervaloDias()), null, 0, agenda));
                if (c.returno()) {
                    jogos.add(agendar(c, ano, c.faseInicial(), grupo, rodada + rodadas, fora, casa,
                            inicio.plusDays((long) (rodada + rodadas - 1) * c.intervaloDias()), null, 0, agenda));
                }
            }
            Collections.rotate(ordem.subList(1, ordem.size()), 1);
        }
        return jogos;
    }

    private List<Jogo> eliminatoria(Competicao c, int ano, Competicao.Fase fase,
                                    List<Time> classificados, List<Jogo> agenda) {
        if (classificados.size() < 2 || classificados.size() % 2 != 0) {
            throw new IllegalStateException("Número inválido de classificados: " + c.nome());
        }
        List<Jogo> jogos = new ArrayList<>();
        LocalDate data = LocalDate.of(ano, fase.mes(), fase.dia());
        OffsetDateTime ultimo = agenda.stream().filter(j -> c.id().equals(j.competicao)
                        && j.temporada == ano).map(j -> j.dataHora).max(Comparator.naturalOrder()).orElse(null);
        if (ultimo != null && !data.isAfter(ultimo.toLocalDate())) data = ultimo.toLocalDate().plusDays(3);
        for (int i = 0; i < classificados.size() / 2; i++) {
            Time casa = classificados.get(i);
            Time fora = classificados.get(classificados.size() - 1 - i);
            String confronto = c.id() + "-" + ano + "-" + fase.nome() + "-" + i;
            Jogo ida = agendar(c, ano, fase.nome(), null, 0, casa, fora, data, confronto, 1, agenda);
            jogos.add(ida);
            if (fase.idaVolta()) {
                jogos.add(agendar(c, ano, fase.nome(), null, 0, fora, casa,
                        ida.dataHora.toLocalDate().plusDays(7), confronto, 2, agenda));
            }
        }
        return jogos;
    }

    private Jogo agendar(Competicao c, int ano, String fase, String grupo, int rodada,
                         Time casa, Time fora, LocalDate preferida, String confronto,
                         int perna, List<Jogo> agenda) {
        OffsetDateTime data = preferida.atTime(20, 0).atZone(clock.getZone()).toOffsetDateTime();
        int tentativas = 0;
        while (conflito(agenda, casa.sigla(), fora.sigla(), data)) {
            data = data.plusDays(1);
            if (++tentativas > 365) throw new IllegalStateException("Calendário sem datas disponíveis.");
        }
        Jogo jogo = new Jogo();
        jogo.competicao = c.id();
        jogo.campeonato = c.nome();
        jogo.temporada = ano;
        jogo.fase = fase;
        jogo.grupo = grupo;
        jogo.rodada = rodada;
        jogo.confronto = confronto;
        jogo.perna = perna;
        jogo.chave = confronto != null ? confronto + "-" + perna
                : (c.id().equals("brasileirao") ? "" : c.id() + "-")
                    + ano + "-" + rodada + "-" + casa.sigla() + "-" + fora.sigla();
        jogo.siglaCasa = casa.sigla();
        jogo.siglaFora = fora.sigla();
        jogo.dataHora = data;
        jogo.estadoJson = mapper.writeValueAsString(new Jogo.Estado());
        agenda.add(jogo);
        return jogo;
    }

    private boolean conflito(List<Jogo> agenda, String casa, String fora, OffsetDateTime data) {
        return agenda.stream().anyMatch(j -> (j.siglaCasa.equals(casa) || j.siglaCasa.equals(fora)
                || j.siglaFora.equals(casa) || j.siglaFora.equals(fora))
                && Math.abs(Duration.between(j.dataHora, data).toHours()) < 48);
    }

    private void decidir(List<Jogo> jogos) {
        Map<String, List<Jogo>> confrontos = new LinkedHashMap<>();
        jogos.forEach(j -> confrontos.computeIfAbsent(j.confronto, chave -> new ArrayList<>()).add(j));
        for (List<Jogo> partidas : confrontos.values()) {
            Jogo ultima = partidas.stream().max(Comparator.comparingInt(j -> j.perna)).orElseThrow();
            if (ultima.vencedor != null) continue;
            int casa = 0, fora = 0;
            for (Jogo jogo : partidas) {
                casa += jogo.siglaCasa.equals(ultima.siglaCasa) ? jogo.golsCasa : jogo.golsFora;
                fora += jogo.siglaCasa.equals(ultima.siglaCasa) ? jogo.golsFora : jogo.golsCasa;
            }
            if (casa == fora) {
                boolean ganhouCasa = new Random(mapper.readValue(ultima.estadoJson, Jogo.Estado.class).semente).nextBoolean();
                ultima.penaltisCasa = ganhouCasa ? 5 : 4;
                ultima.penaltisFora = ganhouCasa ? 4 : 5;
                ultima.vencedor = ganhouCasa ? ultima.siglaCasa : ultima.siglaFora;
            } else ultima.vencedor = casa > fora ? ultima.siglaCasa : ultima.siglaFora;
        }
    }

    public static List<Competicao.Classificacao> classificar(List<Time> clubes, List<Jogo> jogos) {
        Map<String, Competicao.Classificacao> tabela = new LinkedHashMap<>();
        clubes.forEach(t -> tabela.put(t.sigla(), new Competicao.Classificacao(t)));
        for (Jogo jogo : jogos) {
            Competicao.Classificacao casa = tabela.get(jogo.siglaCasa);
            Competicao.Classificacao fora = tabela.get(jogo.siglaFora);
            if (casa == null || fora == null) continue;
            if (jogo.grupo != null) { casa.grupo = jogo.grupo; fora.grupo = jogo.grupo; }
            if (jogo.status == Jogo.Status.ENCERRADO) {
                casa.registrar(jogo.golsCasa, jogo.golsFora);
                fora.registrar(jogo.golsFora, jogo.golsCasa);
            }
        }
        return tabela.values().stream().sorted(
                Comparator.comparing((Competicao.Classificacao c) -> c.grupo == null ? "" : c.grupo)
                        .thenComparing(Comparator.comparingInt((Competicao.Classificacao c) -> c.pontos).reversed())
                        .thenComparing(Comparator.comparingInt((Competicao.Classificacao c) -> c.vitorias).reversed())
                        .thenComparing(Comparator.comparingInt((Competicao.Classificacao c) -> c.saldo).reversed())
                        .thenComparing(Comparator.comparingInt((Competicao.Classificacao c) -> c.golsPro).reversed())
                        .thenComparing(c -> c.time)).toList();
    }
}
