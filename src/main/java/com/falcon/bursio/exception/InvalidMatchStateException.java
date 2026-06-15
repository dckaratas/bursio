package com.falcon.bursio.exception;

public class InvalidMatchStateException extends BaseException {
    public InvalidMatchStateException(String message) {
        super("INVALID_MATCH_STATE", message);
    }
}