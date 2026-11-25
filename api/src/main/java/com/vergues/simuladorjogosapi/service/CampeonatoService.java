package com.vergues.simuladorjogosapi.service;

import com.vergues.simuladorjogosapi.dto.PartidaResponse;
import com.vergues.simuladorjogosapi.dto.AtualizacaoPartidaRequest;
import com.vergues.simuladorjogosapi.model.Competicao;
import com.vergues.simuladorjogosapi.model.Jogo;
import com.vergues.simuladorjogosapi.model.Time;
import com.vergues.simuladorjogosapi.repository.JogoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.UUID;

@Service
public class CampeonatoService {
    public static final String BRASILEIRAO = "Campeonato Brasileiro";
    private final JogoRepository repository;
    private final SimuladorService simulador;
    private final ObjectMapper mapper;
    private final TransactionTemplate transacao;
    private final Clock clock;
    private final boolean automatico;
    private final CalendarioService calendario;
    private final Map<String, Time> times = new LinkedHashMap<>();
    private final Map<String, String> escudos = new LinkedHashMap<>();
    private final List<Competicao> competicoes;

    public CampeonatoService(JogoRepository repository, SimuladorService simulador,
                             ObjectMapper mapper, TransactionTemplate transacao, Clock clock,
                             @Value("${simulador.automatico:true}") boolean automatico,
                             CalendarioService calendario) {
        this.repository = repository;
        this.simulador = simulador;
        this.mapper = mapper;
        this.transacao = transacao;
        this.clock = clock;
        this.automatico = automatico;
        this.calendario = calendario;
        try (InputStream recurso = getClass().getResourceAsStream("/static/img/times/escudos-manifest.json")) {
            for (Escudo escudo : mapper.readValue(recurso, Escudo[].class)) {
                escudos.put(escudo.sigla(), escudo.arquivo());
            }
        } catch (IOException e) { throw new IllegalStateException("Erro ao carregar escudos", e); }
        competicoes = List.of(lerRecurso("competicoes.json", Competicao[].class)).stream()
                .filter(c -> "brasileirao".equals(c.id())).toList();
        for (String arquivo : lerRecurso("times-index.json", String[].class)) carregarTime(arquivo);
        for (Competicao c : competicoes) {
            for (String sigla : c.clubes()) time(sigla);
        }
    }

    private void carregarTime(String arquivo) {
        Time time = lerRecurso(arquivo, Time.class);
        time = new Time(time.nome(), time.sigla(), time.forca(), time.jogadores(), time.slug(), time.pais(),
                escudos.getOrDefault(time.sigla(), time.escudo()));
        if (times.putIfAbsent(time.sigla(), time) != null || time.jogadores().size() < 11
                || time.jogadores().stream().noneMatch(j -> j.posicao().equals("GOL"))) {
            throw new IllegalStateException("Elenco inválido: " + arquivo);
        }
    }

    public synchronized void inicializar() {
        if (!automatico) return;
        transacao.executeWithoutResult(status -> repository.findAll().forEach(j -> {
            if (j.competicao == null) {
                j.competicao = BRASILEIRAO.equals(j.campeonato) ? "brasileirao" : "amistoso";
                j.fase = j.rodada == 0 ? "Amistoso" : "Pontos corridos";
            }
        }));
        int anoAtual = temporadaAtual();
        List<Jogo> antes = jogosTemporada(anoAtual).stream()
                .filter(j -> "brasileirao".equals(j.competicao)).toList();
        boolean podePrepararHistorico = antes.isEmpty()
                || antes.stream().allMatch(j -> j.status == Jogo.Status.AGENDADO);
        prepararTemporada(anoAtual);
        if (podePrepararHistorico) congelarHistorico(anoAtual);
        ajustarAgendaFutura(anoAtual);
        // O próximo calendário recebe expansões sem substituir jogos de temporadas já iniciadas.
        prepararTemporada(anoAtual + 1);
        finalizarPartidasAtrasadasNoInicio();
    }

    private void finalizarPartidasAtrasadasNoInicio() {
        OffsetDateTime agora = OffsetDateTime.now(clock);
        transacao.executeWithoutResult(status -> {
            for (Jogo jogo : repository.findAll()) {
                if (jogo.status == Jogo.Status.ENCERRADO || jogo.dataHora.isAfter(agora)) continue;
                Jogo.Estado estado = lerEstado(jogo);
                simulador.simular(jogo, estado, time(jogo.siglaCasa), time(jogo.siglaFora));
                salvar(jogo, estado);
            }
        });
    }

