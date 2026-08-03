package com.falcon.bursio.exception;

public class EmailReverificationRequiredException extends BaseException {
    public EmailReverificationRequiredException(String message) {
        super("EMAIL_REVERIFICATION_REQUIRED", message);
    }
}
