package com.fiveLink.linkOffice.attendance.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fiveLink.linkOffice.attendance.domain.Attendance;
import com.fiveLink.linkOffice.attendance.domain.AttendanceDto;
import com.fiveLink.linkOffice.attendance.service.AttendanceService;
import com.fiveLink.linkOffice.member.domain.Member;
import com.fiveLink.linkOffice.member.repository.MemberRepository;

@Controller
public class AttendanceApiController {
	
	private static final Logger LOGGER
	   = LoggerFactory.getLogger(AttendanceApiController.class);

	@Autowired
	private AttendanceService attendanceService;
	private MemberRepository memberRepository;
	
   @Autowired
   public AttendanceApiController(AttendanceService attendanceService, MemberRepository memberRepository) {
      this.attendanceService = attendanceService;
      this.memberRepository = memberRepository; 
   }
	// 출근 여부 체크
   	@PostMapping("/attendance/check")
   	public ResponseEntity<Map<String, String>> attendanceCheck(@RequestBody Map<String, Long> payload) {
   		Map<String, String> resultMap = new HashMap<>();
   		resultMap.put("result", "0");
   		Long memberNo = payload.get("memberNo");
   		LocalDate today = LocalDate.now();
   		
   		AttendanceDto attendanceDto = attendanceService.findByMemberNoAndWorkDate(memberNo, today);
   		if(attendanceDto != null) {
   			resultMap.put("result", "1");
   		} 
   		return ResponseEntity.ok(resultMap);
   	}
   
	@PostMapping("/attendance/checkIn")
	@ResponseBody
	public Map<String, String> checkIn(@RequestBody Map<String, Long> payload){
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("res_code", "404");
        resultMap.put("res_msg", "경로 오류");

        Long memberNo = payload.get("memberNo");
        // 오늘 날짜와 시간 
        LocalDate today = LocalDate.now();
        LocalTime time = LocalTime.now();
        
        Member member = memberRepository.findByMemberNo(memberNo);
        
        // Attendance Entity로 build 
        Attendance attendance = Attendance.builder()
        		.member(member)
        		.workDate(today)
        		.checkInTime(time)
        		.build();

        // build한 Entity를 service로 넘겨줘서 return이 1이면 성공
        if (attendanceService.attendanceCheckIn(attendance) > 0) {
            resultMap.put("res_code", "200");
            resultMap.put("res_msg", "출근 확인되었습니다.");
        }
        return resultMap;
    }
	
	@PostMapping("/attendance/checkOut")
	@ResponseBody
	public Map<String, String> checkOut(@RequestBody Map<String, Long> payload){
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("res_code", "404");
        resultMap.put("res_msg", "퇴근 기록 중 오류가 발생했습니다.");

        Long memberNo = payload.get("memberNo");
        // 오늘 날짜와 시간 
        LocalDate today = LocalDate.now();
        LocalTime time = LocalTime.now();
        
        // 오늘 출근을 했는지 DB에서 조회
        AttendanceDto attendanceDto = attendanceService.findByMemberNoAndWorkDate(memberNo, today);
        
        // 조회 성공하면 
        if(attendanceDto != null) {
        	
        	Member member = memberRepository.findByMemberNo(memberNo);
        	
    		Attendance attendance = Attendance.builder()
    			.attendanceNo(attendanceDto.getAttendance_no())
    			.member(member)
    			.workDate(attendanceDto.getWork_date())
    			.checkInTime(attendanceDto.getCheck_in_time())
    			.checkOutTime(time)
    			.build();
    		
    		// 퇴근 기능 수행 
            if(attendanceService.attendanceCheckOut(attendance) > 0) {
            	resultMap.put("res_code", "200");
            	resultMap.put("res_msg", "퇴근 확인되었습니다.");  
        	}
        }
        
        return resultMap;
    }

}
