package com.SecurityLockers.SecureDeliveryLockers.utility;

import  com.SecurityLockers.SecureDeliveryLockers.entity.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

public class ResponseBuilder {

    public static <T> ResponseEntity<ApiResponse<T>> success(T data, String message) {
        return new ResponseEntity<>(
                new ApiResponse<>(HttpStatus.OK.value(), message, data),
                HttpStatus.OK
        );
    }

    public static <T> ResponseEntity<ApiResponse<T>> error(HttpStatus status, String message) {
        return new ResponseEntity<>(
                new ApiResponse<>(status.value(), message, null),
                status
        );
    }
}
