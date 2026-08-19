package com.vergues.simuladorjogosapi.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vergues.simuladorjogosapi.dto.JogoCalendarioResponse;
import com.vergues.simuladorjogosapi.dto.PartidaResponse;
import com.vergues.simuladorjogosapi.model.Jogo;
import com.vergues.simuladorjogosapi.service.BrasileiraoService;

@RestController
@RequestMapping("/api/brasileirao")
public class BrasileiraoController {

    private final BrasileiraoService brasileiraoService;

    public BrasileiraoController(
            BrasileiraoService brasileiraoService) {

        this.brasileiraoService =
                brasileiraoService;
    }

    @PostMapping("/gerar-calendario")
    public String gerarCalendario(
            @RequestParam int ano) {

        brasileiraoService
                .gerarCalendario(ano);

        return "Calendário do Brasileirão "
                + ano
                + " gerado com sucesso!";
    }

    @GetMapping("/jogos")
    public List<JogoCalendarioResponse> listarJogos() {

        return converter(
                brasileiraoService.listarTodos()
        );
    }

    @GetMapping("/rodada/{rodada}")
    public List<JogoCalendarioResponse> buscarPorRodada(
            @PathVariable int rodada) {

        return converter(
                brasileiraoService
                        .buscarPorRodada(rodada)
        );
    }

    @GetMapping("/data/{data}")
    public List<JogoCalendarioResponse> buscarPorData(
            @PathVariable LocalDate data) {

        return converter(
                brasileiraoService
                        .buscarPorData(data)
        );
    }

    @GetMapping("/hoje")
    public List<JogoCalendarioResponse> jogosDeHoje() {

        return converter(
                brasileiraoService
                        .buscarJogosDeHoje()
        );
    }

    @GetMapping("/proximos")
    public List<JogoCalendarioResponse> proximosJogos(
            @RequestParam(
                    defaultValue = "10"
            ) int limite) {

        return converter(
                brasileiraoService
                        .buscarProximosJogos(
                                limite
                        )
        );
    }

    @GetMapping("/jogo/{id}")
    public PartidaResponse buscarJogo(
            @PathVariable Long id) {

        Jogo jogo =
                brasileiraoService
                        .buscarPorId(id);

        if (jogo == null) {

            throw new RuntimeException(
                    "Jogo não encontrado: "
                            + id
            );
        }

        return new PartidaResponse(
                jogo
        );
    }

    private List<JogoCalendarioResponse> converter(
            List<Jogo> jogos) {

        return jogos.stream()
                .map(
                        JogoCalendarioResponse::new
                )
                .toList();
    }
}