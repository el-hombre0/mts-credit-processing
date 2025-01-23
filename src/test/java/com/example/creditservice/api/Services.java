package com.example.creditservice.api;

import com.example.creditservice.api.enums.StrConsts;
import com.example.creditservice.model.request.AuthenticationRequest;
import com.example.creditservice.model.request.CreateOrder;
import com.example.creditservice.model.request.RegisterRequest;
import com.example.creditservice.model.response.AuthenticationResponse;
import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;

public class Services {

    /**
     * Сервисный метод аутентификации
     *
     * @param email    электронная почта пользователя
     * @param password пароль пользователя
     * @return JWT аутентификации
     */
    public static String authentication(String email, String password) {
        AuthenticationRequest authReq = new AuthenticationRequest(email, password);
        Specifications.installSpecification(Specifications.requestSpec(StrConsts.BASE_URL.toString()),
                Specifications.responseSpecOK200());
        AuthenticationResponse request = given().headers("Content-Type",
                        ContentType.JSON).body(authReq).when().post("/auth/authenticate").then().log().all()
                .extract().as(AuthenticationResponse.class);

        return request.getToken();
    }

    /**
     * Сервисный метод регистрации
     *
     * @param firstname Имя пользователя
     * @param lastname  Фамилия пользователя
     * @param email     электронная почта пользователя
     * @param password  пароль пользователя
     * @return JWT аутентификации
     */
    public static String registration(String firstname, String lastname, String email, String password) {
        RegisterRequest registerRequest = new RegisterRequest(firstname, lastname, email, password);
        Specifications.installSpecification(Specifications.requestSpec(StrConsts.BASE_URL.toString()),
                Specifications.responseSpecOK200());
        AuthenticationResponse request = given().headers("Content-Type",
                        ContentType.JSON).body(registerRequest).when().post("/auth/register").then().log().all()
                .extract().as(AuthenticationResponse.class);
        return request.getToken();

    }

    /**
     * Сервисный метод создания заявки на кредит
     *
     * @param userId   Идентификатор пользователя
     * @param tariffId Идентификатор тарифа
     * @return Объект, содержащий UUID заявки
     */
    public static DataResponseLoanOrderS orderLoan(String bearerToken, long userId, long tariffId) {
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

    /**
     * Сервисный метод создания нового тарифа
     *
     * @param token        JW-токен аутентификации
     * @param tariffType   создаваемый тип тарифа
     * @param interestRate процентная ставка по тарифу
     */
    public static void tariffCreation(String token, String tariffType, String interestRate) {
        TariffRequest tariffRequest = new TariffRequest(tariffType, interestRate);
        given().headers("Authorization", "Bearer " + token).body(tariffRequest)
                .when().post("/loan-service/addTariff")
                .then().log().all();
    }
}
