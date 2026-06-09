package com.mybank.gui;

import com.mybank.domain.Bank;
import com.mybank.domain.CheckingAccount;
import com.mybank.domain.Customer;
import com.mybank.domain.SavingsAccount;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Графічний інтерфейс банківської системи MyBank на основі JavaFX.
 *
 * Функціонал:
 *  - вибір клієнта зі списку (ComboBox)
 *  - кнопка Show — відображає всі рахунки обраного клієнта
 *  - кнопка Report — загальний звіт по всіх клієнтах
 *  - кнопка "?" — діалог "Про програму" (Alert)
 *
 * Дані завантажуються з файлу data/test.dat.
 *
 * @author Maksym
 */
public class FXDemo extends Application {

    // Текстовий вузол для відображення імені обраного клієнта
    private Text title;

    // Текстовий вузол для відображення деталей рахунків клієнта
    private Text details;

    // Випадаючий список з іменами клієнтів банку
    private ComboBox<String> clients;

    // -----------------------------------------------------------------------
    // Точка входу JavaFX Application
    // -----------------------------------------------------------------------

    /**
     * Головний метод JavaFX — будує та відображає головну сцену.
     *
     * @param primaryStage головна сцена (вікно) програми
     */
    @Override
    public void start(Stage primaryStage) {
        // Кореневий макет: верхня панель + ліва панель з деталями
        BorderPane border = new BorderPane();
        HBox hbox = addHBox();
        border.setTop(hbox);
        border.setLeft(addVBox());
        // Додаємо кнопку "?" у правий кінець верхньої панелі
        addStackPane(hbox);

        // Сцена з фіксованими розмірами
        Scene scene = new Scene(border, 400, 300);

        primaryStage.setTitle("MyBank Clients");
        primaryStage.setResizable(false); // Заборона зміни розміру вікна
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // -----------------------------------------------------------------------
    // Побудова лівої панелі з деталями клієнта
    // -----------------------------------------------------------------------

    /**
     * Створює ліву панель (VBox) для відображення інформації про клієнта.
     * Містить: ім'я клієнта (заголовок), роздільну лінію та текст рахунків.
     *
     * @return налаштований VBox
     */
    public VBox addVBox() {
        VBox vbox = new VBox();
        vbox.setPadding(new Insets(10));
        vbox.setSpacing(8);

        // Заголовок — ім'я клієнта (оновлюється після натискання Show / Report)
        title = new Text("Оберіть клієнта");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        vbox.getChildren().add(title);

        // Горизонтальний роздільник
        Line separator = new Line(10, 10, 370, 10);
        separator.setStroke(Color.LIGHTGRAY);
        vbox.getChildren().add(separator);

        // Блок деталей рахунків (оновлюється динамічно)
        details = new Text("Натисніть Show для перегляду\nінформації про клієнта.");
        details.setFont(Font.font("Arial", FontWeight.NORMAL, 13));
        vbox.getChildren().add(details);

        return vbox;
    }

    // -----------------------------------------------------------------------
    // Побудова верхньої панелі керування
    // -----------------------------------------------------------------------

    /**
     * Створює верхню панель (HBox) з ComboBox, кнопками Show та Report.
     * Фон панелі — синій (#336699), як у оригінальному стартовому коді.
     *
     * @return налаштований HBox
     */
    public HBox addHBox() {
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(15, 12, 15, 12));
        hbox.setSpacing(10);
        hbox.setStyle("-fx-background-color: #336699;");

        // Заповнюємо список іменами клієнтів із банку
        ObservableList<String> items = FXCollections.observableArrayList();
        for (int i = 0; i < Bank.getNumberOfCustomers(); i++) {
            items.add(Bank.getCustomer(i).getLastName() + ", "
                    + Bank.getCustomer(i).getFirstName());
        }

        // ComboBox для вибору клієнта
        clients = new ComboBox<>(items);
        clients.setPrefSize(160, 20);
        clients.setPromptText("Оберіть клієнта...");

        // ---- Кнопка Show ----
        Button buttonShow = new Button("Show");
        buttonShow.setPrefSize(80, 20);

        // Обробник події кнопки Show — відображення всіх рахунків обраного клієнта
        buttonShow.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    // Отримуємо індекс обраного клієнта
                    int custNo = clients.getItems().indexOf(clients.getValue());
                    if (custNo < 0) {
                        throw new Exception("Спочатку оберіть клієнта зі списку!");
                    }

                    Customer c = Bank.getCustomer(custNo);

                    // Встановлюємо заголовок: ім'я та прізвище клієнта
                    title.setText(c.getLastName() + ", " + c.getFirstName());

                    // Формуємо рядок з інформацією про всі рахунки клієнта
                    StringBuilder sb = new StringBuilder();
                    for (int j = 0; j < c.getNumberOfAccounts(); j++) {
                        // Визначаємо тип рахунку: чековий або ощадний
                        String accType = c.getAccount(j) instanceof CheckingAccount
                                ? "Checking" : "Savings";
                        sb.append("Рахунок #").append(j + 1)
                          .append("  Тип: ").append(accType)
                          .append("  Баланс: $")
                          .append(String.format("%.2f", c.getAccount(j).getBalance()))
                          .append("\n");
                    }

                    // Оновлюємо текстовий блок деталей
                    details.setText(sb.toString().trim());

                } catch (Exception e) {
                    // Показуємо діалог помилки, якщо клієнт не обраний
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Помилка");
                    alert.setHeaderText(null);
                    String msg = e.getMessage() != null
                            ? e.getMessage() : "Спочатку оберіть клієнта!";
                    alert.setContentText(msg);
                    alert.showAndWait();
                }
            }
        });

        // ---- Кнопка Report ----
        Button buttonReport = new Button("Report");
        buttonReport.setPrefSize(80, 20);

        // Обробник події кнопки Report — виводить зведений звіт по всіх клієнтах
        buttonReport.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                // Встановлюємо заголовок звіту
                title.setText("Звіт за клієнтами");

                StringBuilder sb = new StringBuilder();
                double totalBalance = 0; // Загальний баланс по всіх рахунках

                // Перебираємо всіх клієнтів банку
                for (int i = 0; i < Bank.getNumberOfCustomers(); i++) {
                    Customer c = Bank.getCustomer(i);
                    sb.append(c.getLastName()).append(", ").append(c.getFirstName())
                      .append("\n");

                    // Перебираємо всі рахунки клієнта
                    for (int j = 0; j < c.getNumberOfAccounts(); j++) {
                        String accType = c.getAccount(j) instanceof CheckingAccount
                                ? "Checking" : "Savings";
                        double bal = c.getAccount(j).getBalance();
                        totalBalance += bal;

                        sb.append("  [").append(accType).append("] $")
                          .append(String.format("%.2f", bal)).append("\n");
                    }
                    sb.append("\n");
                }

                // Підсумковий рядок — загальний баланс
                sb.append("─────────────────────\n");
                sb.append("Загальний баланс: $")
                  .append(String.format("%.2f", totalBalance));

                // Відображаємо звіт у текстовому блоці
                details.setText(sb.toString());
            }
        });

        // Додаємо всі елементи до панелі
        hbox.getChildren().addAll(clients, buttonShow, buttonReport);

        return hbox;
    }

    // -----------------------------------------------------------------------
    // Кнопка "?" (About) у верхній панелі
    // -----------------------------------------------------------------------

    /**
     * Додає кнопку "?" у правий кінець верхньої панелі HBox.
     * При натисканні показує діалог "Про програму" (Alert).
     *
     * @param hb верхня панель HBox, до якої додається кнопка
     */
    public void addStackPane(HBox hb) {
        StackPane stack = new StackPane();

        // Фон кнопки — градієнт (збережено зі стартового коду)
        Rectangle helpIcon = new Rectangle(30.0, 25.0);
        helpIcon.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop[]{
                    new Stop(0,   Color.web("#4977A3")),
                    new Stop(0.5, Color.web("#B0C6DA")),
                    new Stop(1,   Color.web("#9CB6CF")),
                }));
        helpIcon.setStroke(Color.web("#D0E6FA"));
        helpIcon.setArcHeight(3.5);
        helpIcon.setArcWidth(3.5);

        // Знак "?" на кнопці
        Text helpText = new Text("?");
        helpText.setFont(Font.font("Verdana", FontWeight.BOLD, 18));
        helpText.setFill(Color.WHITE);
        helpText.setStroke(Color.web("#7080A0"));

        // Обробник кліку на прямокутник-іконку
        helpIcon.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                showAboutInfo();
            }
        });

        // Обробник кліку на знак "?"
        helpText.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                showAboutInfo();
            }
        });

        stack.getChildren().addAll(helpIcon, helpText);
        stack.setAlignment(Pos.CENTER_RIGHT);
        StackPane.setMargin(helpText, new Insets(0, 10, 0, 0));

        // Додаємо StackPane до HBox; виштовхуємо вправо
        hb.getChildren().add(stack);
        HBox.setHgrow(stack, Priority.ALWAYS);
    }

    /**
     * Відображає діалогове вікно "Про програму" за допомогою JavaFX Alert.
     */
    private void showAboutInfo() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Про програму");
        alert.setHeaderText(null);
        alert.setContentText(
                "MyBank GUI — Лабораторна робота №5\n\n"
                + "Автор: Maksym\n"
                + "Технологія: JavaFX\n"
                + "Дані: завантажуються з файлу data/test.dat\n\n"
                + "Курс: Об'єктно-орієнтоване програмування (Java)"
        );
        alert.showAndWait();
    }

    // -----------------------------------------------------------------------
    // Завантаження даних з файлу test.dat
    // -----------------------------------------------------------------------

    /**
     * Зчитує дані клієнтів банку з текстового файлу.
     *
     * Формат файлу:
     *   перший рядок — кількість клієнтів
     *   далі блоки: Ім'я Прізвище КількістьРахунків
     *   потім рядки рахунків: тип(S/C) баланс додатковий_параметр
     *
     * @param filename шлях до файлу з даними
     */
    private static void loadData(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {

            // Зчитуємо кількість клієнтів з першого рядка
            int numCustomers = Integer.parseInt(br.readLine().trim());

            for (int i = 0; i < numCustomers; i++) {

                // Пропускаємо порожні рядки між блоками клієнтів
                String line = br.readLine();
                while (line != null && line.trim().isEmpty()) {
                    line = br.readLine();
                }
                if (line == null) break;

                // Розбираємо рядок: ім'я, прізвище, кількість рахунків
                String[] parts = line.trim().split("\\s+");
                String firstName   = parts[0];
                String lastName    = parts[1];
                int    numAccounts = Integer.parseInt(parts[2]);

                // Додаємо клієнта до банку
                Bank.addCustomer(firstName, lastName);
                int custIdx = Bank.getNumberOfCustomers() - 1;

                // Зчитуємо рахунки клієнта
                for (int j = 0; j < numAccounts; j++) {
                    String accLine = br.readLine();
                    while (accLine != null && accLine.trim().isEmpty()) {
                        accLine = br.readLine();
                    }
                    if (accLine == null) break;

                    String[] ap      = accLine.trim().split("\\s+");
                    String   accType = ap[0];                       // S або C
                    double   balance = Double.parseDouble(ap[1]);
                    double   extra   = Double.parseDouble(ap[2]);   // ставка або ліміт

                    if ("S".equals(accType)) {
                        // Ощадний рахунок: баланс + відсоткова ставка
                        Bank.getCustomer(custIdx).addAccount(
                                new SavingsAccount(balance, extra));
                    } else if ("C".equals(accType)) {
                        // Чековий рахунок: баланс + ліміт овердрафту
                        Bank.getCustomer(custIdx).addAccount(
                                new CheckingAccount(balance, extra));
                    }
                }
            }

        } catch (IOException ex) {
            // Виводимо повідомлення про помилку читання файлу
            System.err.println("Помилка читання файлу даних: " + ex.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Точка входу програми
    // -----------------------------------------------------------------------

    /**
     * Головний метод: завантажує дані з файлу та запускає JavaFX-додаток.
     *
     * @param args аргументи командного рядка (не використовуються)
     */
    public static void main(String[] args) {
        // Завантажуємо клієнтів із файлу test.dat перед запуском GUI
        loadData("./data/test.dat");

        // Запускаємо JavaFX-додаток
        launch(args);
    }
}
