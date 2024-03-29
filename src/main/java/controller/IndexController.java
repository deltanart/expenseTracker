package main.java.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import main.java.model.PaymentMethod;
import main.java.model.Receiver;
import main.java.model.Transaction;
import main.java.model.TransactionCategory;

import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;


public class IndexController extends AbstractViewController implements Initializable {

    DataController dc = new DataController();

    @FXML
    Pane MainPane = new Pane();


    @FXML
    DatePicker transDate = new DatePicker();

    @FXML
    ChoiceBox transactionCategorieField;

    @FXML
    ChoiceBox transactionCategorieFieldEntry;

    @FXML
    ChoiceBox paymentMethodField;

    @FXML
    private TextField tx_newPaymentMethod = new TextField();

    @FXML
    private TextField tx_newCategory = new TextField();

    @FXML
    private Spinner<Integer> tx_loadTransaction = new Spinner<>(Integer.MIN_VALUE, Integer.MAX_VALUE, 1, 1);

    @FXML
    private RadioButton radioMoneyOutcome = new RadioButton();

    @FXML
    private RadioButton radioMoneyIncome = new RadioButton();

    @FXML
    private final ToggleGroup toggleGroup = new ToggleGroup();

    @FXML
    private ComboBox<String> tx_receiver = new ComboBox<String>();

    @FXML
    private ComboBox<String> tx_receiver_category_helper = new ComboBox<String>();

    @FXML
    private Spinner<Double> amount = new Spinner<>(Double.MIN_VALUE, Double.MAX_VALUE, 0.00, 1.00);
    private final DecimalFormat df = new DecimalFormat("#.##");

    @FXML
    Button saveButton = new Button();

    @FXML
    private TextArea txf_notes = new TextArea();

    SpinnerValueFactory<Double> amountValueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(Double.MAX_VALUE*(-1), Double.MAX_VALUE);
    SpinnerValueFactory<Integer> loadTransactionValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(-1, Integer.MAX_VALUE);

