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
import main.java.model.BudgetProgressElement;
import main.java.model.TransactionCategory;
import org.sqlite.date.DateFormatUtils;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

public class BudgetOverviewController extends AbstractViewController implements Initializable {

    int MONTH_IN_SECONDS = 2629800;

    @FXML
    ScrollBar scrollBarMonth;

    @FXML
    Pane mainPane = new Pane();

    @FXML
    Label labelMonth, labelMain, labelBudgetForm = new Label();

    @FXML
    Button saveBudgetButton, decreaseMonth, increaseMonth = new Button();

    ObservableList<TransactionCategory> allCategories = null;

    @FXML
    ChoiceBox<String> budgetSelect;

    @FXML
    Spinner<Double> budgetSpinner = new Spinner<Double>(Integer.MIN_VALUE, Integer.MAX_VALUE, 1, 1);

    public int dateOffset;

    public Date displayedDate;

    public BudgetOverviewController(){
        if (this.displayedDate == null){
            this.displayedDate = new Date();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        initBudgetOverview();
    }

    private void initBudgetOverview(){
        setupBudgetForm();
        this.allCategories = (ObservableList<TransactionCategory>) new DataController().selectAll(TransactionCategory.class);
        mainPane = (Pane) scrollBarMonth.getParent();
        ObservableList<ProgressBar> progressList = FXCollections.observableArrayList();
        labelMonth.setText(getDisplayMonthText());
        int offsetCounter = 0;
        try {
            BudgetProgressElement bpe0 = new BudgetProgressElement(
                    "Total",
                    this.allCategories.stream().mapToDouble(TransactionCategory::getBudget).sum(),
                    this.allCategories.stream().mapToDouble(category -> {
                        try {
                            return category.getBudgetOfMonth(getDisplayedDate());
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        return 0;
                    }).sum(),
                    //this.allCategories.get(0).getBudgetOfMonth(new Date()),
                    offsetCounter
            );
            bpe0.addToPane(mainPane);
            offsetCounter++;

            for ( var category : this.allCategories) {

                if (category.getCategory_id() <= 5){ //only get expenditure categories
                    continue;
                }
                if (category.getBudget() == 0){
                    continue;
                }
                BudgetProgressElement bpe = new BudgetProgressElement(
                        category.getCategory(),
                        category.getBudget(),
                        category.getBudgetOfMonth(getDisplayedDate()),
                        offsetCounter
                );

                bpe.addToPane(mainPane);

                offsetCounter += 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            AlertHandler.showErrorAlert("unable to ");
        }
    }

    @Override
    public Stage getStage() {
        return (Stage) this.mainPane.getScene().getWindow();
    }


    private void setupBudgetForm(){
        SpinnerValueFactory<Double> budgetSpinnerValueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(Double.MAX_VALUE*(-1), Double.MAX_VALUE);
        budgetSpinner.setEditable(true);

        budgetSpinnerValueFactory.setConverter(new StringConverter<Double>() {
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
        budgetSpinner.setValueFactory(budgetSpinnerValueFactory);
        budgetSpinner.valueProperty().addListener(new ChangeListener<Double>() {
            @Override
            public void changed(ObservableValue<? extends Double> observableValue, Double aDouble, Double t1) {

            }
        });
        budgetSpinnerValueFactory.setValue(0.0);

        ObservableList<String> olTransCategory = FXCollections.observableArrayList();
        DataController dc = new DataController();
        List<TransactionCategory> CategoryList = (List<TransactionCategory>) dc.selectAll(TransactionCategory.class);
        for (Object item : CategoryList) {
            TransactionCategory thisItem = (TransactionCategory)item;
            olTransCategory.add(thisItem.getCategory());
        }
        budgetSelect.setItems(olTransCategory);
    }

    public void saveNewBudget() {
        DataController dc = new DataController();
        TransactionCategory category = (TransactionCategory) dc.findByIdentifier(TransactionCategory.class, "category", budgetSelect.getValue().toString());
        category.setBudget(budgetSpinner.getValue());

        dc.persist(category);
        this.loadBudget();
        AlertHandler.showInfoAlert("New Budget set.");

    }



    public void decreaseMonth() throws IOException {
        this.dateOffset--;
        System.err.println(this.dateOffset);
        System.err.println(this.displayedDate);
        refreshView();
        this.initBudgetOverview();
    }
    public void increaseMonth() throws IOException {
        this.dateOffset++;
        System.err.println(this.dateOffset);
        System.err.println(this.displayedDate);
        refreshView();
        this.initBudgetOverview();
    }

    private String getDisplayMonthText(){
        ZoneId timeZone = ZoneId.of("Europe/Berlin"); // TODO: intruduce changeable variable for time zone

        LocalDateTime currentMonthTime = LocalDateTime.ofInstant(this.getDisplayedDateInstant(), timeZone);

        return YearMonth.from(currentMonthTime).toString();
    }

    private Instant getDisplayedDateInstant(){
        return Instant.from(this.displayedDate.toInstant()).plusSeconds((long) this.dateOffset * MONTH_IN_SECONDS);
    }

    private Date getDisplayedDate(){
        return new Date(getDisplayedDateInstant().toEpochMilli());
    }

    private void clearPane(){
        this.mainPane.getChildren().clear();
    }

    private void refreshView(){
        this.clearPane();
        this.mainPane.getChildren().addAll(
                this.labelMain,
                this.scrollBarMonth,
                this.labelMonth,
                this.saveBudgetButton,
                this.budgetSelect,
                this.budgetSpinner,
                this.labelBudgetForm,
                this.decreaseMonth,
                this.increaseMonth
        );
    }
}
