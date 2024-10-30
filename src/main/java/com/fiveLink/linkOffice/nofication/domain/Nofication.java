package com.fiveLink.linkOffice.nofication.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name="fl_nofication")
@NoArgsConstructor(access= AccessLevel.PROTECTED)
@AllArgsConstructor(access=AccessLevel.PROTECTED)
@Getter
@Builder
public class Nofication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="nofication_no")
    private Long noficationNo;

    @Column(name="member_no")
    private Long memberNo;

    @Column(name="nofication_type")
    private int noficationType;

    @Column(name="nofication_create_date")
    @CreationTimestamp
    private LocalDateTime noficationCreateDate;

    @Column(name="nofication_title")
    private String noficationTitle;

    @Column(name="nofication_content")
    private String noficationContent;

    @Column(name="nofication_status")
    private int noficationStatus;

    @Column(name="nofication_receive_no")
    private Long noficationReceiveNo;

    @Column(name="nofication_type_pk")
    private Long noficationTypePk;
}
