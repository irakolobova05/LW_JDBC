import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class DatabaseManager {
    private static final String URL = "jdbc:postgresql://localhost:5432/";
    private static final String USER = "postgres";
    private static final String PASSWORD = "3103";

    private static String currentUserRole = null; // Текущая роль пользователя

    // Установка роли пользователя
    public static void setUserRole(String role) {
        currentUserRole = role;
    }

    // Проверка прав доступа
    private static void checkAdminAccess() throws SQLException {
        if (!"admin".equals(currentUserRole)) {
            throw new SQLException("Доступ запрещен: требуется роль администратора.");
        }
    }

    // Функция создания базы данных
    public static void createDatabase() throws SQLException{
        checkAdminAccess();
        try (Connection conn = DriverManager.getConnection(URL + "postgres", USER, PASSWORD);
             Statement stmt = conn.createStatement()) {

            String checkDbSQL = "SELECT 1 FROM pg_database WHERE datname = 'mydatabase';";
            ResultSet rs = stmt.executeQuery(checkDbSQL);

            if (!rs.next()) {
                // Если база данных не существует, создаем её
                String createDbSQL = "CREATE DATABASE mydatabase;";
                stmt.executeUpdate(createDbSQL);
                System.out.println("Database created successfully.");
            } else {
                System.out.println("Database already exists.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Функция создания таблицы
    public static void createTable() throws SQLException{
        checkAdminAccess();
        try (Connection conn = DriverManager.getConnection(URL + "mydatabase", USER, PASSWORD);
             Statement stmt = conn.createStatement()) {

            String function = "CREATE OR REPLACE FUNCTION create_table(t_name TEXT) RETURNS TEXT AS $$\n" +
                    "BEGIN\n" +
                    "    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = create_table.t_name) THEN\n" +
                    "        EXECUTE format('CREATE TABLE %I (id SERIAL PRIMARY KEY, name_app VARCHAR(255) NOT NULL, date DATE NOT NULL, status BOOLEAN)', create_table.t_name);\n" +
                    "        RETURN 'Table created successfully.';\n" +
                    "    ELSE\n" +
                    "        RETURN 'Table already exists.';\n" +
                    "    END IF;\n" +
                    "END;\n" +
                    "$$ LANGUAGE plpgsql;";
            stmt.execute(function);

            String callFunctionSQL = "SELECT create_table('tasks');";
            try (ResultSet rs = stmt.executeQuery(callFunctionSQL)) {
                if (rs.next()) {
                    System.out.println(rs.getString(1));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // Функция удаления базы данных
    public static void deleteDatabase() throws SQLException{
        checkAdminAccess();
        try (Connection conn = DriverManager.getConnection(URL + "postgres", USER, PASSWORD);
             Statement stmt = conn.createStatement()) {

            String function = "DROP DATABASE IF EXISTS mydatabase;";
            stmt.execute(function);

            try (Connection checkConn = DriverManager.getConnection(URL + "postgres", USER, PASSWORD)) {
                DriverManager.getConnection(URL + "mydatabase", USER, PASSWORD);
                System.out.println("Ошибка: база данных не была удалена.");
            } catch (SQLException e) {
                System.out.println("База данных успешно удалена.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error while delete table.");
        }
    }

    // Функция очистки таблицы
    public static void clearTable() throws SQLException{
        checkAdminAccess();
        try (Connection conn = DriverManager.getConnection(URL + "mydatabase", USER, PASSWORD);
             Statement stmt = conn.createStatement()) {

            String function = "CREATE OR REPLACE FUNCTION clear_table(table_name TEXT) RETURNS VOID AS $$\n" +
                    "BEGIN\n" +
                    "    EXECUTE format('DELETE FROM %I', table_name);\n" +
                    "END;\n" +
                    "$$ LANGUAGE plpgsql;";
            stmt.execute(function);

            String callFunctionSQL = "SELECT clear_table('tasks');";
            stmt.execute(callFunctionSQL);

            String checkCountSQL = "SELECT COUNT(*) FROM tasks";  // Подсчитываем количество строк в таблице
            ResultSet rs = stmt.executeQuery(checkCountSQL);

            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("Table cleared successfully.");
            } else {
                System.out.println("Table not cleared or there was an error.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error while clearing table.");
        }
    }

    // Функция добавления данных
    public static void addNewData(int new_id, String new_name_app, LocalDate new_date, boolean new_status) throws SQLException{
        checkAdminAccess();
        try (Connection conn = DriverManager.getConnection(URL + "mydatabase", USER, PASSWORD);
             Statement stmt = conn.createStatement()) {

            String function = "CREATE OR REPLACE FUNCTION add_new_data(table_name TEXT, id INTEGER, name_app VARCHAR, date DATE, status BOOLEAN) RETURNS VOID AS $$\n" +
                        "BEGIN\n" +
                        "    EXECUTE format('INSERT INTO %I (id, name_app, date, status) VALUES ($1, $2, $3, $4) ON CONFLICT (id) DO NOTHING', table_name)\n" +
                        "    USING id, name_app, date, status;\n" +
                        "END;\n" +
                        "$$ LANGUAGE plpgsql;";
            stmt.execute(function);

            String callFunctionSQL = "SELECT add_new_data('tasks', " + new_id + ", '" + new_name_app + "', '" + new_date + "', " + new_status + ");";
            stmt.execute(callFunctionSQL);
            System.out.println("Data added successfully");

        } catch (SQLException e) {
            System.err.println("Ошибка при добавлении новой записи: " + e.getMessage());
        }
    }

    // Функция поиска по имени
    public static List<Object[]> searchByName(String new_name) {
        List<Object[]> result = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(URL + "mydatabase", USER, PASSWORD);
             Statement stmt = conn.createStatement()) {

            String function = "CREATE OR REPLACE FUNCTION search_name(table_name TEXT, s_name VARCHAR) " +
                    "RETURNS TABLE (id INT, name_app VARCHAR, date DATE, status BOOLEAN) AS $$\n" +
                    "BEGIN\n" +
                    "    RETURN QUERY EXECUTE format('SELECT * FROM %I WHERE name_app = $1', table_name)\n" +
                    "    USING s_name;\n" +
                    "END;\n" +
                    "$$ LANGUAGE plpgsql;";
            stmt.execute(function);

            // Вызов функции
            String callFunctionSQL = "SELECT * FROM search_name('tasks', '" + new_name + "');";
            try (ResultSet rs = stmt.executeQuery(callFunctionSQL)) {
                while (rs.next()) {
                    Object[] row = {
                            rs.getInt("id"),
                            rs.getString("name_app"),
                            rs.getDate("date"),
                            rs.getBoolean("status")
                    };
                    result.add(row);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }


    // Функция обновления данных
    public static void updateData(int new_id, String new_name_app, LocalDate new_date, boolean new_status) throws SQLException{
        checkAdminAccess();
        try (Connection conn = DriverManager.getConnection(URL + "mydatabase", USER, PASSWORD);
             Statement stmt = conn.createStatement()) {

            String function = "CREATE OR REPLACE FUNCTION update_data(table_name TEXT, id INTEGER, name_app VARCHAR, date DATE, status BOOLEAN) RETURNS VOID AS $$\n" +
                    "BEGIN\n" +
                    "    EXECUTE format('UPDATE %I SET name_app = $2, date = $3, status = $4 WHERE id = $1', table_name)\n" +
                    "    USING id, name_app, date, status;\n" +
                    "END;\n" +
                    "$$ LANGUAGE plpgsql;";
            stmt.execute(function);

            String callFunctionSQL = "SELECT update_data('tasks', " + new_id + ", '" + new_name_app + "', '" + new_date + "', " + new_status + ");";
            stmt.execute(callFunctionSQL);
            System.out.println("Data updated successfully");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Функция удаления по имени
    public static void deleteByName(String del_name) throws SQLException{
        checkAdminAccess();
        try (Connection conn = DriverManager.getConnection(URL + "mydatabase", USER, PASSWORD);
             Statement stmt = conn.createStatement()) {

            String function = "CREATE OR REPLACE FUNCTION delete_name(table_name TEXT, del_name VARCHAR) RETURNS VOID AS $$\n" +
                    "BEGIN\n" +
                    "    EXECUTE format('DELETE FROM %I WHERE name_app = $1', table_name)\n" +
                    "    USING del_name;\n" +
                    "END;\n" +
                    "$$ LANGUAGE plpgsql;";
            stmt.execute(function);

            String callFunctionSQL = "SELECT delete_name('tasks', '" + del_name + "');";
            stmt.execute(callFunctionSQL);
            System.out.println("Data deleted successfully");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
