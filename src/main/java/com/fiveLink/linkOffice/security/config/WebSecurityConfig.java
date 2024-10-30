package com.fiveLink.linkOffice.security.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

@Configuration
public class WebSecurityConfig implements HttpSessionListener {

    private final DataSource dataSource;
    
    @Autowired
    public WebSecurityConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
        .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                .requestMatchers("/login", "/css/**", "/img/**", "/js/**").permitAll()
                .requestMatchers("/pwchange", "/error", "/error500", "/session-time").permitAll()
                .requestMatchers("/employee/**").hasAuthority("USER")
                .requestMatchers("/admin/approval/**").hasAnyAuthority("TOTAL_ADMIN", "DOCUMENT_ADMIN")
                .requestMatchers("/admin/inventory/**").hasAnyAuthority("TOTAL_ADMIN","INVENTORY_ADMIN")
                .requestMatchers("/admin/meeting/**").hasAnyAuthority("TOTAL_ADMIN","MEETING_ADMIN")
                .requestMatchers("/admin/member/**").hasAnyAuthority("TOTAL_ADMIN","MEMBER_ADMIN")
                .requestMatchers("/admin/notice/**").hasAnyAuthority("USER","TOTAL_ADMIN","NOTICE_ADMIN")
                .requestMatchers("/admin/organization/**").hasAnyAuthority("TOTAL_ADMIN","ORGANIZATION_ADMIN")
                .requestMatchers("/admin/permission/**").hasAnyAuthority("TOTAL_ADMIN","PERMISSION_ADMIN")
                .requestMatchers("/admin/schedule/**").hasAnyAuthority("TOTAL_ADMIN","SCHEDULE_ADMIN")
                .requestMatchers("/admin/vacation/**").hasAnyAuthority("TOTAL_ADMIN","VACATION_ADMIN")
                .anyRequest().authenticated()

            )
            .formLogin(login ->
                login
                    .loginPage("/login")
                    .loginProcessingUrl("/login")
                    .usernameParameter("member_number")
                    .passwordParameter("member_pw")
                    .permitAll()
                    .successHandler(new MyLoginSuccessHandler())
                    .failureHandler(new MyLoginFailureHandler()))
                    
            .logout(logout ->
                logout.permitAll())
            .rememberMe(rememberMe -> 
                rememberMe.rememberMeParameter("remember-me")
                    .tokenValiditySeconds(86400*7)
                    .alwaysRemember(false)
                    .tokenRepository(tokenRepository())
                    .authenticationSuccessHandler(rememberMeSuccessHandler()))
            .sessionManagement(sessionManagement ->
                sessionManagement
                    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) 
                    .invalidSessionUrl("/") 
                    .maximumSessions(1) 
                    .expiredUrl("/") 
            )
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean 
    public PersistentTokenRepository tokenRepository(){
        JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();
        jdbcTokenRepository.setDataSource(dataSource);
        return jdbcTokenRepository;
    }
    
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web -> 
                web.ignoring()
                    .requestMatchers(
                            PathRequest.toStaticResources().atCommonLocations()
                    ));
    }
    
    // remember-me 성공 시 핸들러
    @Bean
    public AuthenticationSuccessHandler rememberMeSuccessHandler() {
        SimpleUrlAuthenticationSuccessHandler handler = new SimpleUrlAuthenticationSuccessHandler();
        handler.setDefaultTargetUrl("/");
        return handler;
    }
    
    @Override
    public void sessionCreated(HttpSessionEvent se) {
        System.out.println("Session created: " + se.getSession().getId());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        System.out.println("Session destroyed: " + se.getSession().getId());
    }

    // 비밀번호 암호화
	 @Bean 
	 public PasswordEncoder passwordEncoder() { 
		 return new BCryptPasswordEncoder(); 
	}
	 

}