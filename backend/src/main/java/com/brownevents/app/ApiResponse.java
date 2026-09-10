package com.brownevents.app;

public class ApiResponse<T> {
    private final T data;

    public ApiResponse(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }
}
