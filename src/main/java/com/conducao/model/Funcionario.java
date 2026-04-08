package com.conducao.model;

import java.math.BigDecimal;

public class Funcionario {

    private Long id;
    private String nome;
    private String tipoConducao;
    private BigDecimal valor;

    public Funcionario() {
    }

    public Funcionario(String nome, String tipoConducao, BigDecimal valor) {
        this.nome = nome;
        this.tipoConducao = tipoConducao;
        this.valor = valor;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTipoConducao() {
        return tipoConducao;
    }

    public void setTipoConducao(String tipoConducao) {
        this.tipoConducao = tipoConducao;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    @Override
    public String toString() {
        return nome;
    }
}
