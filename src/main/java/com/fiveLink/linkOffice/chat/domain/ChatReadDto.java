package com.fiveLink.linkOffice.chat.domain;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class ChatReadDto {
    private Long chat_read_no;
    private Long chat_message_no;
    private Long member_no;
    private LocalDateTime chat_read_date;
    private int unreadCount;

    public ChatRead toEntity() {
        return ChatRead.builder()
                .chatReadNo(chat_read_no)
                .chatMessageNo(chat_message_no)
                .memberNo(member_no)
                .chatReadDate(chat_read_date)
                .build();
    }

    public ChatReadDto toDto(ChatRead ChatRead){
        return ChatReadDto.builder()
                .chat_read_no(ChatRead.getChatReadNo())
                .chat_message_no(ChatRead.getChatMessageNo())
                .member_no(ChatRead.getMemberNo())
                .chat_read_date(ChatRead.getChatReadDate())
                .build();
    }
}
