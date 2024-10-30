package com.fiveLink.linkOffice.schedule.domain;

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
@Table(name = "fl_schedule")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Builder
public class Schedule {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_no")
    private Long scheduleNo;

    @Column(name = "member_no")
    private Long memberNo;
    
    @Column(name = "schedule_title")
    private String scheduleTitle;
    
    @Column(name = "schedule_comment")
    private String scheduleComment;
    
    @Column(name = "schedule_start_date")
    private String scheduleStartDate;

    @Column(name = "schedule_end_date")
    private String scheduleEndDate;
    
    @Column(name = "schedule_start_time")
    private String scheduleStartTime;

    @Column(name = "schedule_end_time")
    private String scheduleEndTime;
    
    @Column(name = "schedule_allday")
    private Long scheduleAllday;
    
    @Column(name = "schedule_category_no")
    private Long scheduleCategoryNo;
    
    @Column(name = "schedule_type")
    private Long scheduleType; 
    
    @Column(name = "department_no")
    private Long departmentNo;
    
    @Column(name = "meetingNo")
    private Long meetingNo;
    
    @Column(name = "schedule_repeat")
    private Long scheduleRepeat;
    
    @CreationTimestamp
    @Column(name = "schedule_create_date")
    private LocalDateTime scheduleCreateDate;
    
    @UpdateTimestamp
    @Column(name = "schedule_update_date")
    private LocalDateTime scheduleUpdateDate;
    
    @Column(name = "schedule_status")
    private Long scheduleStatus;
}
