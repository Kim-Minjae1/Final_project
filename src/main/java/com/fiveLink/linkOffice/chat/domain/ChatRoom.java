package com.fiveLink.linkOffice.chat.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name="fl_chat_room")
@NoArgsConstructor(access= AccessLevel.PROTECTED)
@AllArgsConstructor(access=AccessLevel.PROTECTED)
@Getter
@Builder
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="chat_room_no")
    private Long chatRoomNo;

    @Column(name="chat_room_name")
    private String chatRoomName;

    @Column(name="chat_room_type")
    private int chatRoomType;

    @Column(name="chat_room_create_date")
    @CreationTimestamp
    private LocalDateTime chatRoomCreateDate;

    @Column(name="chat_room_update_date")
    @UpdateTimestamp
    private LocalDateTime chatRoomUpdateDate;

}
