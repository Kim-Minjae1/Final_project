package com.fiveLink.linkOffice.chat.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name="fl_chat_member")
@NoArgsConstructor(access= AccessLevel.PROTECTED)
@AllArgsConstructor(access=AccessLevel.PROTECTED)
@Getter
@Builder
public class ChatMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="chat_member_no")
    private Long chatMemberNo;

    @Column(name="chat_room_no")
    private Long chatRoomNo;

    @Column(name="member_no")
    private Long memberNo;

    @Column(name="chat_member_room_name")
    private String chatMemberRoomName;

    @Column(name="chat_member_join_date")
    @CreationTimestamp
    private LocalDateTime chatMemberJoinDate;

    @Column(name="chat_member_par")
    private int chatMemberPar;

    @Column(name="chat_member_nofication")
    private int chatMemberNofication;

    @Column(name="chat_member_pin")
    private int chatMemberPin;

    @Column(name="chat_member_pin_time")
    private LocalDateTime chatMemberPinTime;

}
