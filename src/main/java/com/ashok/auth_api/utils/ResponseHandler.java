package com.ashok.auth_api.utils;

public interface ResponseHandler {
   <T> ApiResponse<T> success(T data, String message,int status);
   <T> ApiResponse<T> error(String message, int status);
}
