package com.fiveLink.linkOffice.meeting.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "fl_meeting_reservation")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Builder
public class MeetingReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meeting_reservation_no")
    private Long meetingReservationNo;
 
    @Column(name = "meeting_no")
    private Long meetingNo;  
 
    @Column(name = "member_no")
    private Long memberNo;  

    @Column(name = "meeting_reservation_date")
    private String meetingReservationDate;

    @Column(name = "meeting_reservation_start_time")
    private String meetingReservationStartTime;

    @Column(name = "meeting_reservation_end_time")
    private String meetingReservationEndTime;

    @Column(name = "meeting_reservation_purpose")
    private String meetingReservationPurpose;

    @Column(name = "meeting_reservation_create_date")
    @CreationTimestamp
    private LocalDateTime meetingReservationCreateDate;

    @Column(name = "meeting_reservation_update_date")
    @UpdateTimestamp
    private LocalDateTime meetingReservationUpdateDate;

    @Column(name = "meeting_reservation_status")
    private Long meetingReservationStatus;
}
