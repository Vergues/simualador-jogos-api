package com.vergues.simuladorjogosapi.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vergues.simuladorjogosapi.model.TimeEntity;

/*
 * =========================================================
 * REPOSITORIO JPA DE TIMES
 * =========================================================
 *
 * O JpaRepository ja entrega metodos como:
 *
 * save(...)
 * findAll()
 * findById(...)
 * delete(...)
 * count()
 *
 * sem a gente precisar escrever SQL manualmente.
 */
public interface TimeJpaRepository
        extends JpaRepository<TimeEntity, Long> {

    /*
     * O Spring Data gera automaticamente uma consulta
     * procurando o time pela sigla.
     *
     * Exemplo:
     *
     * findBySigla("PAL")
     */
    Optional<TimeEntity> findBySigla(
            String sigla
    );
}