    /*
     * @FXML private TableColumn<Program, String> programName;
     *
     * @FXML private TableColumn<Program, String> (SimpleStringProperty)
     * programVersion;
     */
    @FXML
    public void initialize(URL location, ResourceBundle resources) {
        initChoiceBoxes();

        this.radioMoneyOutcome.setToggleGroup(toggleGroup);
        this.radioMoneyOutcome.setSelected(true);
        this.radioMoneyIncome.setToggleGroup(toggleGroup);

        transDate.setOnAction(e  -> {
            LocalDate date = transDate.getValue();
        });

        amount.setEditable(true);
        tx_loadTransaction.setEditable(true);

        amountValueFactory.setConverter(new StringConverter<Double>() {
            private final DecimalFormat df = new DecimalFormat("##.##");

            @Override
            public String toString(Double aDouble) {
                // If the specified value is null, return a zero-length String
                return this.df.format(aDouble);
            }

            @Override
            public Double fromString(String s) {
                try {
                    // If the specified value is null or zero-length, return null
                    if (s == null) {
                        return null;
                    }
                    s = s.trim();
                    if (s.length() < 1) {
                        return null;
                    }
                    // Perform the requested parsing
                    return df.parse(s).doubleValue();
                } catch (ParseException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        loadTransactionValueFactory.setConverter(new StringConverter<Integer>() {
            private final DecimalFormat df = new DecimalFormat("#");

            @Override
            public String toString(Integer integer) {
                return this.df.format(integer);
            }

            @Override
            public Integer fromString(String s) {
                try {
                    // If the specified value is null or zero-length, return null
                    if (s == null) {
                        return null;
                    }
                    s = s.trim();
                    if (s.length() < 1) {
                        return null;
                    }
                    // Perform the requested parsing
                    return df.parse(s).intValue();
                } catch (ParseException ex) {
                    throw new RuntimeException(ex);
                }
            }

        });

        amount.setValueFactory(amountValueFactory);
        tx_loadTransaction.setValueFactory(loadTransactionValueFactory);

        amount.valueProperty().addListener(new ChangeListener<Double>() {
            @Override
            public void changed(ObservableValue<? extends Double> observableValue, Double aDouble, Double t1) {

            }
        });

        tx_loadTransaction.valueProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> observableValue, Integer integer, Integer t1) {

            }
        });
        loadTransactionValueFactory.setValue(0);
        amountValueFactory.setValue(0.00);
    }


    private void initChoiceBoxes(){
        ObservableList<String> olTransCategory = FXCollections.observableArrayList();
        ObservableList<String> olPaymentMethod = FXCollections.observableArrayList();
        ObservableList<String> olReceivers = FXCollections.observableArrayList();

        List<TransactionCategory> CategoryList = (List<TransactionCategory>) dc.selectAll(TransactionCategory.class);
        List<PaymentMethod> PaymentMethodList = (List<PaymentMethod>) dc.selectAll(PaymentMethod.class);
        List<Receiver> receiverList = (List<Receiver>) dc.selectAll(Receiver.class);
        receiverList = receiverList.stream().sorted(Comparator.comparing(Receiver::getReceiverName)).collect(Collectors.toList());

        for (TransactionCategory item : CategoryList) {
            olTransCategory.add(item.getCategory());
        }
        for (PaymentMethod paymentMethod : PaymentMethodList) {
            olPaymentMethod.add(paymentMethod.getPaymentMethod());
        }
        for (Receiver receiver : receiverList) {
            olReceivers.add(receiver.getReceiverName());
        }

        transactionCategorieField.setItems(olTransCategory);
        transactionCategorieFieldEntry.setItems(olTransCategory);
        paymentMethodField.setItems(olPaymentMethod);
        tx_receiver.setItems(olReceivers);
        tx_receiver_category_helper.setItems(olReceivers);

    }

    @FXML
    private void updateFilter(){
        TransactionCategory category = (TransactionCategory) dc.findByIdentifier(TransactionCategory.class, "category", transactionCategorieField.getValue().toString());
        ObservableList<String> olReceivers = FXCollections.observableArrayList();

        List<Receiver> receiverList = (List<Receiver>) dc.selectAllByField(Receiver.class, "category_id", category, true);
        receiverList = receiverList.stream().sorted(Comparator.comparing(Receiver::getReceiverName)).collect(Collectors.toList());


        for (Receiver receiver : receiverList) {
            olReceivers.add(receiver.getReceiverName());
        }

        tx_receiver.setItems(olReceivers);
    }

    public void saveNewTransaction(){
        try {

            Date date = Date.from(transDate.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());

            var paymentMethods = dc.findByIdentifier(PaymentMethod.class, "paymentMethod", paymentMethodField.getValue().toString());
            var category = dc.findByIdentifier(TransactionCategory.class, "category", transactionCategorieField.getValue().toString());
            var receiver = dc.findByIdentifier(Receiver.class, "receiverName", tx_receiver.getValue().toString());

            Transaction transaction = new Transaction();
            transaction.setDate(date);
            transaction.setPaymentMethod((PaymentMethod) paymentMethods);
            transaction.setTransactionCategory((TransactionCategory) category);

            if (receiver == null){
                Receiver newreceiver = new Receiver(tx_receiver.getValue().toString());
                newreceiver.setTransactionCategory((TransactionCategory) category);
                dc.persist(newreceiver);
                receiver = newreceiver;
            }

            transaction.setReceiver((Receiver) receiver);
            transaction.setAmount(amount.getValue());
            transaction.setCorrectValue(toggleGroup.getSelectedToggle().toString().equals("radioMoneyIncome"));
            transaction.setNotes(txf_notes.getText());

            if (tx_loadTransaction.getValue() > 0){
                transaction.setTransaction_id(tx_loadTransaction.getValue());
            }

            dc.persist(transaction);

            resetView();
        }catch (Exception e){
            AlertHandler.showErrorAlert("Failed to save transaction");
            e.printStackTrace();
        }


    }

    public void saveNewPaymentMethod(){
        DataController dc = new DataController();
        PaymentMethod newPayment = new PaymentMethod();
        newPayment.setPaymentMethod(tx_newPaymentMethod.getText());
        dc.persist(newPayment);
        initChoiceBoxes();

        AlertHandler.showInfoAlert("New Pament Method: '" +tx_newPaymentMethod.getText()+"' created");
    }

    public void saveNewCategory(){
        TransactionCategory newCategory = new TransactionCategory();
        newCategory.setCategory(tx_newCategory.getText());
        dc.persist(newCategory);
        initChoiceBoxes();
        AlertHandler.showInfoAlert("New Category: '"+ newCategory.getCategory() +"' created");
    }

    public void updateCategoryOfReceiver(){
        Receiver receiver = (Receiver) dc.findByIdentifier(Receiver.class, "receiverName", tx_receiver_category_helper.getValue());
        var category = dc.findByIdentifier(TransactionCategory.class, "category", transactionCategorieFieldEntry.getValue().toString());
        if (receiver == null){
            receiver = new Receiver(tx_receiver_category_helper.getValue());
        }
        receiver.setTransactionCategory((TransactionCategory) category);
        dc.persist(receiver);
    }

    public void resetView(){
        tx_newPaymentMethod.setText("");
        tx_newCategory.setText("");
        radioMoneyOutcome.setSelected(true);
        radioMoneyIncome.setSelected(false);
        txf_notes.setText("");

    }

    public Stage getStage(){
        return (Stage) this.transDate.getScene().getWindow();
    }

    public void loadTransaction(){
        int transactionId = tx_loadTransaction.getValue();

        try{
            Transaction transaction = (Transaction) dc.findByIdentifier(Transaction.class, "transaction_id", String.valueOf(transactionId));
            transDate.setValue(Instant.ofEpochMilli(transaction.getDate().getTime()).atZone(ZoneId.systemDefault()).toLocalDate());
            transactionCategorieField.setValue(transaction.getTransactionCategory().getCategory());
            paymentMethodField.setValue(transaction.getPaymentMethod().getPaymentMethod());
            tx_receiver.setValue(transaction.getReceiver().getReceiverName());
            amountValueFactory.setValue(transaction.getAmount());
            txf_notes.setText(transaction.getNotes());
        }catch (Exception exception){
            AlertHandler.showErrorAlert("Failed to load transaction. Try different ID.");
            exception.printStackTrace();
        }


    }

    public void deleteTransaction(){
        int transactionId = tx_loadTransaction.getValue();

        boolean choice = AlertHandler.getChoiceOfChoiceBox("Wirklich löschen?");

        if (choice){
            if (dc.deleteById(transactionId) > 0){
                AlertHandler.showInfoAlert("Successfully deleted Transaction with the ID: " + transactionId);
            }else {
                AlertHandler.showErrorAlert("Failed to delete Transaction with the ID: " + transactionId);
            }
        }


    }





}