    private void congelarHistorico(int ano) {
        Competicao brasileirao = competicao("brasileirao");
        int primeiraRodadaAtiva = calendario.primeiraRodadaAtiva(brasileirao, ano);
        LocalDate hoje = hoje();
        transacao.executeWithoutResult(status -> {
            List<Jogo> historicos = repository.findByTemporadaOrderByDataHoraAsc(ano).stream()
                    .filter(j -> "brasileirao".equals(j.competicao)
                            && j.rodada < primeiraRodadaAtiva
                            && j.dataHora.toLocalDate().isBefore(hoje)
                            && j.status == Jogo.Status.AGENDADO)
                    .sorted(java.util.Comparator.comparingInt((Jogo j) -> j.rodada)
                            .thenComparing(j -> j.chave)).toList();
            if (historicos.isEmpty()) return;

            // A base histórica começa em empates; resultados determinísticos definem a tabela desejada.
            for (Jogo jogo : historicos) {
                definirEmpate(jogo);
                jogo.minutoAtual = 90;
                jogo.status = Jogo.Status.ENCERRADO;
            }
            Set<String> vencedoresContraLideres = new HashSet<>();
            vencedoresContraLideres.add(aplicarResultados(historicos, "PAL", Set.of("FLA", "COR", "VAS"), 9, 1));
            vencedoresContraLideres.add(aplicarResultados(historicos, "FLA", Set.of("PAL", "COR", "VAS"), 4, 1));

            // Corinthians perde uma partida para cada adversário, exceto Palmeiras e Flamengo.
            int derrotasCorinthians = 0;
            for (String adversario : brasileirao.clubes()) {
                if (Set.of("COR", "PAL", "FLA").contains(adversario)) continue;
                Jogo jogo = historicos.stream()
                        .filter(j -> j.siglaCasa.equals("COR") && j.siglaFora.equals(adversario)
                                || j.siglaFora.equals("COR") && j.siglaCasa.equals(adversario))
                        .findFirst().orElse(null);
                if (jogo != null) {
                    definirVencedor(jogo, adversario);
                    derrotasCorinthians++;
                }
            }
            if (derrotasCorinthians != 17) {
                throw new IllegalStateException("Não foi possível preparar o histórico do Corinthians.");
            }

            // O Vasco termina dez pontos acima do Corinthians e permanece em 19º.
            int derrotasVasco = 0;
            for (String adversario : brasileirao.clubes()) {
                if (derrotasVasco == 9) break;
                if (Set.of("VAS", "PAL", "FLA", "COR").contains(adversario)
                        || vencedoresContraLideres.contains(adversario)) continue;
                Jogo jogo = historicos.stream()
                        .filter(j -> j.siglaCasa.equals("VAS") && j.siglaFora.equals(adversario)
                                || j.siglaFora.equals("VAS") && j.siglaCasa.equals(adversario))
                        .findFirst().orElse(null);
                if (jogo != null) {
                    definirVencedor(jogo, adversario);
                    derrotasVasco++;
                }
            }
            if (derrotasVasco != 9) {
                throw new IllegalStateException("Não foi possível preparar os resultados históricos do Brasileirão.");
            }
            repository.saveAll(historicos);
        });
    }

    private String aplicarResultados(List<Jogo> jogos, String sigla, Set<String> ignorar,
                                     int vitorias, int derrotas) {
        List<Jogo> candidatos = jogos.stream()
                .filter(j -> j.siglaCasa.equals(sigla) || j.siglaFora.equals(sigla))
                .filter(j -> !ignorar.contains(j.siglaCasa.equals(sigla) ? j.siglaFora : j.siglaCasa))
                .toList();
        if (candidatos.size() < vitorias + derrotas) {
            throw new IllegalStateException("Partidas históricas insuficientes para " + sigla + ".");
        }
        for (int i = 0; i < vitorias; i++) definirVencedor(candidatos.get(i), sigla);
        String vencedorDaDerrota = null;
        for (int i = 0; i < derrotas; i++) {
            Jogo jogo = candidatos.get(vitorias + i);
            vencedorDaDerrota = jogo.siglaCasa.equals(sigla) ? jogo.siglaFora : jogo.siglaCasa;
            definirVencedor(jogo, vencedorDaDerrota);
        }
        return vencedorDaDerrota;
    }

