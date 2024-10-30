package com.fiveLink.linkOffice.webSocket;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
//@EnableWebSocketMessageBroker //STOMP 사용
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChatWebSocketHandler chatWebSocketHandler;
    private final NoficationWebSocketHandler noficationWebSocketHandler;
    @Autowired
    public WebSocketConfig(ChatWebSocketHandler chatWebSocketHandler, NoficationWebSocketHandler noficationWebSocketHandler) {
        this.chatWebSocketHandler = chatWebSocketHandler;
        this.noficationWebSocketHandler = noficationWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 채팅용 핸들러
        registry.addHandler(chatWebSocketHandler, "/websocket/chat").setAllowedOrigins("*");
        // 알림용 핸들러
        registry.addHandler(noficationWebSocketHandler, "/websocket/notifications").setAllowedOrigins("*");
    }


}
