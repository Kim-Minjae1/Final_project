package com.fiveLink.linkOffice.attendance.domain;

import java.time.LocalDate;
import java.time.LocalTime;

import com.fiveLink.linkOffice.member.domain.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "fl_attendance")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class Attendance {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attendance_no")
    private Long attendanceNo;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="member_no")
	private Member member;
	
	@Column(name = "work_date")
    private LocalDate workDate;

	@Column(name = "check_in_time")
	private LocalTime checkInTime;
	
	@Column(name = "check_out_time")
	private LocalTime checkOutTime;
	
	public AttendanceDto toDto() {
        return AttendanceDto.builder()
                .attendance_no(attendanceNo)
                .member_no(member.getMemberNo())
                .work_date(workDate)
                .check_in_time(checkInTime)
                .check_out_time(checkOutTime)
                .build();
    }
}
