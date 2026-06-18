package com.falcon.bursio.exception;

public class EmailSendException extends BaseException {
    public EmailSendException(String message) {
        super("EMAIL_SEND_FAILED", message);
    }
}