package com.example.creditservice;

import com.example.creditservice.api.*;
import com.example.creditservice.model.request.AuthenticationRequest;
import com.example.creditservice.model.request.CreateOrder;
import com.example.creditservice.model.request.DeleteOrder;
import com.example.creditservice.model.request.RegisterRequest;
import com.example.creditservice.model.response.AuthenticationResponse;
import com.example.creditservice.model.tariff.Tariff;
import io.restassured.http.ContentType;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;


import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;

@SpringBootTest
public class RegressTestSuit {
    private final static String BASE_URL = "http://localhost:8080";
    private final static String ADMIN_LOGIN = "ivanov@mail.ru";
    private final static String ADMIN_PASSWORD = "1234";
    private final static String USER_LOGIN = "petrov@gmail.com";
    private final static String USER_PASSWORD = "1q2w3e4r";
    private final int loanOrderAdminUserId = 1;
    private final int loanOrderUserUserId = 2;
    private final static int loanOrderTariffId = 1;

    /**
     * Метод получения тарифов
     */
    @Test
    public void testGetTariffs() {
        Specifications.installSpecification(Specifications.requestSpec(BASE_URL),
                Specifications.responseSpecOK200());

        List<Tariff> tariffs = given()
                .when()
                .get("/loan-service/getTariffs")
                .then()
                .log().all()
                .extract().body().jsonPath().getList("data.tariffs", Tariff.class);

        // Проверка модели ответа
        Assert.assertNotNull(tariffs.get(0).getType());
        Assert.assertNotNull(tariffs.get(0).getInterestRate());
    }

    /**
     * Тест аутентификации
     */
    @Test
    public void testAuthentication() {
        Specifications.installSpecification(Specifications.requestSpec(BASE_URL),
                Specifications.responseSpecOK200());
        AuthenticationRequest authReq = new AuthenticationRequest(ADMIN_LOGIN, ADMIN_PASSWORD);
        AuthenticationResponse request = given().body(authReq).when().post("/auth/authenticate").then().log().all()
                .extract().as(AuthenticationResponse.class);

        Assert.assertNotNull(request.getToken());

    }

    /**
     * Метод регистрации
     */
    @Test
    public void testRegistration() {
        Specifications.installSpecification(Specifications.requestSpec(BASE_URL),
                Specifications.responseSpecOK200());
        RegisterRequest registerRequest = new RegisterRequest("Arsen", "Norin", "norin@gmail.com", "1q2w3e4r");
        AuthenticationResponse response = given().body(registerRequest)
                .when().post("/auth/register")
                .then().log().all()
                .extract().as(AuthenticationResponse.class);

        Assert.assertNotNull(response.getToken());
    }

