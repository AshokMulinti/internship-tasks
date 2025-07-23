package com.ashok.auth_api.exceptions;

public class InvalidSignupDataException extends RuntimeException {
    public InvalidSignupDataException(String message) {
        super(message);
    }
}
