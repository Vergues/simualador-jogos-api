package com.vergues.simulador;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@SpringBootApplication
public class SimuladorApplication {
    public static void main(String[] args) { SpringApplication.run(SimuladorApplication.class, args); }

    @Bean
    CommandLineRunner demonstracao(RestClient.Builder builder,
            org.springframework.core.env.Environment env) {
        return args -> {
            if (!env.getProperty("simulador.demo", Boolean.class, false)) return;
            long id = env.getProperty("simulador.partida-id", Long.class, 1L);
            String base = env.getProperty("api.base-url", "http://localhost:8080");
            RestClient client = builder.baseUrl(base).build();
            Map jogo = client.get().uri("/api/v1/jogos/{id}", id).retrieve().body(Map.class);
            if (jogo == null) throw new IllegalStateException("A API não retornou a partida " + id);
            int forcaCasa = 75, forcaFora = 70;
            int golsCasa = 0, golsFora = 0;
            for (int minuto : new int[]{1, 30, 60, 90}) {
                double chance = Math.max(.12, Math.min(.88, .5 + (forcaCasa - forcaFora) * .008 + .05));
                if (minuto == 30 && ThreadLocalRandom.current().nextDouble() < chance * .24) golsCasa++;
                if (minuto == 60 && ThreadLocalRandom.current().nextDouble() < (1 - chance) * .28) golsFora++;
                Map<String,Object> evento = minuto == 30 && golsCasa > 0
                        ? Map.of("minuto", minuto, "tipo", "GOL", "descricao", "Gol simulado do mandante") : null;
                Map<String,Object> payload = new java.util.HashMap<>();
                payload.put("minuto", minuto); payload.put("golsMandante", golsCasa);
                payload.put("golsVisitante", golsFora);
                payload.put("status", minuto == 90 ? "ENCERRADO" : "AO_VIVO");
                if (evento != null) payload.put("novoEvento", evento);
                client.post().uri("/api/v1/jogos/{id}/atualizacoes", id).body(payload).retrieve().toBodilessEntity();
                System.out.printf("Atualização enviada por HTTP: partida %d, minuto %d, placar %d x %d%n",
                        id, minuto, golsCasa, golsFora);
            }
        };
    }
}
