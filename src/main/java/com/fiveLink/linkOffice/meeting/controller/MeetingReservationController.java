package com.fiveLink.linkOffice.meeting.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fiveLink.linkOffice.meeting.domain.MeetingDto;
import com.fiveLink.linkOffice.meeting.domain.MeetingParticipantDto;
import com.fiveLink.linkOffice.meeting.domain.MeetingReservationDto;
import com.fiveLink.linkOffice.meeting.service.MeetingParticipantService;
import com.fiveLink.linkOffice.meeting.service.MeetingReservationService;
import com.fiveLink.linkOffice.meeting.service.MeetingService;
import com.fiveLink.linkOffice.member.domain.MemberDto;
import com.fiveLink.linkOffice.member.service.MemberService;
import com.fiveLink.linkOffice.organization.domain.DepartmentDto;
import com.fiveLink.linkOffice.organization.service.DepartmentService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class MeetingReservationController {

    private final MeetingService meetingService; 
    private final MemberService memberService;
    private final MeetingReservationService meetingReservationService;
    private final DepartmentService departmentService;
    private final MeetingParticipantService meetingParticipantService;
    
    @Autowired
    public MeetingReservationController(MeetingService meetingService, MemberService memberService, MeetingReservationService meetingReservationService, 
    									DepartmentService departmentService, MeetingParticipantService meetingParticipantService) {
        this.meetingService = meetingService; 
        this.memberService = memberService;
        this.meetingReservationService = meetingReservationService;
        this.departmentService = departmentService;
        this.meetingParticipantService = meetingParticipantService;
    }

    // 사용자 예약 페이지
    @GetMapping("/employee/meeting/reservation")
    public String empListMeetings(Model model) {
        Long memberNo = memberService.getLoggedInMemberNo();  
        List<MemberDto> memberDto = memberService.getMembersByNo(memberNo);
        List<MeetingDto> meetings = meetingService.getAllMeetings();
 
        model.addAttribute("memberdto", memberDto.get(0));  
        model.addAttribute("meetings", meetings);
        return "/employee/meeting/meetingReservation";
    }
     
    // 해당 날짜 예약 정보 
    @GetMapping("/date/reservations")
    @ResponseBody
    public List<MeetingReservationDto> getReservationsByDate(@RequestParam("date") String date) { 
        return meetingReservationService.getReservationsByDate(date);
    }
    
    // 전체 회의실 목록
    @GetMapping("/api/meetings")
    @ResponseBody
    public List<MeetingDto> getAllMeetings() {
        return meetingService.getAllMeetings();
    }
    
    // 특정 회의실 상세 정보 조회
    @GetMapping("/api/meetings/{meetingNo}")
    @ResponseBody
    public MeetingDto getMeetingById(@PathVariable("meetingNo") Long meetingId) {
        return meetingService.getMeetingById(meetingId);
    }  
    
    
    
    // 조직도
    @GetMapping("/meeting/chart")
    @ResponseBody
	public List<Map<String, Object>> getOrganizationChart() {
		List<DepartmentDto> departments = departmentService.getAllDepartments();
		List<MemberDto> members = memberService.getAllMembersChartOut();
		return buildTree(departments, members);
	}

    private List<Map<String, Object>> buildTree(List<DepartmentDto> departments, List<MemberDto> members) {
        Map<Long, Map<String, Object>> departmentMap = new HashMap<>();
        Map<Long, List<MemberDto>> membersByDepartment = new HashMap<>();
        
        // 부서별 구성원 그룹화
        for (MemberDto member : members) {
            List<MemberDto> departmentMembers = membersByDepartment.get(member.getDepartment_no());
            if (departmentMembers == null) {
                departmentMembers = new ArrayList<>();
                membersByDepartment.put(member.getDepartment_no(), departmentMembers);
            }
            departmentMembers.add(member);
        } 
        
        // 부서 노드
        for (DepartmentDto dept : departments) {
            Map<String, Object> node = new HashMap<>();
            node.put("id", "dept_" + dept.getDepartment_no());
            node.put("text", dept.getDepartment_name());
            node.put("type", "department");
            node.put("children", new ArrayList<>());
            departmentMap.put(dept.getDepartment_no(), node);
        }
        
        // 부서 계층 구조
        List<Map<String, Object>> result = new ArrayList<>();
        for (DepartmentDto dept : departments) {
            if (dept.getDepartment_high() == 0) {
                Map<String, Object> departmentNode = buildDepartmentHierarchy(dept, departmentMap, membersByDepartment);
                if (departmentNode != null) {
                    result.add(departmentNode);
                }
            }
        }
        
        return result;
    }

    private Map<String, Object> buildDepartmentHierarchy(DepartmentDto dept,
			Map<Long, Map<String, Object>> departmentMap, Map<Long, List<MemberDto>> membersByDepartment) {
		Map<String, Object> node = departmentMap.get(dept.getDepartment_no());
		List<Map<String, Object>> children = (List<Map<String, Object>>) node.get("children");

		boolean hasSubDepartments = false;
		boolean hasMembers = false;

		if (dept.getSubDepartments() != null && !dept.getSubDepartments().isEmpty()) {
			for (DepartmentDto subDept : dept.getSubDepartments()) {
				List<MemberDto> subDeptMembers = membersByDepartment.get(subDept.getDepartment_no());
				boolean hasSubDeptMembers = subDeptMembers != null && !subDeptMembers.isEmpty();
				if (hasSubDeptMembers || (subDept.getSubDepartments() != null && !subDept.getSubDepartments().isEmpty())) {
					Map<String, Object> subDeptNode = new HashMap<>();
					subDeptNode.put("id", "subdept_" + subDept.getDepartment_no());
					subDeptNode.put("text", subDept.getDepartment_name());
					subDeptNode.put("type", "subdepartment");
					subDeptNode.put("children", new ArrayList<>());

					// 하위 부서에 속한 구성원 추가
					if (hasSubDeptMembers) {
                        for (MemberDto member : subDeptMembers) {
                            Map<String, Object> memberNode = createMemberNode(member);
                            ((List<Map<String, Object>>) subDeptNode.get("children")).add(memberNode);
                        }
                    }
                     
                    children.add(subDeptNode);
                    hasSubDepartments = true;
                }
            }
		}
 
		List<MemberDto> deptMembers = membersByDepartment.get(dept.getDepartment_no());
		if (deptMembers != null && !deptMembers.isEmpty()) {
			hasMembers = true;
			for (MemberDto member : deptMembers) {
				Map<String, Object> memberNode = createMemberNode(member);
				children.add(memberNode);
			}
		}

		// 부서에 하위 부서나 구성원이 있는 경우에만 노드 반환
		if (hasMembers || hasSubDepartments) {
			return node;
		} else {
			return null;
		}
	}

    private Map<String, Object> createMemberNode(MemberDto member) {
        Map<String, Object> memberNode = new HashMap<>();
        memberNode.put("id", "member_" + member.getMember_no());
        memberNode.put("text", member.getMember_name() + " " + member.getPosition_name());
        memberNode.put("type", "member");
        return memberNode;
    }  
    
    
    // 조직도 확인 버튼 -> 예약 모달 출력
	@PostMapping("/api/meeting/saveSelectedMembers")
	@ResponseBody
	public Map<String, Object> saveSelectedMembers(@RequestBody Map<String, List<String>> selectedMembers) {
		List<String> memberNumbers = selectedMembers.get("members"); 

		Map<String, Object> response = new HashMap<>();
		response.put("status", "success");
		response.put("message", "선택한 사원이 성공적으로 저장되었습니다.");
		return response;
	}
	
	// 예약 등록
	@PostMapping("/reservation/save")
	@ResponseBody
	public Map<String, String> saveReservation( 
	        @RequestParam("member_no") Long memberNo,
	        @RequestParam("reservation_room") Long reservationRoom,
	        @RequestParam("reservation_date") String reservationDate,
	        @RequestParam("reservation_start_time") String reservationStartTime,
	        @RequestParam("reservation_end_time") String reservationEndTime, 
	        @RequestParam("reservation_purpose") String reservationPurpose,
	        @RequestParam(value = "selectedMembers", required = false) String selectedMembers) {

	    Map<String, String> resultMap = new HashMap<>();
	    resultMap.put("res_code", "404");
	    resultMap.put("res_msg", "회의실 예약 중 오류가 발생했습니다.");

	    try {     
	        MeetingReservationDto meetingReservationDto = MeetingReservationDto.builder()
	            .meeting_no(reservationRoom)
	            .member_no(memberNo)
	            .meeting_reservation_date(reservationDate)
	            .meeting_reservation_start_time(reservationStartTime)
	            .meeting_reservation_end_time(reservationEndTime)
	            .meeting_reservation_purpose(reservationPurpose)
	            .meeting_reservation_status(0L)  
	            .build();
 
	        List<String> memberList = (selectedMembers != null && !selectedMembers.isEmpty()) 
	            ? new ArrayList<>(Arrays.asList(selectedMembers.split(","))) 
	            : new ArrayList<>();
 
	        memberList.add(String.valueOf(memberNo));
 
	        List<MeetingParticipantDto> participants = memberList.stream()
	            .map(membersNo -> MeetingParticipantDto.builder()
	                .member_no(Long.parseLong(membersNo.trim()))
	                .meeting_participant_status(0L)  
	                .build())
	            .toList();
 
	        meetingParticipantService.saveReservationAndParticipants(meetingReservationDto, participants);

	        resultMap.put("res_code", "200");
	        resultMap.put("res_msg", "예약 등록이 완료되었습니다.");
	    } catch (Exception e) {
	        e.printStackTrace();
	        resultMap.put("res_code", "404");
	        resultMap.put("res_msg", "회의실 예약 중 오류가 발생했습니다.");
	    }
	    return resultMap; 
	}
	
	// 본인 예약 목록
	@GetMapping("/employee/meeting/reservation/list")
	public String myMeetingReservation(
	        @RequestParam(value = "searchText", defaultValue = "") String searchText, 
	        @RequestParam(value = "startDate", defaultValue = "") String startDate,
	        @RequestParam(value = "endDate", defaultValue = "") String endDate,
	        @RequestParam(value = "sortBy", defaultValue = "latest") String sortBy,
	        @RequestParam(value = "meetingNo", defaultValue = "") String meetingNo,   
	        @PageableDefault(size = 10) Pageable pageable,
	        Model model) {

	    Long memberNo = memberService.getLoggedInMemberNo();
	    List<MemberDto> memberDto = memberService.getMembersByNo(memberNo); 
	    List<MeetingDto> meetings = meetingService.getAllMeetings();
 
        Page<MeetingReservationDto> reservations = meetingReservationService.searchReservations(memberNo, meetingNo, searchText, startDate, endDate, sortBy, pageable);
         
        model.addAttribute("memberdto", memberDto.get(0));
        model.addAttribute("meetings", meetings);
        model.addAttribute("reservations", reservations);
        model.addAttribute("currentSort", sortBy);
        model.addAttribute("searchText", searchText);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("meetingNo", meetingNo); 

	    return "employee/meeting/myMeetingReservation";
	} 
	
	// 예약 상세 페이지  
	@GetMapping("/employee/meeting/reservation/detail/{id}")
	public String getReservationDetail(@PathVariable("id") Long id, Model model) {
	    MeetingReservationDto reservation = meetingReservationService.getReservationById(id);
	    Long memberNo = memberService.getLoggedInMemberNo();
	    List<MeetingParticipantDto> participants = meetingParticipantService.findParticipantsByReservationNoAndNotMemberNo(id, memberNo);
	    
	    List<MemberDto> memberDto = memberService.getMembersByNo(memberNo); 
	     
	    model.addAttribute("memberdto", memberDto.get(0));
	    model.addAttribute("reservation", reservation);
	    model.addAttribute("participants", participants); 
	    return "employee/meeting/reservationDetail";
	}
	
	@GetMapping("/employee/meeting/reservation/detail/modal/{id}")
	@ResponseBody
	public Map<String, Object> getReservationInfoDetail(@PathVariable("id") Long id) {
	    Map<String, Object> response = new HashMap<>();
	    
	    MeetingReservationDto reservation = meetingReservationService.getReservationById(id);
	    List<MeetingParticipantDto> participants = meetingParticipantService.getParticipantsByReservationNo(id);
	    
	    response.put("reservation", reservation);
	    response.put("participants", participants);
	    
	    return response;
	}

	// 수정
	@PostMapping("/reservation/update")
	@ResponseBody
	public Map<String, String> updateReservation(
	        @RequestParam("reservation_id") Long reservationNo,
	        @RequestParam("member_no") Long memberNo,
	        @RequestParam("reservation_room") Long reservationRoom,
	        @RequestParam("reservation_date") String reservationDate,
	        @RequestParam("reservation_start_time") String reservationStartTime,
	        @RequestParam("reservation_end_time") String reservationEndTime,
	        @RequestParam("reservation_purpose") String reservationPurpose,
	        @RequestParam(value = "selectedMembers", required = false) String selectedMembers) {

	    Map<String, String> resultMap = new HashMap<>();
	    resultMap.put("res_code", "404");
	    resultMap.put("res_msg", "회의실 예약 수정 중 오류가 발생했습니다."); 

	    try {
	        MeetingReservationDto meetingReservationDto = MeetingReservationDto.builder()
	            .meeting_reservation_no(reservationNo)
	            .meeting_no(reservationRoom)
	            .member_no(memberNo)
	            .meeting_reservation_date(reservationDate)
	            .meeting_reservation_start_time(reservationStartTime)
	            .meeting_reservation_end_time(reservationEndTime)
	            .meeting_reservation_purpose(reservationPurpose)
	            .meeting_reservation_status(0L)
	            .build();
  
	        meetingReservationService.updateReservation(meetingReservationDto, selectedMembers);

	        resultMap.put("res_code", "200");
	        resultMap.put("res_msg", "회의실 예약이 수정되었습니다.");
	    } catch (Exception e) {
	        e.printStackTrace();
	        resultMap.put("res_code", "404");
	        resultMap.put("res_msg", "회의실 예약 수정 중 오류가 발생했습니다.");
	    }

	    return resultMap;
	} 
	
	// 예약 취소  
	@PostMapping("/reservation/cancel")
	@ResponseBody
	public Map<String, Object> cancelReservation(@RequestBody Map<String, Long> requestBody) {
	    Map<String, Object> resultMap = new HashMap<>();
	    Long reservationNo = requestBody.get("reservationNo");
 
	    try {
	        boolean canceled = meetingReservationService.cancelReservation(reservationNo);

	        if (canceled) {
	            resultMap.put("res_code", "200");
	            resultMap.put("res_msg", "예약이 취소되었습니다.");
	        } else {
	            resultMap.put("res_code", "404");
	            resultMap.put("res_msg", "예약 취소 중 오류가 발생했습니다.");
	        }
	    } catch (Exception e) {
	        resultMap.put("res_code", "500");
	        resultMap.put("res_msg", "서버 오류가 발생했습니다.");
	        e.printStackTrace();
	    }

	    return resultMap;
	}

	// 관리자 - 예약 전체 목록
	@GetMapping("/meeting/reservation/list")
	public String allMeetingReservation(
	        @RequestParam(value = "searchText", defaultValue = "") String searchText, 
	        @RequestParam(value = "startDate", defaultValue = "") String startDate,
	        @RequestParam(value = "endDate", defaultValue = "") String endDate,
	        @RequestParam(value = "sortBy", defaultValue = "latest") String sortBy,
	        @RequestParam(value = "meetingNo", defaultValue = "") String meetingNo,   
	        @PageableDefault(size = 10) Pageable pageable,
	        Model model) { 

	    Long memberNo = memberService.getLoggedInMemberNo();
	    List<MemberDto> memberDto = memberService.getMembersByNo(memberNo); 
	    List<MeetingDto> meetings = meetingService.getAllMeetings();
 
        Page<MeetingReservationDto> reservations = meetingReservationService.allReservations(meetingNo, searchText, startDate, endDate, sortBy, pageable); 
        
        model.addAttribute("memberdto", memberDto.get(0));
        model.addAttribute("meetings", meetings);
        model.addAttribute("reservations", reservations);
        model.addAttribute("currentSort", sortBy);
        model.addAttribute("searchText", searchText);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("meetingNo", meetingNo);   

	    return "admin/meeting/meetingReservationList";
	}  
	
	@GetMapping("/all/reservations")
	@ResponseBody
	public List<MeetingReservationDto> getReservations() { 
	    List<MeetingReservationDto> reservations = meetingReservationService.getAllReservations(); 
	    return reservations;
	}
}