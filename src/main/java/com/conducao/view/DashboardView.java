package com.conducao.view;

import com.conducao.dao.DatabaseManager;
import com.conducao.dao.FuncionarioDAO;
import com.conducao.dao.RetiradaConducaoDAO;
import com.conducao.model.Funcionario;
import com.conducao.model.RetiradaConducao;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class DashboardView {

    private final FuncionarioDAO funcionarioDAO;
    private final RetiradaConducaoDAO retiradaDAO;
    private VBox boxAlertas;
    private VBox boxSituacao;

    public DashboardView() throws SQLException {
        this.funcionarioDAO = new FuncionarioDAO(DatabaseManager.getConnection());
        this.retiradaDAO = new RetiradaConducaoDAO(DatabaseManager.getConnection());
        retiradaDAO.criarTabela();
    }

    public Scene criarScene() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #f5f5f5;");

        Label titulo = new Label("📊 Dashboard - Visão Geral");
        titulo.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #333;");

        // Cards de resumo
        HBox boxCards = new HBox(15);
        boxCards.setPadding(new Insets(10));

        VBox cardFuncionarios = criarCard("Total de Funcionários", String.valueOf(contarFuncionarios()), "#2196F3");
        VBox cardVencidos = criarCard("Condução Vencida", String.valueOf(contarVencidos()), "#f44336");
        VBox cardVencendoHoje = criarCard("Vencem Hoje", String.valueOf(contarVencendoHoje()), "#FF9800");

        boxCards.getChildren().addAll(cardFuncionarios, cardVencidos, cardVencendoHoje);

        // Seção de alertas
        Label lblAlertas = new Label("🔔 Alertas - Condução Vencida ou Vencendo");
        lblAlertas.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #c62828;");

        boxAlertas = new VBox(8);
        boxAlertas.setPadding(new Insets(10));
        boxAlertas.setStyle("-fx-background-color: white; -fx-background-radius: 5;");

        // Seção de situação geral
        Label lblSituacao = new Label("📋 Situação de Todos os Funcionários");
        lblSituacao.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");

        boxSituacao = new VBox(6);
        boxSituacao.setPadding(new Insets(10));
        boxSituacao.setStyle("-fx-background-color: white; -fx-background-radius: 5;");

        atualizarDashboard();

        Button btnAtualizar = new Button("🔄 Atualizar Dashboard");
        btnAtualizar.setStyle("-fx-background-color: #607D8B; -fx-text-fill: white; -fx-padding: 10 20;");
        btnAtualizar.setMaxWidth(220);
        btnAtualizar.setOnAction(e -> atualizarDashboard());

        root.getChildren().addAll(titulo, boxCards, lblAlertas, boxAlertas, lblSituacao, boxSituacao, btnAtualizar);

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #f5f5f5;");

        return new Scene(scroll, 750, 600);
    }

    private VBox criarCard(String titulo, String valor, String cor) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(15));
        card.setPrefWidth(180);
        card.setStyle(String.format(
            "-fx-background-color: %s; -fx-background-radius: 8;", cor
        ));

        Label lblTitulo = new Label(titulo);
        lblTitulo.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.9);");

        Label lblValor = new Label(valor);
        lblValor.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");

        card.getChildren().addAll(lblTitulo, lblValor);
        return card;
    }

    private int contarFuncionarios() {
        try {
            return funcionarioDAO.listarTodos().size();
        } catch (SQLException e) {
            return 0;
        }
    }

    private int contarVencidos() {
        try {
            List<Funcionario> funcionarios = funcionarioDAO.listarTodos();
            int count = 0;
            for (Funcionario f : funcionarios) {
                RetiradaConducao ultima = retiradaDAO.getUltimaRetirada(f.getId());
                if (ultima != null && ultima.isVencido()) count++;
            }
            return count;
        } catch (SQLException e) {
            return 0;
        }
    }

    private int contarVencendoHoje() {
        try {
            List<Funcionario> funcionarios = funcionarioDAO.listarTodos();
            int count = 0;
            for (Funcionario f : funcionarios) {
                RetiradaConducao ultima = retiradaDAO.getUltimaRetirada(f.getId());
                if (ultima != null && ultima.isVenceHoje()) count++;
            }
            return count;
        } catch (SQLException e) {
            return 0;
        }
    }

    private void atualizarDashboard() {
        boxAlertas.getChildren().clear();
        boxSituacao.getChildren().clear();

        try {
            List<Funcionario> funcionarios = funcionarioDAO.listarTodos();
            boolean temAlertas = false;

            for (Funcionario f : funcionarios) {
                RetiradaConducao ultima = retiradaDAO.getUltimaRetirada(f.getId());

                // Alerta se vencida ou vence hoje
                if (ultima == null) {
                    temAlertas = true;
                    adicionarItemAlerta(
                        "❓ " + f.getNome() + " — Nunca pegou condução!",
                        "#FFF9C4"
                    );
                } else if (ultima.isVenceHoje()) {
                    temAlertas = true;
                    adicionarItemAlerta(
                        "⚠️ " + f.getNome() + " — Condução VENCE HOJE! Precisa pegar mais.",
                        "#FFCCBC"
                    );
                } else if (ultima.isVencido()) {
                    temAlertas = true;
                    LocalDate vencimento = ultima.getDataVencimento();
                    adicionarItemAlerta(
                        String.format("🔴 %s — Condução vencida desde %02d/%02d! Precisa pegar mais.",
                            f.getNome(), vencimento.getDayOfMonth(), vencimento.getMonthValue()),
                        "#FFEBEE"
                    );
                } else if (ultima.isProximoVencimento()) {
                    temAlertas = true;
                    LocalDate vencimento = ultima.getDataVencimento();
                    adicionarItemAlerta(
                        String.format("🟡 %s — Condução vence em %02d/%02d. Prepare para renovar!",
                            f.getNome(), vencimento.getDayOfMonth(), vencimento.getMonthValue()),
                        "#FFF3E0"
                    );
                }

                // Situação geral de todos
                String situacao;
                String corFundo;
                if (ultima == null) {
                    situacao = "Sem registro de condução";
                    corFundo = "#FFF9C4";
                } else if (ultima.isVenceHoje()) {
                    LocalDate prox = ultima.getDataVencimento();
                    situacao = String.format("Vence HOJE — Pegar mais amanhã (%02d/%02d)",
                        prox.getDayOfMonth(), prox.getMonthValue());
                    corFundo = "#FFCCBC";
                } else if (ultima.isVencido()) {
                    situacao = "VENCIDA — Precisa pegar condução!";
                    corFundo = "#FFEBEE";
                } else {
                    LocalDate venc = ultima.getDataVencimento();
                    long diasRestantes = ChronoUnit.DAYS.between(LocalDate.now(), venc);
                    situacao = String.format("OK até %02d/%02d (%d dia(s) restante(s))",
                        venc.getDayOfMonth(), venc.getMonthValue(), diasRestantes);
                    corFundo = "#E8F5E9";
                }

                HBox itemSituacao = new HBox(10);
                itemSituacao.setPadding(new Insets(8, 12, 8, 12));
                itemSituacao.setStyle(String.format(
                    "-fx-background-color: %s; -fx-background-radius: 4;", corFundo
                ));

                Label lblNome = new Label(f.getNome());
                lblNome.setStyle("-fx-font-weight: bold; -fx-min-width: 200;");
                Label lblSit = new Label(situacao);
                lblSit.setStyle("-fx-font-size: 13px;");

                itemSituacao.getChildren().addAll(lblNome, lblSit);
                boxSituacao.getChildren().add(itemSituacao);
            }

            if (!temAlertas) {
                Label lblOk = new Label("✅ Nenhum alerta pendente — Todos em dia!");
                lblOk.setStyle("-fx-font-size: 14px; -fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                lblOk.setPadding(new Insets(10));
                boxAlertas.getChildren().add(lblOk);
            }

            if (funcionarios.isEmpty()) {
                Label lblVazio = new Label("Nenhum funcionário cadastrado.");
                lblVazio.setStyle("-fx-text-fill: #999;");
                boxSituacao.getChildren().add(lblVazio);
            }

        } catch (SQLException e) {
            Label lblErro = new Label("Erro ao carregar: " + e.getMessage());
            lblErro.setStyle("-fx-text-fill: red;");
            boxAlertas.getChildren().add(lblErro);
        }
    }

    private void adicionarItemAlerta(String mensagem, String corFundo) {
        HBox item = new HBox(10);
        item.setPadding(new Insets(10, 12, 10, 12));
        item.setStyle(String.format(
            "-fx-background-color: %s; -fx-background-radius: 5;", corFundo
        ));
        Label lbl = new Label(mensagem);
        lbl.setStyle("-fx-font-size: 13px;");
        item.getChildren().add(lbl);
        boxAlertas.getChildren().add(item);
    }
}
