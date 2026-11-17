package com.vergues.simuladorjogosapi.controller;

import com.vergues.simuladorjogosapi.dto.AtualizacaoPartidaRequest;
import com.vergues.simuladorjogosapi.dto.PartidaResponse;
import com.vergues.simuladorjogosapi.model.Competicao;
import com.vergues.simuladorjogosapi.model.Jogo;
import com.vergues.simuladorjogosapi.model.Time;
import com.vergues.simuladorjogosapi.service.CampeonatoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ApiV1Controller {
    private final CampeonatoService campeonato;
    public ApiV1Controller(CampeonatoService campeonato) {
        this.campeonato = campeonato;
    }

    @GetMapping("/times")
    public List<Time> times() { return campeonato.listarTimes(); }

    @GetMapping("/jogos")
    public List<PartidaResponse> jogos(@RequestParam(required = false) Integer temporada,
                                       @RequestParam(defaultValue = "380") int limite) {
        return campeonato.listarJogos(temporada == null ? campeonato.temporadaAtual() : temporada,
                null, null, limite);
    }

    @GetMapping("/jogos/{id}")
    public PartidaResponse jogo(@PathVariable long id) { return campeonato.buscarJogo(id); }

    @GetMapping("/jogos/rodada/{numero}")
    public List<PartidaResponse> rodada(@PathVariable int numero,
                                        @RequestParam(required = false) Integer temporada) {
        return campeonato.listarJogos(temporada == null ? campeonato.temporadaAtual() : temporada,
                numero, null, null);
    }

    @GetMapping("/jogos/ao-vivo")
    public List<PartidaResponse> aoVivo(@RequestParam(required = false) Integer temporada) {
        return campeonato.jogosAoVivo(temporada == null ? campeonato.temporadaAtual() : temporada);
    }

    @GetMapping("/jogos/{id}/eventos")
    public List<Jogo.Evento> eventos(@PathVariable long id) {
        List<Jogo.Evento> eventos = campeonato.buscarJogo(id).eventos();
        return eventos == null ? List.of() : eventos;
    }

    @GetMapping("/classificacao")
    public List<Competicao.Classificacao> classificacao(@RequestParam(required = false) Integer temporada) {
        return campeonato.classificacao(temporada == null ? campeonato.temporadaAtual() : temporada);
    }

    @PostMapping("/jogos/{id}/atualizacoes")
    public PartidaResponse atualizar(@PathVariable long id, @RequestBody AtualizacaoPartidaRequest request) {
        return campeonato.receberAtualizacao(id, request);
    }
}
