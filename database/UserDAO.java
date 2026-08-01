import java.util.List;

/**
 * UserDAO — the CONTRACT for how the backend interacts with user data.
 *
 * Two critical methods here: register (hash password, store new user)
 * and login (fetch by email, verify hash). The backend never touches
 * plain-text passwords directly after the initial register/login call.
 */
public interface UserDAO {
    /**
     * Register a new user. The plain-text password is hashed with BCrypt
     * before storage; the password_hash column gets the hash, never the
     * plain text. Returns the newly created User (with id), or null if
     * the email is already taken.
     */
    User register(String name, String email, String plainPassword, String role);

    /**
     * Login: fetch the user by email, then verify the plain-text password
     * against the stored BCrypt hash. Returns the User object if successful,
     * or null if the email doesn't exist or the password is wrong.
     */
    User login(String email, String plainPassword);

    /**
     * Fetch a user by their ID.
     */
    User getUserById(int id);

    /**
     * Fetch a user by their email (useful for "forgot password" flows, etc).
     */
    User getUserByEmail(String email);

    /**
     * Get all users (admin-only operation in a real app).
     */
    List<User> getAllUsers();

    /**
     * Update a user's role (e.g. promoting a customer to admin).
     */
    boolean updateRole(int userId, String newRole);

    /**
     * Delete a user by ID.
     */
    boolean deleteUser(int id);
}
