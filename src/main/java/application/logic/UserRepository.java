package application.logic;

import java.io.*;
import java.util.*;

public class UserRepository {

    private final List<User> admins = List.of(
            new User("pietro.sala@univr.it", "mela44", Type.ADMIN),
            new User("carlo.combi@univr.it", "ananas37", Type.ADMIN),
            new User("matteo.drago@studenti.univr.it", "fragola82", Type.ADMIN),
            new User("luca.quaresima@studenti.univr.it", "lampone83", Type.ADMIN),
            new User("aa", "aa", Type.ADMIN)
    );

    private Map<String, User> users = new HashMap<>();

    private final File file = new File("./src/main/resources/data/userData.csv");

    public UserRepository() {
        loadUsersFromFile();
        if (!file.exists() || file.length() == 0) {
            try {
                createFileWithAdmins();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void createFileWithAdmins() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writeHeader(writer);
            for (User admin : admins) {
                writeUser(writer, admin);
                users.put(admin.getEmail(), admin);
            }
        }
    }

    private void writeHeader(BufferedWriter writer) throws IOException {
        writer.write("id,email,password,type");
        writer.newLine();
    }

    private void writeUser(BufferedWriter writer, User user) throws IOException {
        writer.write(user.getId() + "," + user.getEmail() + "," + user.getHashedpw() + "," + user.getType());
        writer.newLine();
    }

    public void saveUser(User user) {
        try {
            if (!file.exists() || file.length() == 0) {
                createFileWithAdmins();
            }
            appendUserToFile(user);
            users.put(user.getEmail(), user);
            System.out.println("Utente salvato: " + user.getEmail());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void appendUserToFile(User user) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            writeUser(writer, user);
        }
    }

    public boolean checkCredentials(String email, String password) {
        if (email == null || password == null || email.isEmpty() || password.isEmpty()) return false;
        if (!users.containsKey(email)) return false;
        return users.get(email).checkPassword(password);
    }

    private void loadUsersFromFile() {
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            reader.readLine(); //salto intestazione

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    String id = parts[0];
                    String email = parts[1];
                    String hashedPassword = parts[2];
                    Type type = Type.valueOf(parts[3]);
                    User user = new User(id, email, hashedPassword, type);
                    users.put(email, user);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
