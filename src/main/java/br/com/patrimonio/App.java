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
    private final TableView<Patrimonio> tabela = new TableView<>();
    private final ComboBox<String> categoria = new ComboBox<>(FXCollections.observableArrayList("Computador", "Equipamento"));
    private final TextField busca = new TextField();
    private final TextField codigo = new TextField(), nome = new TextField(), local = new TextField();
    private final CheckBox emUso = new CheckBox("Está em uso");
    private Patrimonio selecionado;

    @Override public void start(Stage stage) {
        Database.initialize();
        categoria.setValue("Computador");
        TabPane abas = new TabPane(new Tab("Patrimônios", painelPatrimonios()), new Tab("Manutenções", painelManutencoes()));
        abas.getTabs().forEach(tab -> tab.setClosable(false));
        Scene cena = new Scene(abas, 1000, 650);
        cena.getStylesheets().add(getClass().getResource("/br/com/patrimonio/estilo.css").toExternalForm());
        stage.setScene(cena);
        stage.setTitle("Gestão de Patrimônios de TI"); stage.show();
        atualizarTabela();
    }

    private Pane painelPatrimonios() {
        tabela.getColumns().addAll(coluna("Código", Patrimonio::codigo), coluna("Nome", Patrimonio::nome), coluna("Local alocado", Patrimonio::localAlocado),
                coluna("Em uso", p -> p.emUso() ? "Sim" : "Não"));
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabela.getSelectionModel().selectedItemProperty().addListener((o, antigo, item) -> { if (item != null) preencher(item); });
        busca.setPromptText("Consultar por código, nome ou local"); busca.textProperty().addListener((o,a,n) -> atualizarTabela());
        categoria.setOnAction(e -> { limpar(); atualizarTabela(); });

        GridPane form = new GridPane(); form.setHgap(10); form.setVgap(10);
        form.addRow(0, new Label("Código:*"), codigo, new Label("Nome:*"), nome);
        form.addRow(1, new Label("Local alocado:*"), local, emUso);
        GridPane.setHgrow(codigo, Priority.ALWAYS); GridPane.setHgrow(nome, Priority.ALWAYS); GridPane.setHgrow(local, Priority.ALWAYS);
        Button salvar = new Button("Salvar patrimônio"); salvar.setOnAction(e -> salvar());
        Button novo = new Button("Novo / Limpar"); novo.setOnAction(e -> limpar());
        HBox botoes = new HBox(10, salvar, novo);
        Label subtituloCadastro = new Label("Cadastro e edição"); subtituloCadastro.getStyleClass().add("section-title");
        VBox caixaForm = new VBox(10, subtituloCadastro, form, botoes); caixaForm.getStyleClass().add("card");
        Label titulo = new Label("Categoria:"); titulo.getStyleClass().add("filter-label");
        HBox filtros = new HBox(10, titulo, categoria, busca); HBox.setHgrow(busca, Priority.ALWAYS);
        VBox raiz = new VBox(14, filtros, tabela, caixaForm); raiz.getStyleClass().add("content-area"); raiz.setPadding(new Insets(24)); VBox.setVgrow(tabela, Priority.ALWAYS);
        return raiz;
    }
    private TableColumn<Patrimonio, String> coluna(String titulo, java.util.function.Function<Patrimonio,String> valor) {
        TableColumn<Patrimonio,String> c = new TableColumn<>(titulo); c.setCellValueFactory(x -> new SimpleStringProperty(valor.apply(x.getValue()))); return c;
    }
    private void atualizarTabela() { tabela.setItems(FXCollections.observableArrayList(patrimonioRepo.buscar(categoria.getValue(), busca.getText()))); }
    private void preencher(Patrimonio p) { selecionado = p; codigo.setText(p.codigo()); nome.setText(p.nome()); local.setText(p.localAlocado()); emUso.setSelected(p.emUso()); }
    private void limpar() { selecionado = null; tabela.getSelectionModel().clearSelection(); codigo.clear(); nome.clear(); local.clear(); emUso.setSelected(true); }
    private void salvar() {
        if (codigo.getText().isBlank() || nome.getText().isBlank() || local.getText().isBlank()) { aviso("Preencha código, nome e local."); return; }
        try { patrimonioRepo.salvar(new Patrimonio(selecionado == null ? 0 : selecionado.id(), codigo.getText().trim(), nome.getText().trim(), categoria.getValue(), emUso.isSelected(), local.getText().trim())); limpar(); atualizarTabela(); }
        catch (RuntimeException e) { aviso(e.getMessage()); }
    }
    private Pane painelManutencoes() {
        TextField lugar = new TextField(); lugar.setPromptText("Ex.: Sala de servidores");
        ComboBox<String> tipo = new ComboBox<>(FXCollections.observableArrayList("Preventiva", "Corretiva", "Limpeza", "Atualização")); tipo.setValue("Preventiva");
        DatePicker data = new DatePicker(LocalDate.now()); TextArea obs = new TextArea(); obs.setPromptText("Observações (opcional)"); obs.setPrefRowCount(2);
        TableView<ManutencaoRepository.Manutencao> lista = new TableView<>(); lista.getColumns().addAll(
                manutCol("Data", m -> m.data().toString()), manutCol("Local", ManutencaoRepository.Manutencao::local), manutCol("Tipo", ManutencaoRepository.Manutencao::tipo), manutCol("Observação", ManutencaoRepository.Manutencao::observacao));
        lista.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        Runnable atualizar = () -> lista.setItems(FXCollections.observableArrayList(manutencaoRepo.listar())); atualizar.run();
        Button agendar = new Button("Agendar manutenção"); agendar.setOnAction(e -> {
            if (lugar.getText().isBlank() || data.getValue() == null) { aviso("Informe o local e a data."); return; }
            try { manutencaoRepo.salvar(lugar.getText().trim(), tipo.getValue(), data.getValue(), obs.getText().trim()); lugar.clear(); obs.clear(); atualizar.run(); } catch (RuntimeException ex) { aviso(ex.getMessage()); }
        });
        GridPane form = new GridPane(); form.setHgap(10); form.setVgap(10); form.addRow(0, new Label("Local:*"), lugar, new Label("Tipo:*"), tipo, new Label("Data:*"), data); form.addRow(1, new Label("Observação:"), obs); GridPane.setColumnSpan(obs, 5); GridPane.setHgrow(lugar, Priority.ALWAYS); GridPane.setHgrow(obs, Priority.ALWAYS);
        Label tituloAgenda = new Label("Agendar manutenção"); tituloAgenda.getStyleClass().add("page-title");
        Label tituloLista = new Label("Próximas manutenções"); tituloLista.getStyleClass().add("section-title");
        VBox cartao = new VBox(12, tituloAgenda, form, agendar); cartao.getStyleClass().add("card");
        VBox raiz = new VBox(18, cartao, tituloLista, lista); raiz.getStyleClass().add("content-area"); raiz.setPadding(new Insets(24)); VBox.setVgrow(lista, Priority.ALWAYS); return raiz;
    }
    private TableColumn<ManutencaoRepository.Manutencao,String> manutCol(String t, java.util.function.Function<ManutencaoRepository.Manutencao,String> f) { TableColumn<ManutencaoRepository.Manutencao,String> c = new TableColumn<>(t); c.setCellValueFactory(x -> new SimpleStringProperty(f.apply(x.getValue()))); return c; }
    private void aviso(String mensagem) { new Alert(Alert.AlertType.WARNING, mensagem, ButtonType.OK).showAndWait(); }
    public static void main(String[] args) { launch(args); }
}
