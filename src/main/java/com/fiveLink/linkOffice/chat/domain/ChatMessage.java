package com.fiveLink.linkOffice.chat.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name="fl_chat_message")
@NoArgsConstructor(access= AccessLevel.PROTECTED)
@AllArgsConstructor(access=AccessLevel.PROTECTED)
@Getter
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="chat_message_no")
    private Long chatMessageNo;

    @Column(name="chat_room_no")
    private Long chatRoomNo;

    @Column(name="chat_sender_no")
    private Long chatSenderNo;

    @Column(name="chat_content")
    private String chatContent;

    @Column(name="chat_message_create_date")
    @CreationTimestamp
    private LocalDateTime chatMessageCreateDate;


}
