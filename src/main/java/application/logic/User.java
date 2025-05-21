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
    private String hashedpw; //Password
    private Type type; //Admin o user

    public User(String email, String password, Type type) {
        this.id="U"+UUID.randomUUID().toString();
        this.email=email;
        this.hashedpw=hashPassword(password);
        this.type=type;
    }

    /*metodo per hashare una password. La libreria bcrypt fa automaticamente anche il salt,
     * ossia aggiunge una stringa random alla fine dell'hash per renderlo unico (se ho capito bene)
     */
    private String hashPassword(String password) {
        return BCrypt.hashpw(password,BCrypt.gensalt());
    }

    /*TODO: In caso aggiungere metodi per cambiare password, ma per ora lasciamo così*/

    /*Metodo per vedere se la password è quella giusta*/
    public boolean checkPassword(String password) {
        return BCrypt.checkpw(password, this.hashedpw);
    }

    /*Getters*/
    public String getId() {
        return this.id;
    }

    public String getEmail() {
        return this.email;
    }

    public Type getType() {
        return this.type;
    }
}