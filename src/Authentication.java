public class Authentication {
    public static final String ADMIN_USER = "admin";
    public static final String ADMIN_PASSWORD = "admin123";
    public static final String GUEST_USER = "guest";
    public static final String GUEST_PASSWORD = "guest123";

    public static String authenticate(String username, String password) {
        if (username.equals(ADMIN_USER) && password.equals(ADMIN_PASSWORD)) {
            return "admin";
        } else if (username.equals(GUEST_USER) && password.equals(GUEST_PASSWORD)) {
            return "guest";
        } else {
            return null; // Неверные учетные данные
        }
    }
}