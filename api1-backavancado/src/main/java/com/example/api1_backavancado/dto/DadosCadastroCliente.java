package com.example.api1_backavancado.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record DadosCadastroCliente(
        @NotBlank(message = "Nome obrigatório")
        String nome,

        @NotBlank(message = "Email obrigatório")
        @Email(message = "Email inválido")
        String email
) {}
