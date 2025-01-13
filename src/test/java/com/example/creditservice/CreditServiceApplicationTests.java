package com.example.creditservice;

import com.example.creditservice.api.ErrorDataResponse;
import com.example.creditservice.api.OrderStatusRequest;
import com.example.creditservice.api.Specifications;
import com.example.creditservice.model.request.AuthenticationRequest;
import com.example.creditservice.model.request.CreateOrder;
import com.example.creditservice.model.request.DeleteOrder;
import com.example.creditservice.model.request.RegisterRequest;
import com.example.creditservice.model.response.AuthenticationResponse;
import com.example.creditservice.model.response.DataResponse;
import com.example.creditservice.model.response.DataResponseLoanOrder;
import com.example.creditservice.model.tariff.Tariff;
import io.restassured.http.ContentType;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;

@SpringBootTest
public class CreditServiceApplicationTests {
    private final static String BASE_URL = "http://localhost:8080";
    private final static String ADMIN_LOGIN = "ivanov@mail.ru";
    private final static String ADMIN_PASSWORD = "1234";
    private final static String USER_LOGIN = "petrov@gmail.com";
    private final static String USER_PASSWORD = "1q2w3e4r";

    /**
     * Метод получения тарифов
     */
    @Test
    public void checkGetUserTest() {
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
     * Метод подачи заявки на кредит
     */
    @Test
    public void testOrderingLoanService() {
        Specifications.installSpecification(Specifications.requestSpec(BASE_URL),
                Specifications.responseSpecOK200());
        String bearerToken = authentication("ivanov@mail.ru", "1234");
        CreateOrder orderDetails = new CreateOrder();
        orderDetails.setUserId(1);
        orderDetails.setTariffId(1);
        DataResponseLoanOrder order = given()
                .headers(
                        "Authorization",
                        "Bearer " + bearerToken,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .body(orderDetails).when().post("/loan-service/order")
                .then().log().all()
                .extract().body().jsonPath().getObject("data", DataResponseLoanOrder.class);
        Assert.assertNotNull(order.getOrderId());

    }

    private String authentication(String email, String password) {
        Specifications.installSpecification(Specifications.requestSpec(BASE_URL),
                Specifications.responseSpecOK200());
        AuthenticationRequest authReq = new AuthenticationRequest(email, password);
        AuthenticationResponse request = given().body(authReq).when().post("/auth/authenticate").then().log().all()
                .extract().as(AuthenticationResponse.class);

        return request.getToken();
    }

    /**
     * Тест аутентификации
     */
    @Test
    public void testAuthentication() {
        Specifications.installSpecification(Specifications.requestSpec(BASE_URL),
                Specifications.responseSpecOK200());
        AuthenticationRequest authReq = new AuthenticationRequest("petrov1@gmail.com", "1q2w3e4r");
        AuthenticationResponse request = given().body(authReq).when().post("/auth/authenticate").then().log().all()
                .extract().as(AuthenticationResponse.class);

        Assert.assertNotNull(request.getToken());

    }

    /**
     * Негативный сценарий - отправка двух запросов
     */
    @Test
    public void testNegativeOrderingLoanService() {
        Specifications.installSpecification(Specifications.requestSpec(BASE_URL),
                Specifications.responseSpecBADREQUEST400());
        String bearerToken = authentication("ivanov@mail.ru", "1234");
        CreateOrder orderDetails = new CreateOrder();
        orderDetails.setUserId(1);
        orderDetails.setTariffId(3);
        DataResponseLoanOrder order1 = given()
                .headers(
                        "Authorization",
                        "Bearer " + bearerToken,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .body(orderDetails).when().post("/loan-service/order")
                .then().log().all()
                .extract().body().jsonPath().getObject("data", DataResponseLoanOrder.class);
        Assert.assertNotNull(order1.getOrderId());

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
        Assert.assertNotNull(order1.getOrderId());
    }

    /**
     * Метод удаления заявки
     */
    @Test
    public void testDeleteLoanRequest() {
        Specifications.installSpecification(Specifications.requestSpec(BASE_URL),
                Specifications.responseSpecOK200());
        String bearerToken = authentication("ivanov@mail.ru", "1234");
        DeleteOrder deleteOrder = new DeleteOrder();
        deleteOrder.setUserId(1);
        deleteOrder.setOrderId(UUID.fromString("5ca0aedc-6911-408f-bc6a-300c5f2d77f5"));
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
     * Негативный метод удаления заявки
     */
    @Test
    public void testNegativeDeleteLoanRequest() {
        Specifications.installSpecification(Specifications.requestSpec(BASE_URL),
                Specifications.responseSpecBADREQUEST400());
        String bearerToken = authentication("ivanov@mail.ru", "1234");
        DeleteOrder deleteOrder = new DeleteOrder();
        deleteOrder.setUserId(1);
        deleteOrder.setOrderId(UUID.fromString("5ca0aedc-6911-408f-bc6a-300c5f2d77f5"));
        ErrorDataResponse errorDataResponse = given()
                .headers(
                        "Authorization",
                        "Bearer " + bearerToken,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .body(deleteOrder).when().contentType(ContentType.JSON).delete("/loan-service/deleteOrder").then().statusCode(400).log().all()
                .extract().body().jsonPath().getObject("error", ErrorDataResponse.class);
        Assert.assertNotNull(errorDataResponse.getCode());
        Assert.assertNotNull(errorDataResponse.getMessage());
    }

    /**
     * Метод получения статуса заявки
     */
    @Test
    public void testGetOrderStatus() {
        Specifications.installSpecification(Specifications.requestSpec(BASE_URL),
                Specifications.responseSpecOK200());
        String bearerToken = authentication("ivanov@mail.ru", "1234");
        OrderStatusRequest orderStatusRequest = new OrderStatusRequest(UUID.fromString("5ca0aedc-6911-408f-bc6a-300c5f2d77f5"));
        DataResponse orderStatusResponse = given()
                .headers(
                        "Authorization",
                        "Bearer " + bearerToken,
                        "Content-Type",
                        ContentType.JSON,
                        "Accept",
                        ContentType.JSON)
                .when()
                .get("/loan-service/getStatusOrder?orderId=" + orderStatusRequest.getOrderId())
                .then().log().all()
                .extract().body().jsonPath().getObject("data.orderStatus", DataResponse.class);
        Assert.assertNotNull(orderStatusResponse.getData());

    }

    /**
     * Метод регистрации
     */
    @Test
    public void testRegistration() {
        Specifications.installSpecification(Specifications.requestSpec(BASE_URL),
                Specifications.responseSpecOK200());
        RegisterRequest registerRequest = new RegisterRequest("Sergei1", "Petrov", "petrov1@gmail.com", "1q2w3e4r");
        AuthenticationResponse response = given().body(registerRequest)
                .when().post("/auth/register")
                .then().log().all()
                .extract().as(AuthenticationResponse.class);

        Assert.assertNotNull(response.getToken());
    }
}
