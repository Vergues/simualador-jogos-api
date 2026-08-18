package com.vergues.simuladorjogosapi.repository;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.vergues.simuladorjogosapi.model.Time;
import com.vergues.simuladorjogosapi.model.TimeIndice;

import tools.jackson.databind.ObjectMapper;

@Repository
public class TimeRepository {

    private final List<Time> times = new ArrayList<>();
    private final ObjectMapper objectMapper;

    public TimeRepository(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;

        carregarTodosOsTimes();
    }

    private void carregarTodosOsTimes() {

        try (InputStream inputStream =
                getClass().getClassLoader()
                        .getResourceAsStream("data/times-index.json")) {

            if (inputStream == null) {
                throw new RuntimeException(
                        "Arquivo times-index.json não encontrado."
                );
            }

            TimeIndice[] indice = objectMapper.readValue(
                    inputStream,
                    TimeIndice[].class
            );

            Arrays.stream(indice)
                    .forEach(timeIndice ->
                            carregarTime(
                                    "data/" + timeIndice.getArquivo()
                            )
                    );

            System.out.println(
                    "Total de times carregados: " + times.size()
            );

        } catch (IOException e) {

            throw new RuntimeException(
                    "Erro ao carregar índice dos times.",
                    e
            );
        }
    }

    private void carregarTime(String arquivo) {

        try (InputStream inputStream =
                getClass().getClassLoader()
                        .getResourceAsStream(arquivo)) {

            if (inputStream == null) {
                throw new RuntimeException(
                        "Arquivo não encontrado: " + arquivo
                );
            }

            Time time = objectMapper.readValue(
                    inputStream,
                    Time.class
            );

            times.add(time);

            System.out.println(
                    "Time carregado: "
                    + time.getNome()
                    + " | Força: "
                    + time.getForca()
                    + " | Jogadores: "
                    + time.getJogadores().size()
            );

        } catch (IOException e) {

            throw new RuntimeException(
                    "Erro ao carregar: " + arquivo,
                    e
            );
        }
    }

    public List<Time> listarTodos() {
        return times;
    }

    public Time buscarPorSigla(String sigla) {

        return times.stream()
                .filter(time ->
                        time.getSigla()
                                .equalsIgnoreCase(sigla))
                .findFirst()
                .orElse(null);
    }
}