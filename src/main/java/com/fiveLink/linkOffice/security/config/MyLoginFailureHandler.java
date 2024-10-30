package com.fiveLink.linkOffice.security.config;

import java.io.IOException;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.fiveLink.linkOffice.security.exception.CustomUserResignedException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
@Component
public class MyLoginFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {

       
        String errorMsg;
        
        Throwable cause = exception.getCause();

        if (cause instanceof CustomUserResignedException) {
            String message = cause.getMessage();
            
            if (message.contains("퇴사하였습니다")) {
                errorMsg = "퇴사한 사용자는 접근할 수 없습니다.";
            } else if (message.contains("찾을 수 없습니다")) {
                errorMsg = "사용자를 찾을 수 없습니다.";
            } else {
                errorMsg = "로그인 중 오류가 발생했습니다.";
            }
        } else if (exception instanceof BadCredentialsException) {
            errorMsg = "아이디 또는 비밀번호가 일치하지 않습니다.";
        } else {
            errorMsg = "로그인 중 오류가 발생했습니다.";
        }
        
        request.getSession().setAttribute("error", errorMsg);

        response.sendRedirect("/");
    }
}


