package com.fiveLink.linkOffice.schedule.domain;

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
@Table(name = "fl_schedule_exception_participant")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Builder
public class ScheduleExceptionParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_exception_participant_no")
    private Long scheduleExceptionParticipantNo;

    @Column(name = "schedule_exception_no")
    private Long scheduleExceptionNo;

    @Column(name = "member_no")
    private Long memberNo;

    @Column(name = "schedule_exception_participant_status")
    private Long scheduleExceptionParticipantStatus;
}