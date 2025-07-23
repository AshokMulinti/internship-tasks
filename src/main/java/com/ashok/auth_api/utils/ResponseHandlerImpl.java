package com.ashok.auth_api.utils;

import org.springframework.stereotype.Component;

@Component
public class ResponseHandlerImpl implements ResponseHandler {
  @Override
    public <T> ApiResponse<T> success(T data, String message,int status){
      return new ApiResponse<>(status,message,data);
  }
  @Override
    public <T> ApiResponse<T> error(String message,int status){
      return new ApiResponse<>(status,message,null);
  }
}
