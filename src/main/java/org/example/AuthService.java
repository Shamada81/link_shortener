package org.example;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class AuthService {

    private static final String USERS_FILE = "users.json";

    public AuthService() {
        ensureFileExists(USERS_FILE);
    }

    public User registerUser(Scanner scanner) {
        JSONArray usersArray = readUsersFromFile();

        System.out.println("Введите логин:");
        String login = scanner.nextLine();

        for (Object obj : usersArray) {
            User user = User.fromJsonObject((JSONObject) obj);
            if (user.getLogin().equals(login)) {
                System.out.println("Этот логин уже занят. Попробуйте другой.");
                return null;
            }
        }

        System.out.println("Введите пароль:");
        String password = scanner.nextLine();

        String uuid = java.util.UUID.randomUUID().toString();
        User newUser = new User(uuid, login, password);

        usersArray.put(newUser.toJsonObject());
        writeUsersToFile(usersArray);

        System.out.println("Вы успешно зарегистрированы!");
        return newUser;
    }

    public String authenticate(Scanner scanner) {
        JSONArray usersArray = readUsersFromFile();

        System.out.println("Введите логин:");
        String login = scanner.nextLine();
        System.out.println("Введите пароль:");
        String password = scanner.nextLine();

        for (Object obj : usersArray) {
            User user = User.fromJsonObject((JSONObject) obj);
            if (user.getLogin().equals(login) && user.getPassword().equals(password)) {
                System.out.println("Авторизация успешна!");
                return user.getUuid();
            }
        }

        System.out.println("Неверные логин или пароль.");
        return null;
    }

    private JSONArray readUsersFromFile() {
        try {
            FileReader reader = new FileReader(USERS_FILE);
            StringBuilder jsonStringBuilder = new StringBuilder();
            int i;
            while ((i = reader.read()) != -1) {
                jsonStringBuilder.append((char) i);
            }
            return new JSONArray(jsonStringBuilder.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new JSONArray();
    }

    private void writeUsersToFile(JSONArray usersArray) {
        try (FileWriter file = new FileWriter(USERS_FILE)) {
            file.write(usersArray.toString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void ensureFileExists(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                Files.write(Paths.get(filePath), new JSONArray().toString().getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
