package com.example.api1_backavancado.dto;

import com.example.api1_backavancado.entities.Cliente;

import java.time.LocalDateTime;

public record DadosListagemCliente( Long id, String nome, String email,
                                    boolean ativo, LocalDateTime dataCadastro ) {
    public static DadosListagemCliente de(Cliente cliente) {
        return new DadosListagemCliente(cliente.getId(), cliente.getNome(),
                cliente.getEmail(), cliente.getAtivo(), cliente.getDataCadastro());
    }
}
