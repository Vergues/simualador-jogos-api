package com.vergues.simuladorjogosapi.service;

import com.vergues.simuladorjogosapi.SimuladorJogosApiApplication;
import com.vergues.simuladorjogosapi.dto.AtualizacaoPartidaRequest;
import com.vergues.simuladorjogosapi.dto.PartidaResponse;
import com.vergues.simuladorjogosapi.model.Jogo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CampeonatoServiceTest {
    @TempDir
    Path diretorio;

    @Test
    void calendarioDoBrasileiraoTem380JogosE19MandosPorTime() {
        try (ConfigurableApplicationContext contexto = iniciar()) {
            CampeonatoService campeonato = contexto.getBean(CampeonatoService.class);
            List<PartidaResponse> jogos = campeonato.criarTemporada(2030);
            assertEquals(380, jogos.size());
            assertEquals(20, campeonato.listarTimes().size());
            assertEquals(38, jogos.stream().map(PartidaResponse::rodada).distinct().count());
            Set<String> confrontos = new HashSet<>();
            Map<String, Integer> mandos = new HashMap<>();
            Map<String, Integer> visitas = new HashMap<>();

            for (int rodada = 1; rodada <= 38; rodada++) {
                int numero = rodada;
                List<PartidaResponse> daRodada = jogos.stream().filter(j -> j.rodada() == numero).toList();
                assertEquals(10, daRodada.size());
                Set<String> participantes = new HashSet<>();
                for (PartidaResponse jogo : daRodada) {
                    assertNotEquals(jogo.siglaCasa(), jogo.siglaFora());
                    assertTrue(participantes.add(jogo.siglaCasa()));
                    assertTrue(participantes.add(jogo.siglaFora()));
                    assertTrue(confrontos.add(jogo.siglaCasa() + "-" + jogo.siglaFora()));
                    mandos.merge(jogo.siglaCasa(), 1, Integer::sum);
                    visitas.merge(jogo.siglaFora(), 1, Integer::sum);
                }
                assertEquals(20, participantes.size());
            }
            assertEquals(380, confrontos.size());
            campeonato.listarTimes().forEach(time -> {
                assertEquals(19, mandos.getOrDefault(time.sigla(), 0));
                assertEquals(19, visitas.getOrDefault(time.sigla(), 0));
            });
        }
    }

    @Test
    void atualizacaoExternaPersistePlacarEEventoEValidaErros() {
        try (ConfigurableApplicationContext contexto = iniciar()) {
            CampeonatoService campeonato = contexto.getBean(CampeonatoService.class);
            PartidaResponse partida = campeonato.criarTemporada(2030).getFirst();
            var evento = new AtualizacaoPartidaRequest.EventoRequest(1, "GOL", "Gol simulado", partida.siglaCasa(), null);
            var request = new AtualizacaoPartidaRequest(1, 1, 0, Jogo.Status.AO_VIVO, evento);

            PartidaResponse atualizada = campeonato.receberAtualizacao(partida.id(), request);
            assertEquals(1, atualizada.golsCasa());
            assertEquals(0, atualizada.golsFora());
            assertEquals(Jogo.Status.AO_VIVO, atualizada.status());
            assertEquals(1, atualizada.eventos().size());
            assertEquals("GOL", atualizada.eventos().getFirst().tipo());
            assertEquals(atualizada, campeonato.buscarJogo(partida.id()));

            var placarInvalido = new AtualizacaoPartidaRequest(2, -1, 0, Jogo.Status.AO_VIVO, null);
            assertThrows(ResponseStatusException.class,
                    () -> campeonato.receberAtualizacao(partida.id(), placarInvalido));
            assertThrows(ResponseStatusException.class,
                    () -> campeonato.buscarJogo(Long.MAX_VALUE));
        }
    }

    @Test
    void resultadoPermaneceDepoisDeReiniciarAplicacao() {
        PartidaResponse encerrada;
        try (ConfigurableApplicationContext contexto = iniciar()) {
            CampeonatoService campeonato = contexto.getBean(CampeonatoService.class);
            PartidaResponse agendada = campeonato.criarTemporada(2030).getFirst();
            encerrada = campeonato.simularJogo(agendada.id());
            assertEquals(Jogo.Status.ENCERRADO, encerrada.status());
        }
        try (ConfigurableApplicationContext contexto = iniciar()) {
            CampeonatoService campeonato = contexto.getBean(CampeonatoService.class);
            assertEquals(encerrada, campeonato.buscarJogo(encerrada.id()));
            assertEquals(380, campeonato.criarTemporada(2030).size());
        }
    }

    private ConfigurableApplicationContext iniciar() {
        return new SpringApplicationBuilder(SimuladorJogosApiApplication.class)
                .web(WebApplicationType.NONE)
                .run("--spring.datasource.url=jdbc:h2:file:" + diretorio.resolve("teste").toAbsolutePath(),
                        "--simulador.automatico=false", "--spring.main.banner-mode=off", "--logging.level.root=WARN");
    }
}