    /**
     * Метод подачи заявки на кредит
     */
    @Test
    public void testOrderingLoanService() {
        Specifications.installSpecification(Specifications.requestSpec(BASE_URL),
                Specifications.responseSpecOK200());
        String bearerToken = authentication(ADMIN_LOGIN, ADMIN_PASSWORD);
        CreateOrder orderDetails = new CreateOrder();
        orderDetails.setUserId(2);
        orderDetails.setTariffId(loanOrderTariffId);
        DataResponseLoanOrderS order = given()
                .headers(
                        "Authorization",
                        "Bearer " + bearerToken,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .body(orderDetails).when().post("/loan-service/order")
                .then().log().all()
                .extract().body().jsonPath().getObject("data", DataResponseLoanOrderS.class);
        Assert.assertNotNull(order.getOrderId());

    }

    /**
     * Негативный сценарий - отправка двух запросов на получение кредита
     */
//    @Negative
    @Test(expected = java.lang.AssertionError.class)
    public void testNegativeOrderingLoanService() {
        Specifications.installSpecification(Specifications.requestSpec(BASE_URL),
                Specifications.responseSpecBADREQUEST400());
        // Первый заказ
        DataResponseLoanOrderS order1 = orderLoan(2, loanOrderTariffId + 1);
        Assert.assertNotNull(order1.getOrderId());

//        DataResponseLoanOrderS order2 = orderLoan(4, loanOrderTariffId);
        String bearerToken = authentication(ADMIN_LOGIN, ADMIN_PASSWORD);
        CreateOrder orderDetails = new CreateOrder();
        orderDetails.setUserId(2);
        orderDetails.setTariffId(loanOrderTariffId + 1);
        // Второй заказ с такими же параметрами
        ErrorDataResponse order2 = given()
                .headers(
                        "Authorization",
                        "Bearer " + bearerToken,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .body(orderDetails).when().post("/loan-service/order")
                .then().log().all()
                .extract().body().jsonPath().getObject("error", ErrorDataResponse.class);
        Assert.assertNotNull(order2.getCode());
        Assert.assertNotNull(order2.getMessage());

    }


    /**
     * Метод получения статуса заявки
     */
    @Test
    public void testGetOrderStatus() {
        Specifications.installSpecification(Specifications.requestSpec(BASE_URL),
                Specifications.responseSpecOK200());
        // Создание заказа
        DataResponseLoanOrderS order = orderLoan(loanOrderUserUserId, loanOrderTariffId);

        // Аутентификация пользователя
        String bearerToken = authentication(USER_LOGIN, USER_PASSWORD);

        // Проверка статуса заказа
        DataResponseStatusS orderStatusResponse = given()
                .headers(
                        "Authorization",
                        "Bearer " + bearerToken,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .get("/loan-service/getStatusOrder?orderId=" + order.getOrderId())
                .then().log().all()
                .extract().body().jsonPath().getObject("data", DataResponseStatusS.class);
        Assert.assertNotNull(orderStatusResponse.getOrderStatus());

    }

    /**
     * Негативный метод получения статуса заявки по несуществующему UUID
     */
    @Test(expected = java.lang.AssertionError.class)
    public void testNegativeGetOrderStatus() {

        // Аутентификация пользователя
        String bearerToken = authentication(USER_LOGIN, USER_PASSWORD);

        // Проверка статуса заказа
        DataResponseStatusS orderStatusResponse = given()
                .headers(
                        "Authorization",
                        "Bearer " + bearerToken,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .get("/loan-service/getStatusOrder?orderId=123e4567-e89b-42d3-a456-556642440000")
                .then().log().all()
                .extract().body().jsonPath().getObject("data", DataResponseStatusS.class);
        Assert.assertNotNull(orderStatusResponse.getOrderStatus());

    }

    /**
     * Метод удаления заявки
     */
    @Test
    public void testDeleteLoanRequest() {
        Specifications.installSpecification(Specifications.requestSpec(BASE_URL),
                Specifications.responseSpecOK200());
        // Создание заказа
        UUID orderId = orderLoan(loanOrderUserUserId, loanOrderTariffId+2).getOrderId();

        String bearerToken = authentication(ADMIN_LOGIN, ADMIN_PASSWORD);
        DeleteOrder deleteOrder = new DeleteOrder();
        deleteOrder.setUserId(loanOrderUserUserId+2);
        deleteOrder.setOrderId(orderId);
        given()
                .headers(
                        "Authorization",
                        "Bearer " + bearerToken,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .body(deleteOrder).when().delete("/loan-service/deleteOrder").then().log().all();
    }

    /**
     * Негативный метод удаления заявки по несуществующему UUID
     */
    @Test(expected = java.lang.AssertionError.class)
    public void testNegativeDeleteLoanRequest() {
        Specifications.installSpecification(Specifications.requestSpec(BASE_URL),
                Specifications.responseSpecBADREQUEST400());

        String bearerToken = authentication(ADMIN_LOGIN, ADMIN_PASSWORD);
        DeleteOrder deleteOrder = new DeleteOrder();
        deleteOrder.setUserId(loanOrderAdminUserId);
        deleteOrder.setOrderId(UUID.fromString("123e4567-e89b-42d3-a456-556642440000"));
        ErrorDataResponse errorDataResponse = given()
                .headers("Authorization", "Bearer " + bearerToken)
                .body(deleteOrder).when().delete("/loan-service/deleteOrder").then().log().all()
                .extract().body().jsonPath().getObject("error", ErrorDataResponse.class);
        Assert.assertNotNull(errorDataResponse.getCode());
        Assert.assertNotNull(errorDataResponse.getMessage());
    }

    /**
     * Метод добавления нового тарифа
     */
    @Test
    public void testAddTariff() {
        Specifications.installSpecification(Specifications.requestSpec(BASE_URL),
                Specifications.responseSpecOK200());
        String bearerToken = authentication(ADMIN_LOGIN, ADMIN_PASSWORD);
        TariffRequest tariffRequest = new TariffRequest("NEW_TYPE", "13.5%");
        given().headers("Authorization", "Bearer " + bearerToken).body(tariffRequest)
                .when().post("/loan-service/addTariff")
                .then().log().all();
    }

    /**
     * Негативный метод добавления нового тарифа, добавление двух одинаковых типов
     */
    @Test
    public void testNegativeAddTariff(){
        Specifications.installSpecification(Specifications.requestSpec(BASE_URL),
                Specifications.responseSpecBADREQUEST400());
        String bearerToken = authentication(ADMIN_LOGIN, ADMIN_PASSWORD);
        TariffRequest tariffRequest = new TariffRequest("NEW_TYPE11", "13.5%");
        given().headers("Authorization", "Bearer " + bearerToken).body(tariffRequest)
                .when().post("/loan-service/addTariff")
                .then().log().all();

        TariffRequest tariffRequest2 = new TariffRequest("NEW_TYPE11", "13.5%");
        given().headers("Authorization", "Bearer " + bearerToken).body(tariffRequest2)
                .when().post("/loan-service/addTariff")
                .then().log().all();
    }

    /**
     * Сервисный метод аутентификации
     * @param email электронная почта пользователя
     * @param password пароль пользователя
     * @return JWT аутентификации
     */
    private String authentication(String email, String password) {
        Specifications.installSpecification(Specifications.requestSpec(BASE_URL),
                Specifications.responseSpecOK200());
        AuthenticationRequest authReq = new AuthenticationRequest(email, password);
        AuthenticationResponse request = given().body(authReq).when().post("/auth/authenticate").then().log().all()
                .extract().as(AuthenticationResponse.class);

        return request.getToken();
    }

    /**
     * Сервисный метод создания заявки на кредит
     * @param userId Идентификатор пользователя
     * @param tariffId Идентификатор тарифа
     * @return Объект, содержащий UUID заявки
     */
    private DataResponseLoanOrderS orderLoan(long userId, long tariffId) {
        String bearerToken = authentication(ADMIN_LOGIN, ADMIN_PASSWORD);
        CreateOrder orderDetails = new CreateOrder();
        orderDetails.setUserId(userId);
        orderDetails.setTariffId(tariffId);
        return given()
                .headers(
                        "Authorization",
                        "Bearer " + bearerToken,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .body(orderDetails).when().post("/loan-service/order")
                .then().log().all()
                .extract().body().jsonPath().getObject("data", DataResponseLoanOrderS.class);
    }
}
