package com.falcon.bursio.exception;

public class InvalidTokenException extends BaseException {
    public InvalidTokenException(String message) {
        super("INVALID_TOKEN", message);
    }
}