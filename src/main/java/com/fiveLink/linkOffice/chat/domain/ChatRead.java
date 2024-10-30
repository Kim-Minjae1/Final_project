package com.fiveLink.linkOffice.chat.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name="fl_chat_read_status")
@NoArgsConstructor(access= AccessLevel.PROTECTED)
@AllArgsConstructor(access=AccessLevel.PROTECTED)
@Getter
@Builder
public class ChatRead {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="chat_read_no")
    private Long chatReadNo;

    @Column(name="chat_message_no")
    private Long chatMessageNo;

    @Column(name="member_no")
    private Long memberNo;

    @Column(name="chat_read_date")
    @CreationTimestamp
    private LocalDateTime chatReadDate;

}
