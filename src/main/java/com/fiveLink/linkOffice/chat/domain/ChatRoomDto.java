package com.fiveLink.linkOffice.chat.domain;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class ChatRoomDto {
    private Long chat_room_no;
    private String chat_room_name;
    private int chat_room_type;
    private LocalDateTime chat_room_create_date;
    private LocalDateTime chat_room_update_date;

    public ChatRoom toEntity() {
        return ChatRoom.builder()
                .chatRoomNo(chat_room_no)
                .chatRoomName(chat_room_name)
                .chatRoomType(chat_room_type)
                .chatRoomCreateDate(chat_room_create_date)
                .chatRoomUpdateDate(chat_room_update_date).build();
    }

    public ChatRoomDto toDto(ChatRoom chatRoom){
        return ChatRoomDto.builder()
                .chat_room_no(chatRoom.getChatRoomNo())
                .chat_room_name(chatRoom.getChatRoomName())
                .chat_room_type(chatRoom.getChatRoomType())
                .chat_room_create_date(chatRoom.getChatRoomCreateDate())
                .chat_room_update_date(chatRoom.getChatRoomUpdateDate())
                .build();
    }
}
