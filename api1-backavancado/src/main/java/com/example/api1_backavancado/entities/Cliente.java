package com.example.api1_backavancado.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Cliente {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @Column(unique = true)
    private String email;

    private boolean ativo;
    private LocalDateTime dataCadastro;

    protected Cliente() {}

    public Cliente(String nome, String email) {
        this.nome = nome;
        this.email = email;
        this.ativo = true;
        this.dataCadastro = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }
    public String getEmail() { return email;}
    public String getNome() { return nome; }
    public boolean getAtivo() { return ativo; }
    public LocalDateTime getDataCadastro() { return dataCadastro; }
}
