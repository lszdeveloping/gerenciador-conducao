package com.conducao.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Representa uma retirada de condução (adiantamento de dias)
 */
public class RetiradaConducao {

    private Long id;
    private Long funcionarioId;
    private String funcionarioNome;
    private LocalDate dataRetirada;
    private int quantidadeDias;
    private BigDecimal valorTotal;
    private BigDecimal valorPorDia;
    private boolean utilizado;

    public RetiradaConducao() {
    }

    public RetiradaConducao(Long funcionarioId, String funcionarioNome, LocalDate dataRetirada,
                            int quantidadeDias, BigDecimal valorTotal, BigDecimal valorPorDia) {
        this.funcionarioId = funcionarioId;
        this.funcionarioNome = funcionarioNome;
        this.dataRetirada = dataRetirada;
        this.quantidadeDias = quantidadeDias;
        this.valorTotal = valorTotal;
        this.valorPorDia = valorPorDia;
        this.utilizado = false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFuncionarioId() {
        return funcionarioId;
    }

    public void setFuncionarioId(Long funcionarioId) {
        this.funcionarioId = funcionarioId;
    }

    public String getFuncionarioNome() {
        return funcionarioNome;
    }

    public void setFuncionarioNome(String funcionarioNome) {
        this.funcionarioNome = funcionarioNome;
    }

    public LocalDate getDataRetirada() {
        return dataRetirada;
    }

    public void setDataRetirada(LocalDate dataRetirada) {
        this.dataRetirada = dataRetirada;
    }

    public int getQuantidadeDias() {
        return quantidadeDias;
    }

    public void setQuantidadeDias(int quantidadeDias) {
        this.quantidadeDias = quantidadeDias;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public BigDecimal getValorPorDia() {
        return valorPorDia;
    }

    public void setValorPorDia(BigDecimal valorPorDia) {
        this.valorPorDia = valorPorDia;
    }

    public boolean isUtilizado() {
        return utilizado;
    }

    public void setUtilizado(boolean utilizado) {
        this.utilizado = utilizado;
    }

    /**
     * Calcula a data de vencimento dos dias (data da retirada + quantidade de dias)
     */
    public LocalDate getDataVencimento() {
        return dataRetirada.plusDays(quantidadeDias);
    }

    /**
     * Verifica se os dias já venceram (hoje >= data de vencimento)
     */
    public boolean isVencido() {
        return !LocalDate.now().isBefore(getDataVencimento());
    }

    /**
     * Verifica se vence hoje
     */
    public boolean isVenceHoje() {
        return LocalDate.now().isEqual(getDataVencimento());
    }

    /**
     * Verifica se está próximo do vencimento (1 ou 2 dias antes)
     */
    public boolean isProximoVencimento() {
        LocalDate vencimento = getDataVencimento();
        LocalDate hoje = LocalDate.now();
        return !isVencido() && (hoje.isEqual(vencimento.minusDays(2)) || hoje.isEqual(vencimento.minusDays(1)));
    }
}
