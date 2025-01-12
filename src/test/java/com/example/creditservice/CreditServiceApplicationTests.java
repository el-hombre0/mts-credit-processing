package com.example.creditservice;

import com.example.creditservice.api.Specifications;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static io.restassured.RestAssured.given;

@SpringBootTest
class CreditServiceApplicationTests {
    private final static String BASE_URL = "https://localhost:8080";

    @Test
    public void checkGetUserTest() {
        Specifications.installSpecification(Specifications.requestSpec(BASE_URL),
                Specifications.responseSpecOK200());
        UserData user = given()
                .when()
                .get("/api/users/2")
                .then().log().all()
                .extract().body().jsonPath().getObject("data", UserData.class);

        // Проверка модели ответа
        Assert.assertNotNull(user.getId());
        Assert.assertNotNull(user.getEmail());
        Assert.assertNotNull(user.getFirst_name());
        Assert.assertNotNull(user.getLast_name());
        Assert.assertNotNull(user.getAvatar());

        // Проверка бизнес-модели
        Assert.assertTrue(user.getAvatar().contains(user.getId().toString()));
    }
}
