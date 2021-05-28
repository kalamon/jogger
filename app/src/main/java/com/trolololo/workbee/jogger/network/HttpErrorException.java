package com.trolololo.workbee.jogger.network;

import java.io.IOException;

public class HttpErrorException extends IOException {
    private int errorCode;
    private String message;

    public HttpErrorException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
