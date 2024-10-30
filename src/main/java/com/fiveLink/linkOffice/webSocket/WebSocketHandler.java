//package com.fiveLink.linkOffice.webSocket;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fiveLink.linkOffice.chat.service.ChatMessageService;
//import com.fiveLink.linkOffice.chat.domain.ChatMessageDto;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.socket.CloseStatus;
//import org.springframework.web.socket.TextMessage;
//import org.springframework.web.socket.WebSocketSession;
//import org.springframework.web.socket.handler.TextWebSocketHandler;
//
//public class WebSocketHandler extends TextWebSocketHandler {
//    private final ChatMessageService chatMessageService;
//    private final ObjectMapper objectMapper= new ObjectMapper();
//
//    @Autowired
//    public WebSocketHandler(ChatMessageService chatMessageService) {
//        this.chatMessageService =chatMessageService;
//    }
//
//    @Override
//    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
//        String payload = message.getPayload();
//        System.out.println("Received: " + payload);
//
//        session.sendMessage(new org.springframework.web.socket.TextMessage(message.getPayload()));
//    }
//
//    @Override
//    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
//        System.out.println("Connected: " + session.getId());
//    }
//
//    @Override
//    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
//        System.out.println("Disconnected: " + session.getId());
//    }
//}
