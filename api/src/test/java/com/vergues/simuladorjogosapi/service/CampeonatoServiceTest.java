package com.vergues.simuladorjogosapi.service;

import com.vergues.simuladorjogosapi.SimuladorJogosApiApplication;
import com.vergues.simuladorjogosapi.dto.PartidaResponse;
import com.vergues.simuladorjogosapi.model.Jogo;
import com.vergues.simuladorjogosapi.model.Time;
import com.vergues.simuladorjogosapi.model.Competicao;
import com.vergues.simuladorjogosapi.repository.JogoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.ObjectMapper;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

class CampeonatoServiceTest {
    @TempDir
    Path diretorio;

    @Test
    void calendarioPersistenciaReinicioEUnicidade() throws Exception {
        PartidaResponse encerrada;
        PartidaResponse parcial;
        PartidaResponse esperada;
        try (ConfigurableApplicationContext contexto = iniciar()) {
            CampeonatoService campeonato = contexto.getBean(CampeonatoService.class);
            JogoRepository repository = contexto.getBean(JogoRepository.class);
            List<PartidaResponse> calendario = campeonato.criarTemporada(2030);
            assertEquals(380, calendario.size());
            Set<String> confrontos = new HashSet<>();
            for (int rodada = 1; rodada <= 38; rodada++) {
                int numero = rodada;
                List<PartidaResponse> jogos = calendario.stream().filter(j -> j.rodada() == numero).toList();
                assertEquals(10, jogos.size());
                Set<String> times = new HashSet<>();
                for (PartidaResponse jogo : jogos) {
                    assertNotEquals(jogo.siglaCasa(), jogo.siglaFora());
                    assertTrue(times.add(jogo.siglaCasa()));
                    assertTrue(times.add(jogo.siglaFora()));
                    assertTrue(confrontos.add(jogo.siglaCasa() + "-" + jogo.siglaFora()));
                    assertEquals(2030, jogo.temporada());
                    assertEquals(CampeonatoService.BRASILEIRAO, jogo.campeonato());
                }
                assertEquals(20, times.size());
            }

            long id = calendario.getFirst().id();
            try (var tarefas = Executors.newFixedThreadPool(3)) {
                var a = tarefas.submit(() -> campeonato.simularJogo(id));
                var b = tarefas.submit(() -> campeonato.simularJogo(id));
                var c = tarefas.submit(() -> campeonato.criarTemporada(2030));
                encerrada = a.get();
                assertEquals(encerrada, b.get());
                assertEquals(380, c.get().size());
            }
            assertEquals(encerrada, campeonato.buscarJogo(id));
            assertEquals(380, repository.count());
            assertEquals(2, campeonato.classificacao(2030).stream().mapToInt(c -> c.jogos).sum());
            assertEquals(380, campeonato.criarTemporada(2031).size());
            assertEquals(760, repository.count());

            Jogo duplicado = repository.findById(id).orElseThrow();
            duplicado.id = null;
            assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(duplicado));

            // Salva um jogo no meio da partida e calcula a continuação esperada sem gravá-la.
            long outroId = calendario.get(1).id();
            Jogo jogo = repository.findById(outroId).orElseThrow();
            Clock relogio = Clock.fixed(jogo.dataHora.plusMinutes(20).toInstant(), ZoneId.of("America/Sao_Paulo"));
            CampeonatoService aoVivo = new CampeonatoService(repository, contexto.getBean(SimuladorService.class),
                    contexto.getBean(ObjectMapper.class), contexto.getBean(TransactionTemplate.class), relogio, true,
                    contexto.getBean(CalendarioService.class));
            aoVivo.atualizarPartidas();
            parcial = campeonato.buscarJogo(outroId);
            assertEquals(Jogo.Status.AO_VIVO, parcial.status());
            assertEquals(21, parcial.minutoAtual());
            jogo = repository.findById(outroId).orElseThrow();
            ObjectMapper mapper = contexto.getBean(ObjectMapper.class);
            Jogo.Estado estado = mapper.readValue(jogo.estadoJson, Jogo.Estado.class);
            List<Time> times = campeonato.listarTimes();
            String casa = jogo.siglaCasa;
            String fora = jogo.siglaFora;
            Time timeCasa = times.stream().filter(t -> t.sigla().equals(casa)).findFirst().orElseThrow();
            Time timeFora = times.stream().filter(t -> t.sigla().equals(fora)).findFirst().orElseThrow();
            contexto.getBean(SimuladorService.class).simular(jogo, estado, timeCasa, timeFora);
            esperada = PartidaResponse.de(jogo, timeCasa, timeFora, estado);
        }

