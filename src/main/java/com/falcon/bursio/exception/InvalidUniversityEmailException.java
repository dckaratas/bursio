package com.falcon.bursio.exception;

public class InvalidUniversityEmailException extends BaseException {
    public InvalidUniversityEmailException(String message) {
        super("INVALID_UNIVERSITY_EMAIL", message);
    }
}