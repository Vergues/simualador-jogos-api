package com.vergues.simuladorjogosapi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PaginaController {
    @GetMapping({"/jogos", "/competicoes", "/competicoes/{id}", "/classificacao", "/artilharia",
            "/times", "/times/{slug}", "/calendario", "/partidas/{id}", "/partida.html"})
    public String pagina() { return "forward:/index.html"; }
}
