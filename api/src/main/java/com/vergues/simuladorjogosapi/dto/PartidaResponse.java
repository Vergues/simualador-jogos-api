package com.vergues.simuladorjogosapi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vergues.simuladorjogosapi.model.Jogo;
import com.vergues.simuladorjogosapi.model.Time;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PartidaResponse(Long id, String campeonato, int temporada, int rodada,
                              OffsetDateTime dataHora, Jogo.Status status, int minutoAtual,
                              String timeCasa, String siglaCasa, int golsCasa,
                              String timeFora, String siglaFora, int golsFora,
                              List<Jogo.Evento> eventos, Estatisticas estatisticas,
                              String competicao, String fase, String grupo, int perna,
                              String slugCasa, String slugFora, String vencedor,
                              Integer penaltisCasa, Integer penaltisFora,
                              Map<String, List<JogadorEmJogo>> escalacoes) {
    public static PartidaResponse de(Jogo jogo, Time casa, Time fora, Jogo.Estado estado) {
        return new PartidaResponse(jogo.id, jogo.campeonato, jogo.temporada, jogo.rodada,
                jogo.dataHora, jogo.status, jogo.minutoAtual, casa.nome(), casa.sigla(), jogo.golsCasa,
                fora.nome(), fora.sigla(), jogo.golsFora,
                estado == null ? null : List.copyOf(estado.eventos),
                estado == null ? null : new Estatisticas(estado.casa, estado.fora),
                jogo.competicao, jogo.fase, jogo.grupo, jogo.perna, casa.slug(), fora.slug(), jogo.vencedor,
                jogo.penaltisCasa, jogo.penaltisFora,
                estado == null ? null : Map.of("casa", elenco(casa, estado.casa, estado),
                        "fora", elenco(fora, estado.fora, estado)));
    }

    public record JogadorEmJogo(int id, String nome, String posicao, String situacao, int amarelos) { }

    private static List<JogadorEmJogo> elenco(Time time, Jogo.Equipe equipe, Jogo.Estado estado) {
        return IntStream.range(0, time.jogadores().size()).mapToObj(i -> {
            Time.Jogador jogador = time.jogadores().get(i);
            boolean expulso = estado.eventos.stream().anyMatch(e -> e.tipo().equals("CARTAO_VERMELHO")
                    && time.nome().equals(e.time()) && (e.jogadorId() == null
                    ? jogador.nome().equals(e.jogador()) : e.jogadorId() == i));
            String situacao = estado.ultimoMinuto == 0 ? "Elenco" : expulso ? "Expulso"
                    : equipe.campo.contains(i) ? "Em campo" : equipe.reservas.contains(i) ? "Reserva" : "Substituído";
            return new JogadorEmJogo(i, jogador.nome(), jogador.posicao(), situacao, equipe.amarelos.getOrDefault(i, 0));
        }).toList();
    }

    public record Estatisticas(int posseCasa, int posseFora, int finalizacoesCasa,
                               int finalizacoesFora, int finalizacoesGolCasa, int finalizacoesGolFora,
                               int escanteiosCasa, int escanteiosFora, int faltasCasa, int faltasFora,
                               int amarelosCasa, int amarelosFora, int vermelhosCasa,
                               int vermelhosFora, int impedimentosCasa, int impedimentosFora) {
        Estatisticas(Jogo.Equipe casa, Jogo.Equipe fora) {
            this(casa.posse, fora.posse, casa.finalizacoes, fora.finalizacoes,
                    casa.finalizacoesGol, fora.finalizacoesGol, casa.escanteios, fora.escanteios,
                    casa.faltas, fora.faltas, casa.cartoesAmarelos, fora.cartoesAmarelos,
                    casa.expulsos, fora.expulsos, casa.impedimentos, fora.impedimentos);
        }
    }
}
