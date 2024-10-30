package com.fiveLink.linkOffice.chat.controller;

import com.fiveLink.linkOffice.chat.domain.ChatMemberDto;
import com.fiveLink.linkOffice.chat.domain.ChatRoom;
import com.fiveLink.linkOffice.chat.domain.ChatRoomDto;
import com.fiveLink.linkOffice.chat.service.ChatMemberService;
import com.fiveLink.linkOffice.chat.service.ChatRoomService;
import com.fiveLink.linkOffice.member.domain.MemberDto;
import com.fiveLink.linkOffice.vacation.domain.VacationTypeDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
public class ChatMemberController {
    private final ChatMemberService chatMemberService;

    @Autowired
    public ChatMemberController(ChatMemberService chatMemberService){
        this.chatMemberService =chatMemberService;

    }
    // 채팅방 이름 가져오기
    @GetMapping("/api/chat/roomName/{chatRoomNo}/{currentMember}")
    @ResponseBody
    public String roomName(@PathVariable("chatRoomNo") Long chatRoomNo, @PathVariable("currentMember") Long currentMember) {
        try {
            String chatRoomName = chatMemberService.selectChatRoomName(chatRoomNo, currentMember);

            if (chatRoomName != null) {
                return chatRoomName;
            } else {
                return "채팅방을 찾을 수 없습니다.";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "에러발생";
        }
    }

    @GetMapping("/api/chat/roomType/{chatRoomNo}")
    @ResponseBody
    public int chatRoomType(@PathVariable("chatRoomNo") Long chatRoomNo){
        return chatMemberService.chatRoomType(chatRoomNo);

    }

    @GetMapping("/api/chat/exist/{currentChatRoomNo}")
    @ResponseBody
    public List<Long> getChatRoomMemberNo(@PathVariable("currentChatRoomNo") Long currentChatRoomNo) {
        List<Long> memberNos = chatMemberService.chatRoomMemberNo(currentChatRoomNo);
        return memberNos;
    }

    // 채팅방 이름 가져오기(2)
    @GetMapping("/api/chat/room/name/{chatRoomNo}")
    @ResponseBody
    public String getMemberChatRoomName(@PathVariable("chatRoomNo") Long chatRoomNo) {
        try {
            String chatRoomName = chatMemberService.selectMemberChatRoomName(chatRoomNo);

            if (chatRoomName != null) {
                return chatRoomName;
            } else {
                return "채팅방을 찾을 수 없습니다.";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "에러발생";
        }
    }


}