    private void definirVencedor(Jogo jogo, String vencedor) {
        int diferenca = Math.floorMod(jogo.chave.hashCode(), 3) + 1;
        int golsDerrotado = Math.floorMod(jogo.chave.hashCode() >>> 3, diferenca);
        if (jogo.siglaCasa.equals(vencedor)) {
            jogo.golsCasa = golsDerrotado + diferenca;
            jogo.golsFora = golsDerrotado;
        } else {
            jogo.golsCasa = golsDerrotado;
            jogo.golsFora = golsDerrotado + diferenca;
        }
    }

    private void definirEmpate(Jogo jogo) {
        int gols = Math.floorMod(jogo.chave.hashCode(), 3);
        jogo.golsCasa = gols;
        jogo.golsFora = gols;
    }

    private void ajustarAgendaFutura(int ano) {
        Competicao brasileirao = competicao("brasileirao");
        transacao.executeWithoutResult(status -> {
            List<Jogo> jogos = repository.findByTemporadaOrderByDataHoraAsc(ano);
            List<Jogo> alterados = calendario.reagendarFuturos(brasileirao, ano, jogos);
            if (!alterados.isEmpty()) repository.saveAll(alterados);
        });
    }

    public int temporadaAtual() { return Year.now(clock).getValue(); }
    public LocalDate hoje() { return LocalDate.now(clock); }
    public List<Time> listarTimes() { return competicao("brasileirao").clubes().stream().map(this::time).toList(); }
    public List<Competicao> competicoes() { return competicoes; }

    public Competicao competicao(String id) {
        return competicoes.stream().filter(c -> c.id().equals(id)).findFirst()
                .orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Competição não encontrada."));
    }

    public Time time(String siglaOuSlug) {
        if (siglaOuSlug == null || siglaOuSlug.isBlank()) {
            throw erro(HttpStatus.BAD_REQUEST, "Informe o time.");
        }
        Time time = times.get(siglaOuSlug.trim().toUpperCase(Locale.ROOT));
        if (time == null) time = times.values().stream().filter(t -> t.slug().equals(siglaOuSlug)).findFirst().orElse(null);
        if (time == null) throw erro(HttpStatus.NOT_FOUND, "Time não encontrado: " + siglaOuSlug);
        return time;
    }

    public List<Time> participantes(Competicao c, List<Jogo> jogos) {
        List<String> siglas = jogos.stream().filter(j -> c.id().equals(j.competicao))
                .flatMap(j -> java.util.stream.Stream.of(j.siglaCasa, j.siglaFora)).distinct().toList();
        if (siglas.isEmpty() || c.formato().equals("ELIMINATORIA")
                && jogos.stream().noneMatch(j -> c.id().equals(j.competicao) && "Final".equals(j.fase))) {
            siglas = c.clubes();
        }
        return siglas.stream().map(this::time).toList();
    }

    public synchronized void prepararTemporada(int ano) {
        validarTemporada(ano);
        for (Competicao c : competicoes) criarCompeticao(c, ano);
    }

    public synchronized List<PartidaResponse> criarTemporada(int ano) {
        validarTemporada(ano);
        return criarCompeticao(competicao("brasileirao"), ano);
    }

    private List<PartidaResponse> criarCompeticao(Competicao c, int ano) {
        return transacao.execute(status -> {
            List<Jogo> todos = repository.findByTemporadaOrderByDataHoraAsc(ano);
            List<Jogo> jogos = todos.stream().filter(j -> c.id().equals(j.competicao)).toList();
            if (jogos.isEmpty()) jogos = repository.saveAll(calendario.inicial(c, ano, participantes(c, List.of()), todos));
            return jogos.stream().map(this::resumo).toList();
        });
    }

    // O monitor permanece ocupado até o commit, incluindo criação das fases seguintes.
    public synchronized List<Jogo> jogosTemporada(int ano) {
        validarTemporada(ano);
        return transacao.execute(status -> repository.findByTemporadaOrderByDataHoraAsc(ano));
    }

    public synchronized List<Jogo> historico() {
        return transacao.execute(status -> repository.findAll());
    }

