package com.fiveLink.linkOffice.attendance.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fiveLink.linkOffice.attendance.domain.Attendance;
import com.fiveLink.linkOffice.attendance.repository.AttendanceRepository;
import com.fiveLink.linkOffice.member.domain.Member;
import com.fiveLink.linkOffice.member.repository.MemberRepository;

@Component
public class AttendanceSchedulerService {

    private final AttendanceService attendanceService;
    private final AttendanceRepository attendanceRepository;
    private final MemberRepository memberRepository;
    
    @Autowired
    public AttendanceSchedulerService(AttendanceService attendanceService,
                                      AttendanceRepository attendanceRepository,
                                      MemberRepository memberRepository) {
        this.attendanceService = attendanceService;
        this.attendanceRepository = attendanceRepository;
        this.memberRepository = memberRepository;
    }
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AttendanceSchedulerService.class);
    
    // 매일 오전 9시 10분에 실행됨

    //@Scheduled(cron = "0 10 9 * * *")

    public void checkAndInsertAttendance() {
        // 모든 사원 조회 
        List<Member> members = memberRepository.findAll();
        
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        
        for (Member member : members) {
            
            // DB에 attendance 데이터 존재 여부 확인
            Attendance attendance = attendanceRepository.findByMemberMemberNoAndWorkDate(member.getMemberNo(), yesterday);
            
            // 출근 기록이 없으면 새로 추가
            if (attendance == null) {
                Attendance newAttendance = Attendance.builder()
                        .member(member)
                        .workDate(yesterday)
                        .checkInTime(null)
                        .checkOutTime(null)
                        .build();
                
                attendanceRepository.save(newAttendance);
            }
        }
    }
}
