package com.example.creditservice;

import com.example.creditservice.api.*;
import com.example.creditservice.api.enums.IntConsts;
import com.example.creditservice.api.enums.StrConsts;
import com.example.creditservice.model.enums.OrderStatus;
import com.example.creditservice.model.request.AuthenticationRequest;
import com.example.creditservice.model.request.CreateOrder;
import com.example.creditservice.model.request.DeleteOrder;
import com.example.creditservice.model.request.RegisterRequest;
import com.example.creditservice.model.response.AuthenticationResponse;
import com.example.creditservice.model.tariff.Tariff;
import io.restassured.http.ContentType;
import jdk.jfr.Description;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;

class SmokeTestSuit extends BaseTest{
    @Test
    @Description("Проверка получения тарифов, их наличие в ответе и правильного кода ответа")
    @DisplayName("Получение тарифов")
    void testGetTariffs() {
        Specifications.installSpecification(Specifications.requestSpec(StrConsts.BASE_URL.toString()),
                Specifications.responseSpecOK200());

        List<Tariff> tariffs = given()
                .when()
                .get("/loan-service/getTariffs")
                .then()
                .log().all()
                .extract().body().jsonPath().getList("data.tariffs", Tariff.class);

        // Проверка модели ответа
        Assertions.assertNotNull(tariffs.get(0).getType());
        Assertions.assertNotNull(tariffs.get(0).getInterestRate());
    }


    @Test
    @Description("Проверка выполнения аутентификации по данным администратора, наличие токена в ответе и правильного кода ответа")
    @DisplayName("Аутентификация")
    void testAuthentication() {
        Specifications.installSpecification(Specifications.requestSpec(StrConsts.BASE_URL.toString()),
                Specifications.responseSpecOK200());
        AuthenticationRequest authReq = new AuthenticationRequest(StrConsts.ADMIN_LOGIN.toString(), StrConsts.ADMIN_PASSWORD.toString());
        AuthenticationResponse request = given().body(authReq).when().post("/auth/authenticate").then().log().all()
                .extract().as(AuthenticationResponse.class);

        Assertions.assertNotNull(request.getToken());
    }

    @ParameterizedTest
    @Description("Проверка выполнения регистрации нового пользователя, наличие токена в ответе и правильного кода ответа")
    @DisplayName("Регистрация")
    @CsvSource({"Arsen,Gorin,gorin@gmail.com,1q2w3e4r", "Ivan,Stepanov,stepanov@mail.com,1"})
    void testRegistration(String firstname, String lastname, String email, String password) {
        Specifications.installSpecification(Specifications.requestSpec(StrConsts.BASE_URL.toString()),
                Specifications.responseSpecOK200());
        RegisterRequest registerRequest = new RegisterRequest(firstname, lastname, email, password);
        AuthenticationResponse response = given().body(registerRequest)
                .when().post("/auth/register")
                .then().log().all()
                .extract().as(AuthenticationResponse.class);

        Assertions.assertNotNull(response.getToken());
    }


    @Test
    @Description("Проверка создания новой заявки на кредит, наличие ID заказа в ответе и правильного кода ответа")
    @DisplayName("Создание заявки на кредит")
    void testOrderingLoanService() {
        Specifications.installSpecification(Specifications.requestSpec(StrConsts.BASE_URL.toString()),
                Specifications.responseSpecOK200());
        String bearerToken = Services.authentication(StrConsts.ADMIN_LOGIN.toString(), StrConsts.ADMIN_PASSWORD.toString());
        CreateOrder orderDetails = new CreateOrder();
        orderDetails.setUserId(IntConsts.LOAN_ORDER_ADMIN_USER_ID.getValue());
        orderDetails.setTariffId(IntConsts.LOAN_ORDER_TARIFF_ID.getValue());
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
        Assertions.assertNotNull(order.getOrderId());

    }


    @Test
    @Description("Проверка получения статуса заявки на оформление кредита, наличие статуса заявки в ответе и правильного кода ответа")
    @DisplayName("Получение статуса заявки")
    void testGetOrderStatus() {
        String bearerToken = Services.authentication(StrConsts.ADMIN_LOGIN.toString(), StrConsts.ADMIN_PASSWORD.toString());

        // Создание заказа
        DataResponseLoanOrderS order = Services.orderLoan(bearerToken, IntConsts.LOAN_ORDER_ADMIN_USER_ID.getValue(), IntConsts.LOAN_ORDER_TARIFF_ID.getValue() + 2);

        Specifications.installSpecification(Specifications.requestSpec(StrConsts.BASE_URL.toString()),
                Specifications.responseSpecOK200());
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
        Assertions.assertNotNull(orderStatusResponse.getOrderStatus());
        Assertions.assertEquals(OrderStatus.IN_PROGRESS, orderStatusResponse.getOrderStatus());
    }


    @Test
    @Description("Проверка удаления заявки на оформление кредита и правильного кода ответа")
    @DisplayName("Удаление заявки на кредит")
    void testDeleteLoanRequest() {
        String bearerToken = Services.authentication(StrConsts.ADMIN_LOGIN.toString(), StrConsts.ADMIN_PASSWORD.toString());

        // Создание нового тарифа
        Services.tariffCreation(bearerToken, "DOUBLE_RATE", "20%");

        // Создание заказа
        UUID orderId = Services.orderLoan(bearerToken, IntConsts.LOAN_ORDER_ADMIN_USER_ID.getValue(), IntConsts.ADDED_TARIFF_ID.getValue()).getOrderId();

        DeleteOrder deleteOrder = new DeleteOrder();
        deleteOrder.setUserId(IntConsts.LOAN_ORDER_ADMIN_USER_ID.getValue());
        deleteOrder.setOrderId(orderId);
        Specifications.installSpecification(Specifications.requestSpec(StrConsts.BASE_URL.toString()),
                Specifications.responseSpecOK200());
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


    @Test
    @Description("Проверка добавления нового тарифа и правильного кода ответа")
    @DisplayName("Добавление тарифа")
    void testAddTariff() {
        String bearerToken = Services.authentication(StrConsts.ADMIN_LOGIN.toString(), StrConsts.ADMIN_PASSWORD.toString());

        Specifications.installSpecification(Specifications.requestSpec(StrConsts.BASE_URL.toString()),
                Specifications.responseSpecOK200());

        TariffRequest tariffRequest = new TariffRequest("NEW_TYPE", "13.5%");
        given().headers("Authorization", "Bearer " + bearerToken).body(tariffRequest)
                .when().post("/loan-service/addTariff")
                .then().log().all();
    }
}