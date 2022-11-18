package ru.netology.test;

import com.codeborne.selenide.Condition;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.netology.data.DataGenerator;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static io.restassured.RestAssured.given;
import static ru.netology.data.DataGenerator.Registration.getRegisteredUser;
import static ru.netology.data.DataGenerator.Registration.getUser;
import static ru.netology.data.DataGenerator.getRandomLogin;
import static ru.netology.data.DataGenerator.getRandomPassword;

class AuthTest {
    // спецификация нужна для того, чтобы переиспользовать настройки в разных запросах
    private static RequestSpecification requestSpec = new RequestSpecBuilder()
            .setBaseUri("http://localhost")
            .setPort(9999)
            .setAccept(ContentType.JSON)
            .setContentType(ContentType.JSON)
            .log(LogDetail.ALL)
            .build();

    @BeforeAll
    static void setUpAll() {
        // сам запрос
        given() // "дано"
                .spec(requestSpec) // указываем, какую спецификацию используем
                .body(new DataGenerator.RegistrationDto("vasya", "password", "active")) // передаём в теле объект, который будет преобразован в JSON
                .when() // "когда"
                .post("/api/system/users") // на какой путь относительно BaseUri отправляем запрос
                .then() // "тогда ожидаем"
                .statusCode(200); // код 200 OK
    }

    @BeforeEach
    void setup() {
        open("http://localhost:9999");
    }

    @Test
    @DisplayName("Should successfully login with active registered user," +
            "Должен успешно войти в систему с активным зарегистрированным пользователем")
    void shouldSuccessfulLoginIfRegisteredActiveUser() {
        var registeredUser = getRegisteredUser("active");

        $("[data-test-id=login] input").setValue(registeredUser.getLogin());
        $("[data-test-id=password] input").setValue(registeredUser.getPassword());
        $("[data-test-id=action-login]").click();
        $(".heading").shouldHave(Condition.text("Личный кабинет"), Duration.ofSeconds(15)).shouldBe(Condition.visible);
    }

    @Test
    @DisplayName("Should get error message if login with not registered user" +
            "Должно появиться сообщение об ошибке, если войти в систему с незарегистрированным пользователем")
    void shouldGetErrorIfNotRegisteredUser() {
        var notRegisteredUser = getUser("active");

        $("[data-test-id=login] input").setValue(notRegisteredUser.getLogin());
        $("[data-test-id=password] input").setValue(notRegisteredUser.getPassword());
        $("[data-test-id=action-login]").click();
        $("[data-test-id=error-notification]").shouldHave(Condition.text("Ошибка! " + "Неверно указан логин или пароль"), Duration.ofSeconds(15)).shouldBe(Condition.visible);
    }

    @Test
    @DisplayName("Should get error message if login with blocked registered user " +
            "Должно появиться сообщение об ошибке, если вход в систему с заблокированным зарегистрированным пользователем")
    void shouldGetErrorIfBlockedUser() {
        var blockedUser = getRegisteredUser("blocked");

        $("[data-test-id=login] input").setValue(blockedUser.getLogin());
        $("[data-test-id=password] input").setValue(blockedUser.getPassword());
        $("[data-test-id=action-login]").click();
        $("[data-test-id=error-notification]").shouldHave(Condition.text("Ошибка! " + "Пользователь заблокирован"), Duration.ofSeconds(15)).shouldBe(Condition.visible);
    }

    @Test
    @DisplayName("Should get error message if login with wrong login " +
            "Должно появиться сообщение об ошибке, если войти с неправильным логином")
    void shouldGetErrorIfWrongLogin() {
        var registeredUser = getRegisteredUser("active");
        var wrongLogin = getRandomLogin();

        $("[data-test-id=login] input").setValue(wrongLogin);
        $("[data-test-id=password] input").setValue(registeredUser.getPassword());
        $("[data-test-id=action-login]").click();
        $("[data-test-id=error-notification]").shouldHave(Condition.text("Ошибка! " + "Неверно указан логин или пароль"), Duration.ofSeconds(15)).shouldBe(Condition.visible);
    }

    @Test
    @DisplayName("Should get error message if login with wrong password" +
            "Должно появиться сообщение об ошибке при входе с неправильным паролем")
    void shouldGetErrorIfWrongPassword() {
        var registeredUser = getRegisteredUser("active");
        var wrongPassword = getRandomPassword();

        $("[data-test-id=login] input").setValue(registeredUser.getLogin());
        $("[data-test-id=password] input").setValue(wrongPassword);
        $("[data-test-id=action-login]").click();
        $("[data-test-id=error-notification]").shouldHave(Condition.text("Ошибка! " + "Неверно указан логин или пароль"), Duration.ofSeconds(15)).shouldBe(Condition.visible);
    }
}