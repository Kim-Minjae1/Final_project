package com.fiveLink.linkOffice.security.exception;

import org.springframework.security.core.AuthenticationException;

public class CustomUserResignedException extends AuthenticationException {
    public CustomUserResignedException(String message) {
        super(message);
    }
}
