package com.jsj.datacenter.adapter.dto;

public class ResponseResult<T> {
    private final String code;
    private final String message;
    private final T data;
    public ResponseResult(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
}
