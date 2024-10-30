package com.fiveLink.linkOffice.chat.domain;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class ChatMessageDto {
    private Long chat_message_no;
    private Long chat_sender_no;
    private Long chat_room_no;
    private String chat_content;
    private LocalDateTime chat_message_create_date;
    private String chat_sender_name;

    public ChatMessage toEntity(){
        return ChatMessage.builder()
                .chatMessageNo(chat_message_no)
                .chatSenderNo(chat_sender_no)
                .chatRoomNo(chat_room_no)
                .chatContent(chat_content)
                .chatMessageCreateDate(chat_message_create_date)
                .build();

    }

    public ChatMessageDto toDto(ChatMessage chatMessage){
        return ChatMessageDto.builder()
                .chat_message_no(chatMessage.getChatMessageNo())
                .chat_sender_no(chatMessage.getChatSenderNo())
                .chat_room_no(chatMessage.getChatRoomNo())
                .chat_content(chatMessage.getChatContent())
                .chat_message_create_date(chatMessage.getChatMessageCreateDate())
                .build();

    }

}
