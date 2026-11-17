package com.vergues.simuladorjogosapi.service;

import com.vergues.simuladorjogosapi.model.Jogo;
import com.vergues.simuladorjogosapi.model.Time;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import tools.jackson.databind.ObjectMapper;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class SimuladorServiceTest {
    private final SimuladorService simulador = new SimuladorService();

    @Test
    void forcaInfluenciaMediasSemEliminarEmpatesEZebras() {
        List<Time> clubes = List.of(clube("palmeiras"), clube("flamengo"), clube("santos"),
                clube("goias"), clube("corinthians"));
        List<Time> adversarios = List.of(clube("botafogo"), clube("atletico-mg"), clube("cruzeiro"),
                clube("sao-paulo"), clube("bahia"), clube("internacional"), clube("vasco"),
                clube("red-bull-bragantino"), clube("fortaleza"), clube("ceara"));
        assertEquals(96, clubes.get(0).forca());
        assertEquals(92, clubes.get(1).forca());
        assertEquals(50, clubes.get(4).forca());
        double anterior = 4;
        double golsAnteriores = 10, chutesAnteriores = 100;
        for (Time clube : clubes) {
            int pontos = 0, vitorias = 0, empates = 0, derrotas = 0, gols = 0, chutes = 0, partidas = 0;
            Random seeds = new Random(20261006);
            for (int amostra = 0; amostra < 100; amostra++) {
                for (Time adversario : adversarios) {
                    // Mesmos adversários, seeds e dois mandos para todos os níveis de força.
                    for (boolean mandante : List.of(true, false)) {
                        Jogo jogo = jogo();
                        Jogo.Estado estado = estado();
                        estado.semente = seeds.nextLong();
                        simulador.simular(jogo, estado, mandante ? clube : adversario, mandante ? adversario : clube);
                        int pro = mandante ? jogo.golsCasa : jogo.golsFora;
                        int contra = mandante ? jogo.golsFora : jogo.golsCasa;
                        vitorias += pro > contra ? 1 : 0;
                        empates += pro == contra ? 1 : 0;
                        derrotas += pro < contra ? 1 : 0;
                        pontos += pro > contra ? 3 : pro == contra ? 1 : 0;
                        gols += pro;
                        chutes += (mandante ? estado.casa : estado.fora).finalizacoes;
                        partidas++;
                    }
                }
            }
            double media = (double) pontos / partidas;
            System.out.printf(java.util.Locale.ROOT,
                    "FORCA %s (%d): %d jogos, %.3f pontos/jogo, %.2f%% vitórias, %.3f gols/jogo, %.3f chutes/jogo%n",
                    clube.nome(), clube.forca(), partidas, media, 100.0 * vitorias / partidas,
                    (double) gols / partidas, (double) chutes / partidas);
            assertTrue(anterior - media > (clube.sigla().equals("FLA") ? 0.025 : 0.15), "Diferença pequena: " + clube.nome());
            assertTrue(empates > partidas * 0.10);
            assertTrue(derrotas > partidas * 0.05);
            assertTrue(vitorias > partidas * 0.03);
            assertTrue(vitorias < partidas * 0.85);
            assertTrue((double) gols / partidas < golsAnteriores);
            assertTrue((double) chutes / partidas < chutesAnteriores);
            anterior = media;
            golsAnteriores = (double) gols / partidas;
            chutesAnteriores = (double) chutes / partidas;
        }
    }

    @Test
    void somenteAForcaMudaODesempenhoComOMesmoElenco() {
        Time elenco = clube("santos");
        Time forte = new Time("Forte", "FRT", 96, elenco.jogadores(), null, null, null);
        Time fraco = new Time("Fraco", "FRC", 50, elenco.jogadores(), null, null, null);
        int venceuForte = 0, venceuFraco = 0, empates = 0;
        Random seeds = new Random(96150);
        for (int i = 0; i < 2000; i++) {
            boolean casa = i % 2 == 0;
            Jogo jogo = jogo();
            Jogo.Estado estado = estado();
            estado.semente = seeds.nextLong();
            simulador.simular(jogo, estado, casa ? forte : fraco, casa ? fraco : forte);
            int diferenca = casa ? jogo.golsCasa - jogo.golsFora : jogo.golsFora - jogo.golsCasa;
            venceuForte += diferenca > 0 ? 1 : 0;
            venceuFraco += diferenca < 0 ? 1 : 0;
            empates += diferenca == 0 ? 1 : 0;
        }
        assertTrue(venceuForte > 1000 && venceuForte < 1700);
        assertTrue(venceuFraco > 60);
        assertTrue(empates > 200);
        System.out.printf("FORCA ISOLADA 96 x 50: %.2f%% forte, %.2f%% empates, %.2f%% zebras%n",
                venceuForte / 20.0, empates / 20.0, venceuFraco / 20.0);
    }

    private Time clube(String arquivo) {
        try (var recurso = getClass().getResourceAsStream("/data/" + arquivo + ".json")) {
            return new ObjectMapper().readValue(recurso, Time.class);
        } catch (java.io.IOException e) { throw new IllegalStateException(e); }
    }

    @Test
    void simulacaoEncerraComPlacarEEventosCoerentes() {
        Jogo jogo = jogo();
        Jogo.Estado estado = estado();
        simulador.simular(jogo, estado, time("CAS"), time("FOR"));

        assertEquals(Jogo.Status.ENCERRADO, jogo.status);
        assertEquals(90, jogo.minutoAtual);
        assertFalse(estado.eventos.isEmpty());
        assertEquals(jogo.golsCasa + jogo.golsFora,
                estado.eventos.stream().filter(e -> e.tipo().equals("GOL")).count());
        assertEquals(100, estado.casa.posse + estado.fora.posse);
        int eventos = estado.eventos.size();
        simulador.simular(jogo, estado, time("CAS"), time("FOR"));
        assertEquals(eventos, estado.eventos.size());
    }

    @Test
    void reservaQueEntraESaiNaoVoltaAoCampoNemAosEventos() {
        Time casa = time("CAS");
        Jogo jogo = jogo();
        Jogo.Estado estado = estado();
        simulador.atualizar(jogo, estado, casa, time("FOR"), jogo.dataHora);
        int titular = estado.casa.campo.stream().filter(i -> i != 0).findFirst().orElseThrow();
        int reserva = estado.casa.reservas.getFirst();
        estado.casa.campo = new ArrayList<>(List.of(0, titular));
        estado.casa.reservas = new ArrayList<>(List.of(reserva, reserva + 1));
        simulador.substituir(estado, casa, estado.casa, 2, new Random(1));
        assertFalse(estado.casa.campo.contains(titular));
        assertTrue(estado.casa.campo.contains(reserva));
        simulador.substituir(estado, casa, estado.casa, 3, new Random(1));
        assertFalse(estado.casa.campo.contains(reserva));
        assertFalse(estado.casa.reservas.contains(reserva));

        int eventosAntes = estado.eventos.size();
        simulador.simular(jogo, estado, casa, time("FOR"));
        assertTrue(estado.eventos.subList(eventosAntes, estado.eventos.size()).stream()
                .noneMatch(e -> e.time().equals(casa.nome()) &&
                        (e.jogador().equals(casa.jogadores().get(titular).nome())
                                || e.jogador().equals(casa.jogadores().get(reserva).nome()))));
    }

    @Test
    void homonimosNaoCompartilhamCartoesOuExpulsoes() {
        Jogo jogo = jogo();
        Jogo.Estado estado = estado();
        Time casa = time("CAS");
        Time fora = time("FOR");
        simulador.atualizar(jogo, estado, casa, fora, jogo.dataHora);
        simulador.amarelo(estado, casa, estado.casa, 1, 2);
        simulador.amarelo(estado, casa, estado.casa, 1, 3);

        assertEquals(casa.jogadores().get(1).nome(), fora.jogadores().get(1).nome());
        assertFalse(estado.casa.campo.contains(1));
        assertTrue(estado.fora.campo.contains(1));
        assertEquals(2, estado.casa.amarelos.get(1));
        assertFalse(estado.fora.amarelos.containsKey(1));
        assertEquals(0, estado.fora.expulsos);
    }

    @Test
    void classicosSaoReconhecidosNosDoisMandos() {
        assertTrue(simulador.ehClassico("SAN", "SAO"));
        assertTrue(simulador.ehClassico("SAO", "SAN"));
        assertTrue(simulador.ehClassico("PAL", "COR"));
        assertFalse(simulador.ehClassico("PAL", "GOI"));
    }

    @Test
    void relogioRespeitaIntervaloEContinuaSemRepetirMinutos() {
        Jogo jogo = jogo();
        Jogo.Estado estado = estado();
        Time casa = time("CAS");
        Time fora = time("FOR");
        simulador.atualizar(jogo, estado, casa, fora, jogo.dataHora.minusSeconds(1));
        assertEquals(Jogo.Status.AGENDADO, jogo.status);
        simulador.atualizar(jogo, estado, casa, fora, jogo.dataHora.plusMinutes(45));
        assertEquals(Jogo.Status.INTERVALO, jogo.status);
        assertEquals(45, jogo.minutoAtual);
        int eventos = estado.eventos.size();
        simulador.atualizar(jogo, estado, casa, fora, jogo.dataHora.plusMinutes(59));
        assertEquals(eventos, estado.eventos.size());
        simulador.atualizar(jogo, estado, casa, fora, jogo.dataHora.plusMinutes(60));
        assertEquals(46, jogo.minutoAtual);
        simulador.atualizar(jogo, estado, casa, fora, jogo.dataHora.plusMinutes(105));
        assertEquals(Jogo.Status.ENCERRADO, jogo.status);
        assertEquals(90, jogo.minutoAtual);
    }

    private Jogo jogo() {
        Jogo jogo = new Jogo();
        jogo.dataHora = OffsetDateTime.parse("2030-04-05T16:00:00-03:00");
        return jogo;
    }

    private Jogo.Estado estado() {
        Jogo.Estado estado = new Jogo.Estado();
        estado.semente = 12345;
        return estado;
    }

    private Time time(String sigla) {
        List<Time.Jogador> jogadores = IntStream.range(0, 15)
                .mapToObj(i -> new Time.Jogador("Jogador " + i, i == 0 ? "GOL" : "ATA",
                        90 - i, 70, 70, 70, 70)).toList();
        return new Time(sigla, sigla, 80, jogadores, null, null, null);
    }
}
