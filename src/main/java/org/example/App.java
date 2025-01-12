package org.example;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Scanner;

public class App {

public static void main(String[] args) throws IOException, URISyntaxException {
    Scanner scanner = new Scanner(System.in);
    AuthService authService = new AuthService();
    LinkService linkService = null;

    System.out.println("Добро пожаловать в сервис сокращения ссылок!");

    String uuid = null;
    while (uuid == null) {
        System.out.println("1. Регистрация");
        System.out.println("2. Авторизация");
        System.out.print(">");
        int choice = scanner.nextInt();
        scanner.nextLine();

        if (choice == 1) {
            User newUser = authService.registerUser(scanner);
            if (newUser != null) {
                uuid = newUser.getUuid();
            }
        } else if (choice == 2) {
            uuid = authService.authenticate(scanner);
        } else {
            System.out.println("Неверный выбор, попробуйте снова.");
        }
    }

    linkService = new LinkService(uuid);

    while (true) {
        System.out.println("\nВыберите действие:");
        System.out.println("1. Сократить ссылку");
        System.out.println("2. Посмотреть мои ссылки");
        System.out.println("3. Удалить ссылку");
        System.out.println("4. Перейти по короткой ссылке");
        System.out.println("0. Выйти");

        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1:
                linkService.createLink(scanner);
                break;
            case 2:
                linkService.viewLinks();
                break;
            case 3:
                linkService.deleteLink(scanner);
                break;
            case 4:
                System.out.print("Введите короткую ссылку для перехода: ");
                String shortLink = scanner.nextLine();
                linkService.redirectToOriginalUrl(shortLink);
                break;
            case 0:
                System.out.println("До свидания!");
                return;
            default:
                System.out.println("Неверный выбор, попробуйте снова.");
        }
    }
}
}
