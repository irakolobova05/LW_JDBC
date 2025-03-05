import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Vector;

public class DatabaseGUI extends JFrame {
    private JTextArea textArea;
    private JTextField inputId, inputName, inputDate, inputStatus;
    private DefaultTableModel tableModel;
    private JTable table;
    JFrame frame;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            new DatabaseGUI().setVisible(true);
        });
    }

    public DatabaseGUI() {
        frame = new JFrame("Database GUI");
        setBounds(100, 100, 1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        showLoginDialog();
    }

    private void initializeGUI(String role) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 4, 10, 10)); // 2 ряда по 4 кнопки в каждом
        getContentPane().add(BorderLayout.NORTH, panel);

        String[] columnNames = {"ID", "Название", "Дата", "Статус"};
        tableModel = new DefaultTableModel(columnNames, 0);
        tableModel.setColumnIdentifiers(columnNames);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(700, 300));
        add(scrollPane, BorderLayout.EAST);

        loadDataFromDatabase();

        // Кнопки для взаимодействия

        if ("admin".equals(role)) {

            addButton(panel, "Создать БД", e -> {
                try {
                    DatabaseManager.createDatabase();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                }

            });

            addButton(panel, "Создать таблицу", e -> {
                try {
                    DatabaseManager.createTable();
                    loadDataFromDatabase();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            });

            addButton(panel, "Удалить БД", e -> {
                try {
                    DatabaseManager.deleteDatabase();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            });

            addButton(panel, "Очистить БД", e -> {
                try {
                    DatabaseManager.clearTable();
                    loadDataFromDatabase();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            });

            addButton(panel, "Удалить по имени", e -> {
                String userInput = JOptionPane.showInputDialog(
                        frame,
                        "Введите значение:",
                        "Удаление по имени",
                        JOptionPane.QUESTION_MESSAGE
                );

                if (userInput != null && !userInput.trim().isEmpty()) {
                    try {
                        DatabaseManager.deleteByName(userInput);
                        loadDataFromDatabase();
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(this, ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }

                } else {
                    JOptionPane.showMessageDialog(
                            frame,
                            "Вы ничего не ввели!",
                            "Ошибка",
                            JOptionPane.WARNING_MESSAGE
                    );
                }
            });

            // Панель для ввода данных новой записи
            JPanel inputPanel = new JPanel();
            inputPanel.setLayout(new GridLayout(5, 2, 5, 5)); // Сетка для ввода данных
            getContentPane().add(inputPanel, BorderLayout.CENTER);

            inputPanel.add(new JLabel("ID:"));
            inputId = new JTextField();
            inputId.setPreferredSize(new Dimension(150, 25)); // Устанавливаем размер поля ввода
            inputPanel.add(inputId);

            inputPanel.add(new JLabel("Название:"));
            inputName = new JTextField();
            inputName.setPreferredSize(new Dimension(150, 25)); // Устанавливаем размер поля ввода
            inputPanel.add(inputName);

            inputPanel.add(new JLabel("Дата (dd.MM.yyyy):"));
            inputDate = new JTextField();
            inputDate.setPreferredSize(new Dimension(150, 25)); // Устанавливаем размер поля ввода
            inputPanel.add(inputDate);

            inputPanel.add(new JLabel("Статус (true/false):"));
            inputStatus = new JTextField();
            inputStatus.setPreferredSize(new Dimension(150, 25)); // Устанавливаем размер поля ввода
            inputPanel.add(inputStatus);

            // Кнопка для добавления новой записи
            JButton btnAddRecord = new JButton("Добавить запись");
            btnAddRecord.addActionListener(e -> {
                try {
                    String idText = inputId.getText();
                    String name = inputName.getText();
                    String dateText = inputDate.getText();
                    String statusText = inputStatus.getText();

                    // Проверка ID
                    if (idText.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Поле ID не может быть пустым!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    int id;
                    try {
                        id = Integer.parseInt(idText); // Попытка преобразовать ID в целое число
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "ID должен быть числом!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Проверка имени
                    if (name.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Поле 'Имя' не может быть пустым!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Проверка даты
                    LocalDate date;
                    try {
                        date = LocalDate.parse(dateText, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Неверный формат даты! Используйте формат dd.MM.yyyy", "Ошибка", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Проверка статуса
                    boolean status;
                    if (statusText.equalsIgnoreCase("true")) {
                        status = true;
                    } else if (statusText.equalsIgnoreCase("false")) {
                        status = false;
                    } else {
                        JOptionPane.showMessageDialog(this, "Поле 'Статус' должно быть 'true' или 'false'!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                        return;
                    }


                    DatabaseManager.addNewData(id, name, date, status);
                    loadDataFromDatabase();

                    // Очищаем текстовые поля после добавления
                    inputId.setText("");
                    inputName.setText("");
                    inputDate.setText("");
                    inputStatus.setText("");




                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Ошибка при добавлении записи: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            });

            inputPanel.add(btnAddRecord);

            JButton btnUpdate = new JButton("Редактировать запись");
            btnUpdate.addActionListener(e -> {
                try {
                    String idText = inputId.getText();
                    String name = inputName.getText();
                    String dateText = inputDate.getText();
                    String statusText = inputStatus.getText();

                    // Проверка ID
                    if (idText.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Поле ID не может быть пустым!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    int new_id;
                    try {
                        new_id = Integer.parseInt(idText); // Попытка преобразовать ID в целое число
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "ID должен быть числом!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Проверка имени
                    if (name.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Поле 'Имя' не может быть пустым!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Проверка даты
                    LocalDate date;
                    try {
                        date = LocalDate.parse(dateText, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Неверный формат даты! Используйте формат dd.MM.yyyy", "Ошибка", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Проверка статуса
                    boolean status;
                    if (statusText.equalsIgnoreCase("true")) {
                        status = true;
                    } else if (statusText.equalsIgnoreCase("false")) {
                        status = false;
                    } else {
                        JOptionPane.showMessageDialog(this, "Поле 'Статус' должно быть 'true' или 'false'!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    DatabaseManager.updateData(new_id, name, date, status); // Вызов метода редактирования данных
                    loadDataFromDatabase();

                    // Очищаем текстовые поля после добавления
                    inputId.setText("");
                    inputName.setText("");
                    inputDate.setText("");
                    inputStatus.setText("");


                } catch (Exception ex) {
                    textArea.append("Ошибка при редактировании записи: " + ex.getMessage() + "\n");
                }
            });
            inputPanel.add(btnUpdate);
        }

        addButton(panel, "Поиск по имени", e -> {
            String userInput = JOptionPane.showInputDialog(
                    frame,
                    "Введите значение:",
                    "Поиск по имени",
                    JOptionPane.QUESTION_MESSAGE
            );

            if (userInput != null && !userInput.trim().isEmpty()) {
                List<Object[]> res = DatabaseManager.searchByName(userInput);
                updateTable(res);

            } else {
                JOptionPane.showMessageDialog(
                        frame,
                        "Вы ничего не ввели!",
                        "Ошибка",
                        JOptionPane.WARNING_MESSAGE
                );
            }
        });
        pack();
    }


    // Метод для добавления кнопки в панель
    private void addButton(JPanel panel, String text, ActionListener actionListener) {
        JButton button = new JButton(text);
        button.addActionListener(actionListener);
        panel.add(button);
    }

    private void loadDataFromDatabase() {
        // Очищаем таблицу перед загрузкой новых данных
        tableModel.setRowCount(0);


        // Подключение к базе данных и выполнение запроса
        String url = "jdbc:postgresql://localhost:5432/mydatabase";
        String user = "postgres";
        String password = "3103";

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            // Выполняем SQL-запрос
            String query = "SELECT * FROM tasks"; // Замените на ваш запрос
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                Vector<Object> row = new Vector<>();
                row.add(resultSet.getInt("id")); // ID
                row.add(resultSet.getString("name_app")); // Название
                row.add(resultSet.getDate("date")); // Дата
                row.add(resultSet.getBoolean("status")); // Статус
                tableModel.addRow(row);
            }

            // Закрываем ресурсы
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ошибка при загрузке данных: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTable(List<Object[]> data) {
        tableModel.setRowCount(0);

        for (Object[] row : data) {
            tableModel.addRow(row);
        }
    }

    private void showLoginDialog() {
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        Object[] message = {
                "Логин:", usernameField,
                "Пароль:", passwordField
        };
        int option = JOptionPane.showConfirmDialog(this, message, "Аутентификация", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String role = Authentication.authenticate(username, password);

            if (role != null) {
                DatabaseManager.setUserRole(role);
                initializeGUI(role);
            } else {
                JOptionPane.showMessageDialog(this, "Неверные учетные данные!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                showLoginDialog(); // Повторная попытка
            }
        } else {
            System.exit(0); // Выход, если пользователь нажал "Отмена"
        }

    }
}
