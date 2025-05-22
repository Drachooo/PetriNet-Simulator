package application.logic;

import java.util.UUID;
import org.mindrot.jbcrypt.BCrypt;

public class User {
    public static enum Type {
        ADMIN,
        USER
    }

    private String id;
    private String email;
    private String hashedpw; // password hashata
    private Type type;

    /**
     * Costruttore per creare un nuovo utente da password in chiaro.
     * La password verrà hashata automaticamente.
     */
    public User(String email, String plainPassword, Type type) {
        this.email = email;
        this.hashedpw = hashPassword(plainPassword);
        this.type = type;

        if (type == Type.ADMIN)
            this.id = "ADM" + UUID.randomUUID().toString();
        else
            this.id = "USR" + UUID.randomUUID().toString();
    }

    /**
     * Costruttore per caricare un utente esistente, con password già hashata (es. da file CSV)
     */
    public User(String id, String email, String hashedpw, Type type) {
        this.id = id;
        this.email = email;
        this.hashedpw = hashedpw;
        this.type = type;
    }

    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    /**
     * Verifica che la password in chiaro corrisponda alla password hashata memorizzata.
     */
    public boolean checkPassword(String password) {
        return BCrypt.checkpw(password, this.hashedpw);
    }

    /* Getters */
    public String getId() {
        return this.id;
    }

    public String getEmail() {
        return this.email;
    }

    public Type getType() {
        return this.type;
    }

    public String getHashedpw() {
        return this.hashedpw;
    }
}
