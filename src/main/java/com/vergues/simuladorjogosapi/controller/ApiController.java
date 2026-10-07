package com.vergues.simuladorjogosapi.controller;

import com.vergues.simuladorjogosapi.dto.PartidaResponse;
import com.vergues.simuladorjogosapi.model.Time;
import com.vergues.simuladorjogosapi.model.Competicao;
import com.vergues.simuladorjogosapi.service.CampeonatoService;
import com.vergues.simuladorjogosapi.service.PortalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final CampeonatoService campeonato;
    private final PortalService portal;

    public ApiController(CampeonatoService campeonato, PortalService portal) {
        this.campeonato = campeonato;
        this.portal = portal;
    }

    @GetMapping("/portal")
    public Map<String, Object> inicio(@RequestParam(required = false) Integer temporada) {
        return portal.inicio(ano(temporada));
    }

    @GetMapping("/competicoes")
    public List<Map<String, Object>> competicoes(@RequestParam(required = false) Integer temporada) {
        return portal.competicoes(ano(temporada));
    }

    @PostMapping("/competicoes/calendario")
    public Map<String, Integer> calendarios(@RequestParam int ano) {
        campeonato.prepararTemporada(ano);
        return Map.of("temporada", ano);
    }

    @GetMapping("/jogos")
    public List<PartidaResponse> consulta(@RequestParam(required = false) Integer temporada,
            @RequestParam(required = false) String competicao, @RequestParam(required = false) Integer rodada,
            @RequestParam(required = false) String fase, @RequestParam(required = false) String time,
            @RequestParam(required = false) LocalDate data, @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1000") int limite) {
        return portal.jogos(ano(temporada), competicao, rodada, fase, time, data, status, limite);
    }

    @GetMapping("/jogos/{id}")
    public PartidaResponse detalhe(@PathVariable long id) { return campeonato.buscarJogo(id); }

    @GetMapping("/classificacao")
    public List<Competicao.Classificacao> tabela(@RequestParam(required = false) Integer temporada,
                                                @RequestParam(defaultValue = "brasileirao") String competicao) {
        return portal.classificacao(ano(temporada), competicao);
    }

    @GetMapping("/artilharia")
    public List<PortalService.JogadorEstatisticas> artilharia(@RequestParam(required = false) Integer temporada,
                                                            @RequestParam(required = false) String competicao) {
        return portal.artilharia(ano(temporada), competicao);
    }

    @GetMapping("/times/{slug}")
    public Map<String, Object> time(@PathVariable String slug, @RequestParam(required = false) Integer temporada) {
        return portal.time(slug, ano(temporada));
    }

    @GetMapping("/times")
    public List<Time> times() {
        return campeonato.listarTimes();
    }

    @PostMapping("/brasileirao/gerar-calendario")
    public Map<String, Integer> criarCalendario(@RequestParam int ano) {
        return Map.of("temporada", ano, "jogos", campeonato.criarTemporada(ano).size());
    }

    @GetMapping("/brasileirao/jogos")
    public List<PartidaResponse> jogos(@RequestParam(required = false) Integer temporada) {
        return campeonato.listarJogos(ano(temporada), null, null, null);
    }

    @GetMapping("/brasileirao/rodada/{rodada}")
    public List<PartidaResponse> rodada(@PathVariable int rodada,
                                      @RequestParam(required = false) Integer temporada) {
        return campeonato.listarJogos(ano(temporada), rodada, null, null);
    }

    @GetMapping("/brasileirao/data/{data}")
    public List<PartidaResponse> data(@PathVariable LocalDate data,
                                    @RequestParam(required = false) Integer temporada) {
        return campeonato.listarJogos(temporada == null ? data.getYear() : temporada, null, data, null);
    }

    @GetMapping("/brasileirao/hoje")
    public List<PartidaResponse> hoje() {
        return campeonato.listarJogos(campeonato.temporadaAtual(), null, campeonato.hoje(), null);
    }

    @GetMapping("/brasileirao/proximos")
    public List<PartidaResponse> proximos(@RequestParam(defaultValue = "10") int limite,
                                        @RequestParam(required = false) Integer temporada) {
        return campeonato.listarJogos(ano(temporada), null, null, limite);
    }

    @GetMapping("/brasileirao/jogo/{id}")
    public PartidaResponse partida(@PathVariable long id) {
        return campeonato.buscarJogo(id);
    }

    @PostMapping("/brasileirao/jogo/{id}/simular")
    public PartidaResponse simular(@PathVariable long id) {
        return campeonato.simularJogo(id);
    }

    @PostMapping("/jogos/simular")
    public PartidaResponse amistoso(@RequestParam String casa, @RequestParam String fora) {
        return campeonato.simularAmistoso(casa, fora);
    }

    @GetMapping("/brasileirao/classificacao")
    public List<Competicao.Classificacao> classificacao(
            @RequestParam(required = false) Integer temporada) {
        return campeonato.classificacao(ano(temporada));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> erro(ResponseStatusException erro) {
        int status = erro.getStatusCode().value();
        String mensagem = erro.getReason() == null ? "Erro na requisição." : erro.getReason();
        return ResponseEntity.status(status).body(Map.of("status", status, "erro", mensagem));
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<Map<String, Object>> parametroInvalido() {
        return ResponseEntity.badRequest().body(Map.of("status", 400,
                "erro", "Parâmetro ausente ou inválido. Datas devem usar AAAA-MM-DD."));
    }

    private int ano(Integer temporada) {
        return temporada == null ? campeonato.temporadaAtual() : temporada;
    }
}
