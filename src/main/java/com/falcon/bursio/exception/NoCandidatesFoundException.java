package com.falcon.bursio.exception;

import com.falcon.bursio.common.ErrorMessages;

public class NoCandidatesFoundException extends BaseException {
    public NoCandidatesFoundException() {
        super("NO_CANDIDATES_FOUND", ErrorMessages.NO_CANDIDATES_FOUND);
    }
}