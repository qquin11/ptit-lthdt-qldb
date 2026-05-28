package com.app.service;

/**
 * Unchecked exception cho lỗi DAO/Service. UI bắt và hiển thị qua Toast.
 * Wraps SQLException để UI không phải import java.sql.*.
 */
public class DataAccessException extends RuntimeException {

    public DataAccessException(String message) {
        super(message);
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
