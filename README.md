1. Проект сделан с использованием maven. Запускается из ide (jar не создавал).
2. Для того, что бы задать дефолтные значения лимитов переходов и время жизни ссылок служит  файл config.json.
3. При первом использовании необходимо пройти регистрацию нажав цифру 2 (валидация для имени и пароля отсутствует, так как не было в задании). ![image](https://github.com/user-attachments/assets/8956b75b-590a-4ad4-8434-27a3c1bf0f42)
4. После регистрации и авторизации Вы получаете возможность выбрать действия из списка: ![image](https://github.com/user-attachments/assets/bf2290ae-dd6a-445e-9e17-d7ccc8b16044)


Реализация:
1. Основной компонент App
2. За регистрацию и авторизацию отвечает компонет User
3. За работу с ссылками LinkService
4. За авторизацию AuthService.
5. Удаление "протухших" по времени или лимиту переходов ссылок реализовано в классе LinkService в методе readLinksFromFile.
6. При попытке сократить длинную ссылку, которая уже была у данного пользователя, происходит обновление короткой ссылки и лимита переходов (дублирования одинаковых ссылок нет)