    public synchronized List<PartidaResponse> listarJogos(int ano, Integer rodada, LocalDate data, Integer limite) {
        validarTemporada(ano);
        if (rodada != null && (rodada < 1 || rodada > 38)) throw erro(HttpStatus.BAD_REQUEST, "Rodada deve estar entre 1 e 38.");
        if (limite != null && (limite < 1 || limite > 380)) throw erro(HttpStatus.BAD_REQUEST, "Limite deve estar entre 1 e 380.");
        OffsetDateTime agora = OffsetDateTime.now(clock);
        return jogosTemporada(ano).stream().filter(j -> "brasileirao".equals(j.competicao))
                .filter(j -> rodada == null || j.rodada == rodada)
                .filter(j -> data == null || j.dataHora.toLocalDate().equals(data))
                .filter(j -> limite == null || j.status == Jogo.Status.AGENDADO && j.dataHora.isAfter(agora))
                .limit(limite == null ? 380 : limite).map(this::resumo).toList();
    }

    public synchronized PartidaResponse buscarJogo(long id) {
        return transacao.execute(status -> {
            Jogo jogo = encontrar(id);
            return resposta(jogo, lerEstado(jogo));
        });
    }

    public synchronized List<PartidaResponse> jogosAoVivo(int ano) {
        validarTemporada(ano);
        return jogosTemporada(ano).stream()
                .filter(j -> "brasileirao".equals(j.competicao) && j.status == Jogo.Status.AO_VIVO)
                .map(this::resumo).toList();
    }

    /** Recebe uma atualização enviada por um processo externo via HTTP. */
    public synchronized PartidaResponse receberAtualizacao(long id, AtualizacaoPartidaRequest request) {
        if (request == null || request.minuto() == null || request.golsMandante() == null
                || request.golsVisitante() == null || request.status() == null) {
            throw erro(HttpStatus.BAD_REQUEST, "Minuto, placar e status são obrigatórios.");
        }
        if (request.minuto() < 0 || request.minuto() > 120 || request.golsMandante() < 0
                || request.golsVisitante() < 0) {
            throw erro(HttpStatus.BAD_REQUEST, "Minuto deve estar entre 0 e 120 e gols não podem ser negativos.");
        }
        return transacao.execute(status -> {
            Jogo jogo = encontrar(id);
            Jogo.Estado estado = lerEstado(jogo);
            if (jogo.status == Jogo.Status.ENCERRADO) {
                throw erro(HttpStatus.BAD_REQUEST, "A partida já está encerrada.");
            }
            if (request.minuto() < jogo.minutoAtual) {
                throw erro(HttpStatus.BAD_REQUEST, "O minuto informado não pode regredir.");
            }
            jogo.minutoAtual = request.minuto();
            jogo.golsCasa = request.golsMandante();
            jogo.golsFora = request.golsVisitante();
            jogo.status = request.status();
            estado.ultimoMinuto = request.minuto();
            if (request.novoEvento() != null) {
                var e = request.novoEvento();
                if (e.minuto() == null || e.minuto() < 0 || e.minuto() > 120
                        || e.tipo() == null || e.tipo().isBlank() || e.descricao() == null
                        || e.descricao().isBlank()) {
                    throw erro(HttpStatus.BAD_REQUEST, "Evento inválido: minuto, tipo e descrição são obrigatórios.");
                }
                String sigla = e.siglaTime();
                if (sigla != null && !sigla.equals(jogo.siglaCasa) && !sigla.equals(jogo.siglaFora)) {
                    throw erro(HttpStatus.BAD_REQUEST, "O time do evento não participa desta partida.");
                }
                String nome = sigla == null ? null : time(sigla).nome();
                estado.eventos.add(new Jogo.Evento(e.minuto(), e.tipo(), nome, e.jogador(), e.descricao(), sigla, null));
            }
            salvar(jogo, estado);
            return resposta(jogo, estado);
        });
    }

    public synchronized PartidaResponse simularJogo(long id) {
        return transacao.execute(status -> {
            Jogo jogo = encontrar(id);
            Jogo.Estado estado = lerEstado(jogo);
            simulador.simular(jogo, estado, time(jogo.siglaCasa), time(jogo.siglaFora));
            salvar(jogo, estado);
            avancarFases(repository.findByTemporadaOrderByDataHoraAsc(jogo.temporada), jogo.temporada);
            return resposta(jogo, estado);
        });
    }

