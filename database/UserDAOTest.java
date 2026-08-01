/**
 * Quick manual test for UserDAO — confirms register/login work and that
 * passwords are stored hashed, not plain text. Uses a unique email each
 * run (timestamp-based) so it doesn't collide with the UNIQUE constraint.
 */
public class UserDAOTest {
    public static void main(String[] args) {
        UserDAO userDAO = new UserDAOImpl();

        String email = "test+" + System.currentTimeMillis() + "@shopease.com";
        String password = "S3cret!pass";

        System.out.println("=== Register ===");
        User registered = userDAO.register("Test User", email, password, "customer");
        System.out.println("Registered: " + registered);
        System.out.println("Stored hash (NOT plain text): " + registered.getPasswordHash());

        System.out.println("\n=== Login with correct password ===");
        User ok = userDAO.login(email, password);
        System.out.println("Login result: " + ok);

        System.out.println("\n=== Login with WRONG password (should fail) ===");
        User bad = userDAO.login(email, "wrongpassword");
        System.out.println("Login result: " + bad);

        System.out.println("\n=== Duplicate register (should fail) ===");
        User dup = userDAO.register("Someone Else", email, "another", "customer");
        System.out.println("Duplicate result: " + dup);
    }
}
