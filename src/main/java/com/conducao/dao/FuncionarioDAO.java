package com.conducao.dao;

import com.conducao.model.Funcionario;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FuncionarioDAO {

    private final Connection connection;

    public FuncionarioDAO(Connection connection) {
        this.connection = connection;
    }

    public void criarTabela() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS funcionarios (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nome TEXT NOT NULL,
                tipo_conducao TEXT NOT NULL,
                valor REAL NOT NULL
            )
        """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    public void salvar(Funcionario funcionario) throws SQLException {
        if (funcionario.getId() == null) {
            inserir(funcionario);
        } else {
            atualizar(funcionario);
        }
    }

    private void inserir(Funcionario funcionario) throws SQLException {
        String sql = "INSERT INTO funcionarios (nome, tipo_conducao, valor) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, funcionario.getNome());
            stmt.setString(2, funcionario.getTipoConducao());
            stmt.setBigDecimal(3, funcionario.getValor());
            stmt.executeUpdate();
        }
    }

    private void atualizar(Funcionario funcionario) throws SQLException {
        String sql = "UPDATE funcionarios SET nome = ?, tipo_conducao = ?, valor = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, funcionario.getNome());
            stmt.setString(2, funcionario.getTipoConducao());
            stmt.setBigDecimal(3, funcionario.getValor());
            stmt.setLong(4, funcionario.getId());
            stmt.executeUpdate();
        }
    }

    public void excluir(Long id) throws SQLException {
        String sql = "DELETE FROM funcionarios WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }

    public Funcionario buscarPorId(Long id) throws SQLException {
        String sql = "SELECT * FROM funcionarios WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
            }
        }
        return null;
    }

    public List<Funcionario> listarTodos() throws SQLException {
        List<Funcionario> funcionarios = new ArrayList<>();
        String sql = "SELECT * FROM funcionarios ORDER BY nome";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                funcionarios.add(mapearResultSet(rs));
            }
        }
        return funcionarios;
    }

    private Funcionario mapearResultSet(ResultSet rs) throws SQLException {
        Funcionario funcionario = new Funcionario();
        funcionario.setId(rs.getLong("id"));
        funcionario.setNome(rs.getString("nome"));
        funcionario.setTipoConducao(rs.getString("tipo_conducao"));
        funcionario.setValor(rs.getBigDecimal("valor"));
        return funcionario;
    }
}
