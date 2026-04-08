# Gerenciador de Conduções

Sistema desktop para controle de pagamento de condução de funcionários, desenvolvido em JavaFX com banco de dados SQLite.

## Download

Acesse a página de [Releases](https://github.com/lszdeveloping/gerenciador-conducao/releases) e baixe o arquivo `.zip` da versão mais recente. Extraia a pasta e execute `GerenciadorConducao.exe`.

> Não é necessário ter Java instalado — o programa já inclui tudo que precisa.

## Funcionalidades

- **Dashboard** — visão geral com alertas de conduções vencidas ou próximas do vencimento
- **Cadastro de Funcionários** — gerenciamento de funcionários com tipo e valor de condução
- **Pagamento de Condução** — registro de retiradas, cálculo automático de valor e data de vencimento
- **Relatório** — geração de relatórios por período com exportação em CSV

## Tecnologias

- Java 17
- JavaFX 17
- SQLite (via sqlite-jdbc)
- Maven

## Como rodar em desenvolvimento

**Pré-requisitos:** JDK 17+ e Maven 3.6+

```bash
mvn javafx:run
```

## Como gerar o executável

```bash
mvn package jpackage:jpackage
```

O executável será gerado em `target/dist/Gerenciador de Conducao/`. Compacte essa pasta em `.zip` e suba como uma nova Release no GitHub.

> **Nota:** o banco de dados (`conducao.db`) é criado automaticamente na primeira execução, na mesma pasta do executável.

## Estrutura do projeto

```
src/
└── main/
    └── java/com/conducao/
        ├── MainApp.java          # Ponto de entrada da aplicação
        ├── Launcher.java         # Launcher para compatibilidade com jpackage
        ├── model/
        │   ├── Funcionario.java
        │   └── RetiradaConducao.java
        ├── dao/
        │   ├── DatabaseManager.java
        │   ├── FuncionarioDAO.java
        │   └── RetiradaConducaoDAO.java
        └── view/
            ├── DashboardView.java
            ├── FuncionarioView.java
            ├── RetiradaConducaoView.java
            └── RelatorioView.java
```
