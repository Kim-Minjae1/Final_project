package com.fiveLink.linkOffice.chat.service;

import com.fiveLink.linkOffice.chat.domain.ChatMember;
import com.fiveLink.linkOffice.chat.domain.ChatMemberDto;
import com.fiveLink.linkOffice.chat.repository.ChatMemberRepository;
import com.fiveLink.linkOffice.mapper.ChatMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatMemberService {
    private final ChatMemberRepository chatMemberRepository;
    private final ChatMapper chatMapper;


    @Autowired
    public ChatMemberService(ChatMemberRepository chatMemberRepository, ChatMapper chatMapper) {
        this.chatMemberRepository =chatMemberRepository;
        this.chatMapper =chatMapper;

    }

    public List<ChatMemberDto> selectChatList(Long memberNo){
        return chatMapper.selectChatList(memberNo);

    }
    public int createMemberRoomOne(ChatMemberDto dto) {

        int result = -1;
        try{
            ChatMember chatMember = dto.toEntity();
            chatMemberRepository.save(chatMember);
            result = 1;

        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }
    public int createMemberRoomMany(ChatMemberDto dto) {

        int result = -1;
        try{
            ChatMember chatMember = dto.toEntity();
            chatMemberRepository.save(chatMember);
            result = 1;

        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }
    public String selectChatRoomName(Long chatRoomNo, Long memberNo) {
        Map<String, Object> params = new HashMap<>();
        params.put("chatRoomNo", chatRoomNo);
        params.put("memberNo", memberNo);

        String roomName = chatMapper.selectChatRoomName(params);
        return roomName;
    }

    public int updateChatRoom(String roomName, Long memberNo, Long roomNo) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("roomName", roomName);
            params.put("memberNo", memberNo);
            params.put("roomNo", roomNo);

            chatMapper.updateChatRoom(params);
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int chatRoomType(Long chatRoomNo) {
        return chatMapper.chatRoomType(chatRoomNo);

    }
    public List<Long> chatRoomMemberNo(Long chatRoomNo) {
        return chatMapper.chatRoomMemberNo(chatRoomNo);
    }
    public String selectMemberChatRoomName(Long chatRoomNo){
        return chatMapper.selectMemberChatRoomName(chatRoomNo);
    }
    public List<Long> getMemberInfo(Long chatRoomNo) {
        return chatMapper.getMemberInfo(chatRoomNo);
    }
    public List<ChatMemberDto> getMembersByChatRoomNo(Long chatRoomNo){
        return chatMapper.getMembersByChatRoomNo(chatRoomNo);
    }
    public List<Long> selectAnotherUser(Long memberNo, Long chatRoomNo){
        Map<String, Object> params = new HashMap<>();
        params.put("chatRoomNo", chatRoomNo);
        params.put("memberNo", memberNo);

        return chatMapper.selectAnotherUser(params);
    }
    public String getProfileImageByMemberNo(Long memberNo) {
        // 멤버 번호로 프로필 이미지 경로를 가져오는 메서드
        return chatMapper.getProfileImageByMemberNo(memberNo);
    }


}
