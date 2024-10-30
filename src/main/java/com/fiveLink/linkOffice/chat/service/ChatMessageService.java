package com.fiveLink.linkOffice.chat.service;

import com.fiveLink.linkOffice.chat.domain.*;
import com.fiveLink.linkOffice.chat.repository.ChatMessageRepository;
import com.fiveLink.linkOffice.chat.repository.ChatReadRepository;
import com.fiveLink.linkOffice.mapper.ChatMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatMessageService {
    private final ChatMessageRepository chatMessageRepository;
    private final ChatReadRepository chatReadRepository;
    private final ChatMapper chatMapper;

    @Autowired
    public ChatMessageService(ChatMessageRepository chatMessageRepository, ChatMapper chatMapper, ChatReadRepository chatReadRepository){
        this.chatMessageRepository =chatMessageRepository;
        this.chatReadRepository = chatReadRepository;
        this.chatMapper = chatMapper;
    }
    // 채팅 메시지를 저장하는 메서드
    public void saveChatMessage(ChatMessageDto chatMessageDto) {
        ChatMessage chat = chatMessageDto.toEntity();
        chatMessageRepository.save(chat);
    }
    public List<Map<String, Object>> getChatMessages(Long roomNo, Long currentMember) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("roomNo", roomNo);
        paramMap.put("memberNo", currentMember);

        // Map을 파라미터로 전달
        return chatMapper.getChatMessages(paramMap);
    }
    public List<Long> markMessagesAsReadForChatRoom(Long memberNo, Long chatRoomNo){
        Map<String, Object> params = new HashMap<>();
        params.put("memberNo", memberNo);
        params.put("chatRoomNo", chatRoomNo);


        return chatMapper.markMessagesAsReadForChatRoom(params);
    }

    public void insertReadStatus(ChatReadDto chatReadDto) {
        ChatRead chat = chatReadDto.toEntity();
        chatReadRepository.save(chat);

    }

    public List<Map<String, Object>> getUnreadCounts(Long memberNo) {
        return chatMapper.getUnreadCounts(memberNo);
    }
    //채팅 내용 가져오기
    public String getChatMessageText(Long chatRoom) {
        return chatMapper.getChatMessageText(chatRoom);
    }
}