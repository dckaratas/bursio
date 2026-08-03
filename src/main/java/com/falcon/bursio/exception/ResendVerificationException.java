package com.falcon.bursio.exception;

public class ResendVerificationException extends BaseException {
    public ResendVerificationException(String message) {
        super("RESEND_VERIFICATION_ERROR", message);
    }
}