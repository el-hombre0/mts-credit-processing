package com.example.creditservice.api.enums;

public enum StrConsts {
    BASE_URL("http://localhost:8080"),
    ADMIN_LOGIN("ivanov@mail.ru"),
    ADMIN_PASSWORD("1234");

    private final String text;

    StrConsts(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
