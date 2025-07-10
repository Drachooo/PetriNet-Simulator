package application.logic;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class UserRepository {
    private static final String FILE_PATH = "src/main/resources/data/users.csv";
    private Map<String, User> users = new HashMap<>(); // ✅ id → User

    public UserRepository() {
        File file = new File(FILE_PATH);

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
        List<User> admins = List.of(
                new User("admin1@example.com", "admin1", Type.ADMIN),
                new User("admin2@example.com", "admin2", Type.ADMIN)
        );

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (User admin : admins) {
                writeUser(writer, admin);
                users.put(admin.getId(), admin); // ✅ usa id come chiave
            }
        }
    }

    private void writeUser(BufferedWriter writer, User user) throws IOException {
        writer.write(String.format("%s,%s,%s,%s\n",
                user.getId(), user.getEmail(), user.getHashedpw(), user.getType().name()));
    }

    private void loadUsersFromFile() {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(",");
                if (tokens.length == 4) {
                    String id = tokens[0];
                    String email = tokens[1];
                    String hashedPassword = tokens[2];
                    Type type = Type.valueOf(tokens[3]);

                    User user = new User(id, email, hashedPassword, type);
                    users.put(id, user); // ✅ usa id come chiave
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveUser(User user) {
        users.put(user.getId(), user); // ✅ usa id come chiave
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (User u : users.values()) {
                writeUser(writer, u);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String, User> getUsers() {
        return users;
    }

    public User getUserById(String id) {
        return users.get(id);
    }
}
