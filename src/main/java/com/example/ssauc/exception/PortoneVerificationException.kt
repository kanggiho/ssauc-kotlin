package com.example.ssauc.exception;

public class PortoneVerificationException extends RuntimeException {
    public PortoneVerificationException(String message) {
        super(message);
    }

    // (선택사항) 원인(Throwable)을 함께 전달하는 생성자
    public PortoneVerificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
