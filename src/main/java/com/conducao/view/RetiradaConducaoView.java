package com.conducao.view;

import com.conducao.dao.DatabaseManager;
import com.conducao.dao.FuncionarioDAO;
import com.conducao.dao.RetiradaConducaoDAO;
import com.conducao.model.Funcionario;
import com.conducao.model.RetiradaConducao;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class RetiradaConducaoView {

    private final RetiradaConducaoDAO retiradaDAO;
    private final FuncionarioDAO funcionarioDAO;
    private final ObservableList<RetiradaConducao> listaRetiradas = FXCollections.observableArrayList();

    private ComboBox<Funcionario> cbFuncionario;
    private Spinner<Integer> spinnerDias;
    private DatePicker dpDataRetirada;
    private Label lblValorPorDia;
    private Label lblValorTotal;
    private Label lblProximaRetirada;
    private TableView<RetiradaConducao> tabela;
    private VBox painelResumo;
    private Label lblResumoTitulo;

    public RetiradaConducaoView() throws SQLException {
        this.retiradaDAO = new RetiradaConducaoDAO(DatabaseManager.getConnection());
        this.funcionarioDAO = new FuncionarioDAO(DatabaseManager.getConnection());
        retiradaDAO.criarTabela();
    }

    public Scene criarScene() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        Label titulo = new Label("💰 Pagamento de Condução");
        titulo.getStyleClass().add("titulo-principal");

        VBox painelForm = new VBox(15);
        painelForm.getStyleClass().add("painel-fundo");

        GridPane form = new GridPane();
        form.setHgap(20);
        form.setVgap(12);

        // Coluna esquerda
        Label lblFunc = new Label("Funcionário:");
        lblFunc.getStyleClass().add("label-destaque");

        cbFuncionario = new ComboBox<>();
        try {
            cbFuncionario.getItems().addAll(funcionarioDAO.listarTodos());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        cbFuncionario.setPromptText("Selecione o funcionário");
        cbFuncionario.setPrefWidth(300);
        cbFuncionario.setOnAction(e -> {
            tabela.getSelectionModel().clearSelection();
            atualizarValores();
        });

        Label lblDias = new Label("Quantidade de Dias:");
        lblDias.getStyleClass().add("label-destaque");

        spinnerDias = new Spinner<>(1, 31, 3);
        spinnerDias.setPrefWidth(300);
        spinnerDias.setEditable(true);
        spinnerDias.valueProperty().addListener((obs, old, novo) -> atualizarValores());

        Label lblData = new Label("Data da Retirada:");
        lblData.getStyleClass().add("label-destaque");

        dpDataRetirada = new DatePicker(LocalDate.now());
        dpDataRetirada.setPrefWidth(300);
        dpDataRetirada.setOnAction(e -> atualizarValores());

        form.add(lblFunc, 0, 0);
        form.add(cbFuncionario, 0, 1);
        form.add(lblDias, 0, 2);
        form.add(spinnerDias, 0, 3);
        form.add(lblData, 0, 4);
        form.add(dpDataRetirada, 0, 5);

        // Coluna direita - resumo calculado
        painelResumo = new VBox(10);
        painelResumo.setStyle("-fx-background-color: #E3F2FD; -fx-background-radius: 8; -fx-padding: 15;");
        painelResumo.setPrefWidth(280);

        lblResumoTitulo = new Label("Resumo");
        lblResumoTitulo.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1565C0;");

        Label lblVPDLabel = new Label("Valor por Dia (ida + volta):");
        lblVPDLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");
        lblValorPorDia = new Label("R$ 0,00");
        lblValorPorDia.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");

        Label lblVTLabel = new Label("Valor Total:");
        lblVTLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");
        lblValorTotal = new Label("R$ 0,00");
        lblValorTotal.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1565C0;");

        Label lblPRLabel = new Label("Próxima Retirada:");
        lblPRLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");
        lblProximaRetirada = new Label("--/--/----");
        lblProximaRetirada.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #E65100;");

        painelResumo.getChildren().addAll(
            lblResumoTitulo,
            lblVPDLabel, lblValorPorDia,
            new Separator(),
            lblVTLabel, lblValorTotal,
            new Separator(),
            lblPRLabel, lblProximaRetirada
        );

        form.add(painelResumo, 1, 0, 1, 6);

        Button btnRegistrar = new Button("💰 Registrar Retirada");
        btnRegistrar.getStyleClass().add("btn-primario");
        btnRegistrar.setPrefHeight(45);
        btnRegistrar.setMaxWidth(Double.MAX_VALUE);
        btnRegistrar.setOnAction(e -> registrarRetirada());

        Button btnExcluir = new Button("❌ Excluir Registro");
        btnExcluir.getStyleClass().add("btn-perigo");
        btnExcluir.setPrefHeight(40);
        btnExcluir.setMaxWidth(Double.MAX_VALUE);
        btnExcluir.setOnAction(e -> excluirRetirada());

        Button btnAtualizar = new Button("🔄 Atualizar");
        btnAtualizar.getStyleClass().add("btn-secundario");
        btnAtualizar.setPrefHeight(40);
        btnAtualizar.setMaxWidth(Double.MAX_VALUE);
        btnAtualizar.setOnAction(e -> {
            carregarRetiradas();
            try {
                cbFuncionario.getItems().setAll(funcionarioDAO.listarTodos());
            } catch (SQLException ex) {
                mostrarErro("Erro ao atualizar funcionários: " + ex.getMessage());
            }
        });

        HBox botoes = new HBox(10);
        botoes.getChildren().addAll(btnRegistrar, btnExcluir, btnAtualizar);
        HBox.setHgrow(btnRegistrar, Priority.ALWAYS);
        HBox.setHgrow(btnExcluir, Priority.ALWAYS);
        HBox.setHgrow(btnAtualizar, Priority.ALWAYS);

        painelForm.getChildren().addAll(form, botoes);

        // Tabela histórico
        Label lblTabela = new Label("📋 Histórico de Retiradas");
        lblTabela.getStyleClass().add("titulo-secao");
        lblTabela.setPadding(new Insets(10, 0, 5, 0));

        tabela = new TableView<>();
        tabela.setPlaceholder(new Label("Nenhuma retirada registrada"));
        tabela.setPrefHeight(280);

        TableColumn<RetiradaConducao, Long> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(50);

        TableColumn<RetiradaConducao, String> colFuncionario = new TableColumn<>("Funcionário");
        colFuncionario.setCellValueFactory(new PropertyValueFactory<>("funcionarioNome"));
        colFuncionario.setPrefWidth(180);

        TableColumn<RetiradaConducao, LocalDate> colData = new TableColumn<>("Data Retirada");
        colData.setCellValueFactory(new PropertyValueFactory<>("dataRetirada"));
        colData.setPrefWidth(110);

        TableColumn<RetiradaConducao, Integer> colDias = new TableColumn<>("Dias");
        colDias.setCellValueFactory(new PropertyValueFactory<>("quantidadeDias"));
        colDias.setPrefWidth(55);

        TableColumn<RetiradaConducao, BigDecimal> colVPD = new TableColumn<>("Vlr/Dia");
        colVPD.setCellValueFactory(new PropertyValueFactory<>("valorPorDia"));
        colVPD.setPrefWidth(80);

        TableColumn<RetiradaConducao, BigDecimal> colTotal = new TableColumn<>("Total (R$)");
        colTotal.setCellValueFactory(new PropertyValueFactory<>("valorTotal"));
        colTotal.setPrefWidth(90);

        TableColumn<RetiradaConducao, String> colVencimento = new TableColumn<>("Próxima Retirada");
        colVencimento.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getDataVencimento().toString())
        );
        colVencimento.setPrefWidth(120);

        TableColumn<RetiradaConducao, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(cellData -> {
            RetiradaConducao r = cellData.getValue();
            if (r.isVenceHoje()) return new SimpleStringProperty("⚠️ Vence Hoje");
            if (r.isVencido()) return new SimpleStringProperty("🔴 Vencida");
            if (r.isProximoVencimento()) return new SimpleStringProperty("🟡 Vence em Breve");
            return new SimpleStringProperty("🟢 OK");
        });
        colStatus.setPrefWidth(130);

        tabela.getColumns().addAll(colId, colFuncionario, colData, colDias, colVPD, colTotal, colVencimento, colStatus);
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        carregarRetiradas();

        tabela.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novo) -> {
            if (novo != null) {
                mostrarDetalhesRetirada(novo);
            } else {
                resetarResumo();
            }
        });

        root.getChildren().addAll(titulo, painelForm, lblTabela, tabela);

        return new Scene(root, 900, 700);
    }

    private void atualizarValores() {
        Funcionario f = cbFuncionario.getValue();
        int dias = spinnerDias.getValue();
        LocalDate dataRetirada = dpDataRetirada.getValue();

        if (f != null && f.getValor() != null) {
            BigDecimal valorPorDia = f.getValor().multiply(BigDecimal.valueOf(2));
            BigDecimal valorTotal = valorPorDia.multiply(BigDecimal.valueOf(dias));
            lblValorPorDia.setText(String.format("R$ %.2f", valorPorDia));
            lblValorTotal.setText(String.format("R$ %.2f", valorTotal));

            if (dataRetirada != null) {
                LocalDate proximaRetirada = dataRetirada.plusDays(dias);
                lblProximaRetirada.setText(String.format("%02d/%02d/%d",
                    proximaRetirada.getDayOfMonth(),
                    proximaRetirada.getMonthValue(),
                    proximaRetirada.getYear()
                ));
            }
        } else {
            lblValorPorDia.setText("R$ 0,00");
            lblValorTotal.setText("R$ 0,00");
            lblProximaRetirada.setText("--/--/----");
        }
    }

    private void registrarRetirada() {
        Funcionario funcionario = cbFuncionario.getValue();
        int dias = spinnerDias.getValue();
        LocalDate dataRetirada = dpDataRetirada.getValue();

        if (funcionario == null) {
            mostrarAlerta("Selecione o funcionário!");
            return;
        }
        if (dataRetirada == null) {
            mostrarAlerta("Selecione a data da retirada!");
            return;
        }

        BigDecimal valorPorDia = funcionario.getValor().multiply(BigDecimal.valueOf(2));
        BigDecimal valorTotal = valorPorDia.multiply(BigDecimal.valueOf(dias));
        LocalDate proximaRetirada = dataRetirada.plusDays(dias);

        RetiradaConducao retirada = new RetiradaConducao(
            funcionario.getId(),
            funcionario.getNome(),
            dataRetirada,
            dias,
            valorTotal,
            valorPorDia
        );

        try {
            retiradaDAO.salvar(retirada);
            carregarRetiradas();
            mostrarAlerta(String.format(
                "%s pegou condução por %d dias.\n\nValor por dia: R$ %.2f (ida + volta)\nValor total entregue: R$ %.2f\n\nPróxima retirada: %02d/%02d/%d",
                funcionario.getNome(), dias,
                valorPorDia, valorTotal,
                proximaRetirada.getDayOfMonth(),
                proximaRetirada.getMonthValue(),
                proximaRetirada.getYear()
            ));
        } catch (SQLException e) {
            mostrarErro("Erro ao registrar: " + e.getMessage());
        }
    }

    private void excluirRetirada() {
        RetiradaConducao selecionada = tabela.getSelectionModel().getSelectedItem();
        if (selecionada == null) {
            mostrarAlerta("Selecione um registro na tabela para excluir!");
            return;
        }

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar Exclusão");
        confirmacao.setHeaderText("Excluir registro de retirada?");
        confirmacao.setContentText("Tem certeza que deseja excluir o registro de \"" + selecionada.getFuncionarioNome() + "\" de " + selecionada.getDataRetirada() + "?\nEsta ação não pode ser desfeita.");

        confirmacao.showAndWait().ifPresent(resposta -> {
            if (resposta == ButtonType.OK) {
                try {
                    retiradaDAO.excluir(selecionada.getId());
                    carregarRetiradas();
                    mostrarAlerta("Registro excluído com sucesso!");
                } catch (SQLException e) {
                    mostrarErro("Erro ao excluir: " + e.getMessage());
                }
            }
        });
    }

    private void carregarRetiradas() {
        try {
            List<RetiradaConducao> retiradas = retiradaDAO.listarTodas();
            listaRetiradas.setAll(retiradas);
            tabela.setItems(listaRetiradas);
        } catch (SQLException e) {
            mostrarErro("Erro ao carregar retiradas: " + e.getMessage());
        }
    }

    private void mostrarDetalhesRetirada(RetiradaConducao r) {
        lblResumoTitulo.setText("📋 Retirada Selecionada");
        lblResumoTitulo.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2E7D32;");
        painelResumo.setStyle("-fx-background-color: #E8F5E9; -fx-background-radius: 8; -fx-padding: 15;");

        LocalDate venc = r.getDataVencimento();
        String status;
        String corStatus;
        if (r.isVenceHoje()) { status = "⚠️ Vence Hoje"; corStatus = "#E65100"; }
        else if (r.isVencido()) { status = "🔴 Vencida"; corStatus = "#c62828"; }
        else if (r.isProximoVencimento()) { status = "🟡 Vence em Breve"; corStatus = "#F57F17"; }
        else { status = "🟢 OK"; corStatus = "#2E7D32"; }

        painelResumo.getChildren().setAll(
            lblResumoTitulo,
            criarLabelSecundario("Funcionário:"),
            criarLabelValor(r.getFuncionarioNome(), "#333", "13px"),
            criarLabelSecundario("Data da Retirada:"),
            criarLabelValor(String.format("%02d/%02d/%d", r.getDataRetirada().getDayOfMonth(),
                r.getDataRetirada().getMonthValue(), r.getDataRetirada().getYear()), "#333", "13px"),
            criarLabelSecundario("Quantidade de Dias:"),
            criarLabelValor(r.getQuantidadeDias() + " dias", "#333", "13px"),
            new Separator(),
            criarLabelSecundario("Valor por Dia (ida + volta):"),
            criarLabelValor(String.format("R$ %.2f", r.getValorPorDia()), "#333", "14px"),
            criarLabelSecundario("Valor Total:"),
            criarLabelValor(String.format("R$ %.2f", r.getValorTotal()), "#1565C0", "20px"),
            new Separator(),
            criarLabelSecundario("Próxima Retirada:"),
            criarLabelValor(String.format("%02d/%02d/%d", venc.getDayOfMonth(),
                venc.getMonthValue(), venc.getYear()), "#E65100", "16px"),
            criarLabelSecundario("Status:"),
            criarLabelValor(status, corStatus, "13px")
        );
    }

    private void resetarResumo() {
        lblResumoTitulo.setText("Resumo");
        lblResumoTitulo.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1565C0;");
        painelResumo.setStyle("-fx-background-color: #E3F2FD; -fx-background-radius: 8; -fx-padding: 15;");

        Label lblVPDLabel = new Label("Valor por Dia (ida + volta):");
        lblVPDLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");
        Label lblVTLabel = new Label("Valor Total:");
        lblVTLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");
        Label lblPRLabel = new Label("Próxima Retirada:");
        lblPRLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");

        painelResumo.getChildren().setAll(
            lblResumoTitulo,
            lblVPDLabel, lblValorPorDia,
            new Separator(),
            lblVTLabel, lblValorTotal,
            new Separator(),
            lblPRLabel, lblProximaRetirada
        );

        atualizarValores();
    }

    private Label criarLabelSecundario(String texto) {
        Label lbl = new Label(texto);
        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");
        return lbl;
    }

    private Label criarLabelValor(String texto, String cor, String tamanho) {
        Label lbl = new Label(texto);
        lbl.setStyle(String.format("-fx-font-size: %s; -fx-font-weight: bold; -fx-text-fill: %s;", tamanho, cor));
        return lbl;
    }

    private void mostrarAlerta(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informação");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    private void mostrarErro(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}
