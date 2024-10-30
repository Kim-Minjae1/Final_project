package com.fiveLink.linkOffice.meeting.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "fl_meeting")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Builder
public class Meeting {
    @Id
    @Column(name = "meeting_no")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long meetingNo;

    @Column(name = "meeting_name")
    private String meetingName;

    @Column(name = "meeting_max")
    private Long meetingMax;

    @Column(name = "meeting_available_start")
    private String meetingAvailableStart;

    @Column(name = "meeting_available_end")
    private String meetingAvailableEnd;

    @Column(name = "meeting_ori_image")
    private String meetingOriImage;

    @Column(name = "meeting_new_image")
    private String meetingNewImage;

    @Column(name = "meeting_comment")
    private String meetingComment;

    @Column(name = "meeting_create_date")
    @CreationTimestamp
    private LocalDateTime meetingCreateDate;

    @Column(name = "meeting_update_date")
    @UpdateTimestamp
    private LocalDateTime meetingUpdateDate;

    @Column(name = "meeting_status")
    private Long meetingStatus;
}
