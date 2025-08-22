package com.example.poemai.utils;

// import java.util.HashMap;
// import java.util.Map;

/**
 * 统一 API 响应格式
 */
public class Result<T> {

    private int code;
    private String message;
    private T data;

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("成功");
        result.setData(data);
        return result;
    }

    public static Result<?> error(int code, String message) {
        Result<Object> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    public static Result<?> error(String message) {
        return error(500, message);
    }

    // Getter and Setter
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}