    public synchronized PartidaResponse simularAmistoso(String siglaCasa, String siglaFora) {
        Time casa = time(siglaCasa), fora = time(siglaFora);
        if (casa.sigla().equals(fora.sigla())) throw erro(HttpStatus.BAD_REQUEST, "Um time não pode jogar contra ele mesmo.");
        return transacao.execute(status -> {
            Jogo jogo = new Jogo();
            jogo.competicao = "amistoso";
            jogo.campeonato = "Amistoso";
            jogo.fase = "Amistoso";
            jogo.temporada = temporadaAtual();
            jogo.dataHora = OffsetDateTime.now(clock);
            jogo.siglaCasa = casa.sigla();
            jogo.siglaFora = fora.sigla();
            jogo.chave = "amistoso-" + UUID.randomUUID();
            Jogo.Estado estado = new Jogo.Estado();
            simulador.simular(jogo, estado, casa, fora);
            salvar(jogo, estado);
            return resposta(jogo, estado);
        });
    }

    @Scheduled(fixedDelayString = "${simulador.intervalo-ms:5000}")
    public synchronized void atualizarPartidas() {
        if (!automatico) return;
        OffsetDateTime agora = OffsetDateTime.now(clock);
        transacao.executeWithoutResult(status -> {
            List<Jogo> todos = new ArrayList<>(repository.findAll());
            for (int etapa = 0; etapa < 10; etapa++) {
                for (Jogo jogo : todos) {
                    if (jogo.status == Jogo.Status.ENCERRADO || jogo.dataHora.isAfter(agora)) continue;
                    Jogo.Estado estado = lerEstado(jogo);
                    int ultimo = estado.ultimoMinuto;
                    Jogo.Status anterior = jogo.status;
                    simulador.atualizar(jogo, estado, time(jogo.siglaCasa), time(jogo.siglaFora), agora);
                    if (ultimo != estado.ultimoMinuto || anterior != jogo.status) salvar(jogo, estado);
                }
                List<Jogo> novos = new ArrayList<>();
                for (int ano : todos.stream().map(j -> j.temporada).distinct().toList()) novos.addAll(avancarFases(todos, ano));
                if (novos.isEmpty()) break;
                todos.addAll(novos);
            }
        });
    }

    private List<Jogo> avancarFases(List<Jogo> todos, int ano) {
        List<Jogo> novos = new ArrayList<>();
        List<Jogo> agenda = new ArrayList<>(todos);
        for (Competicao c : competicoes) {
            List<Jogo> jogos = calendario.avancar(c, ano, participantes(c, todos), agenda);
            novos.addAll(repository.saveAll(jogos));
            agenda.addAll(jogos);
        }
        return novos;
    }

    public synchronized List<Competicao.Classificacao> classificacao(int ano) {
        List<Jogo> jogos = jogosTemporada(ano).stream().filter(j -> "brasileirao".equals(j.competicao)).toList();
        return CalendarioService.classificar(participantes(competicao("brasileirao"), jogos), jogos);
    }

    public Jogo.Estado lerEstado(Jogo jogo) { return mapper.readValue(jogo.estadoJson, Jogo.Estado.class); }
    public PartidaResponse resumo(Jogo jogo) { return resposta(jogo, null); }
    private PartidaResponse resposta(Jogo jogo, Jogo.Estado estado) {
        return PartidaResponse.de(jogo, time(jogo.siglaCasa), time(jogo.siglaFora), estado);
    }
    private void salvar(Jogo jogo, Jogo.Estado estado) {
        jogo.estadoJson = mapper.writeValueAsString(estado);
        repository.save(jogo);
    }
    private Jogo encontrar(long id) {
        if (id < 1) throw erro(HttpStatus.BAD_REQUEST, "ID deve ser positivo.");
        return repository.findById(id).orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Partida não encontrada."));
    }
    private void validarTemporada(int ano) {
        if (ano < 2000 || ano > 2100) throw erro(HttpStatus.BAD_REQUEST, "Temporada deve estar entre 2000 e 2100.");
    }
    private ResponseStatusException erro(HttpStatus status, String mensagem) { return new ResponseStatusException(status, mensagem); }
    private <T> T lerRecurso(String arquivo, Class<T> tipo) {
        try (InputStream recurso = getClass().getResourceAsStream("/data/" + arquivo)) {
            if (recurso == null) throw new IllegalStateException("Arquivo não encontrado: " + arquivo);
            return mapper.readValue(recurso, tipo);
        } catch (IOException e) { throw new IllegalStateException("Erro ao carregar " + arquivo, e); }
    }

    private record Escudo(String nome, String sigla, String arquivo, String tipo, String observacao) { }
}
