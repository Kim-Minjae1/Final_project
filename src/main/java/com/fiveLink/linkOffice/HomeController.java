package com.fiveLink.linkOffice;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fiveLink.linkOffice.approval.service.ApprovalService;
import com.fiveLink.linkOffice.attendance.domain.AttendanceDto;
import com.fiveLink.linkOffice.attendance.service.AttendanceService;
import com.fiveLink.linkOffice.meeting.domain.MeetingParticipantDto;
import com.fiveLink.linkOffice.meeting.domain.MeetingReservationDto;
import com.fiveLink.linkOffice.meeting.service.MeetingReservationService;
import com.fiveLink.linkOffice.member.domain.Member;
import com.fiveLink.linkOffice.member.domain.MemberDto;
import com.fiveLink.linkOffice.member.repository.MemberRepository;
import com.fiveLink.linkOffice.member.service.MemberService;
import com.fiveLink.linkOffice.notice.domain.NoticeDto;
import com.fiveLink.linkOffice.notice.service.NoticeService;
import com.fiveLink.linkOffice.organization.domain.DepartmentDto;
import com.fiveLink.linkOffice.organization.service.DepartmentService;
import com.fiveLink.linkOffice.schedule.domain.Schedule;
import com.fiveLink.linkOffice.schedule.domain.ScheduleCategoryDto;
import com.fiveLink.linkOffice.schedule.domain.ScheduleDto;
import com.fiveLink.linkOffice.schedule.domain.ScheduleException;
import com.fiveLink.linkOffice.schedule.domain.ScheduleExceptionDto;
import com.fiveLink.linkOffice.schedule.domain.ScheduleExceptionParticipantDto;
import com.fiveLink.linkOffice.schedule.domain.ScheduleParticipantDto;
import com.fiveLink.linkOffice.schedule.domain.ScheduleRepeat;
import com.fiveLink.linkOffice.schedule.domain.ScheduleRepeatDto;
import com.fiveLink.linkOffice.schedule.service.ScheduleCategoryService;
import com.fiveLink.linkOffice.schedule.service.ScheduleService;
import com.fiveLink.linkOffice.survey.domain.SurveyDto;
import com.fiveLink.linkOffice.survey.service.SurveyService;
import com.fiveLink.linkOffice.vacationapproval.domain.VacationApprovalDto;
import com.fiveLink.linkOffice.vacationapproval.service.VacationApprovalService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {
	
	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

	private final MemberService memberService;
	private final AttendanceService attendanceService;
	private final ApprovalService approvalService;
	private final ScheduleService scheduleService;
	private final NoticeService noticeService;
	private final SurveyService surveyService;
	 
	private final ScheduleCategoryService scheduleCategoryService; 
	private final DepartmentService departmentService;  
	private final VacationApprovalService vacationApprovalService; 
	private final MemberRepository memberRepository;
	private final MeetingReservationService meetingReservationService;

	@Autowired
	public HomeController(MemberService memberService,
			AttendanceService attendanceService, ApprovalService approvalService, ScheduleService scheduleService,
			ScheduleCategoryService scheduleCategoryService, DepartmentService departmentService,
			VacationApprovalService vacationApprovalService, MemberRepository memberRepository, MeetingReservationService meetingReservationService
			,NoticeService noticeService, SurveyService surveyService) {
		this.memberService = memberService;
		this.attendanceService = attendanceService;
		this.approvalService = approvalService;
		this.scheduleService = scheduleService;
		 
		this.scheduleCategoryService = scheduleCategoryService; 
		this.departmentService = departmentService;
		this.vacationApprovalService = vacationApprovalService;
		this.memberRepository = memberRepository;
		this.meetingReservationService = meetingReservationService;
		this.noticeService = noticeService;
		this.surveyService = surveyService;}

	@GetMapping("/login")
	public String loginPage(HttpSession session, Model model) {
	    String errorMsg = (String) session.getAttribute("error");
	    
	    if (errorMsg != null) {
            model.addAttribute("error", errorMsg);
            session.removeAttribute("error");
        }
		return "login";
	}

	@GetMapping("/pwchange")
	public String pwchangePage() {
		return "pwchange";
	}

	@GetMapping({"/",""})
	public String home(HttpServletRequest request, Model model) {

			Long member_no = memberService.getLoggedInMemberNo();
			List<MemberDto> memberdto = memberService.getMembersByNo(member_no);

		    
		    // [박혜선] 현재 시간을 00:00:00 형태로 만들기
		    LocalTime now = LocalTime.now();
		    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
		    String time = now.format(dtf);
		    
		    // [박혜선] 출퇴근 기록 조회
		    Long memberNo = memberdto.get(0).getMember_no();
		    LocalDate today = LocalDate.now();
		    AttendanceDto attendanceDto = attendanceService.findByMemberNoAndWorkDate(memberNo, today);
		    logger.info("AttendanceDto: {}", attendanceDto);
		    String isCheckedIn = "false";
	        String isCheckedOut = "false";
	        
	        if (attendanceDto != null) {
	            if (attendanceDto.getCheck_in_time() != null) {
	                isCheckedIn = "true";
	                model.addAttribute("checkInTime", attendanceDto.getCheck_in_time().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
	            }
	            if (attendanceDto.getCheck_out_time() != null) {
	                isCheckedOut = "true";
	                model.addAttribute("checkOutTime", attendanceDto.getCheck_out_time().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
	            }
	        }
	        // 공지사항
	        Pageable noticePageable = PageRequest.of(0, 5);  
	        NoticeDto noticeSearchDto = new NoticeDto();
	        Page<NoticeDto> allNotices = noticeService.getAllNoticePage(noticePageable, "latest", noticeSearchDto);  
	        model.addAttribute("noticeList", allNotices.getContent());
	        
	    
	     // 설문 목록 조회 (진행중인 설문)
	        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Order.desc("surveyStartDate")));  
	        SurveyDto searchDto = new SurveyDto();  
	        Page<SurveyDto> mySurveyList = surveyService.getIngAllSurveyPage1(pageable, searchDto, member_no);

	        // 필터링된 설문 중에서 5개만 반환하도록 처리
	        model.addAttribute("mySurveyList", mySurveyList.getContent()); 
	        
	        
	        // [전주영] 전자결재 개수 조회
	        long approvalCount = approvalService.countApprovalHistory(member_no);
	        long referenceCount = approvalService.countApprovalReferences(member_no);
	        long progressCount = approvalService.countApprovalProgress(memberNo);
	        
		    // [전주영] 멤버 객체 전달
		    model.addAttribute("memberdto", memberdto);
		    
		    // [전주영] 결재 내역함 개수
		    model.addAttribute("approvalCount", approvalCount);
		    // [전주영] 결재 참조함 개수
		    model.addAttribute("referenceCount", referenceCount);
		    // [전주영] 결재 진행함 개수
		    model.addAttribute("progressCount", progressCount);
		    
		    // [박혜선] 현재 시간 전달
		    model.addAttribute("time", time);
		    
		    // [박혜선] 출퇴근 여부 전달
		    model.addAttribute("isCheckedIn", isCheckedIn);
		    model.addAttribute("isCheckedOut", isCheckedOut); 

		    model.addAttribute("mySurveyList", mySurveyList.getContent());
		      

		    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		    boolean isAdmin = authentication.getAuthorities().stream()
		                         .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("TOTAL_ADMIN"));

		    if (isAdmin) {
		        return "redirect:/admin/member/list"; 
		    }
		    

		    return "home";
		
	}
	
	// [서혜원] 관리자 - 월간 일정
	@ResponseBody
	@GetMapping("/home/api/company/schedules")
	public List<ScheduleDto> getSchedules() {
		List<Schedule> schedules = scheduleService.getAllSchedules();

		List<ScheduleDto> scheduleDtos = new ArrayList<>();
		for (Schedule schedule : schedules) {
			ScheduleDto dto = ScheduleDto.toDto(schedule);
			scheduleDtos.add(dto);
		} 
		return scheduleDtos;
	}

	// [서혜원] 관리자 - 반복 일정
	@ResponseBody
	@GetMapping("/home/api/repeat/schedules")
	public List<ScheduleRepeatDto> getRepeatSchedules() {
		List<ScheduleRepeat> scheduleRepeats = scheduleService.getAllRepeatSchedules();

		List<ScheduleRepeatDto> scheduleRepeatDtos = new ArrayList<>();
		for (ScheduleRepeat scheduleRepeat : scheduleRepeats) {
			ScheduleRepeatDto dto = ScheduleRepeatDto.toDto(scheduleRepeat);
			scheduleRepeatDtos.add(dto);
		}
 
		return scheduleRepeatDtos;
	}
	
    // [서혜원] 예외 일정
	@ResponseBody
	@GetMapping("/home/api/company/exception/schedules")
	public List<ScheduleExceptionDto> getExceptionSchedules() {
	    List<ScheduleException> exceptionSchedules = scheduleService.getAllExceptionSchedules();

	    List<ScheduleExceptionDto> scheduleExceptionDtos = new ArrayList<>();
	    for (ScheduleException scheduleException : exceptionSchedules) {
	        ScheduleExceptionDto dto = ScheduleExceptionDto.toDto(scheduleException);
	         
	        String memberName = memberRepository.findById(scheduleException.getMemberNo())
	                .map(Member::getMemberName)
	                .orElse("사원");

	        String positionName = "직위";
	        String departmentName = "부서";
	         
	        List<Object[]> memberInfo = memberRepository.findMemberWithDepartmentAndPosition(scheduleException.getMemberNo());
	        
	        if (!memberInfo.isEmpty()) {
	            Object[] row = memberInfo.get(0);
	            positionName = (String) row[1];
	            departmentName = (String) row[2];
	        }
	         
	        dto.setMember_name(memberName);
	        dto.setDepartment_name(departmentName);
	        dto.setPosition_name(positionName);

	        scheduleExceptionDtos.add(dto);
	    }

	    return scheduleExceptionDtos;
	}

	// [서혜원] 개인 일정
	@ResponseBody
	@GetMapping("/home/api/personal/schedules/{memberNo}")
	public List<ScheduleDto> getpersonalSchedules(@PathVariable("memberNo") Long memberNo) {
		List<Schedule> schedules = scheduleService.getAllpersonalSchedules(memberNo);
		
		List<ScheduleDto> personalResult = new ArrayList<>();
		for (Schedule schedule : schedules) {
			ScheduleDto dto = ScheduleDto.toDto(schedule);
			personalResult.add(dto);
		}   
		return personalResult;
	} 
	 
	// [서혜원] 부서 일정
	@ResponseBody
	@GetMapping("/home/api/department/schedules")
	public List<ScheduleDto> getpersonalSchedules() {
		List<Schedule> schedules = scheduleService.getAlldepartmentSchedules();

		List<ScheduleDto> departmentResult = new ArrayList<>();
		for (Schedule schedule : schedules) {
			ScheduleDto dto = ScheduleDto.toDto(schedule);
			
	        String memberName = memberRepository.findById(schedule.getMemberNo())
	                .map(Member::getMemberName)
	                .orElse("사원"); 
	        
            String positionName = "직위";
            String departmentName = "부서"; 
			List<Object[]> memberInfo = memberRepository.findMemberWithDepartmentAndPosition(schedule.getMemberNo()); 
            
            Object[] row = memberInfo.get(0);  
            positionName = (String) row[1];   
            departmentName = (String) row[2]; 
              
	        dto.setDepartment_name(departmentName);
	        dto.setPosition_name(positionName);
	        dto.setMember_name(memberName);
			departmentResult.add(dto);
		} 
		return departmentResult;
	} 
	
	// [서혜원] 참여자 일정 
	@ResponseBody
	@GetMapping("/home/api/participate/schedules")
	public List<ScheduleDto> getparticipateSchedules() {
	    List<Schedule> schedules = scheduleService.getAllparticipateSchedules();
	    
	    List<ScheduleDto> participateResult = new ArrayList<>(); 
	    for (Schedule schedule : schedules) { 
	        ScheduleDto dto = ScheduleDto.toDto(schedule);
	         
	        String memberName = memberRepository.findById(schedule.getMemberNo())
	                .map(Member::getMemberName)
	                .orElse("사원"); 
	        
            String positionName = "직위";
            String departmentName = "부서"; 
            
            List<Object[]> memberInfo = memberRepository.findMemberWithDepartmentAndPosition(schedule.getMemberNo()); 
            
            Object[] row = memberInfo.get(0);  
            positionName = (String) row[1];   
            departmentName = (String) row[2]; 
            
	        dto.setMember_name(memberName); 
	        dto.setDepartment_name(departmentName);
	        dto.setPosition_name(positionName);
	        
	        participateResult.add(dto);
	    }  
	    return participateResult;
	}

	
	// [서혜원] 참여자 정보  
	@GetMapping("/home/api/participate/member/schedules/{scheduleNo}/{memberNo}")
	@ResponseBody
	public Map<String, Object> getparticipateMemberSchedules(@PathVariable("scheduleNo") Long scheduleNo, @PathVariable("memberNo") Long memberNo) {
	    Map<String, Object> response = new HashMap<>();
	     
	    List<ScheduleParticipantDto> participants = scheduleService.getParticipantsByReservationNoOwn(scheduleNo, memberNo);
	     
	    response.put("participants", participants); 
	    return response;
	} 
	
	// [서혜원] 부서 정보
    @ResponseBody
    @GetMapping("/home/api/schedule/department/list")
    public List<DepartmentDto> getAllDepartments() {
        List<DepartmentDto> departments = departmentService.getSecondDepartments(); 
        return departments;
    }


    // [서혜원] 예외 일정 참여자 정보  
	@GetMapping("/home/api/participate/member/schedules/exception/{scheduleExceptionNo}/{memberNo}")
	@ResponseBody
	public Map<String, Object> getparticipateMemberExceptionSchedules(@PathVariable("scheduleExceptionNo") Long scheduleExceptionNo, @PathVariable("memberNo") Long memberNo) {
	    Map<String, Object> response = new HashMap<>();
	     
	    List<ScheduleExceptionParticipantDto> participants = scheduleService.getParticipantsByExceptionReservationNo(scheduleExceptionNo, memberNo);
	     
	    response.put("participants", participants);  
	    return response;
	}

	// [서혜원] 예외 일정 참여자
	@GetMapping("/home/employee/schedule/participate/exception/{scheduleNo}")
	@ResponseBody
	public Map<String, Object> getExceptionScheduleParticipant(@PathVariable("scheduleNo") Long scheduleNo) {
	    Map<String, Object> response = new HashMap<>();
	     
	    List<ScheduleExceptionParticipantDto> participants = scheduleService.getParticipantsByExceptionReservationNo(scheduleNo);
	     
	    response.put("participants", participants);
	    
	    return response;
	}
	
	// [서혜원] 휴가 정보
	@GetMapping("/home/api/employee/vacation/schedules")
	@ResponseBody
	public Map<String, Object> getVacationSchedules() {
        Map<String, Object> response = new HashMap<>();
         
        List<VacationApprovalDto> vacationSchedules = vacationApprovalService.getApprovedVacationSchedules();
         
        response.put("vacationSchedules", vacationSchedules);
        
        return response;
    }
	
	// [서혜원] 회의 정보
	@GetMapping("/home/api/employee/meeting/schedules")
	@ResponseBody
	public Map<String, Object> getMeetingSchedules() {
	    Map<String, Object> response = new HashMap<>();
	    
	    List<MeetingReservationDto> meetingSchedules = meetingReservationService.getMeetingSchedules();
	    
	    response.put("meetingSchedules", meetingSchedules);
	    
	    return response;
	} 
	
	// [서혜원] 회의실 참여자
	@GetMapping("/home/api/employee/meeting/schedules/{scheduleNo}/{memberNo}")
	@ResponseBody
	public Map<String, Object> getparticipateMeeting(@PathVariable("scheduleNo") Long scheduleNo, @PathVariable("memberNo") Long memberNo) {
	    Map<String, Object> response = new HashMap<>();
	     
	    List<MeetingParticipantDto> participants = meetingReservationService.getParticipantsByMeetingNoOwn(scheduleNo, memberNo);
	     
	    response.put("participants", participants); 
	    return response;
	}  
	
	// [서혜원] 일정 카테고리	
	@GetMapping("/home/categories")
	@ResponseBody
	public List<ScheduleCategoryDto> getAlladminScheduleCategory() { 
		return scheduleCategoryService.getAlladminScheduleCategory();
	}
	
	// [서혜원] 회의실 예약 내역
	@GetMapping("/home/reservations")
	@ResponseBody
	public List<MeetingReservationDto> getReservations() { 
	    List<MeetingReservationDto> reservations = meetingReservationService.getAllReservations(); 
	    return reservations;
	}
	
	
	
}