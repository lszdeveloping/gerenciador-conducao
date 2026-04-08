package com.conducao.view;

import com.conducao.dao.DatabaseManager;
import com.conducao.dao.RetiradaConducaoDAO;
import com.conducao.model.RetiradaConducao;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class RelatorioView {

    private final RetiradaConducaoDAO retiradaDAO;
    private final ObservableList<RetiradaConducao> listaRelatorio = FXCollections.observableArrayList();

    private DatePicker dpInicio;
    private DatePicker dpFim;
    private TableView<RetiradaConducao> tabela;
    private Label lblTotal;
    private List<RetiradaConducao> dadosAtuais;

    public RelatorioView() throws SQLException {
        this.retiradaDAO = new RetiradaConducaoDAO(DatabaseManager.getConnection());
    }

    public Scene criarScene() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        Label titulo = new Label("📄 Relatório de Conduções");
        titulo.getStyleClass().add("titulo-principal");

        // Painel de filtro
        VBox painelFiltro = new VBox(12);
        painelFiltro.getStyleClass().add("painel-fundo");

        Label lblFiltroTitulo = new Label("Filtrar por Período");
        lblFiltroTitulo.getStyleClass().add("titulo-secao");

        HBox filtros = new HBox(20);
        filtros.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label lblInicio = new Label("De:");
        lblInicio.getStyleClass().add("label-destaque");
        dpInicio = new DatePicker(LocalDate.now().withDayOfMonth(1));
        dpInicio.setPrefWidth(160);

        Label lblFim = new Label("Até:");
        lblFim.getStyleClass().add("label-destaque");
        dpFim = new DatePicker(LocalDate.now());
        dpFim.setPrefWidth(160);

        Button btnFiltrar = new Button("🔍 Filtrar");
        btnFiltrar.getStyleClass().add("btn-primario");
        btnFiltrar.setOnAction(e -> filtrar());

        filtros.getChildren().addAll(lblInicio, dpInicio, lblFim, dpFim, btnFiltrar);

        // Card total
        HBox cardTotal = new HBox();
        cardTotal.setStyle("-fx-background-color: #1565C0; -fx-background-radius: 8; -fx-padding: 15 20;");
        cardTotal.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        cardTotal.setSpacing(15);

        Label lblTotalLabel = new Label("Total Gasto no Período:");
        lblTotalLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: rgba(255,255,255,0.85);");

        lblTotal = new Label("R$ 0,00");
        lblTotal.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");

        cardTotal.getChildren().addAll(lblTotalLabel, lblTotal);

        painelFiltro.getChildren().addAll(lblFiltroTitulo, filtros, cardTotal);

        // Tabela
        VBox painelTabela = new VBox(10);
        painelTabela.getStyleClass().add("painel-fundo");

        Label lblTabela = new Label("📋 Registros do Período");
        lblTabela.getStyleClass().add("titulo-secao");

        tabela = new TableView<>();
        tabela.setPlaceholder(new Label("Nenhum registro encontrado para o período selecionado"));
        tabela.setPrefHeight(320);

        TableColumn<RetiradaConducao, Long> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(50);

        TableColumn<RetiradaConducao, String> colFuncionario = new TableColumn<>("Funcionário");
        colFuncionario.setCellValueFactory(new PropertyValueFactory<>("funcionarioNome"));
        colFuncionario.setPrefWidth(200);

        TableColumn<RetiradaConducao, LocalDate> colData = new TableColumn<>("Data Retirada");
        colData.setCellValueFactory(new PropertyValueFactory<>("dataRetirada"));
        colData.setPrefWidth(120);

        TableColumn<RetiradaConducao, Integer> colDias = new TableColumn<>("Dias");
        colDias.setCellValueFactory(new PropertyValueFactory<>("quantidadeDias"));
        colDias.setPrefWidth(60);

        TableColumn<RetiradaConducao, BigDecimal> colVPD = new TableColumn<>("Vlr/Dia (R$)");
        colVPD.setCellValueFactory(new PropertyValueFactory<>("valorPorDia"));
        colVPD.setPrefWidth(100);

        TableColumn<RetiradaConducao, BigDecimal> colTotal = new TableColumn<>("Total (R$)");
        colTotal.setCellValueFactory(new PropertyValueFactory<>("valorTotal"));
        colTotal.setPrefWidth(100);

        tabela.getColumns().addAll(colId, colFuncionario, colData, colDias, colVPD, colTotal);
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabela.setItems(listaRelatorio);

        // Botões de exportação
        HBox botoes = new HBox(15);
        botoes.setPadding(new Insets(5, 0, 0, 0));

        Button btnJson = new Button("⬇ Exportar JSON");
        btnJson.getStyleClass().add("btn-primario");
        btnJson.setOnAction(e -> exportar("json"));

        Button btnXml = new Button("⬇ Exportar XML");
        btnXml.getStyleClass().add("btn-secundario");
        btnXml.setOnAction(e -> exportar("xml"));

        botoes.getChildren().addAll(btnJson, btnXml);

        painelTabela.getChildren().addAll(lblTabela, tabela, botoes);
        root.getChildren().addAll(titulo, painelFiltro, painelTabela);

        // Carrega o mês atual ao abrir
        filtrar();

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        return new Scene(scroll, 850, 650);
    }

    private void filtrar() {
        LocalDate inicio = dpInicio.getValue();
        LocalDate fim = dpFim.getValue();

        if (inicio == null || fim == null) {
            mostrarAlerta("Selecione as datas de início e fim!");
            return;
        }
        if (inicio.isAfter(fim)) {
            mostrarAlerta("A data inicial não pode ser maior que a data final!");
            return;
        }

        try {
            dadosAtuais = retiradaDAO.listarPorPeriodo(inicio, fim);
            listaRelatorio.setAll(dadosAtuais);
            tabela.setItems(listaRelatorio);

            BigDecimal total = dadosAtuais.stream()
                    .map(RetiradaConducao::getValorTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            lblTotal.setText(String.format("R$ %.2f", total));
        } catch (SQLException e) {
            mostrarErro("Erro ao carregar relatório: " + e.getMessage());
        }
    }

    private void exportar(String tipo) {
        if (dadosAtuais == null || dadosAtuais.isEmpty()) {
            mostrarAlerta("Nenhum dado para exportar. Faça um filtro primeiro.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salvar Relatório");
        fileChooser.setInitialFileName("relatorio_conducao." + tipo);
        fileChooser.getExtensionFilters().add(
            tipo.equals("json")
                ? new FileChooser.ExtensionFilter("JSON", "*.json")
                : new FileChooser.ExtensionFilter("XML", "*.xml")
        );

        Stage stage = (Stage) tabela.getScene().getWindow();
        File arquivo = fileChooser.showSaveDialog(stage);
        if (arquivo == null) return;

        try (FileWriter writer = new FileWriter(arquivo)) {
            if (tipo.equals("json")) {
                writer.write(gerarJson());
            } else {
                writer.write(gerarXml());
            }
            mostrarAlerta("Relatório exportado com sucesso!\n" + arquivo.getAbsolutePath());
        } catch (IOException e) {
            mostrarErro("Erro ao salvar arquivo: " + e.getMessage());
        }
    }

    private String gerarJson() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        BigDecimal total = dadosAtuais.stream()
                .map(RetiradaConducao::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append(String.format("  \"periodo\": \"%s ate %s\",\n",
                dpInicio.getValue().format(fmt), dpFim.getValue().format(fmt)));
        sb.append(String.format("  \"total_gasto\": %.2f,\n", total));
        sb.append("  \"registros\": [\n");

        for (int i = 0; i < dadosAtuais.size(); i++) {
            RetiradaConducao r = dadosAtuais.get(i);
            sb.append("    {\n");
            sb.append(String.format("      \"id\": %d,\n", r.getId()));
            sb.append(String.format("      \"funcionario\": \"%s\",\n", r.getFuncionarioNome()));
            sb.append(String.format("      \"data_retirada\": \"%s\",\n", r.getDataRetirada().format(fmt)));
            sb.append(String.format("      \"quantidade_dias\": %d,\n", r.getQuantidadeDias()));
            sb.append(String.format("      \"valor_por_dia\": %.2f,\n", r.getValorPorDia()));
            sb.append(String.format("      \"valor_total\": %.2f\n", r.getValorTotal()));
            sb.append(i < dadosAtuais.size() - 1 ? "    },\n" : "    }\n");
        }

        sb.append("  ]\n}");
        return sb.toString();
    }

    private String gerarXml() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        BigDecimal total = dadosAtuais.stream()
                .map(RetiradaConducao::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<relatorio>\n");
        sb.append(String.format("  <periodo>%s ate %s</periodo>\n",
                dpInicio.getValue().format(fmt), dpFim.getValue().format(fmt)));
        sb.append(String.format("  <total_gasto>%.2f</total_gasto>\n", total));
        sb.append("  <registros>\n");

        for (RetiradaConducao r : dadosAtuais) {
            sb.append("    <registro>\n");
            sb.append(String.format("      <id>%d</id>\n", r.getId()));
            sb.append(String.format("      <funcionario>%s</funcionario>\n", escaparXml(r.getFuncionarioNome())));
            sb.append(String.format("      <data_retirada>%s</data_retirada>\n", r.getDataRetirada().format(fmt)));
            sb.append(String.format("      <quantidade_dias>%d</quantidade_dias>\n", r.getQuantidadeDias()));
            sb.append(String.format("      <valor_por_dia>%.2f</valor_por_dia>\n", r.getValorPorDia()));
            sb.append(String.format("      <valor_total>%.2f</valor_total>\n", r.getValorTotal()));
            sb.append("    </registro>\n");
        }

        sb.append("  </registros>\n</relatorio>");
        return sb.toString();
    }

    private String escaparXml(String texto) {
        return texto.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&apos;");
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
