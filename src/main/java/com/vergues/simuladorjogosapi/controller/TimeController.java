package com.vergues.simuladorjogosapi.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vergues.simuladorjogosapi.model.Time;
import com.vergues.simuladorjogosapi.repository.TimeRepository;

@RestController
@RequestMapping("/api/times")
public class TimeController {

    private final TimeRepository timeRepository;

    public TimeController(TimeRepository timeRepository) {
        this.timeRepository = timeRepository;
    }

    @GetMapping
    public List<Time> listarTimes() {
        return timeRepository.listarTodos();
    }
}