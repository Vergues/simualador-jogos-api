package com.vergues.simuladorjogosapi.service;

import com.vergues.simuladorjogosapi.dto.PartidaResponse;
import com.vergues.simuladorjogosapi.model.Competicao;
import com.vergues.simuladorjogosapi.model.Jogo;
import com.vergues.simuladorjogosapi.model.Time;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PortalService {
    private final CampeonatoService campeonato;

    public PortalService(CampeonatoService campeonato) { this.campeonato = campeonato; }

    public List<PartidaResponse> jogos(int ano, String competicao, Integer rodada, String fase,
                                     String time, LocalDate data, String status, int limite) {
        if (limite < 1 || limite > 1500 || rodada != null && (rodada < 1 || rodada > 38)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Limite: 1–1500; rodada: 1–38.");
        }
        if (competicao != null) campeonato.competicao(competicao);
        String sigla = time == null ? null : campeonato.time(time).sigla();
        if (status != null && !List.of("proximos", "andamento", "encerrados").contains(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status: proximos, andamento ou encerrados.");
        }
        List<Jogo> jogos = ano == 0 ? campeonato.historico() : campeonato.jogosTemporada(ano);
        var consulta = jogos.stream().filter(j -> competicao == null || competicao.equals(j.competicao))
                .filter(j -> rodada == null || j.rodada == rodada)
                .filter(j -> fase == null || fase.equals(j.fase))
                .filter(j -> sigla == null || sigla.equals(j.siglaCasa) || sigla.equals(j.siglaFora))
                .filter(j -> data == null || data.equals(j.dataHora.toLocalDate()))
                .filter(j -> status == null || switch (status) {
                    case "proximos" -> j.status == Jogo.Status.AGENDADO;
                    case "andamento" -> j.status == Jogo.Status.AO_VIVO || j.status == Jogo.Status.INTERVALO;
                    default -> j.status == Jogo.Status.ENCERRADO;
                });
        Comparator<Jogo> ordem = Comparator.comparing(j -> j.dataHora);
        if ("encerrados".equals(status) || ano == 0) ordem = ordem.reversed();
        return consulta.sorted(ordem).limit(limite).map(campeonato::resumo).toList();
    }

    public Map<String, Object> inicio(int ano) {
        List<Jogo> jogos = campeonato.jogosTemporada(ano);
        return Map.of("temporada", ano, "data", campeonato.hoje(),
                "hoje", jogos.stream().filter(j -> j.dataHora.toLocalDate().equals(campeonato.hoje())).map(campeonato::resumo).toList(),
                "proximos", selecionar(jogos, false, 6), "resultados", selecionar(jogos, true, 6),
                "classificacao", tabela("brasileirao", jogos).stream().limit(6).toList(),
                "artilheiros", jogadores(jogos).stream().filter(j -> j.gols > 0).limit(5).toList(),
                "competicoes", competicoes(ano, jogos),
                "gols", jogos.stream().mapToInt(j -> j.golsCasa + j.golsFora).sum());
    }

    private List<PartidaResponse> selecionar(List<Jogo> jogos, boolean encerrados, int limite) {
        Comparator<Jogo> ordem = Comparator.comparing(j -> j.dataHora);
        return jogos.stream().filter(j -> encerrados ? j.status == Jogo.Status.ENCERRADO : j.status == Jogo.Status.AGENDADO)
                .sorted(encerrados ? ordem.reversed() : ordem).limit(limite).map(campeonato::resumo).toList();
    }

    public List<Map<String, Object>> competicoes(int ano) { return competicoes(ano, campeonato.jogosTemporada(ano)); }

    private List<Map<String, Object>> competicoes(int ano, List<Jogo> jogos) {
        return campeonato.competicoes().stream().map(c -> {
            List<Jogo> partidas = jogos.stream().filter(j -> c.id().equals(j.competicao)).toList();
            List<Time> clubes = campeonato.participantes(c, partidas);
            String campeao = partidas.stream().filter(j -> "Final".equals(j.fase) && j.vencedor != null)
                    .map(j -> campeonato.time(j.vencedor).nome()).findFirst().orElse("");
            if (c.finais().isEmpty() && !partidas.isEmpty() && partidas.stream().allMatch(j -> j.status == Jogo.Status.ENCERRADO)) {
                campeao = tabela(c.id(), jogos).getFirst().time;
            }
            Map<String, Object> resposta = new LinkedHashMap<>();
            resposta.put("id", c.id()); resposta.put("nome", c.nome()); resposta.put("formato", c.formato());
            resposta.put("descricao", !partidas.isEmpty() && clubes.size() != c.clubes().size()
                    ? "Esta temporada mantém os participantes e o calendário com que foi iniciada." : c.descricao());
            resposta.put("temporada", ano);
            resposta.put("clubes", clubes); resposta.put("jogos", partidas.size());
            resposta.put("encerrados", partidas.stream().filter(j -> j.status == Jogo.Status.ENCERRADO).count());
            resposta.put("fases", partidas.stream().anyMatch(j -> "Final".equals(j.fase))
                    ? partidas.stream().map(j -> j.fase).distinct().toList()
                    : java.util.stream.Stream.concat(java.util.stream.Stream.of(c.faseInicial()),
                    c.finais().stream().map(Competicao.Fase::nome)).distinct().toList());
            resposta.put("campeao", campeao);
            long grupos = partidas.isEmpty() ? c.grupos()
                    : partidas.stream().map(j -> j.grupo).filter(java.util.Objects::nonNull).distinct().count();
            resposta.put("grupos", grupos);
            int classificados = c.finais().isEmpty() ? 0
                    : c.finais().getFirst().nome().equals("Quartas") ? 8 : 16;
            resposta.put("classificadosPorGrupo", grupos > 0 ? classificados / grupos : c.classificadosPorGrupo());
            return resposta;
        }).toList();
    }

    public List<Competicao.Classificacao> classificacao(int ano, String id) {
        return tabela(id, campeonato.jogosTemporada(ano));
    }

    private List<Competicao.Classificacao> tabela(String id, List<Jogo> jogos) {
        Competicao c = campeonato.competicao(id);
        List<Jogo> partidas = jogos.stream().filter(j -> id.equals(j.competicao)
                && c.faseInicial().equals(j.fase)).toList();
        return CalendarioService.classificar(campeonato.participantes(c, jogos), partidas);
    }

    public List<JogadorEstatisticas> artilharia(int ano, String id) {
        if (id != null) campeonato.competicao(id);
        return jogadores(campeonato.jogosTemporada(ano).stream()
                .filter(j -> id == null || id.equals(j.competicao)).toList()).stream().filter(j -> j.gols > 0).toList();
    }

    private List<JogadorEstatisticas> jogadores(List<Jogo> jogos) {
        Map<String, JogadorEstatisticas> jogadores = new LinkedHashMap<>();
        for (Jogo jogo : jogos) {
            if (jogo.minutoAtual == 0) continue;
            for (Jogo.Evento evento : campeonato.lerEstado(jogo).eventos) {
                if (!List.of("GOL", "CARTAO_AMARELO", "CARTAO_VERMELHO").contains(evento.tipo())) continue;
                Time time = evento.siglaTime() != null ? campeonato.time(evento.siglaTime())
                        : campeonato.time(campeonato.time(jogo.siglaCasa).nome().equals(evento.time()) ? jogo.siglaCasa : jogo.siglaFora);
                int id = evento.jogadorId() == null ? -1 : evento.jogadorId();
                String chave = time.sigla() + ":" + (id < 0 ? evento.jogador() : id);
                // Eventos antigos são ligados ao mesmo jogador pelo elenco do seu próprio time.
                if (id < 0) {
                    for (int i = 0; i < time.jogadores().size(); i++) {
                        if (time.jogadores().get(i).nome().equals(evento.jogador())) { id = i; chave = time.sigla() + ":" + i; break; }
                    }
                }
                JogadorEstatisticas estatistica = jogadores.get(chave);
                if (estatistica == null) {
                    estatistica = new JogadorEstatisticas(id, evento.jogador(), time);
                    jogadores.put(chave, estatistica);
                }
                switch (evento.tipo()) {
                    case "GOL" -> estatistica.gols++;
                    case "CARTAO_AMARELO" -> estatistica.amarelos++;
                    default -> estatistica.vermelhos++;
                }
            }
        }
        return jogadores.values().stream().sorted(Comparator.comparingInt((JogadorEstatisticas j) -> j.gols).reversed()
                .thenComparing(j -> j.nome).thenComparing(j -> j.sigla)).toList();
    }

    public Map<String, Object> time(String slug, int ano) {
        Time time = campeonato.time(slug);
        List<Jogo> jogos = campeonato.jogosTemporada(ano).stream()
                .filter(j -> time.sigla().equals(j.siglaCasa) || time.sigla().equals(j.siglaFora)).toList();
        Competicao.Classificacao desempenho = CalendarioService.classificar(campeonato.listarTimes(), jogos).stream()
                .filter(c -> c.sigla.equals(time.sigla())).findFirst().orElseThrow();
        for (Jogo jogo : jogos) {
            if (jogo.status != Jogo.Status.ENCERRADO) continue;
            Jogo.Estado estado = campeonato.lerEstado(jogo);
            Jogo.Equipe equipe = jogo.siglaCasa.equals(time.sigla()) ? estado.casa : estado.fora;
            desempenho.amarelos += equipe.cartoesAmarelos;
            desempenho.vermelhos += equipe.expulsos;
        }
        List<Map<String, Object>> posicoes = new ArrayList<>();
        List<Jogo> temporada = campeonato.jogosTemporada(ano);
        for (Competicao c : campeonato.competicoes()) {
            if (jogos.stream().noneMatch(j -> c.id().equals(j.competicao))) continue;
            List<Competicao.Classificacao> tabela = tabela(c.id(), temporada);
            Competicao.Classificacao linha = tabela.stream().filter(l -> l.sigla.equals(time.sigla())).findFirst().orElseThrow();
            int posicao = (int) tabela.stream().filter(l -> java.util.Objects.equals(l.grupo, linha.grupo))
                    .takeWhile(l -> !l.sigla.equals(time.sigla())).count() + 1;
            String fase = jogos.stream().filter(j -> c.id().equals(j.competicao))
                    .max(Comparator.comparing(j -> j.dataHora)).map(j -> j.fase).orElse("");
            posicoes.add(Map.of("id", c.id(), "nome", c.nome(), "grupo", linha.grupo == null ? "" : linha.grupo,
                    "posicao", c.formato().equals("ELIMINATORIA") ? 0 : posicao, "formato", c.formato(), "fase", fase));
        }
        return Map.of("time", time, "desempenho", desempenho, "competicoes", posicoes,
                "ultimos", selecionar(jogos, true, 6), "proximos", selecionar(jogos, false, 6),
                "jogadores", jogadores(jogos));
    }

    public static class JogadorEstatisticas {
        public final int id;
        public final String nome, time, sigla, slug;
        public int gols, amarelos, vermelhos;

        JogadorEstatisticas(int id, String nome, Time time) {
            this.id = id; this.nome = nome; this.time = time.nome(); this.sigla = time.sigla(); this.slug = time.slug();
        }
    }
}
