package com.example.api1_backavancado.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(EmailJaCadastradoException.class)
    public ResponseEntity<String> tratar(EmailJaCadastradoException e) {
        return ResponseEntity.badRequest().body("Email já cadastrado");
    }
}
