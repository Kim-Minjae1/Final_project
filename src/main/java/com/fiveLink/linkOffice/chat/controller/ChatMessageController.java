package com.fiveLink.linkOffice.chat.controller;

import com.fiveLink.linkOffice.chat.domain.ChatMemberDto;
import com.fiveLink.linkOffice.chat.domain.ChatMessage;
import com.fiveLink.linkOffice.chat.service.ChatMemberService;
import com.fiveLink.linkOffice.chat.service.ChatMessageService;
import com.fiveLink.linkOffice.chat.domain.ChatMessageDto;
import com.fiveLink.linkOffice.member.domain.Member;
import com.fiveLink.linkOffice.member.domain.MemberDto;
import com.fiveLink.linkOffice.member.repository.MemberRepository;
import com.fiveLink.linkOffice.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller

public class ChatMessageController {
    private final ChatMessageService chatMessageService;
    private final MemberService memberService;
    private final ChatMemberService chatMemberService;
    private final MemberRepository memberRepository;

    @Autowired
    public ChatMessageController(ChatMessageService chatMessageService, MemberService memberService, MemberRepository memberRepository,ChatMemberService chatMemberService){
        this.chatMessageService =chatMessageService;
        this.memberService =memberService;
        this.chatMemberService =chatMemberService;
        this.memberRepository = memberRepository;

    }

    @GetMapping("/api/chat/{member_no}")
    public String chatMessagePage(@PathVariable("member_no") Long memberNo, Model model){
        try {
            List<MemberDto> memberDtoList = memberService.getMembersByNo(memberNo);
            List<ChatMemberDto> chatList = chatMemberService.selectChatList(memberNo);
            if (memberDtoList.isEmpty()) {
                model.addAttribute("error", "회원번호가 없습니다.");
                return "error";
            }

            model.addAttribute("memberdto", memberDtoList);
            model.addAttribute("chatList",chatList);


            List<Integer> chatRoomNos = new ArrayList<>();
            List<List<Long>> anotherUserList = new ArrayList<>();
            for (ChatMemberDto chatMember : chatList) {
                int type = chatMemberService.chatRoomType(chatMember.getChat_room_no());
                //본인을 제외한 사람들의 사진 정보를 위한 멤버 값
                anotherUserList.add(chatMemberService.selectAnotherUser(memberNo, chatMember.getChat_room_no()));
                chatRoomNos.add(type);
            }
            List<Map<String, Object>> combinedList = new ArrayList<>();
            System.out.println(anotherUserList);
            for (int i = 0; i < chatList.size(); i++) {
                ChatMemberDto chatMember = chatList.get(i);
                int chatType = chatRoomNos.get(i);
                List<Long> anotherUsers = anotherUserList.get(i);
                // 사용자들의 프로필 이미지 경로 가져오기
                List<String> profileImages = new ArrayList<>();
                for (Long anotherUserNo : anotherUsers) {
                    String profileImage = chatMemberService.getProfileImageByMemberNo(anotherUserNo);
                    profileImages.add(profileImage);
                }

                Map<String, Object> combinedItem = new HashMap<>();
                combinedItem.put("profileImages", profileImages);

                combinedItem.put("chatMember", chatMember);
                combinedItem.put("chatType", chatType);

                combinedList.add(combinedItem);
            }
            System.out.println(combinedList);
            model.addAttribute("combinedChatList", combinedList);


            return "admin/chat/chatMessage";
        } catch (Exception e) {
            model.addAttribute("error", "회원 정보를 가져오는 동안 오류가 발생했습니다.");
            return "error";
        }

    }
    @GetMapping("/api/chat/messages/{chat_room_no}/{currentMember}")
    @ResponseBody
    public List<Map<String, Object>> chatMessages(@PathVariable("chat_room_no") Long chatRoomNo, @PathVariable("currentMember") Long currentMember){
        try{
            return chatMessageService.getChatMessages(chatRoomNo, currentMember);
        }catch (Exception e){
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
    //실시간 사용자 사진
    @GetMapping("/api/getMemberImage/{memberNo}/{chatRoomNo}")
    @ResponseBody
    public String getMemberImage(@PathVariable("memberNo") Long memberNo, @PathVariable("chatRoomNo") Long chatRoomNo){
        try{
            List<Long> another = chatMemberService.selectAnotherUser(memberNo, chatRoomNo);
            String profileImage = chatMemberService.getProfileImageByMemberNo(another.get(0));
            return profileImage;
        }catch (Exception e){
            e.printStackTrace();
            return "값이 존재하지 않음.";
        }
    }


}