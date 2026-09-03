package com.example.api1_backavancado.controller;

import com.example.api1_backavancado.dto.DadosCadastroCliente;
import com.example.api1_backavancado.dto.DadosListagemCliente;
import com.example.api1_backavancado.entities.Cliente;
import com.example.api1_backavancado.services.ClienteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService cadastrarClienteService;

    public ClienteController(ClienteService cadastrarClienteService) {
        this.cadastrarClienteService = cadastrarClienteService;
    }

    @PostMapping
    public ResponseEntity<DadosListagemCliente> cadastrar(
            @RequestBody @Valid DadosCadastroCliente request) {

        Cliente salvo = cadastrarClienteService.cadastrar(request);
        return ResponseEntity.ok(DadosListagemCliente.de(salvo));
    }
}
