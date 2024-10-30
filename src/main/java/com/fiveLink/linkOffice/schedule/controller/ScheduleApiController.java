package com.fiveLink.linkOffice.schedule.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fiveLink.linkOffice.meeting.domain.MeetingParticipantDto;
import com.fiveLink.linkOffice.meeting.domain.MeetingReservationDto;
import com.fiveLink.linkOffice.meeting.service.MeetingReservationService;
import com.fiveLink.linkOffice.member.domain.Member;
import com.fiveLink.linkOffice.member.domain.MemberDto;
import com.fiveLink.linkOffice.member.repository.MemberRepository;
import com.fiveLink.linkOffice.member.service.MemberService;
import com.fiveLink.linkOffice.organization.domain.DepartmentDto;
import com.fiveLink.linkOffice.organization.service.DepartmentService;
import com.fiveLink.linkOffice.schedule.domain.Schedule;
import com.fiveLink.linkOffice.schedule.domain.ScheduleCategoryDto;
import com.fiveLink.linkOffice.schedule.domain.ScheduleCheckDto;
import com.fiveLink.linkOffice.schedule.domain.ScheduleDto;
import com.fiveLink.linkOffice.schedule.domain.ScheduleException;
import com.fiveLink.linkOffice.schedule.domain.ScheduleExceptionDto;
import com.fiveLink.linkOffice.schedule.domain.ScheduleExceptionParticipantDto;
import com.fiveLink.linkOffice.schedule.domain.ScheduleParticipantDto;
import com.fiveLink.linkOffice.schedule.domain.ScheduleRepeat;
import com.fiveLink.linkOffice.schedule.domain.ScheduleRepeatDto;
import com.fiveLink.linkOffice.schedule.service.ScheduleCategoryService;
import com.fiveLink.linkOffice.schedule.service.ScheduleService;
import com.fiveLink.linkOffice.vacationapproval.domain.VacationApprovalDto;
import com.fiveLink.linkOffice.vacationapproval.service.VacationApprovalService;

@Controller
public class ScheduleApiController {
	private final MemberService memberService;
	private final ScheduleCategoryService scheduleCategoryService;
	private final ScheduleService scheduleService;
	private final DepartmentService departmentService;  
	private final VacationApprovalService vacationApprovalService; 
	private final MemberRepository memberRepository;
	private final MeetingReservationService meetingReservationService;
	
	@Autowired
	public ScheduleApiController(MemberService memberService, ScheduleCategoryService scheduleCategoryService,
			ScheduleService scheduleService, DepartmentService departmentService, VacationApprovalService vacationApprovalService, MemberRepository memberRepository, MeetingReservationService meetingReservationService) {
		this.memberService = memberService;
		this.scheduleCategoryService = scheduleCategoryService;
		this.scheduleService = scheduleService;
		this.departmentService = departmentService;
		this.vacationApprovalService = vacationApprovalService;
		this.memberRepository = memberRepository;
		this.meetingReservationService = meetingReservationService;
	}

	@GetMapping("/schedule/category/get/{categoryId}")
	@ResponseBody
	public ScheduleCategoryDto getScheduleCategory(@PathVariable("categoryId") Long id) { 
		return scheduleCategoryService.getScheduleCategoryById(id);
	}

	// 수정
	@PostMapping("/schedule/category/update")
	@ResponseBody
	public Map<String, String> updateScheduleCategory(@RequestBody Map<String, Object> payload) {
		Long categoryId = Long.valueOf(payload.get("scheduleCategoryId").toString());
		String categoryName = (String) payload.get("scheduleCategoryName");
		String categoryColor = (String) payload.get("scheduleCategoryColor");
		Long onlyAdmin = (long) ((Boolean) payload.get("onlyAdmin") ? 1 : 0);

		Map<String, String> response = scheduleCategoryService.updateScheduleCategory(categoryId, categoryName,
				categoryColor, onlyAdmin);

		if (response.containsKey("success")) {
			response.put("res_code", "200");
		} else {
			response.put("res_code", "400");
		}

		return response;
	}

	// 삭제
	@PostMapping("/schedule/category/delete")
	@ResponseBody
	public Map<String, String> deleteScheduleCategory(@RequestBody Map<String, Object> payload) {
		Map<String, String> resultMap = new HashMap<>();
		resultMap.put("res_code", "404");
		resultMap.put("res_msg", "카테고리 삭제 중 오류가 발생했습니다.");

		try {
			Long categoryId = Long.valueOf(payload.get("scheduleCategoryId").toString());

			boolean deleteSuccess = scheduleCategoryService.deleteScheduleCategory(categoryId);

			if (deleteSuccess) {
				resultMap.put("res_code", "200");
				resultMap.put("res_msg", "카테고리가 삭제되었습니다.");
			} else {
				resultMap.put("res_code", "400");
				resultMap.put("res_msg", "해당 카테고리로 등록된 일정이 있어 삭제가 불가능합니다.");
			}
		} catch (Exception e) {
			resultMap.put("res_msg", e.getMessage());
		}
		return resultMap;
	}

	// 등록
	@PostMapping("/schedule/category/add")
	@ResponseBody
	public Map<String, String> addScheduleCategory(@RequestBody Map<String, Object> payload) {
		String categoryName = (String) payload.get("scheduleCategoryName");
		String categoryColor = (String) payload.get("scheduleCategoryColor");
		Long onlyAdmin = (long) ((Boolean) payload.get("onlyAdmin") ? 1 : 0);

		Map<String, String> response = scheduleCategoryService.addScheduleCategory(categoryName, categoryColor,
				onlyAdmin);

		if (response.containsKey("success")) {
			response.put("res_code", "200");
		} else {
			response.put("res_code", "400");
		}

		return response;
	}

	// 관리자 - 일정 등록 카테고리
	@GetMapping("/categories")
	@ResponseBody
	public List<ScheduleCategoryDto> getAlladminScheduleCategory() { 
		return scheduleCategoryService.getAlladminScheduleCategory();
	}

