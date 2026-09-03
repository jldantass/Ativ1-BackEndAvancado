package com.example.api1_backavancado.services;

import com.example.api1_backavancado.dto.DadosCadastroCliente;
import com.example.api1_backavancado.entities.Cliente;
import com.example.api1_backavancado.exception.EmailJaCadastradoException;
import com.example.api1_backavancado.repository.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClienteService {
    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Transactional
    public Cliente cadastrar(DadosCadastroCliente request) {

        if (clienteRepository.existsByEmail(request.email())) {
            throw new EmailJaCadastradoException(request.email());
        }

        Cliente cliente = new Cliente(request.nome(), request.email());

        return clienteRepository.save(cliente);
    }
}
