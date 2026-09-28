package br.com.patrimonio;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.time.LocalDate;

public class App extends Application {

    private final PatrimonioRepository patrimonioRepo = new PatrimonioRepository();
    private final ManutencaoRepository manutencaoRepo = new ManutencaoRepository();
    private final LocalizacaoRepository localizacaoRepo
            = new LocalizacaoRepository();
    private final TableView<Patrimonio> tabela = new TableView<>();
    private final ComboBox<String> categoria = new ComboBox<>(FXCollections.observableArrayList("Computador", "Equipamento"));
    private final TextField busca = new TextField();
    private final TextField codigo = new TextField(), nome = new TextField(), local = new TextField();
    private final CheckBox emUso = new CheckBox("Está em uso");
    private final ComboBox<String> bloco = new ComboBox<>();

    private final ComboBox<String> sala = new ComboBox<>();
    private final Spinner<Integer> quantidade = new Spinner<>(1, 100, 1);
    private Patrimonio selecionado;

    @Override
    public void start(Stage stage) {
        Database.initialize();
        configurarLocais();
        categoria.setValue("Computador");
        TabPane abas = new TabPane(
                new Tab("Patrimônios", painelPatrimonios()),
                new Tab("Manutenções", painelManutencoes()),
                new Tab("Locais", painelLocais())
        );
        abas.getTabs().forEach(tab -> tab.setClosable(false));
        Scene cena = new Scene(abas, 1000, 650);
        cena.getStylesheets().add(getClass().getResource("/br/com/patrimonio/estilo.css").toExternalForm());
        stage.setScene(cena);
        stage.setTitle("Gestão de Patrimônios de TI");
        stage.show();
        atualizarTabela();
    }

    private void configurarLocais() {
        bloco.setItems(FXCollections.observableArrayList(
                localizacaoRepo.listarBlocos()
        ));

        if (!bloco.getItems().isEmpty()) {
            bloco.setValue(bloco.getItems().get(0));
            atualizarSalas();
        }

        bloco.setOnAction(e -> atualizarSalas());
    }

    private void atualizarSalas() {
        if (bloco.getValue() == null) {
            sala.getItems().clear();
            sala.setValue(null);
            return;
        }

        sala.setItems(FXCollections.observableArrayList(
                localizacaoRepo.listarSalas(bloco.getValue())
        ));

        if (!sala.getItems().isEmpty()) {
            sala.setValue(sala.getItems().get(0));
        }
    }

    private String localSelecionado() {
        return bloco.getValue() + " — " + sala.getValue();
    }

    private Pane painelPatrimonios() {
        tabela.getColumns().addAll(coluna("Código", Patrimonio::codigo), coluna("Nome", Patrimonio::nome), coluna("Local alocado", Patrimonio::localAlocado),
                coluna("Em uso", p -> p.emUso() ? "Sim" : "Não"));
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabela.getSelectionModel().selectedItemProperty().addListener((o, antigo, item) -> {
            if (item != null) {
                preencher(item);
            }
        });
        busca.setPromptText("Consultar por código, nome ou local");
        busca.textProperty().addListener((o, a, n) -> atualizarTabela());
        categoria.setOnAction(e -> {
            limpar();
            atualizarTabela();
        });

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);

        form.addRow(
                0,
                new Label("Código inicial:*"), codigo,
                new Label("Nome:*"), nome
        );

        form.addRow(
                1,
                new Label("Bloco:*"), bloco,
                new Label("Sala:*"), sala
        );

        form.addRow(
                2,
                new Label("Quantidade:"), quantidade,
                emUso
        );

