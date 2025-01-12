package org.example;

import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Scanner;

public class LinkService {
    private final String uuid; // UUID пользователя
    private final String configFilePath = "config.json";
    private final String linksFilePath = "links.json";
    private final String usersFilePath = "users.json";

    public LinkService(String uuid) {
        this.uuid = uuid;
        ensureFileExists(linksFilePath);
        ensureFileExists(usersFilePath);
        ensureFileExists(configFilePath);
    }

    private void ensureFileExists(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                if (filePath.equals(linksFilePath)) {
                    Files.write(Paths.get(filePath), new JSONArray().toString().getBytes());
                } else if (filePath.equals(usersFilePath)) {
                    Files.write(Paths.get(filePath), new JSONArray().toString().getBytes());
                } else {
                    Files.write(Paths.get(filePath), new JSONObject().toString().getBytes());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void createLink(Scanner scanner) {
        System.out.print("Введите длинную ссылку: ");
        String originalUrl = scanner.nextLine();

        int redirectLimitByUser = 0;
        int redirectLimitByConfig = getRedirectLimit();

        System.out.print("Введите лимит переходов, или нажмите \"Enter\" если хотите оставить значение по умолчанию: ");
        String redirectLimitByUserString = scanner.nextLine();

        if (!redirectLimitByUserString.isBlank()) {
            try {
                redirectLimitByUser = Integer.parseInt(redirectLimitByUserString);
            } catch (NumberFormatException e) {
                System.out.println("Вы ввели не корректное значение. Допустимы только числовые");
            }
        }

        int redirectLimit = (redirectLimitByUser > 0 && redirectLimitByUser > redirectLimitByConfig) ? redirectLimitByUser : redirectLimitByConfig;

        System.out.print("Введите время жизни ссылки в часах, или нажмите \"Enter\" если хотите оставить значение по умолчанию: ");
        String expirationTimeByUserString = scanner.nextLine();

        long expirationTimeByUser = 0;
        long expirationTimeByConfig = getExpirationTime();

        if (!expirationTimeByUserString.isBlank()) {
            try {
                expirationTimeByUser = System.currentTimeMillis() + (Integer.parseInt(expirationTimeByUserString) * 24L * 60L * 60L * 1000L);
            } catch (NumberFormatException e) {
                System.out.println("Вы ввели не корректное значение. Допустимы только числовые");
            }
        }

        long expirationTime = (expirationTimeByUser > 0 && expirationTimeByUser < expirationTimeByConfig) ? expirationTimeByUser :  expirationTimeByConfig;

        JSONArray linksArray = readLinksFromFile();
        boolean hasOriginalUrl = false;

        for (int i = 0; i < linksArray.length(); i++) {
            JSONObject link = linksArray.getJSONObject(i);
            String storedLink = link.getString("originalUrl");

            if (link.getString("uuid").equals(uuid) && storedLink.equals(originalUrl)) {
                hasOriginalUrl = true;
                link.put("redirectLimit", redirectLimit);
                link.put("expirationTime", expirationTime);

                String shortUrl = generateShortUrl();

                link.put("shortUrl", shortUrl);

                writeLinksToFile(linksArray);

                System.out.println("Ваша новая ссылка: " + shortUrl);
                return;
            }
        }

        if (!hasOriginalUrl) {
            String shortUrl = generateShortUrl();

            JSONObject newLink = new JSONObject();
            newLink.put("uuid", uuid);
            newLink.put("originalUrl", originalUrl);
            newLink.put("shortUrl", shortUrl);
            newLink.put("redirectLimit", redirectLimit);
            newLink.put("expirationTime", expirationTime);

            linksArray.put(newLink);
            writeLinksToFile(linksArray);

            System.out.println("Ваша ссылка: " + shortUrl);
        }
    }

    public void viewLinks() {
        JSONArray linksArray = readLinksFromFile();
        boolean hasLinks = false;

        for (int i = 0; i < linksArray.length(); i++) {
            JSONObject link = linksArray.getJSONObject(i);
            if (link.getString("uuid").equals(uuid)) {
                hasLinks = true;
                Date date = new Date(link.getLong("expirationTime"));
                SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                System.out.println("Оригинальная ссылка: " + link.getString("originalUrl"));
                System.out.println("Короткая ссылка: " + link.getString("shortUrl"));
                System.out.println("Осталось переходов: " + link.getInt("redirectLimit"));
                System.out.println("Время жизни: " + formater.format(date));
                System.out.println("-------------------------------------------");
            }
        }

        if (!hasLinks) {
            System.out.println("У вас нет созданных ссылок.");
        }
    }

    public void deleteLink(Scanner scanner) {
        System.out.print("Введите короткую ссылку для удаления: ");
        String shortUrl = scanner.nextLine();

        JSONArray linksArray = readLinksFromFile();
        boolean linkFound = false;

        Iterator<Object> iterator = linksArray.iterator();

        while (iterator.hasNext()) {
            JSONObject link = (JSONObject) iterator.next();
            if (link.getString("uuid").equals(uuid) && link.getString("shortUrl").equals(shortUrl)) {
                iterator.remove();
                linkFound = true;
                break;
            }
        }

        if (linkFound) {
            writeLinksToFile(linksArray);
            System.out.println("Ссылка удалена.");
        } else {
            System.out.println("Ссылка не найдена или она не принадлежит вам.");
        }
    }

    public void redirectToOriginalUrl(String shortLink) throws URISyntaxException, IOException {
        JSONArray linksArray = readLinksFromFile();
        for (int i = 0; i < linksArray.length(); i++) {
            JSONObject link = linksArray.getJSONObject(i);
            String storedShortLink = link.getString("shortUrl");

            if (link.getString("uuid").equals(uuid) && storedShortLink.equals(shortLink)) {
                int redirectLimit = link.getInt("redirectLimit");
                if (redirectLimit <= 0) {
                    System.out.println("Ошибка: Лимит переходов для этой ссылки исчерпан.");
                    return;
                }

                link.put("redirectLimit", redirectLimit - 1);

                writeLinksToFile(linksArray);

                System.out.println("Перенаправляем на оригинальный URL: " + link.getString("originalUrl"));
                Desktop.getDesktop().browse(new URI(link.getString("originalUrl")));
                return;
            }
        }
        System.out.println("Ошибка: У Вас такая короткая ссылка не найдена или она устарела.");
    }

    private JSONArray readLinksFromFile() {
        String content;
        try {
            content = new String(Files.readAllBytes(Paths.get(linksFilePath)));

            JSONArray linksArray = new JSONArray(content);

            Iterator<Object> iterator = linksArray.iterator();
            Date currentDate = new Date();
            boolean linkFound = false;

            while (iterator.hasNext()) {
                JSONObject link = (JSONObject) iterator.next();

                if (currentDate.after(new Date(link.getLong("expirationTime"))) || link.getInt("redirectLimit") <= 0) {
                    iterator.remove();
                    linkFound = true;
                }
            }

            if (linkFound) {
                writeLinksToFile(linksArray);
            }

            return linksArray;
        } catch (IOException e) {
            e.printStackTrace();
            return new JSONArray();
        }
    }

    private void writeLinksToFile(JSONArray linksArray) {
        try (FileWriter file = new FileWriter(linksFilePath)) {
            file.write(linksArray.toString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String generateShortUrl() {
        return "clck.ru/" + Long.toHexString(System.nanoTime()).substring(0, 6); // Простой метод генерации
    }

    private long getExpirationTime() {
        int defaultExpirationDays = 1;

        try {
            File configFile = new File(configFilePath);
            if (configFile.exists()) {
                String content = new String(Files.readAllBytes(configFile.toPath()));
                JSONObject config = new JSONObject(content);
                if (config.has("defaultExpirationDays")) {
                    defaultExpirationDays = config.getInt("defaultExpirationDays");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return System.currentTimeMillis() + (defaultExpirationDays * 24L * 60L * 60L * 1000L);
    }

    private int getRedirectLimit() {
        int defaultRedirectLimit = 1;

        try {
            File configFile = new File(configFilePath);
            if (configFile.exists()) {
                String content = new String(Files.readAllBytes(configFile.toPath()));
                JSONObject config = new JSONObject(content);
                if (config.has("defaultRedirectLimit")) {
                    defaultRedirectLimit = config.getInt("defaultRedirectLimit");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return defaultRedirectLimit;
    }
}

