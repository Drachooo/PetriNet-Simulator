package application.logic;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link User} class.
 * Focuses on ID generation, password hashing, and username derivation.
 */
class UserTest {

    /**
     * Verifies that a new Admin user gets the 'ADM' prefix and correctly hashed password.
     */
    @Test
    void testAdminInitialization() {
        String email = "boss@company.com";
        String password = "securePass123";

        User admin = new User(email, password, Type.ADMIN);

        assertTrue(admin.getId().startsWith("ADM"), "Admin ID should start with ADM");
        assertEquals("boss", admin.getUsername(), "Username should be extracted from email");
        assertTrue(admin.isAdmin(), "isAdmin should return true for ADMIN type");

        // Security check
        assertTrue(admin.checkPassword(password), "Password check should succeed with correct password");
        assertNotEquals(password, admin.getHashedpw(), "Stored password should be hashed, not plain text");
    }

    /**
     * Verifies that a regular User gets the 'USR' prefix and handles emails without '@'.
     */
    @Test
    void testRegularUserInitialization() {
        String email = "john.doe"; // No @ symbol
        User user = new User(email, "pass", Type.USER);

        assertTrue(user.getId().startsWith("USR"), "User ID should start with USR");
        assertEquals("john.doe", user.getUsername(), "Username should equal email if no @ is present");
        assertFalse(user.isAdmin(), "isAdmin should return false for USER type");
    }

    /**
     * Verifies the BCrypt password verification logic.
     */
    @Test
    void testPasswordVerification() {
        User user = new User("test@test.com", "mySecret", Type.USER);

        assertTrue(user.checkPassword("mySecret"));
        assertFalse(user.checkPassword("wrongSecret"));
        assertFalse(user.checkPassword("MYSECRET")); // Check case sensitivity
    }

    /**
     * Verifies updating the password correctly re-hashes the new value.
     */
    @Test
    void testSetPassword() {
        User user = new User("test@test.com", "oldPass", Type.USER);
        String initialHash = user.getHashedpw();

        user.setPassword("newPass");

        assertNotEquals(initialHash, user.getHashedpw(), "Hash should change when password is reset");
        assertTrue(user.checkPassword("newPass"));
        assertFalse(user.checkPassword("oldPass"));
    }

    /**
     * Verifies the loading constructor (used for persistence/CSV).
     */
    @Test
    void testLoadingConstructor() {
        String existingId = "USR-12345";
        String existingHash = "$2a$10$abcdefghijk"; // Mock hash

        User user = new User(existingId, "saved@test.com", existingHash, Type.USER, "savedUser");

        assertEquals(existingId, user.getId());
        assertEquals(existingHash, user.getHashedpw());
        assertEquals("savedUser", user.getUsername());
    }
}