        GridPane.setHgrow(codigo, Priority.ALWAYS);
        GridPane.setHgrow(nome, Priority.ALWAYS);
        GridPane.setHgrow(bloco, Priority.ALWAYS);
        GridPane.setHgrow(sala, Priority.ALWAYS);
        Button salvar = new Button("Salvar patrimônio");
        salvar.setOnAction(e -> salvar());
        Button novo = new Button("Novo / Limpar");
        novo.setOnAction(e -> limpar());
        HBox botoes = new HBox(10, salvar, novo);
        Label subtituloCadastro = new Label("Cadastro e edição");
        subtituloCadastro.getStyleClass().add("section-title");
        VBox caixaForm = new VBox(10, subtituloCadastro, form, botoes);
        caixaForm.getStyleClass().add("card");
        Label titulo = new Label("Categoria:");
        titulo.getStyleClass().add("filter-label");
        HBox filtros = new HBox(10, titulo, categoria, busca);
        HBox.setHgrow(busca, Priority.ALWAYS);
        VBox raiz = new VBox(14, filtros, tabela, caixaForm);
        raiz.getStyleClass().add("content-area");
        raiz.setPadding(new Insets(24));
        VBox.setVgrow(tabela, Priority.ALWAYS);
        return raiz;
    }

    private TableColumn<Patrimonio, String> coluna(String titulo, java.util.function.Function<Patrimonio, String> valor) {
        TableColumn<Patrimonio, String> c = new TableColumn<>(titulo);
        c.setCellValueFactory(x -> new SimpleStringProperty(valor.apply(x.getValue())));
        return c;
    }

    private void atualizarTabela() {
        tabela.setItems(FXCollections.observableArrayList(patrimonioRepo.buscar(categoria.getValue(), busca.getText())));
    }

    private void preencher(Patrimonio p) {
        selecionado = p;

        codigo.setText(p.codigo());
        nome.setText(p.nome());
        emUso.setSelected(p.emUso());

        String[] partes = p.localAlocado().split(" — ", 2);

        if (partes.length == 2 && bloco.getItems().contains(partes[0])) {
            bloco.setValue(partes[0]);
            atualizarSalas();

            if (sala.getItems().contains(partes[1])) {
                sala.setValue(partes[1]);
            }
        }
    }

    private void limpar() {
        selecionado = null;
        tabela.getSelectionModel().clearSelection();

        codigo.clear();
        nome.clear();
        if (!bloco.getItems().isEmpty()) {
            bloco.setValue(bloco.getItems().get(0));
            atualizarSalas();
        }

        emUso.setSelected(true);
        quantidade.getValueFactory().setValue(1);
    }

    private void salvar() {
        if (codigo.getText().isBlank()
                || nome.getText().isBlank()
                || bloco.getValue() == null
                || sala.getValue() == null) {

            aviso("Preencha código, nome e local.");
            return;
        }

        try {
            if (selecionado == null) {
                patrimonioRepo.salvarEmLote(
                        codigo.getText().trim(),
                        nome.getText().trim(),
                        categoria.getValue(),
                        emUso.isSelected(),
                        localSelecionado(),
                        quantidade.getValue()
                );

            } else {
                patrimonioRepo.salvar(
                        new Patrimonio(
                                selecionado.id(),
                                codigo.getText().trim(),
                                nome.getText().trim(),
                                categoria.getValue(),
                                emUso.isSelected(),
                                localSelecionado()
                        )
                );
            }

            limpar();
            atualizarTabela();

        } catch (RuntimeException e) {
            aviso(e.getMessage());
        }
    }

    private Pane painelLocais() {
        TextField campoBloco = new TextField();
        campoBloco.setPromptText("Ex.: Bloco 6");

        TextField campoSala = new TextField();
        campoSala.setPromptText("Ex.: Laboratório de Informática");

        TableView<LocalizacaoRepository.Localizacao> tabelaLocais
                = new TableView<>();

        tabelaLocais.getColumns().addAll(
                localizacaoCol(
                        "Bloco",
                        LocalizacaoRepository.Localizacao::bloco
                ),
                localizacaoCol(
                        "Sala",
                        LocalizacaoRepository.Localizacao::sala
                )
        );

        tabelaLocais.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        Runnable atualizar = () -> {
            tabelaLocais.setItems(FXCollections.observableArrayList(
                    localizacaoRepo.listarTodas()
            ));

            configurarLocais();
        };

        atualizar.run();

        Button adicionar = new Button("Adicionar sala");

        adicionar.setOnAction(e -> {
            if (campoBloco.getText().isBlank()
                    || campoSala.getText().isBlank()) {

                aviso("Informe o bloco e o nome da sala.");
                return;
            }

            try {
                localizacaoRepo.salvar(
                        campoBloco.getText(),
                        campoSala.getText()
                );

                campoSala.clear();
                atualizar.run();

            } catch (RuntimeException ex) {
                aviso(ex.getMessage());
            }
        });

        Button excluir = new Button("Excluir local selecionado");
        excluir.getStyleClass().add("danger-button");

        excluir.setOnAction(e -> {
            LocalizacaoRepository.Localizacao selecionado
                    = tabelaLocais.getSelectionModel().getSelectedItem();

            if (selecionado == null) {
                aviso("Selecione um local para excluir.");
                return;
            }

            Alert confirmacao = new Alert(
                    Alert.AlertType.CONFIRMATION,
                    "Deseja excluir o local selecionado?",
                    ButtonType.YES,
                    ButtonType.NO
            );

            confirmacao.setHeaderText("Confirmar exclusão");

            if (confirmacao.showAndWait().orElse(ButtonType.NO)
                    == ButtonType.YES) {

                localizacaoRepo.excluir(selecionado.id());
                atualizar.run();
            }
        });

        GridPane formulario = new GridPane();
        formulario.setHgap(10);
        formulario.setVgap(10);

        formulario.addRow(
                0,
                new Label("Bloco:*"), campoBloco,
                new Label("Sala:*"), campoSala
        );

        GridPane.setHgrow(campoBloco, Priority.ALWAYS);
        GridPane.setHgrow(campoSala, Priority.ALWAYS);

        HBox acoes = new HBox(10, adicionar, excluir);

        Label titulo = new Label("Gerenciar locais");
        titulo.getStyleClass().add("page-title");

        VBox cartao = new VBox(12, titulo, formulario, acoes);
        cartao.getStyleClass().add("card");

        Label tituloTabela = new Label("Blocos e salas cadastrados");
        tituloTabela.getStyleClass().add("section-title");

        VBox raiz = new VBox(18, cartao, tituloTabela, tabelaLocais);
        raiz.getStyleClass().add("content-area");
        raiz.setPadding(new Insets(24));

        VBox.setVgrow(tabelaLocais, Priority.ALWAYS);

        return raiz;
    }

    private Pane painelManutencoes() {
        TextField lugar = new TextField();
        lugar.setPromptText("Ex.: Sala de servidores");
        ComboBox<String> tipo = new ComboBox<>(FXCollections.observableArrayList("Preventiva", "Corretiva", "Limpeza", "Atualização"));
        tipo.setValue("Preventiva");
        DatePicker data = new DatePicker(LocalDate.now());
        TextArea obs = new TextArea();
        obs.setPromptText("Observações (opcional)");
        obs.setPrefRowCount(2);
        TableView<ManutencaoRepository.Manutencao> lista = new TableView<>();
        lista.getColumns().addAll(
                manutCol("Data", m -> m.data().toString()), manutCol("Local", ManutencaoRepository.Manutencao::local), manutCol("Tipo", ManutencaoRepository.Manutencao::tipo), manutCol("Observação", ManutencaoRepository.Manutencao::observacao));
        lista.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        Runnable atualizar = () -> lista.setItems(FXCollections.observableArrayList(manutencaoRepo.listar()));
        atualizar.run();
        Button agendar = new Button("Agendar manutenção");

        agendar.setOnAction(e -> {
            if (lugar.getText().isBlank() || data.getValue() == null) {
                aviso("Informe o local e a data.");
                return;
            }

            try {
                manutencaoRepo.salvar(
                        lugar.getText().trim(),
                        tipo.getValue(),
                        data.getValue(),
                        obs.getText().trim()
                );

                lugar.clear();
                obs.clear();
                atualizar.run();

            } catch (RuntimeException ex) {
                aviso(ex.getMessage());
            }
        });

        Button excluir = new Button("Excluir manutenção selecionada");
        excluir.getStyleClass().add("danger-button");

        excluir.setOnAction(e -> {
            ManutencaoRepository.Manutencao selecionada
                    = lista.getSelectionModel().getSelectedItem();

            if (selecionada == null) {
                aviso("Selecione uma manutenção na tabela para excluí-la.");
                return;
            }

            Alert confirmacao = new Alert(
                    Alert.AlertType.CONFIRMATION,
                    "Deseja excluir a manutenção selecionada?",
                    ButtonType.YES,
                    ButtonType.NO
            );

            confirmacao.setHeaderText("Confirmar exclusão");

            if (confirmacao.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
                manutencaoRepo.excluir(selecionada.id());
                atualizar.run();
            }
        });

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);

        form.addRow(
                0,
                new Label("Local:*"), lugar,
                new Label("Tipo:*"), tipo,
                new Label("Data:*"), data
        );

        form.addRow(1, new Label("Observação:"), obs);

        GridPane.setColumnSpan(obs, 5);
        GridPane.setHgrow(lugar, Priority.ALWAYS);
        GridPane.setHgrow(obs, Priority.ALWAYS);

        Label tituloAgenda = new Label("Agendar manutenção");
        tituloAgenda.getStyleClass().add("page-title");

        Label tituloLista = new Label("Próximas manutenções");
        tituloLista.getStyleClass().add("section-title");

        HBox acoes = new HBox(10, agendar, excluir);

        VBox cartao = new VBox(12, tituloAgenda, form, acoes);
        cartao.getStyleClass().add("card");

        VBox raiz = new VBox(18, cartao, tituloLista, lista);
        raiz.getStyleClass().add("content-area");
        raiz.setPadding(new Insets(24));

        VBox.setVgrow(lista, Priority.ALWAYS);

        return raiz;

    }
    
    private TableColumn<LocalizacaoRepository.Localizacao, String>
        localizacaoCol(
                String titulo,
                java.util.function.Function<
                        LocalizacaoRepository.Localizacao,
                        String
                > valor
        ) {

    TableColumn<LocalizacaoRepository.Localizacao, String> coluna =
            new TableColumn<>(titulo);

    coluna.setCellValueFactory(
            item -> new SimpleStringProperty(valor.apply(item.getValue()))
    );

    return coluna;
}

    private TableColumn<ManutencaoRepository.Manutencao, String> manutCol(String t, java.util.function.Function<ManutencaoRepository.Manutencao, String> f) {
        TableColumn<ManutencaoRepository.Manutencao, String> c = new TableColumn<>(t);
        c.setCellValueFactory(x -> new SimpleStringProperty(f.apply(x.getValue())));
        return c;
    }

    private void aviso(String mensagem) {
        new Alert(Alert.AlertType.WARNING, mensagem, ButtonType.OK).showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
