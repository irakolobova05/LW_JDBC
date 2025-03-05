import javax.swing.*;
class InputDialogExample {
    public static void main(String[] args) {
        // Создаем основное окно
        JFrame frame = new JFrame("Пример всплывающего окна");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        // Кнопка для вызова всплывающего окна
        JButton button = new JButton("Ввести значение");
        button.addActionListener(e -> {
            // Вызов всплывающего окна с полем для ввода
            String userInput = JOptionPane.showInputDialog(
                    frame, // Родительское окно
                    "Введите значение:", // Сообщение
                    "Ввод данных", // Заголовок окна
                    JOptionPane.QUESTION_MESSAGE // Тип сообщения
            );

            // Обработка введенного значения
            if (userInput != null && !userInput.trim().isEmpty()) {
                JOptionPane.showMessageDialog(
                        frame,
                        "Вы ввели: " + userInput,
                        "Результат",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } else {
                JOptionPane.showMessageDialog(
                        frame,
                        "Вы ничего не ввели!",
                        "Ошибка",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        // Добавляем кнопку в основное окно
        frame.add(button);
        frame.setVisible(true);
    }
}