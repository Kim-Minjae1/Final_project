package com.fiveLink.linkOffice.security.config;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;

@RestController
public class SessionController {


	@GetMapping("/session-time")
	public long getSessionTimeLeft(HttpSession session) {
	    long maxInactiveInterval = session.getMaxInactiveInterval();
	    long lastAccessedTime = session.getLastAccessedTime();
	    long currentTime = System.currentTimeMillis();

	    long elapsed = (currentTime - lastAccessedTime) / 1000;
	    long remaining = maxInactiveInterval - elapsed;
	    
	    // 세션 시간 확인 코드
	    System.out.println("Max Inactive Interval: " + maxInactiveInterval);
	    System.out.println("Last Accessed Time: " + lastAccessedTime);
	    System.out.println("Current Time: " + currentTime);
	    System.out.println("Elapsed Time: " + elapsed);
	    System.out.println("Remaining Time: " + remaining);

	    return Math.max(remaining, 0);
	}

}