	// 관리자 - 일정 등록
	@PostMapping("/company/schedule/save")
	@ResponseBody
	public String saveSchedule(@RequestBody Map<String, Object> request) {
		Long memberNo = memberService.getLoggedInMemberNo();
		ScheduleDto scheduleDto = new ScheduleDto();
		ScheduleRepeatDto scheduleRepeatDto = new ScheduleRepeatDto();

		scheduleDto.setMember_no(memberNo);
		scheduleDto.setSchedule_title((String) request.get("title"));
		scheduleDto.setSchedule_comment((String) request.get("description"));
		scheduleDto.setSchedule_start_date((String) request.get("startDate"));
		scheduleDto.setSchedule_allday((Boolean) request.get("allDay") ? 1L : 0L);
		scheduleDto.setSchedule_end_date((String) request.get("endDate"));
		if (request.get("repeat") != null) {
			Object repeatValue = request.get("repeat");
			long repeat = repeatValue instanceof Integer ? ((Integer) repeatValue).longValue()
					: Long.parseLong(repeatValue.toString());
			scheduleDto.setSchedule_repeat(repeat != 0 ? 1L : 0L);

			if (repeat != 0) {
				scheduleRepeatDto.setSchedule_repeat_type(repeat);
			}
		}

		if (request.get("category") != null) {
			Object category = request.get("category");
			scheduleDto.setSchedule_category_no(
					category instanceof Integer ? ((Integer) category).longValue() : Long.parseLong((String) category));
		}

		scheduleDto.setSchedule_start_time((String) request.get("startTime"));
		scheduleDto.setSchedule_end_time((String) request.get("endTime"));

		if (request.get("schedule_day_of_week") != null) {
			Object scheduleDayOfWeek = request.get("schedule_day_of_week");
			scheduleRepeatDto.setSchedule_repeat_day(
					scheduleDayOfWeek instanceof Integer ? ((Integer) scheduleDayOfWeek).longValue()
							: Long.parseLong((String) scheduleDayOfWeek));
		}
		if (request.get("schedule_week") != null) {
			Object scheduleWeek = request.get("schedule_week");
			scheduleRepeatDto
					.setSchedule_repeat_week(scheduleWeek instanceof Integer ? ((Integer) scheduleWeek).longValue()
							: Long.parseLong((String) scheduleWeek));
		}
		if (request.get("schedule_date") != null) {
			Object scheduleDate = request.get("schedule_date");
			scheduleRepeatDto
					.setSchedule_repeat_date(scheduleDate instanceof Integer ? ((Integer) scheduleDate).longValue()
							: Long.parseLong((String) scheduleDate));
		}
		if (request.get("schedule_month") != null) {
			Object scheduleMonth = request.get("schedule_month");
			scheduleRepeatDto
					.setSchedule_repeat_month(scheduleMonth instanceof Integer ? ((Integer) scheduleMonth).longValue()
							: Long.parseLong((String) scheduleMonth));
		}
		if (request.get("repeatEndDate") != null) {
			scheduleRepeatDto.setSchedule_repeat_end_date((String) request.get("repeatEndDate"));
		}

		scheduleService.saveCompanySchedule(scheduleDto, scheduleRepeatDto);

		return "success";
	}

	// 관리자 - 월간 일정
	@ResponseBody
	@GetMapping("/api/company/schedules")
	public List<ScheduleDto> getSchedules() {
		List<Schedule> schedules = scheduleService.getAllSchedules();

		List<ScheduleDto> scheduleDtos = new ArrayList<>();
		for (Schedule schedule : schedules) {
			ScheduleDto dto = ScheduleDto.toDto(schedule);
			scheduleDtos.add(dto);
		} 
		return scheduleDtos;
	}

	// 관리자 - 반복 일정
	@ResponseBody
	@GetMapping("/api/repeat/schedules")
	public List<ScheduleRepeatDto> getRepeatSchedules() {
		List<ScheduleRepeat> scheduleRepeats = scheduleService.getAllRepeatSchedules();

		List<ScheduleRepeatDto> scheduleRepeatDtos = new ArrayList<>();
		for (ScheduleRepeat scheduleRepeat : scheduleRepeats) {
			ScheduleRepeatDto dto = ScheduleRepeatDto.toDto(scheduleRepeat);
			scheduleRepeatDtos.add(dto);
		}
 
		return scheduleRepeatDtos;
	}

