package application.repositories;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import application.logic.Type;
import application.logic.User;
import org.apache.commons.validator.routines.EmailValidator;

public class UserRepository {

    private final List<User> admins = List.of(
            new User("pietro.sala@univr.it", "mela44", Type.ADMIN),
            new User("carlo.combi@univr.it", "ananas37", Type.ADMIN),
            new User("matteo.drago@studenti.univr.it", "fragola82", Type.ADMIN),
            new User("luca.quaresima@studenti.univr.it", "lampone83", Type.ADMIN),
            new User("aa", "aa", Type.ADMIN)
    );

    private final Map<String, User> usersById = new HashMap<>();
    private final Map<String, User> usersByEmail = new HashMap<>();

    private final File file = new File("data/userData.csv");

    public UserRepository() {
        if (!file.exists() || file.length() == 0) {
            try {
                createFileWithAdmins();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            loadUsersFromFile();
        }
    }

    private void createFileWithAdmins() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writeHeader(writer);
            for (User admin : admins) {
                writeUser(writer, admin);
                addUserToMaps(admin);
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

    private void addUserToMaps(User user) {
        usersById.put(user.getId(), user);
        usersByEmail.put(user.getEmail().toLowerCase(), user);
    }

    private void loadUsersFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            reader.readLine(); // salta intestazione

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    String id = parts[0];
                    String email = parts[1];
                    String hashedPassword = parts[2];
                    Type type = Type.valueOf(parts[3]);
                    User user = new User(id, email, hashedPassword, type);
                    addUserToMaps(user);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveUser(User user) {
        try {
            if (!file.exists() || file.length() == 0) {
                createFileWithAdmins();
            }
            appendUserToFile(user);
            addUserToMaps(user);
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

    public User getUserByEmail(String email) {
        return usersByEmail.get(email.toLowerCase());
    }

    public User getUserById(String id) {
        return usersById.get(id);
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(usersById.values());
    }

    public boolean checkCorrectCredentials(String email, String password) {
        if (email == null || password == null || email.isEmpty() || password.isEmpty()) return false;
        User user = getUserByEmail(email);
        if (user == null) return false;
        return user.checkPassword(password);
    }

    public boolean isEmailAvailable(String email) {
        return !usersByEmail.containsKey(email.toLowerCase());
    }

    public boolean isEmailValid(String email) {
        return EmailValidator.getInstance().isValid(email);
    }
}
