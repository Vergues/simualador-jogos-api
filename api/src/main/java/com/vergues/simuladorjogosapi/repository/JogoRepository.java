package com.vergues.simuladorjogosapi.repository;

import com.vergues.simuladorjogosapi.model.Jogo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JogoRepository extends JpaRepository<Jogo, Long> {
    List<Jogo> findByTemporadaOrderByDataHoraAsc(int temporada);
}
