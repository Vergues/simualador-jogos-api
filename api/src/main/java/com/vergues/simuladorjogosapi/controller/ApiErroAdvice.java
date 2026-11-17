package com.vergues.simuladorjogosapi.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.Map;

@RestControllerAdvice(basePackages = "com.vergues.simuladorjogosapi.controller")
public class ApiErroAdvice {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> respostaStatus(ResponseStatusException ex, HttpServletRequest req) {
        int codigo = ex.getStatusCode().value();
        return ResponseEntity.status(ex.getStatusCode()).body(body(codigo,
                ex.getReason() == null ? "Requisição não atendida." : ex.getReason(), req));
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class,
            org.springframework.web.bind.MissingServletRequestParameterException.class,
            org.springframework.http.converter.HttpMessageNotReadableException.class})
    public ResponseEntity<Map<String, Object>> requisicaoInvalida(Exception ex, HttpServletRequest req) {
        return ResponseEntity.badRequest().body(body(400, "Parâmetro ou corpo JSON inválido.", req));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> inesperado(Exception ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(body(500, "Erro interno do servidor.", req));
    }

    private Map<String, Object> body(int status, String mensagem, HttpServletRequest req) {
        return Map.of("status", status, "mensagem", mensagem, "caminho", req.getRequestURI(),
                "timestamp", OffsetDateTime.now().toString());
    }
}
