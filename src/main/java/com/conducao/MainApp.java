package com.conducao;

import com.conducao.dao.DatabaseManager;
import com.conducao.view.DashboardView;
import com.conducao.view.FuncionarioView;
import com.conducao.view.RelatorioView;
import com.conducao.view.RetiradaConducaoView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

import java.sql.SQLException;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Gerenciador de Conduções");
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(650);

        // Criar abas
        TabPane tabPane = new TabPane();
        tabPane.setStyle("-fx-font-size: 14px;");

        // Aba Dashboard (primeira aba)
        DashboardView dashboardView = new DashboardView();
        Tab tabDashboard = new Tab("📊 Dashboard");
        tabDashboard.setContent(dashboardView.criarScene().getRoot());
        tabDashboard.setClosable(false);

        // Aba de Funcionários
        FuncionarioView funcionarioView = new FuncionarioView();
        Tab tabFuncionarios = new Tab("👥 Funcionários");
        tabFuncionarios.setContent(funcionarioView.criarScene().getRoot());
        tabFuncionarios.setClosable(false);

        // Aba de Pagamento de Condução
        RetiradaConducaoView retiradaView = new RetiradaConducaoView();
        Tab tabRetirada = new Tab("💰 Pagamento de Condução");
        tabRetirada.setContent(retiradaView.criarScene().getRoot());
        tabRetirada.setClosable(false);

        // Aba de Relatório
        RelatorioView relatorioView = new RelatorioView();
        Tab tabRelatorio = new Tab("📄 Relatório");
        tabRelatorio.setContent(relatorioView.criarScene().getRoot());
        tabRelatorio.setClosable(false);

        tabPane.getTabs().addAll(tabDashboard, tabFuncionarios, tabRetirada, tabRelatorio);

        // Carregar CSS
        Scene scene = new Scene(tabPane, 900, 650);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.show();

        // Fechar conexão ao fechar a janela
        primaryStage.setOnCloseRequest(e -> DatabaseManager.closeConnection());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
