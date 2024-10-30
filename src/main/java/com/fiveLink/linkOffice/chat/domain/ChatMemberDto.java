package com.fiveLink.linkOffice.chat.domain;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder

public class ChatMemberDto {
    private Long chat_member_no;
    private Long chat_room_no;
    private Long member_no;
    private String chat_member_room_name;
    private LocalDateTime chat_member_join_date;
    private int chat_member_par;
    private int chat_member_nofication;
    private int chat_member_pin;
    private LocalDateTime chat_member_pin_time;

    public ChatMember toEntity() {
        return ChatMember.builder()
                .chatMemberNo(chat_member_no)
                .chatRoomNo(chat_room_no)
                .memberNo(member_no)
                .chatMemberRoomName(chat_member_room_name)
                .chatMemberJoinDate(chat_member_join_date)
                .chatMemberPar(chat_member_par)
                .chatMemberNofication(chat_member_nofication)
                .chatMemberPin(chat_member_pin)
                .chatMemberPinTime(chat_member_pin_time)
                .build();
    }
    public ChatMemberDto toDto(ChatMember chatMember){
        return ChatMemberDto.builder()
                .chat_member_no(chatMember.getChatMemberNo())
                .chat_room_no(chatMember.getChatRoomNo())
                .member_no(chatMember.getMemberNo())
                .chat_member_room_name(chatMember.getChatMemberRoomName())
                .chat_member_join_date(chatMember.getChatMemberJoinDate())
                .chat_member_par(chatMember.getChatMemberPar())
                .chat_member_nofication(chatMember.getChatMemberNofication())
                .chat_member_pin(chatMember.getChatMemberPin())
                .chat_member_pin_time(chatMember.getChatMemberPinTime())
                .build();
    }

}
