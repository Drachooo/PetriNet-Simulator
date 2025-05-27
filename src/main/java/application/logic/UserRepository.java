package application.logic;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserRepository {

    private final List<User> admins = List.of(
            new User("pietro.sala@univr.it", "mela44", Type.ADMIN),
            new User("carlo.combi@univr.it", "ananas37", Type.ADMIN),
            new User("matteo.drago@studenti.univr.it", "fragola82", Type.ADMIN),
            new User("luca.quaresima@studenti.univr.it", "lampone83", Type.ADMIN)
    );

    private Map<String,User> users=new HashMap<>();

    // Metodo che salva un utente. Se file non esiste, lo crea e "appende" l'utente.
    public void saveUser(User user) {
        File file = new File("./src/main/resources/data/userData.csv");

        try {
            if (!file.exists() || file.length() == 0) {
                createFileWithAdminsAndUser(file, user);
            } else {
                appendUserToFile(file, user);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Metodo privato che CREA il file se NON ESISTE, scrive intestazione, admin iniziali e aggiunge l'utente nuovo

    private void createFileWithAdminsAndUser(File file, User user) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writeHeader(writer);
            writeAdmins(writer);
            writeUser(writer, user);

            System.out.println("File creato con admin iniziali e utente salvato.");
        }
    }

    // Metodo privato che aggiunge un utente in append al file ESISTENTE
    private void appendUserToFile(File file, User user) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            writeUser(writer, user);

            System.out.println("Utente aggiunto in append.");
        }
    }

    // Scrive l’intestazione nel file
    private void writeHeader(BufferedWriter writer) throws IOException {
        writer.write("email,password,type");
        writer.newLine();
    }

    // Scrive tutti gli admin nel file
    private void writeAdmins(BufferedWriter writer) throws IOException {
        for (User admin : admins) {
            writeUser(writer, admin);
        }
    }

    // Scrive un singolo utente nel file
    private void writeUser(BufferedWriter writer, User user) throws IOException {
        writer.write(user.getEmail() + "," + user.getHashedpw() + "," + user.getType());
        writer.newLine();
    }

    /*Metodo per verificare se le credenziali inserite durante il login sono corrette.*/
    public boolean checkCredentials(String email, String password) {
        return users.containsKey(email) && users.get(email).checkPassword(password);
    }

}
