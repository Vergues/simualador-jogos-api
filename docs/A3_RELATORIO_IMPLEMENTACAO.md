# A3 — Implementação da primeira versão

**Projeto:** API de Resultados de Jogos de Futebol

**Equipe:** Guilherme Gabriel Krull dos Santos, Lucas Ferraz Rezeck, Luiz Rogério Ribeiro de Castro Neto e Matheus Vergueiro Silva.

## Implementação

- API REST em Java 21 e Spring Boot, com Spring Data JPA e H2 em arquivo.
- Oito rotas sob `/api/v1`: times, jogos, detalhe, rodada, partidas ao vivo, eventos, classificação e atualização de partida.
- Calendário de 20 clubes, 38 rodadas, 380 partidas, em turno e returno.
- Classificação calculada com base nas partidas encerradas.
- Em uma base nova, o histórico anterior à rodada atual é encerrado e congelado. A tabela inicial preparada coloca o Palmeiras dez pontos à frente do Flamengo e o Corinthians dez pontos atrás do Vasco; o Palmeiras tem derrota registrada.
- As partidas futuras são distribuídas pelos sete dias, em horários entre 18h30 e 22h30. A API atualiza os jogos enquanto estiver em execução. Partidas cujo horário passou durante o desligamento são concluídas na inicialização; resultados já encerrados são preservados.

## Banco de dados

O H2 é criado localmente em `api/data/futebol-portal.mv.db` quando a aplicação inicia. O arquivo de banco não é versionado no GitHub. O esquema JPA usa `update` para manter a estrutura.

## Demonstração e validação

A API foi iniciada localmente na porta 8080, conectada ao H2. O Swagger UI abriu em `http://localhost:8080/swagger-ui.html`, e as consultas de classificação, times, jogos e rodada foram verificadas. A classificação observada foi: Palmeiras 50 pontos, Flamengo 40, Vasco 26 e Corinthians 16.

Os testes da API passaram: 11 executados, 0 falhas, 0 erros. O módulo de demonstração compilou com `mvnw.cmd test`; não possui testes automatizados próprios. A demonstração por atualização HTTP do módulo `simulador/` ainda não foi executada nesta validação.
