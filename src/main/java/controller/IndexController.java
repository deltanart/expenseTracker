package main.java.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;
import main.java.controller.DataController;
import main.java.model.Category;
import main.java.model.Transaction;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.Date;
import java.util.ResourceBundle;


public class IndexController extends AbstractViewController {

    @FXML
    Pane MainPane = new Pane();
    @FXML
    ProgressIndicator saveProgress = new ProgressIndicator();
    @FXML
    Button SettingsButton = new Button();
    @FXML
    Label OneServerLabel = new Label();

    @FXML
    DatePicker transDate = new DatePicker();


    @FXML
    private final TableView<Transaction> TransactionTable = new TableView<Transaction>();

    /*
     * @FXML private TableColumn<Program, String> programName;
     *
     * @FXML private TableColumn<Program, String> (SimpleStringProperty)
     * programVersion;
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("INITIALIZE! Index View");
        // programName.setCellValueFactory(new PropertyValueFactory<>("Program Name"));
        // programVersion.setCellValueFactory(new PropertyValueFactory<>("Version"));
        transDate.setOnAction(e  -> {
            LocalDate date = transDate.getValue();
            System.err.println("Selected date: " + date);
        });
        initTableCols();

    }

    @FXML
    private void loadShowAllTransactions() throws IOException {
        try {
            loadView("AllTransactions");
        }catch (Exception e){
            Alert alert = new Alert(Alert.AlertType.ERROR,  "Failed to load!");
            alert.show();
        }

    }



    public void showOverview() {

    }
    //TODO Make DRY!!
    private void initTableCols() {
        TransactionTable.setEditable(true);
        TableColumn product_name_col = new TableColumn("Produktbezeichnung");
        product_name_col.setMinWidth(400);
        product_name_col.setEditable(true);
        product_name_col.setCellFactory(TextFieldTableCell.forTableColumn());
        product_name_col.setCellValueFactory(new PropertyValueFactory<Transaction, String>("product_name"));
        product_name_col.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Transaction, String>>() {
            @Override
            public void handle(TableColumn.CellEditEvent event) {
                Transaction transaction = (Transaction) event.getRowValue();
                transaction.setProduct_name(event.getNewValue().toString());
            }
        });

        TableColumn single_price_col = new TableColumn("Einzelpreis");
        single_price_col.setMinWidth(100);
        single_price_col.setEditable(true);
        single_price_col.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));//TODO Handle Exeption
        single_price_col.setCellValueFactory(new PropertyValueFactory<Transaction, Double>("single_price"));
        single_price_col.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Transaction, Double>>() {
            @Override
            public void handle(TableColumn.CellEditEvent event) {
                Transaction transaction = (Transaction) event.getRowValue();
                transaction.setSingle_price((Double) event.getNewValue());
            }
        });

        TableColumn amount_col = new TableColumn("Menge");
        amount_col.setMinWidth(100);
        amount_col.setEditable(true);
        amount_col.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));//TODO Handle Exeption
        amount_col.setCellValueFactory(new PropertyValueFactory<Transaction, Double>("amount"));
        amount_col.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Transaction, Double>>() {
            @Override
            public void handle(TableColumn.CellEditEvent event) {
                Transaction transaction = (Transaction) event.getRowValue();
                transaction.setAmount((Double) event.getNewValue());
            }
        });

        TableColumn total_price_col = new TableColumn("Gesamtpreis");
        total_price_col.setMinWidth(100);
        total_price_col.setEditable(true);
        total_price_col.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter())); //TODO Handle Exeption
        total_price_col.setCellValueFactory(new PropertyValueFactory<Transaction, Double>("total_price"));
        total_price_col.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Transaction, Double>>() {
            @Override
            public void handle(TableColumn.CellEditEvent event) {
                Transaction transaction = (Transaction) event.getRowValue();
                transaction.setTotal_price((Double) event.getNewValue());
            }
        });

        // Casting the program Array to an ObservableList
        ObservableList<Transaction> transactionObservableList = FXCollections.observableArrayList();
        transactionObservableList.add(new Transaction());


        TransactionTable.setItems(transactionObservableList);
        //TransactionTable.getColumns().removeAll();
        TransactionTable.getColumns().addAll(product_name_col, single_price_col, amount_col,total_price_col );

        TransactionTable.getRowFactory();
    }


    private void changeMouse(final Cursor cursor) {
        Platform.runLater(() -> MainPane.setCursor(cursor));
    }

    public void saveNewTransaction(){
        System.out.print("Saving...");
        try {
            String date = transDate.getValue().toString();
            String location = "TestLocation";
            String product_name = "TestProduct";
            Category category = new Category(1);
            category.setCategory("Test");
            double single_price = 5.0;
            double amount = 2.0;
            double total_price = 10.0;
            Transaction transaction = new Transaction(date, location, product_name, category, single_price, amount, total_price);

            DataController.SaveTransaction(transaction);
            System.out.println("done.");
        }catch (Exception e){
            Alert alert = new Alert(Alert.AlertType.ERROR,  "Date missing");
            alert.show();
            System.out.println("---- FAILED! ----");
        }


    }

}
