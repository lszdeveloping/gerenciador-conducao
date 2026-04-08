package com.conducao.view;

import com.conducao.dao.DatabaseManager;
import com.conducao.dao.FuncionarioDAO;
import com.conducao.model.Funcionario;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.sql.SQLException;

public class FuncionarioView {

    private final FuncionarioDAO dao;
    private final ObservableList<Funcionario> listaFuncionarios = FXCollections.observableArrayList();

    private TextField txtNome;
    private ComboBox<String> cbTipoConducao;
    private TextField txtValor;
    private TableView<Funcionario> tabela;
    private Funcionario funcionarioSelecionado;

    public FuncionarioView() throws SQLException {
        this.dao = new FuncionarioDAO(DatabaseManager.getConnection());
        dao.criarTabela();
    }

    public Scene criarScene() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        // Título
        Label titulo = new Label("📋 Cadastro de Funcionários");
        titulo.getStyleClass().add("titulo-principal");

        // Painel do formulário
        VBox painelForm = new VBox(15);
        painelForm.getStyleClass().add("painel-fundo");

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);

        txtNome = new TextField();
        txtNome.setPromptText("Digite o nome completo");
        txtNome.setPrefWidth(350);

        cbTipoConducao = new ComboBox<>();
        cbTipoConducao.getItems().addAll("Onibus", "Barquinha", "Balsa");
        cbTipoConducao.setPromptText("Selecione o tipo de condução");
        cbTipoConducao.setPrefWidth(350);

        txtValor = new TextField();
        txtValor.setPromptText("Ex: 5.50");
        txtValor.setPrefWidth(350);

        Label lblNome = new Label("Nome:");
        lblNome.getStyleClass().add("label-destaque");
        Label lblTipo = new Label("Tipo de Condução:");
        lblTipo.getStyleClass().add("label-destaque");
        Label lblValor = new Label("Valor da Condução (R$):");
        lblValor.getStyleClass().add("label-destaque");

        form.add(lblNome, 0, 0);
        form.add(txtNome, 0, 1);
        form.add(lblTipo, 0, 2);
        form.add(cbTipoConducao, 0, 3);
        form.add(lblValor, 0, 4);
        form.add(txtValor, 0, 5);

        // Botões
        HBox botoes = new HBox(15);
        botoes.setPadding(new Insets(10, 0, 0, 0));
        Button btnSalvar = new Button("💾 Salvar");
        Button btnLimpar = new Button("🧹 Limpar");
        Button btnExcluir = new Button("🗑️ Excluir");
        Button btnAtualizar = new Button("🔄 Atualizar");

        btnSalvar.getStyleClass().add("btn-primario");
        btnLimpar.getStyleClass().add("btn-secundario");
        btnExcluir.getStyleClass().add("btn-perigo");
        btnAtualizar.getStyleClass().add("btn-secundario");

        botoes.getChildren().addAll(btnSalvar, btnLimpar, btnExcluir, btnAtualizar);
        painelForm.getChildren().addAll(form, botoes);

        // Tabela
        VBox painelTabela = new VBox(10);
        painelTabela.getStyleClass().add("painel-fundo");

        Label lblTabela = new Label("📊 Funcionários Cadastrados");
        lblTabela.getStyleClass().add("titulo-secao");

        tabela = new TableView<>();
        tabela.setPlaceholder(new Label("Nenhum funcionário cadastrado"));

        TableColumn<Funcionario, Long> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(60);

        TableColumn<Funcionario, String> colNome = new TableColumn<>("Nome");
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colNome.setPrefWidth(250);

        TableColumn<Funcionario, String> colTipo = new TableColumn<>("Tipo de Condução");
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipoConducao"));
        colTipo.setPrefWidth(150);

        TableColumn<Funcionario, BigDecimal> colValor = new TableColumn<>("Valor (R$)");
        colValor.setCellValueFactory(new PropertyValueFactory<>("valor"));
        colValor.setPrefWidth(120);

        tabela.getColumns().addAll(colId, colNome, colTipo, colValor);
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Carregar dados
        carregarDados();

        // Ações dos botões
        btnSalvar.setOnAction(e -> salvar());
        btnLimpar.setOnAction(e -> limparFormulario());
        btnExcluir.setOnAction(e -> excluir());
        btnAtualizar.setOnAction(e -> carregarDados());

        tabela.getSelectionModel().selectedItemProperty()
                .addListener((obs, antigo, novo) -> funcionarioSelecionado = novo);

        painelTabela.getChildren().addAll(lblTabela, tabela);
        root.getChildren().addAll(titulo, painelForm, painelTabela);

        return new Scene(root, 750, 600);
    }

    private void carregarDados() {
        try {
            listaFuncionarios.setAll(dao.listarTodos());
            tabela.setItems(listaFuncionarios);
        } catch (SQLException e) {
            mostrarErro("Erro ao carregar dados: " + e.getMessage());
        }
    }

    private void salvar() {
        String nome = txtNome.getText().trim();
        String tipo = cbTipoConducao.getValue();
        String valorStr = txtValor.getText().trim();

        if (nome.isEmpty() || tipo == null || valorStr.isEmpty()) {
            mostrarAlerta("Preencha todos os campos!");
            return;
        }

        BigDecimal valor;
        try {
            valor = new BigDecimal(valorStr.replace(",", "."));
        } catch (NumberFormatException e) {
            mostrarAlerta("Valor inválido! Use formato numérico (ex: 5.50)");
            return;
        }

        Funcionario funcionario = new Funcionario(nome, tipo, valor);
        if (funcionarioSelecionado != null) {
            funcionario.setId(funcionarioSelecionado.getId());
        }

        try {
            dao.salvar(funcionario);
            carregarDados();
            limparFormulario();
            mostrarAlerta("Funcionário salvo com sucesso!");
        } catch (SQLException e) {
            mostrarErro("Erro ao salvar: " + e.getMessage());
        }
    }

    private void excluir() {
        if (funcionarioSelecionado == null) {
            mostrarAlerta("Selecione um funcionário na tabela!");
            return;
        }

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar Exclusão");
        confirmacao.setHeaderText("Excluir funcionário?");
        confirmacao.setContentText("Tem certeza que deseja excluir \"" + funcionarioSelecionado.getNome() + "\"?\nEsta ação não pode ser desfeita.");

        confirmacao.showAndWait().ifPresent(resposta -> {
            if (resposta == ButtonType.OK) {
                try {
                    dao.excluir(funcionarioSelecionado.getId());
                    carregarDados();
                    limparFormulario();
                    mostrarAlerta("Funcionário excluído com sucesso!");
                } catch (SQLException e) {
                    mostrarErro("Erro ao excluir: " + e.getMessage());
                }
            }
        });
    }

    private void limparFormulario() {
        txtNome.clear();
        cbTipoConducao.setValue(null);
        txtValor.clear();
        funcionarioSelecionado = null;
        txtNome.requestFocus();
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
