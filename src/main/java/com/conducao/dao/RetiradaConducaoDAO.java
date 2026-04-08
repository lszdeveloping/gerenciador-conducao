package com.conducao.dao;

import com.conducao.model.RetiradaConducao;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RetiradaConducaoDAO {

    private final Connection connection;

    public RetiradaConducaoDAO(Connection connection) {
        this.connection = connection;
    }

    public void criarTabela() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS retiradas_conducao (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                funcionario_id INTEGER NOT NULL,
                funcionario_nome TEXT NOT NULL,
                data_retirada TEXT NOT NULL,
                quantidade_dias INTEGER NOT NULL,
                valor_total REAL NOT NULL,
                valor_por_dia REAL NOT NULL,
                utilizado INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY (funcionario_id) REFERENCES funcionarios(id)
            )
        """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    public void salvar(RetiradaConducao retirada) throws SQLException {
        if (retirada.getId() == null) {
            inserir(retirada);
        } else {
            atualizar(retirada);
        }
    }

    private void inserir(RetiradaConducao retirada) throws SQLException {
        String sql = """
            INSERT INTO retiradas_conducao
            (funcionario_id, funcionario_nome, data_retirada, quantidade_dias, valor_total, valor_por_dia, utilizado)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, retirada.getFuncionarioId());
            stmt.setString(2, retirada.getFuncionarioNome());
            stmt.setString(3, retirada.getDataRetirada().toString());
            stmt.setInt(4, retirada.getQuantidadeDias());
            stmt.setBigDecimal(5, retirada.getValorTotal());
            stmt.setBigDecimal(6, retirada.getValorPorDia());
            stmt.setInt(7, retirada.isUtilizado() ? 1 : 0);
            stmt.executeUpdate();
        }
    }

    private void atualizar(RetiradaConducao retirada) throws SQLException {
        String sql = """
            UPDATE retiradas_conducao
            SET utilizado = ?
            WHERE id = ?
        """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, retirada.isUtilizado() ? 1 : 0);
            stmt.setLong(2, retirada.getId());
            stmt.executeUpdate();
        }
    }

    public void excluir(Long id) throws SQLException {
        String sql = "DELETE FROM retiradas_conducao WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }

    public List<RetiradaConducao> listarPorFuncionario(Long funcionarioId) throws SQLException {
        List<RetiradaConducao> retiradas = new ArrayList<>();
        String sql = """
            SELECT * FROM retiradas_conducao
            WHERE funcionario_id = ?
            ORDER BY data_retirada DESC
        """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, funcionarioId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    retiradas.add(mapearResultSet(rs));
                }
            }
        }
        return retiradas;
    }

    public List<RetiradaConducao> listarTodas() throws SQLException {
        List<RetiradaConducao> retiradas = new ArrayList<>();
        String sql = """
            SELECT * FROM retiradas_conducao
            ORDER BY data_retirada DESC, funcionario_nome
        """;
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                retiradas.add(mapearResultSet(rs));
            }
        }
        return retiradas;
    }

    public List<RetiradaConducao> listarNaoUtilizadas() throws SQLException {
        List<RetiradaConducao> retiradas = new ArrayList<>();
        String sql = """
            SELECT * FROM retiradas_conducao
            WHERE utilizado = 0
            ORDER BY data_retirada ASC
        """;
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                retiradas.add(mapearResultSet(rs));
            }
        }
        return retiradas;
    }

    /**
     * Calcula o saldo de dias de condução para um funcionário
     * Soma dos dias das retiradas não utilizadas que ainda não venceram
     */
    public int calcularSaldoDias(Long funcionarioId) throws SQLException {
        String sql = """
            SELECT COALESCE(SUM(quantidade_dias), 0) as saldo
            FROM retiradas_conducao
            WHERE funcionario_id = ? AND utilizado = 0
        """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, funcionarioId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("saldo");
                }
            }
        }
        return 0;
    }

    /**
     * Retorna a data de vencimento mais próxima para um funcionário
     */
    public LocalDate getProximoVencimento(Long funcionarioId) throws SQLException {
        String sql = """
            SELECT data_retirada, quantidade_dias
            FROM retiradas_conducao
            WHERE funcionario_id = ? AND utilizado = 0
            ORDER BY data_retirada ASC
            LIMIT 1
        """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, funcionarioId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    LocalDate dataRetirada = LocalDate.parse(rs.getString("data_retirada"));
                    int qtdDias = rs.getInt("quantidade_dias");
                    return dataRetirada.plusDays(qtdDias);
                }
            }
        }
        return null;
    }

    /**
     * Retorna a retirada mais recente de um funcionário
     */
    public RetiradaConducao getUltimaRetirada(Long funcionarioId) throws SQLException {
        String sql = """
            SELECT * FROM retiradas_conducao
            WHERE funcionario_id = ?
            ORDER BY data_retirada DESC
            LIMIT 1
        """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, funcionarioId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
            }
        }
        return null;
    }

    public List<RetiradaConducao> listarPorPeriodo(LocalDate dataInicio, LocalDate dataFim) throws SQLException {
        List<RetiradaConducao> retiradas = new ArrayList<>();
        String sql = """
            SELECT * FROM retiradas_conducao
            WHERE data_retirada >= ? AND data_retirada <= ?
            ORDER BY data_retirada ASC, funcionario_nome
        """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, dataInicio.toString());
            stmt.setString(2, dataFim.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    retiradas.add(mapearResultSet(rs));
                }
            }
        }
        return retiradas;
    }

    private RetiradaConducao mapearResultSet(ResultSet rs) throws SQLException {
        RetiradaConducao retirada = new RetiradaConducao();
        retirada.setId(rs.getLong("id"));
        retirada.setFuncionarioId(rs.getLong("funcionario_id"));
        retirada.setFuncionarioNome(rs.getString("funcionario_nome"));
        retirada.setDataRetirada(LocalDate.parse(rs.getString("data_retirada")));
        retirada.setQuantidadeDias(rs.getInt("quantidade_dias"));
        retirada.setValorTotal(rs.getBigDecimal("valor_total"));
        retirada.setValorPorDia(rs.getBigDecimal("valor_por_dia"));
        retirada.setUtilizado(rs.getInt("utilizado") == 1);
        return retirada;
    }
}
