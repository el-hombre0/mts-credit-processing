package com.example.creditservice;

import com.example.creditservice.api.*;
import com.example.creditservice.api.enums.ErrorCodes;
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
import org.springframework.boot.test.context.SpringBootTest;


import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;

@SpringBootTest
class RegressTestSuit extends BaseTest{

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


    @Test
//    @ParameterizedTest
    @Description("Проверка выполнения регистрации нового пользователя, наличие токена в ответе и правильного кода ответа")
    @DisplayName("Регистрация")
//    @CsvSource({"Arsen,Gorin,gorin@gmailcom,1q2w3e4r", "Ivan,Stepanov,stepanov@mailcom,1"})
//    void testRegistration(String firstname, String lastname, String email, String password) {
    void testRegistration() {

            Specifications.installSpecification(Specifications.requestSpec(StrConsts.BASE_URL.toString()),
                Specifications.responseSpecOK200());
//        RegisterRequest registerRequest = new RegisterRequest(firstname, lastname, email, password);
        RegisterRequest registerRequest = new RegisterRequest("Ivan", "Ivanov", "iv@mail.ro", "123");

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
    @Description("Проверка создания двух одинаковых заявок на кредит, наличие ID заказа, кода и сообщения ошибки в ответе и правильного кода ответа")
    @DisplayName("Создание двух одинаковых заявок на кредит ")
    void testNegativeOrderingLoanService() {
        String bearerToken = Services.authentication(StrConsts.ADMIN_LOGIN.toString(), StrConsts.ADMIN_PASSWORD.toString());
        // Первый заказ
        DataResponseLoanOrderS order1 = Services.orderLoan(bearerToken, IntConsts.LOAN_ORDER_ADMIN_USER_ID.getValue(), IntConsts.LOAN_ORDER_TARIFF_ID.getValue() + 1);
        Assertions.assertNotNull(order1.getOrderId());

        Specifications.installSpecification(Specifications.requestSpec(StrConsts.BASE_URL.toString()),
                Specifications.responseSpecBADREQUEST400());

        CreateOrder orderDetails = new CreateOrder();
        orderDetails.setUserId(IntConsts.LOAN_ORDER_ADMIN_USER_ID.getValue());
        orderDetails.setTariffId(IntConsts.LOAN_ORDER_TARIFF_ID.getValue() + 1);
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
        Assertions.assertNotNull(order2.getCode());
        Assertions.assertNotNull(order2.getMessage());

        Assertions.assertEquals(ErrorCodes.LOAN_CONSIDERATION.toString(), order2.getCode());
        Assertions.assertEquals("Заявка на рассмотрении", order2.getMessage());
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
    @Description("Проверка получения статуса заявки на оформление кредита по несуществующему UUID, наличия статуса заявки в ответе и правильного кода ответа")
    @DisplayName("Получение статуса заявки")
    void testNegativeGetOrderStatus() {
        // Регистрация пользователя
        String bearerToken = Services.registration("Ivan", "Orlov", "orlov@mail.com", "1234");

        Specifications.installSpecification(Specifications.requestSpec(StrConsts.BASE_URL.toString()),
                Specifications.responseSpecBADREQUEST400());
        // Проверка статуса заказа
        ErrorDataResponse orderStatusResponse = given()
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
                .extract().body().jsonPath().getObject("error", ErrorDataResponse.class);
        Assertions.assertNotNull(orderStatusResponse.getCode());
        Assertions.assertNotNull(orderStatusResponse.getMessage());

        Assertions.assertEquals(ErrorCodes.ORDER_NOT_FOUND.toString(), orderStatusResponse.getCode());
        Assertions.assertEquals("Заявка не найдена", orderStatusResponse.getMessage());
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
    @Description("Проверка удаления заявки на оформление кредита по несуществующему UUID, наличия верных кода ошибки и сообщения и правильного кода ответа")
    @DisplayName("Удаление несуществующей заявки")
    void testNegativeDeleteLoanRequest() {
        String bearerToken = Services.authentication(StrConsts.ADMIN_LOGIN.toString(), StrConsts.ADMIN_PASSWORD.toString());

        // Описание заявки на удаление
        DeleteOrder deleteOrder = new DeleteOrder();
        deleteOrder.setUserId(IntConsts.LOAN_ORDER_ADMIN_USER_ID.getValue());
        deleteOrder.setOrderId(UUID.fromString("123e4567-e89b-42d3-a456-556642440000"));

        Specifications.installSpecification(Specifications.requestSpec(StrConsts.BASE_URL.toString()),
                Specifications.responseSpecBADREQUEST400());

        ErrorDataResponse errorDataResponse = given()
                .headers("Authorization", "Bearer " + bearerToken)
                .body(deleteOrder).when().delete("/loan-service/deleteOrder").then().log().all()
                .extract().body().jsonPath().getObject("error", ErrorDataResponse.class);
        Assertions.assertNotNull(errorDataResponse.getCode());
        Assertions.assertNotNull(errorDataResponse.getMessage());

        Assertions.assertEquals(ErrorCodes.ORDER_NOT_FOUND.toString(), errorDataResponse.getCode());
        Assertions.assertEquals("Заявка не найдена", errorDataResponse.getMessage());
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


    @Test
    @Description("Проверка добавления двух одинаковых тарифов и правильного кода ответа")
    @DisplayName("Добавление двух одинаковых тарифов")
    void testNegativeAddTariff() {
        String bearerToken = Services.authentication(StrConsts.ADMIN_LOGIN.toString(), StrConsts.ADMIN_PASSWORD.toString());
        Services.tariffCreation(bearerToken, "SOCIAL+", "7.7%");

        TariffRequest tariffRequest2 = new TariffRequest("SOCIAL+", "7.7%");

        Specifications.installSpecification(Specifications.requestSpec(StrConsts.BASE_URL.toString()),
                Specifications.responseSpecBADREQUEST400());

        given().headers("Authorization", "Bearer " + bearerToken).body(tariffRequest2)
                .when().post("/loan-service/addTariff")
                .then().log().all();
    }
}
