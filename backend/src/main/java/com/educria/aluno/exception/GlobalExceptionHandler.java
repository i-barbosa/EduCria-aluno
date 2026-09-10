package com.educria.aluno.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<Object> handleNaoEncontrado(RecursoNaoEncontradoException ex) {
        return corpoErro(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(RespostaInvalidaException.class)
    public ResponseEntity<Object> handleRespostaInvalida(RespostaInvalidaException ex) {
        return corpoErro(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(RecursoConflitanteException.class)
    public ResponseEntity<Object> handleConflito(RecursoConflitanteException ex) {
        return corpoErro(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<Object> handleCredenciaisInvalidas(CredenciaisInvalidasException ex) {
        return corpoErro(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAcessoNegado(AccessDeniedException ex) {
        return corpoErro(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidacao(MethodArgumentNotValidException ex) {
        String mensagem = ex.getBindingResult().getFieldErrors().isEmpty()
                ? "Dados invalidos"
                : ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        return corpoErro(HttpStatus.BAD_REQUEST, mensagem);
    }

    private ResponseEntity<Object> corpoErro(HttpStatus status, String mensagem) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("timestamp", LocalDateTime.now());
        corpo.put("status", status.value());
        corpo.put("erro", mensagem);
        return ResponseEntity.status(status).body(corpo);
    }
}