        try (ConfigurableApplicationContext contexto = iniciar()) {
            CampeonatoService campeonato = contexto.getBean(CampeonatoService.class);
            assertEquals(encerrada, campeonato.buscarJogo(encerrada.id()));
            assertEquals(encerrada, campeonato.simularJogo(encerrada.id()));
            assertEquals(parcial, campeonato.buscarJogo(parcial.id()));
            assertEquals(esperada, campeonato.simularJogo(parcial.id()));
            campeonato.criarTemporada(2030);
            assertEquals(760, contexto.getBean(JogoRepository.class).count());
        }
    }

    private ConfigurableApplicationContext iniciar() {
        return new SpringApplicationBuilder(SimuladorJogosApiApplication.class)
                .web(WebApplicationType.NONE)
                .run("--spring.datasource.url=jdbc:h2:file:" + diretorio.resolve("teste").toAbsolutePath(),
                        "--simulador.automatico=false", "--spring.main.banner-mode=off", "--logging.level.root=WARN");
    }

    @Test
    void calendarioGeralTemGruposEIntervaloDeDescanso() {
        try (var contexto = iniciar()) {
            var campeonato = contexto.getBean(CampeonatoService.class);
            campeonato.prepararTemporada(2032);
            List<Jogo> jogos = campeonato.jogosTemporada(2032);
            assertEquals(540, jogos.size());
            assertEquals(380, jogos.stream().filter(j -> j.competicao.equals("brasileirao")).count());
            assertEquals(48, jogos.stream().filter(j -> j.competicao.equals("paulista")).count());
            assertEquals(96, jogos.stream().filter(j -> j.competicao.equals("libertadores")).count());
            assertEquals(16, jogos.stream().filter(j -> j.competicao.equals("copa-do-brasil")).count());
            for (String grupo : List.of("A", "B", "C", "D", "E", "F", "G", "H")) {
                var partidas = jogos.stream().filter(j -> grupo.equals(j.grupo) && j.competicao.equals("libertadores")).toList();
                assertEquals(12, partidas.size());
                assertEquals(4, partidas.stream().flatMap(j -> java.util.stream.Stream.of(j.siglaCasa, j.siglaFora)).distinct().count());
            }
            for (Time time : campeonato.listarTimes()) {
                List<Jogo> agenda = jogos.stream().filter(j -> j.siglaCasa.equals(time.sigla()) || j.siglaFora.equals(time.sigla()))
                        .sorted(Comparator.comparing(j -> j.dataHora)).toList();
                for (int i = 1; i < agenda.size(); i++) {
                    assertTrue(Duration.between(agenda.get(i - 1).dataHora, agenda.get(i).dataHora).toHours() >= 48);
                }
            }
            campeonato.prepararTemporada(2032);
            assertEquals(540, contexto.getBean(JogoRepository.class).count());
            assertEquals(62, campeonato.listarTimes().size());
            for (Time time : campeonato.listarTimes()) {
                assertNotNull(time.escudo());
                assertNotNull(getClass().getResource("/static" + time.escudo()), time.sigla());
            }
            assertEquals(List.of("PAL", "FLA"), campeonato.listarTimes().stream()
                    .sorted(Comparator.comparingInt(Time::forca).reversed()).limit(2).map(Time::sigla).toList());
        }
    }

    @Test
    void mataMataEsperaVoltaResolvePenaltisEChegaAFinal() {
        try (var contexto = iniciar()) {
            var campeonato = contexto.getBean(CampeonatoService.class);
            var repository = contexto.getBean(JogoRepository.class);
            campeonato.prepararTemporada(2032);
            List<Jogo> preliminar = campeonato.jogosTemporada(2032).stream()
                    .filter(j -> j.competicao.equals("copa-do-brasil")).toList();
            Jogo ida = preliminar.getFirst();
            Jogo volta = preliminar.stream().filter(j -> j.confronto.equals(ida.confronto) && j.perna == 2).findFirst().orElseThrow();
            ida.status = Jogo.Status.ENCERRADO;
            repository.save(ida);
            campeonato.simularJogo(ida.id);
            assertEquals(16, campeonato.jogosTemporada(2032).stream().filter(j -> j.competicao.equals("copa-do-brasil")).count());
            for (Jogo jogo : preliminar) {
                jogo.status = Jogo.Status.ENCERRADO;
                jogo.golsCasa = jogo.golsFora = 0;
                repository.save(jogo);
            }
            campeonato.simularJogo(volta.id);
            Jogo decidida = repository.findById(volta.id).orElseThrow();
            assertNotNull(decidida.vencedor);
            assertNotEquals(decidida.penaltisCasa, decidida.penaltisFora);
            for (String competicao : List.of("copa-do-brasil", "paulista")) {
                for (int fase = 0; fase < 6; fase++) {
                    var agendados = campeonato.jogosTemporada(2032).stream().filter(j -> competicao.equals(j.competicao)
                            && j.status != Jogo.Status.ENCERRADO).toList();
                    if (agendados.isEmpty()) break;
                    agendados.forEach(j -> campeonato.simularJogo(j.id));
                }
                Jogo finalJogo = campeonato.jogosTemporada(2032).stream().filter(j -> competicao.equals(j.competicao)
                        && "Final".equals(j.fase)).findFirst().orElseThrow();
                assertEquals(Jogo.Status.ENCERRADO, finalJogo.status);
                assertNotNull(finalJogo.vencedor);
            }
            long quantidade = repository.count();
            campeonato.simularJogo(volta.id);
            assertEquals(quantidade, repository.count());
        }
    }

    @Test
    void libertadoresClassificaDezesseisEAvancaAteCampeao() {
        try (var contexto = iniciar()) {
            var campeonato = contexto.getBean(CampeonatoService.class);
            campeonato.prepararTemporada(2032);
            campeonato.jogosTemporada(2032).stream().filter(j -> "libertadores".equals(j.competicao))
                    .forEach(j -> campeonato.simularJogo(j.id));
            var oitavas = campeonato.jogosTemporada(2032).stream()
                    .filter(j -> "libertadores".equals(j.competicao) && "Oitavas".equals(j.fase)).toList();
            assertEquals(16, oitavas.size());
            assertEquals(16, oitavas.stream().flatMap(j -> java.util.stream.Stream.of(j.siglaCasa, j.siglaFora)).distinct().count());
            assertEquals(32, contexto.getBean(PortalService.class).classificacao(2032, "libertadores").size());
            for (int fase = 0; fase < 4; fase++) {
                campeonato.jogosTemporada(2032).stream().filter(j -> "libertadores".equals(j.competicao)
                        && j.status != Jogo.Status.ENCERRADO).forEach(j -> campeonato.simularJogo(j.id));
            }
            var jogos = campeonato.jogosTemporada(2032).stream().filter(j -> "libertadores".equals(j.competicao)).toList();
            assertEquals(125, jogos.size());
            assertTrue(jogos.stream().allMatch(j -> j.status == Jogo.Status.ENCERRADO));
            assertNotNull(jogos.stream().filter(j -> "Final".equals(j.fase)).findFirst().orElseThrow().vencedor);
        }
    }

    @Test
    void artilhariaECartoesDerivadosNaoMisturamHomonimos() {
        try (var contexto = iniciar()) {
            var campeonato = contexto.getBean(CampeonatoService.class);
            var repository = contexto.getBean(JogoRepository.class);
            var portal = contexto.getBean(PortalService.class);
            Time casa = campeonato.time("PAL");
            Time fora = campeonato.listarTimes().stream().filter(t -> !t.sigla().equals(casa.sigla())
                    && t.jogadores().stream().anyMatch(j -> casa.jogadores().stream().anyMatch(p -> p.nome().equals(j.nome()))))
                    .findFirst().orElseThrow();
            String nome = casa.jogadores().stream().filter(j -> fora.jogadores().stream().anyMatch(p -> p.nome().equals(j.nome())))
                    .findFirst().orElseThrow().nome();
            var partida = campeonato.simularAmistoso(casa.sigla(), fora.sigla());
            Jogo jogo = repository.findById(partida.id()).orElseThrow();
            var estado = campeonato.lerEstado(jogo);
            int idCasa = java.util.stream.IntStream.range(0, casa.jogadores().size())
                    .filter(i -> casa.jogadores().get(i).nome().equals(nome)).findFirst().orElseThrow();
            int idFora = java.util.stream.IntStream.range(0, fora.jogadores().size())
                    .filter(i -> fora.jogadores().get(i).nome().equals(nome)).findFirst().orElseThrow();
            estado.eventos = new java.util.ArrayList<>(List.of(
                    new Jogo.Evento(1, "GOL", casa.nome(), nome, "Gol", casa.sigla(), idCasa),
                    new Jogo.Evento(2, "GOL", fora.nome(), nome, "Gol", fora.sigla(), idFora),
                    new Jogo.Evento(3, "CARTAO_AMARELO", casa.nome(), nome, "Amarelo", casa.sigla(), idCasa)));
            estado.casa.cartoesAmarelos = 1;
            estado.fora.cartoesAmarelos = 0;
            jogo.golsCasa = jogo.golsFora = 1;
            jogo.estadoJson = contexto.getBean(ObjectMapper.class).writeValueAsString(estado);
            repository.save(jogo);
            var artilheiros = portal.artilharia(jogo.temporada, null);
            assertEquals(2, artilheiros.size());
            assertEquals(2, artilheiros.stream().map(a -> a.sigla).distinct().count());
            assertTrue(artilheiros.stream().allMatch(a -> a.gols == 1));
            assertEquals(1, artilheiros.stream().filter(a -> a.sigla.equals("PAL")).findFirst().orElseThrow().amarelos);
            var desempenho = (com.vergues.simuladorjogosapi.model.Competicao.Classificacao) portal.time("palmeiras", jogo.temporada).get("desempenho");
            assertEquals(1, desempenho.empates);
            assertEquals(33.3, desempenho.aproveitamento);
        }
    }

    @Test
    void expansaoNaoRecriaUmaCompeticaoJaEncerrada() {
        try (var contexto = iniciar()) {
            var campeonato = contexto.getBean(CampeonatoService.class);
            var calendario = contexto.getBean(CalendarioService.class);
            var repository = contexto.getBean(JogoRepository.class);
            var antigo = new Competicao("paulista", "Campeonato Paulista", "LIGA", "Formato anterior",
                    List.of("PAL", "COR", "SAN", "SAO", "RBB"), "Primeira fase", 1, 10, 7,
                    true, 0, 4, List.of(new Competicao.Fase("Semifinal", 3, 21, false),
                    new Competicao.Fase("Final", 3, 28, false)));
            var clubes = antigo.clubes().stream().map(campeonato::time).toList();
            var jogos = new java.util.ArrayList<>(calendario.inicial(antigo, 2032, clubes, List.of()));
            for (int fase = 0; fase < 3; fase++) {
                jogos.forEach(j -> j.status = Jogo.Status.ENCERRADO);
                jogos.addAll(calendario.avancar(antigo, 2032, clubes, jogos));
            }
            repository.saveAll(jogos);
            assertEquals(23, jogos.size());
            var finalJogo = jogos.stream().filter(j -> "Final".equals(j.fase)).findFirst().orElseThrow();
            String vencedor = finalJogo.vencedor;
            campeonato.prepararTemporada(2032);
            campeonato.simularJogo(finalJogo.id);
            var preservados = campeonato.jogosTemporada(2032).stream()
                    .filter(j -> "paulista".equals(j.competicao)).toList();
            assertEquals(23, preservados.size());
            assertEquals(vencedor, repository.findById(finalJogo.id).orElseThrow().vencedor);
            assertEquals(5, campeonato.participantes(campeonato.competicao("paulista"), preservados).size());
        }
    }
}