	// 관리자 - 일정 수정
	@ResponseBody
	@GetMapping("/schedule/edit/{eventNo}")
	public Map<String, Object> getScheduleById(@PathVariable("eventNo") Long eventNo) {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			ScheduleDto scheduleDto = scheduleService.getScheduleById(eventNo);
			ScheduleRepeatDto scheduleRepeatDto = scheduleService.getScheduleRepeatById(eventNo); // 반복 일정 정보 가져오기

			if (scheduleDto != null) {
				resultMap.put("res_code", "200");
				resultMap.put("schedule", scheduleDto);
				resultMap.put("scheduleRepeat", scheduleRepeatDto);
			} else {
				resultMap.put("res_code", "404");
				resultMap.put("res_msg", "일정을 찾을 수 없습니다.");
			}
		} catch (Exception e) {
			resultMap.put("res_code", "500");
			resultMap.put("res_msg", "서버 오류가 발생했습니다.");
			e.printStackTrace();
		}
		return resultMap;
	}
 
	// 관리자 일정 수정
	// 일반 수정
	@PostMapping("/company/schedule/edit/{eventId}")
	@ResponseBody
	public String editSchedule(@PathVariable("eventId") Long eventId, @RequestBody Map<String, Object> request) {
		ScheduleDto scheduleDto = new ScheduleDto();
		ScheduleRepeatDto scheduleRepeatDto = new ScheduleRepeatDto();

		scheduleDto.setSchedule_no(eventId);
		scheduleDto.setSchedule_title((String) request.get("title"));
		scheduleDto.setSchedule_comment((String) request.get("description"));
		scheduleDto.setSchedule_start_date((String) request.get("startDate"));
		scheduleDto.setSchedule_end_date((String) request.get("endDate"));
		scheduleDto.setSchedule_allday(Boolean.TRUE.equals(request.get("allDay")) ? 1L : 0L);
		scheduleDto.setSchedule_start_time((String) request.get("startTime"));
		scheduleDto.setSchedule_end_time((String) request.get("endTime"));
		scheduleDto.setSchedule_category_no(getLongValue(request.get("category")));
		scheduleDto.setSchedule_type(3L);

		Long repeatValue = getLongValue(request.get("repeat"));
		scheduleDto.setSchedule_repeat(repeatValue != null && repeatValue != 0 ? 1L : 0L);

		if (repeatValue != null && repeatValue != 0) {
			scheduleRepeatDto.setSchedule_repeat_type(repeatValue);
			scheduleRepeatDto.setSchedule_repeat_day(getLongValue(request.get("schedule_day_of_week")));
			scheduleRepeatDto.setSchedule_repeat_week(getLongValue(request.get("schedule_week")));
			scheduleRepeatDto.setSchedule_repeat_date(getLongValue(request.get("schedule_date")));
			scheduleRepeatDto.setSchedule_repeat_month(getLongValue(request.get("schedule_month")));
			scheduleRepeatDto.setSchedule_repeat_end_date((String) request.get("repeatEndDate"));
		}

		scheduleService.updateCompanySchedule(eventId, scheduleDto, scheduleRepeatDto);

		return "success";
	}

	private Long getLongValue(Object value) {
		if (value == null)
			return null;
		if (value instanceof Integer)
			return ((Integer) value).longValue();
		if (value instanceof Long)
			return (Long) value;
		if (value instanceof String) {
			try {
				return Long.parseLong((String) value);
			} catch (NumberFormatException e) {
				return null;
			}
		}
		return null;
	}

	
	// 관리자 - 반복 일정 수정 
	@ResponseBody
	@PostMapping("/company/schedule/edit/recurring/{eventId}")
	public Map<String, String> editRecurringEvent(
	        @PathVariable("eventId") Long eventId,
	        @RequestBody Map<String, Object> request,  
	        @RequestParam("editOption") Long editOption,
	        @RequestParam("pickStartDate") String pickStartDate,
	        @RequestParam("pickEndDate") String pickEndDate) { 
	    Map<String, String> resultMap = new HashMap<>();
	    resultMap.put("res_code", "404");
	    resultMap.put("res_msg", "일정 수정 중 오류가 발생했습니다.");

	    ScheduleDto scheduleDto = new ScheduleDto();
	    ScheduleRepeatDto scheduleRepeatDto = new ScheduleRepeatDto();
	    Long memberNo = memberService.getLoggedInMemberNo();
 
	    scheduleDto.setMember_no(memberNo);
	    scheduleDto.setSchedule_title((String) request.get("title"));
	    scheduleDto.setSchedule_comment((String) request.get("description"));
	    scheduleDto.setSchedule_start_date((String) request.get("startDate"));
	    scheduleDto.setSchedule_allday((Boolean) request.get("allDay") ? 1L : 0L);
	    scheduleDto.setSchedule_end_date((String) request.get("endDate"));
	    scheduleDto.setSchedule_start_time((String) request.get("startTime"));
	    scheduleDto.setSchedule_end_time((String) request.get("endTime"));

	    // 반복 설정
	    if (request.get("repeat") != null) {
	        Object repeatValue = request.get("repeat");
	        long repeat = repeatValue instanceof Integer ? ((Integer) repeatValue).longValue()
	                : Long.parseLong(repeatValue.toString());
	        scheduleDto.setSchedule_repeat(repeat != 0 ? 1L : 0L);

	        if (repeat != 0) {
	            scheduleRepeatDto.setSchedule_repeat_type(repeat);
	        }
	    }

	    if (request.get("category") != null) {
	        Object category = request.get("category");
	        scheduleDto.setSchedule_category_no(
	                category instanceof Integer ? ((Integer) category).longValue() : Long.parseLong((String) category));
	    }

	    // 반복 주기 설정
	    if (request.get("schedule_day_of_week") != null) {
	        Object scheduleDayOfWeek = request.get("schedule_day_of_week");
	        scheduleRepeatDto.setSchedule_repeat_day(
	                scheduleDayOfWeek instanceof Integer ? ((Integer) scheduleDayOfWeek).longValue()
	                        : Long.parseLong((String) scheduleDayOfWeek));
	    }
	    if (request.get("schedule_week") != null) {
	        Object scheduleWeek = request.get("schedule_week");
	        scheduleRepeatDto.setSchedule_repeat_week(
	                scheduleWeek instanceof Integer ? ((Integer) scheduleWeek).longValue()
	                        : Long.parseLong((String) scheduleWeek));
	    }
	    if (request.get("schedule_date") != null) {
	        Object scheduleDate = request.get("schedule_date");
	        scheduleRepeatDto.setSchedule_repeat_date(
	                scheduleDate instanceof Integer ? ((Integer) scheduleDate).longValue()
	                        : Long.parseLong((String) scheduleDate));
	    }
	    if (request.get("schedule_month") != null) {
	        Object scheduleMonth = request.get("schedule_month");
	        scheduleRepeatDto.setSchedule_repeat_month(
	                scheduleMonth instanceof Integer ? ((Integer) scheduleMonth).longValue()
	                        : Long.parseLong((String) scheduleMonth));
	    }
	    if (request.get("repeatEndDate") != null) {
	        scheduleRepeatDto.setSchedule_repeat_end_date((String) request.get("repeatEndDate"));
	    }
 
	    try {
	        switch (editOption.intValue()) {
	            case 1: // 이 일정만 수정
	                scheduleService.updateSingleEvent(eventId, scheduleDto, scheduleRepeatDto, pickStartDate, pickEndDate);
	                resultMap.put("res_code", "200");
	                resultMap.put("res_msg", "일정이 수정되었습니다.");
	                break;

	            case 2: // 이 일정 및 향후 일정 수정
	                scheduleService.updateFutureEvents(eventId, scheduleDto, scheduleRepeatDto, pickStartDate);
	                resultMap.put("res_code", "200");
	                resultMap.put("res_msg", "일정이 수정되었습니다.");
	                break;

	            case 3: // 모든 일정 수정
	                scheduleService.updateAllEvents(eventId, scheduleDto, scheduleRepeatDto);
	                resultMap.put("res_code", "200");
	                resultMap.put("res_msg", "일정이 수정되었습니다.");
	                break;

	            default:
	                resultMap.put("res_code", "400");
	                resultMap.put("res_msg", "유효하지 않은 수정 옵션입니다.");
	                break;
	        }
	    } catch (Exception e) {
	        resultMap.put("res_msg", e.getMessage());
	    }

	    return resultMap;
	}
	
	// 예외 일정
	@ResponseBody
	@GetMapping("/api/company/exception/schedules")
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

	
	// 예외 상세
	@ResponseBody
	@GetMapping("/schedule/exception/edit/{eventNo}")
	public Map<String, Object> getExceptionScheduleById(@PathVariable("eventNo") Long eventNo) {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			ScheduleExceptionDto scheduleExceptionDto = scheduleService.getScheduleExceptionById(eventNo); 

			if (scheduleExceptionDto != null) {
				resultMap.put("res_code", "200");
				resultMap.put("schedule", scheduleExceptionDto); 
			} else {
				resultMap.put("res_code", "404");
				resultMap.put("res_msg", "일정을 찾을 수 없습니다.");
			}
		} catch (Exception e) {
			resultMap.put("res_code", "500");
			resultMap.put("res_msg", "서버 오류가 발생했습니다.");
			e.printStackTrace();
		}
		return resultMap;
	}

	// 예외 일정 수정
	@PostMapping("/company/schedule/exception/edit/{eventId}")
	@ResponseBody
	public String editExceptionSchedule(@PathVariable("eventId") Long eventId, @RequestBody Map<String, Object> request) {

		ScheduleExceptionDto scheduleExceptionDto = new ScheduleExceptionDto(); 

		scheduleExceptionDto.setSchedule_exception_no(eventId);
		scheduleExceptionDto.setSchedule_exception_title((String) request.get("title"));
		scheduleExceptionDto.setSchedule_exception_comment((String) request.get("description"));
		scheduleExceptionDto.setSchedule_exception_start_date((String) request.get("startDate"));
		scheduleExceptionDto.setSchedule_exception_end_date((String) request.get("endDate"));
		scheduleExceptionDto.setSchedule_exception_allday(Boolean.TRUE.equals(request.get("allDay")) ? 1L : 0L);
		scheduleExceptionDto.setSchedule_exception_start_time((String) request.get("startTime"));
		scheduleExceptionDto.setSchedule_exception_end_time((String) request.get("endTime"));
		scheduleExceptionDto.setSchedule_category_no(getLongValue(request.get("category")));

		scheduleService.updateCompanyExceptionSchedule(eventId, scheduleExceptionDto);

		return "success";
	}
	
	// 기본 일정 삭제
	@PostMapping("/company/schedule/delete")
    @ResponseBody
    public Map<String, String> deleteScheduleCompany(@RequestBody Map<String, Object> payload) {
        Map<String, String> resultMap = new HashMap<>();
        Long eventId = Long.valueOf(payload.get("eventId").toString());

        boolean isDeleted = scheduleService.deleteBasicSchedule(eventId);

        if (isDeleted) {
            resultMap.put("res_code", "200");
            resultMap.put("res_msg", "일정이 삭제되었습니다.");
        } else {
            resultMap.put("res_code", "404");
            resultMap.put("res_msg", "해당 일정이 존재하지 않습니다.");
        }

        return resultMap;
    }
	
	// 예외 일정 삭제
	@PostMapping("/company/schedule/exception/delete")
    @ResponseBody
    public Map<String, String> deleteScheduleExceptionCompany(@RequestBody Map<String, Object> payload) {
        Map<String, String> resultMap = new HashMap<>();
        Long eventId = Long.valueOf(payload.get("eventId").toString());

        boolean isDeleted = scheduleService.deleteExceptionSchedule(eventId);

        if (isDeleted) {
            resultMap.put("res_code", "200");
            resultMap.put("res_msg", "일정이 삭제되었습니다.");
        } else {
            resultMap.put("res_code", "404");
            resultMap.put("res_msg", "해당 일정이 존재하지 않습니다.");
        }

        return resultMap;
    }
	
	// 관리자 - 반복 일정 삭제
	@ResponseBody
	@PostMapping("/company/schedule/repeat/delete/{eventId}")
	public Map<String, String> deleteRepeatEvent(
			@PathVariable("eventId") Long eventId,  
	        @RequestParam("editOption") Long editOption,
	        @RequestParam("pickStartDate") String pickStartDate,
	        @RequestParam("pickEndDate") String pickEndDate) { 
		
		Map<String, String> resultMap = new HashMap<>();
	    resultMap.put("res_code", "404");
	    resultMap.put("res_msg", "일정 수정 중 오류가 발생했습니다.");
	     
	    ScheduleDto scheduleDto = scheduleService.getScheduleById(eventId);
		ScheduleRepeatDto scheduleRepeatDto = scheduleService.getScheduleRepeatById(eventId); 
	     
	    boolean isDeleted;
	    
	    try {
	        switch (editOption.intValue()) {
	            case 1: // 이 일정만 삭제
	            	isDeleted = scheduleService.deleteSingleEvent(eventId, scheduleDto, scheduleRepeatDto, pickStartDate, pickEndDate);
	            	if (isDeleted) {
	                    resultMap.put("res_code", "200");
	                    resultMap.put("res_msg", "일정이 삭제되었습니다.");
	                } else {
	                    resultMap.put("res_code", "404");
	                    resultMap.put("res_msg", "해당 일정이 존재하지 않습니다.");
	                }
	                break;

	            case 2: // 이 일정 및 향후 일정 삭제
	            	isDeleted = scheduleService.deleteFutureEvents(eventId, scheduleDto, scheduleRepeatDto, pickStartDate, pickEndDate);
	            	if (isDeleted) {
	                    resultMap.put("res_code", "200");
	                    resultMap.put("res_msg", "일정이 삭제되었습니다.");
	                } else {
	                    resultMap.put("res_code", "404");
	                    resultMap.put("res_msg", "해당 일정이 존재하지 않습니다.");
	                }
	                break;

	            case 3: // 모든 일정 삭제
	            	isDeleted = scheduleService.deleteAllEvents(eventId);
	            	if (isDeleted) {
	                    resultMap.put("res_code", "200");
	                    resultMap.put("res_msg", "일정이 삭제되었습니다.");
	                } else {
	                    resultMap.put("res_code", "404");
	                    resultMap.put("res_msg", "해당 일정이 존재하지 않습니다.");
	                }
	                break;

	            default:
	            	resultMap.put("res_code", "404");
                    resultMap.put("res_msg", "해당 일정이 존재하지 않습니다.");
	                break;
	        }
	    } catch (Exception e) {
	        resultMap.put("res_msg", e.getMessage());
	    }

	    return resultMap;
	}
	
	
	// 사원
	// 사원 카테고리
	@GetMapping("/employee/categories")
	@ResponseBody
	public List<ScheduleCategoryDto> getAllemployeeScheduleCategory() { 
		return scheduleCategoryService.getAllemployeeScheduleCategory();
	}
	
	// 개인 일정
	@ResponseBody
	@GetMapping("/api/personal/schedules/{memberNo}")
	public List<ScheduleDto> getpersonalSchedules(@PathVariable("memberNo") Long memberNo) {
		List<Schedule> schedules = scheduleService.getAllpersonalSchedules(memberNo);
		
		List<ScheduleDto> personalResult = new ArrayList<>();
		for (Schedule schedule : schedules) {
			ScheduleDto dto = ScheduleDto.toDto(schedule);
			personalResult.add(dto);
		}   
		return personalResult;
	} 
	 
	// 부서 일정
	@ResponseBody
	@GetMapping("/api/department/schedules")
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
	
	// 참여자 일정 
	@ResponseBody
	@GetMapping("/api/participate/schedules")
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

	
	// 참여자 정보  
	@GetMapping("/api/participate/member/schedules/{scheduleNo}/{memberNo}")
	@ResponseBody
	public Map<String, Object> getparticipateMemberSchedules(@PathVariable("scheduleNo") Long scheduleNo, @PathVariable("memberNo") Long memberNo) {
	    Map<String, Object> response = new HashMap<>();
	     
	    List<ScheduleParticipantDto> participants = scheduleService.getParticipantsByReservationNoOwn(scheduleNo, memberNo);
	     
	    response.put("participants", participants); 
	    return response;
	} 
	
	// 부서 정보
    @ResponseBody
    @GetMapping("/api/schedule/department/list")
    public List<DepartmentDto> getAllDepartments() {
        List<DepartmentDto> departments = departmentService.getSecondDepartments();
         
        return departments;
    }
    
    // 부서 체크박스 상태 확인
    @ResponseBody
    @GetMapping("/api/schedule/checks/{memberNo}")
    public List<ScheduleCheckDto> getScheduleChecks(@PathVariable("memberNo") Long memberNo) {
        List<ScheduleCheckDto> scheduleChecks = scheduleService.getScheduleChecksByMemberNo(memberNo);

        return scheduleChecks;
    }

    // 부서 체크박스 상태 저장 
    @ResponseBody
    @PostMapping("/api/schedule/checks/save")
    public void updateScheduleCheck(@RequestBody ScheduleCheckDto scheduleCheckDto) { 
        scheduleService.updateScheduleCheck(
            scheduleCheckDto.getMember_no(),
            scheduleCheckDto.getDepartment_no(),
            scheduleCheckDto.getSchedule_check_status()
        );
    }

    
    // 조직도
    @GetMapping("/schedule/chart")
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
	@PostMapping("/api/schedule/saveSelectedMembers")
	@ResponseBody
	public Map<String, Object> saveSelectedMembers(@RequestBody Map<String, List<String>> selectedMembers) {
		List<String> memberNumbers = selectedMembers.get("members"); 

		Map<String, Object> response = new HashMap<>();
		response.put("status", "success");
		response.put("message", "선택한 사원이 성공적으로 저장되었습니다.");
		return response;
	}
	
    // 일정 등록
	@PostMapping("/employee/schedule/save/{memberNo}")
	@ResponseBody
	public Map<String, String> saveEmployeeSchedule(
	        @RequestBody Map<String, Object> request,
	        @PathVariable("memberNo") Long memberNo) {

	    Map<String, String> resultMap = new HashMap<>();
	    resultMap.put("res_code", "404");
	    resultMap.put("res_msg", "일정 등록 중 오류가 발생했습니다.");

	    try {
	        ScheduleDto scheduleDto = new ScheduleDto();
	        ScheduleRepeatDto scheduleRepeatDto = new ScheduleRepeatDto();

	        // 일정 기본 정보 설정
	        scheduleDto.setMember_no(memberNo);
	        scheduleDto.setSchedule_title((String) request.get("title"));
	        scheduleDto.setSchedule_comment((String) request.get("description"));
	        scheduleDto.setSchedule_start_date((String) request.get("startDate"));
	        scheduleDto.setSchedule_allday((Boolean) request.get("allDay") ? 1L : 0L);
	        scheduleDto.setSchedule_end_date((String) request.get("endDate")); 
	        Object scheduleTypeObj = request.get("schedule_type");
	        if (scheduleTypeObj != null) {
	            scheduleDto.setSchedule_type(Long.valueOf(scheduleTypeObj.toString()));
	        }
	         
	        Object departmentNoObj = request.get("department_no");
	        if (departmentNoObj != null) {
	            scheduleDto.setDepartment_no(Long.valueOf(departmentNoObj.toString()));
	        }

	        // 반복 설정
	        if (request.get("repeat") != null) {
	            Object repeatValue = request.get("repeat");
	            long repeat = repeatValue instanceof Integer ? ((Integer) repeatValue).longValue()
	                    : Long.parseLong(repeatValue.toString());
	            scheduleDto.setSchedule_repeat(repeat != 0 ? 1L : 0L);

	            if (repeat != 0) {
	                scheduleRepeatDto.setSchedule_repeat_type(repeat);
	            }
	        }

	        // 카테고리 설정
	        if (request.get("category") != null) {
	            Object category = request.get("category");
	            scheduleDto.setSchedule_category_no(
	                    category instanceof Integer ? ((Integer) category).longValue() : Long.parseLong((String) category));
	        }

	        // 시간 설정
	        scheduleDto.setSchedule_start_time((String) request.get("startTime"));
	        scheduleDto.setSchedule_end_time((String) request.get("endTime"));

	        // 반복 설정 상세
	        if (request.get("schedule_day_of_week") != null) {
	            Object scheduleDayOfWeek = request.get("schedule_day_of_week");
	            scheduleRepeatDto.setSchedule_repeat_day(
	                    scheduleDayOfWeek instanceof Integer ? ((Integer) scheduleDayOfWeek).longValue()
	                            : Long.parseLong((String) scheduleDayOfWeek));
	        }
	        if (request.get("schedule_week") != null) {
	            Object scheduleWeek = request.get("schedule_week");
	            scheduleRepeatDto
	                    .setSchedule_repeat_week(scheduleWeek instanceof Integer ? ((Integer) scheduleWeek).longValue()
	                            : Long.parseLong((String) scheduleWeek));
	        }
	        if (request.get("schedule_date") != null) {
	            Object scheduleDate = request.get("schedule_date");
	            scheduleRepeatDto
	                    .setSchedule_repeat_date(scheduleDate instanceof Integer ? ((Integer) scheduleDate).longValue()
	                            : Long.parseLong((String) scheduleDate));
	        }
	        if (request.get("schedule_month") != null) {
	            Object scheduleMonth = request.get("schedule_month");
	            scheduleRepeatDto
	                    .setSchedule_repeat_month(scheduleMonth instanceof Integer ? ((Integer) scheduleMonth).longValue()
	                            : Long.parseLong((String) scheduleMonth));
	        }
	        if (request.get("repeatEndDate") != null) {
	            scheduleRepeatDto.setSchedule_repeat_end_date((String) request.get("repeatEndDate"));
	        }
 
	        // 참여자 일정
	        String selectedMembers = (String) request.get("selectedMembers");
	        if(selectedMembers != null && !selectedMembers.isEmpty()) {  
		        List<String> memberList = (selectedMembers != null && !selectedMembers.isEmpty()) 
			            ? new ArrayList<>(Arrays.asList(selectedMembers.split(","))) 
			            : new ArrayList<>();
		        
		        memberList.add(String.valueOf(memberNo));
		        
		        List<ScheduleParticipantDto> participants = memberList.stream()
		            .map(membersNo -> ScheduleParticipantDto.builder()
		                .member_no(Long.parseLong(membersNo.trim()))
		                .schedule_participant_status(0L)  
		                .build())
		            .toList();
	 
		        scheduleService.saveScheduleAndParticipants(scheduleDto, scheduleRepeatDto, participants);
	        }
	        // 개인, 부서
	        else { 
	        	scheduleService.saveEmployeeSchedule(scheduleDto, scheduleRepeatDto);
	        }
	        
	        resultMap.put("res_code", "200");
	        resultMap.put("res_msg", "일정이 저장되었습니다.");
	    } catch (Exception e) {
	        e.printStackTrace();
	        resultMap.put("res_code", "404");
	        resultMap.put("res_msg", "일정 등록 중 오류가 발생했습니다.");
	    }
	    return resultMap; 
	}
	
	// 수정 참여자 정보
	@GetMapping("/employee/schedule/participate/{scheduleNo}")
	@ResponseBody
	public Map<String, Object> getScheduleParticipant(@PathVariable("scheduleNo") Long scheduleNo) {
	    Map<String, Object> response = new HashMap<>();
	     
	    List<ScheduleParticipantDto> participants = scheduleService.getParticipantsByScheduleNo(scheduleNo);
	     
	    response.put("participants", participants);
	    
	    return response;
	}
	
	// 사원 일반 수정
	@ResponseBody
	@PostMapping("/employee/schedule/edit/{eventId}/{scheduleEditType}")
	public Map<String, Object> getEmployeeScheduleById(@PathVariable("eventId") Long eventId, @PathVariable("scheduleEditType") String scheduleEditType, @RequestBody Map<String, Object> request) {

	    Map<String, Object> resultMap = new HashMap<>(); 
	     
	    try {  
	    	ScheduleDto scheduleDto = new ScheduleDto();
	    	ScheduleRepeatDto scheduleRepeatDto = new ScheduleRepeatDto();
 
	    	scheduleDto.setSchedule_no(eventId);
	    	scheduleDto.setSchedule_title((String) request.get("title"));
	    	scheduleDto.setSchedule_comment((String) request.get("description"));
	    	scheduleDto.setSchedule_start_date((String) request.get("startDate"));
	    	scheduleDto.setSchedule_end_date((String) request.get("endDate"));
	    	scheduleDto.setSchedule_allday(Boolean.TRUE.equals(request.get("allDay")) ? 1L : 0L);
	    	scheduleDto.setSchedule_start_time((String) request.get("startTime"));
	    	scheduleDto.setSchedule_end_time((String) request.get("endTime"));
	    	scheduleDto.setSchedule_category_no(getLongValue(request.get("category")));
	    	scheduleDto.setSchedule_type(getLongValue(request.get("schedule_type")));
	    	scheduleDto.setDepartment_no(getLongValue(request.get("department_no")));
	    	scheduleDto.setMember_no(getLongValue(request.get("memberNo")));
 
	    	Long repeatValue = getLongValue(request.get("repeat"));
	    	scheduleDto.setSchedule_repeat(repeatValue != null && repeatValue != 0 ? 1L : 0L);
	    	if (repeatValue != null && repeatValue != 0) {
	    	    scheduleRepeatDto.setSchedule_repeat_type(repeatValue);
	    	    scheduleRepeatDto.setSchedule_repeat_day(getLongValue(request.get("schedule_day_of_week")));
	    	    scheduleRepeatDto.setSchedule_repeat_week(getLongValue(request.get("schedule_week")));
	    	    scheduleRepeatDto.setSchedule_repeat_date(getLongValue(request.get("schedule_date")));
	    	    scheduleRepeatDto.setSchedule_repeat_month(getLongValue(request.get("schedule_month")));
	    	    scheduleRepeatDto.setSchedule_repeat_end_date((String) request.get("repeatEndDate"));
	    	}
	    	
	    	String selectedMembers = (String) request.get("selectedMembers");
	    	
	    	scheduleService.updateEmployeeSchedule(eventId, scheduleDto, scheduleRepeatDto);
	        scheduleService.updateParticipants(scheduleDto, selectedMembers);  

	    	resultMap.put("res_code", "200");
	    	resultMap.put("res_msg", "일정이 수정되었습니다.");
	    	return resultMap;

 
	    } catch (Exception e) {
	        resultMap.put("res_code", "500");
	        resultMap.put("res_msg", "서버 오류가 발생했습니다.");
	        e.printStackTrace();
	    }

	    return resultMap;
	}

	// 예외 일정 참여자 정보  
	@GetMapping("/api/participate/member/schedules/exception/{scheduleExceptionNo}/{memberNo}")
	@ResponseBody
	public Map<String, Object> getparticipateMemberExceptionSchedules(@PathVariable("scheduleExceptionNo") Long scheduleExceptionNo, @PathVariable("memberNo") Long memberNo) {
	    Map<String, Object> response = new HashMap<>();
	     
	    List<ScheduleExceptionParticipantDto> participants = scheduleService.getParticipantsByExceptionReservationNo(scheduleExceptionNo, memberNo);
	     
	    response.put("participants", participants);  
	    return response;
	}
	 
	// 사원 - 반복 일정 수정
	@ResponseBody
	@PostMapping("/employee/schedule/edit/recurring/{eventId}/{scheduleEditType}")
	public Map<String, String> editEmployeeRecurringEvent(
	        @PathVariable("eventId") Long eventId,
	        @RequestBody Map<String, Object> request,  
	        @RequestParam("editOption") Long editOption,
	        @RequestParam("pickStartDate") String pickStartDate,
	        @RequestParam("pickEndDate") String pickEndDate,
	        @PathVariable("scheduleEditType") String scheduleEditType) { 
	    Map<String, String> resultMap = new HashMap<>();
	    resultMap.put("res_code", "404");
	    resultMap.put("res_msg", "일정 수정 중 오류가 발생했습니다."); 
 
	    ScheduleDto scheduleDto = new ScheduleDto();
    	ScheduleRepeatDto scheduleRepeatDto = new ScheduleRepeatDto();

    	scheduleDto.setSchedule_no(eventId);
    	scheduleDto.setSchedule_title((String) request.get("title"));
    	scheduleDto.setSchedule_comment((String) request.get("description"));
    	scheduleDto.setSchedule_start_date((String) request.get("startDate"));
    	scheduleDto.setSchedule_end_date((String) request.get("endDate"));
    	scheduleDto.setSchedule_allday(Boolean.TRUE.equals(request.get("allDay")) ? 1L : 0L);
    	scheduleDto.setSchedule_start_time((String) request.get("startTime"));
    	scheduleDto.setSchedule_end_time((String) request.get("endTime"));
    	scheduleDto.setSchedule_category_no(getLongValue(request.get("category")));
    	scheduleDto.setSchedule_type(getLongValue(request.get("schedule_type")));
    	scheduleDto.setDepartment_no(getLongValue(request.get("department_no")));
    	scheduleDto.setMember_no(getLongValue(request.get("memberNo")));

    	Long repeatValue = getLongValue(request.get("repeat"));
    	scheduleDto.setSchedule_repeat(repeatValue != null && repeatValue != 0 ? 1L : 0L);
    	if (repeatValue != null && repeatValue != 0) {
    	    scheduleRepeatDto.setSchedule_repeat_type(repeatValue);
    	    scheduleRepeatDto.setSchedule_repeat_day(getLongValue(request.get("schedule_day_of_week")));
    	    scheduleRepeatDto.setSchedule_repeat_week(getLongValue(request.get("schedule_week")));
    	    scheduleRepeatDto.setSchedule_repeat_date(getLongValue(request.get("schedule_date")));
    	    scheduleRepeatDto.setSchedule_repeat_month(getLongValue(request.get("schedule_month")));
    	    scheduleRepeatDto.setSchedule_repeat_end_date((String) request.get("repeatEndDate"));
    	}
	    
	    String selectedMembers = (String) request.get("selectedMembers");
	    try {
	        switch (editOption.intValue()) {
	            case 1: // 이 일정만 수정
	            	var newExceptionScheduleNo = scheduleService.updateEmployeeSingleEvent(eventId, scheduleDto, scheduleRepeatDto, pickStartDate, pickEndDate);
	            	scheduleDto.setSchedule_no(newExceptionScheduleNo);
	            	scheduleService.saveExceptionParticipants(scheduleDto, selectedMembers); 
	            	resultMap.put("res_code", "200");
	                resultMap.put("res_msg", "일정이 수정되었습니다.");
	                break;

	            case 2: // 이 일정 및 향후 일정 수정
	            	var newScheduleNo =  scheduleService.updateFutureEmployeeSchedule(eventId, scheduleDto, scheduleRepeatDto, pickStartDate);
	    	        scheduleDto.setSchedule_no(newScheduleNo);
	    	        scheduleService.updateParticipants(scheduleDto, selectedMembers); 
	    	        
	                resultMap.put("res_code", "200");
	    	    	resultMap.put("res_msg", "일정이 수정되었습니다."); 
	    	        break;

	            case 3: // 모든 일정 수정
	            	scheduleService.updateEmployeeSchedule(eventId, scheduleDto, scheduleRepeatDto);
    	            scheduleService.updateParticipants(scheduleDto, selectedMembers);  

    	            resultMap.put("res_code", "200");
	    	    	resultMap.put("res_msg", "일정이 수정되었습니다."); 
	    	        break;
	            default: 
	                resultMap.put("res_code", "200");
	    	    	resultMap.put("res_msg", "일정이 수정되었습니다.");
	    	    	return resultMap;
	        }
	    } catch (Exception e) {
	        resultMap.put("res_msg", e.getMessage());
	    }

	    return resultMap;
	}
	
	// 예외 수정
	@ResponseBody
	@PostMapping("/employee/schedule/exception/edit/{eventId}/{scheduleEditType}")
	public Map<String, Object> getExceptionEmployeeScheduleById(@PathVariable("eventId") Long eventId, @PathVariable("scheduleEditType") String scheduleEditType, @RequestBody Map<String, Object> request) {

	    Map<String, Object> resultMap = new HashMap<>(); 
	     
	    try {  
	    	ScheduleExceptionDto scheduleExceptionDto = new ScheduleExceptionDto(); 
 
	    	scheduleExceptionDto.setSchedule_exception_no(eventId);
	    	scheduleExceptionDto.setSchedule_exception_title((String) request.get("title"));
	    	scheduleExceptionDto.setSchedule_exception_comment((String) request.get("description"));
	    	scheduleExceptionDto.setSchedule_exception_start_date((String) request.get("startDate"));
	    	scheduleExceptionDto.setSchedule_exception_end_date((String) request.get("endDate"));
	    	scheduleExceptionDto.setSchedule_exception_allday(Boolean.TRUE.equals(request.get("allDay")) ? 1L : 0L);
	    	scheduleExceptionDto.setSchedule_exception_start_time((String) request.get("startTime"));
	    	scheduleExceptionDto.setSchedule_exception_end_time((String) request.get("endTime"));
	    	scheduleExceptionDto.setSchedule_category_no(getLongValue(request.get("category")));
	    	scheduleExceptionDto.setSchedule_exception_type(getLongValue(request.get("schedule_type")));
	    	scheduleExceptionDto.setDepartment_no(getLongValue(request.get("department_no")));
	    	scheduleExceptionDto.setMember_no(getLongValue(request.get("memberNo")));
   
	    	String selectedMembers = (String) request.get("selectedMembers");
	    	 
	    	scheduleService.updateExceptionEmployeeSchedule(eventId, scheduleExceptionDto);
	        scheduleService.updateExceptionParticipants(eventId, scheduleExceptionDto, selectedMembers);   
	        return resultMap;  

 
	    } catch (Exception e) {
	        resultMap.put("res_code", "500");
	        resultMap.put("res_msg", "서버 오류가 발생했습니다.");
	        e.printStackTrace();
	    }

	    return resultMap;
	}
	
	// 예외 일정 참여자
	@GetMapping("/employee/schedule/participate/exception/{scheduleNo}")
	@ResponseBody
	public Map<String, Object> getExceptionScheduleParticipant(@PathVariable("scheduleNo") Long scheduleNo) {
	    Map<String, Object> response = new HashMap<>();
	     
	    List<ScheduleExceptionParticipantDto> participants = scheduleService.getParticipantsByExceptionReservationNo(scheduleNo);
	     
	    response.put("participants", participants);
	    
	    return response;
	}
	
	// 휴가 정보
	@GetMapping("/api/employee/vacation/schedules")
	@ResponseBody
	public Map<String, Object> getVacationSchedules() {
        Map<String, Object> response = new HashMap<>();
         
        List<VacationApprovalDto> vacationSchedules = vacationApprovalService.getApprovedVacationSchedules();
         
        response.put("vacationSchedules", vacationSchedules);
        
        return response;
    }
	
	// 회의 정보
	@GetMapping("/api/employee/meeting/schedules")
	@ResponseBody
	public Map<String, Object> getMeetingSchedules() {
	    Map<String, Object> response = new HashMap<>();
	    
	    List<MeetingReservationDto> meetingSchedules = meetingReservationService.getMeetingSchedules();
	    
	    response.put("meetingSchedules", meetingSchedules);
	    
	    return response;
	} 
	
	// 회의실 참여자
	@GetMapping("/api/employee/meeting/schedules/{scheduleNo}/{memberNo}")
	@ResponseBody
	public Map<String, Object> getparticipateMeeting(@PathVariable("scheduleNo") Long scheduleNo, @PathVariable("memberNo") Long memberNo) {
	    Map<String, Object> response = new HashMap<>();
	     
	    List<MeetingParticipantDto> participants = meetingReservationService.getParticipantsByMeetingNoOwn(scheduleNo, memberNo);
	     
	    response.put("participants", participants); 
	    return response;
	} 
}
