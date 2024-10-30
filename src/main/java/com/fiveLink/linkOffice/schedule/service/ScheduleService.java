package com.fiveLink.linkOffice.schedule.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fiveLink.linkOffice.meeting.domain.MeetingParticipant;
import com.fiveLink.linkOffice.meeting.domain.MeetingParticipantDto;
import com.fiveLink.linkOffice.meeting.domain.MeetingReservation;
import com.fiveLink.linkOffice.meeting.domain.MeetingReservationDto;
import com.fiveLink.linkOffice.member.domain.Member;
import com.fiveLink.linkOffice.member.repository.MemberRepository;
import com.fiveLink.linkOffice.schedule.domain.Schedule;
import com.fiveLink.linkOffice.schedule.domain.ScheduleCheck;
import com.fiveLink.linkOffice.schedule.domain.ScheduleCheckDto;
import com.fiveLink.linkOffice.schedule.domain.ScheduleDto;
import com.fiveLink.linkOffice.schedule.domain.ScheduleException;
import com.fiveLink.linkOffice.schedule.domain.ScheduleExceptionDto;
import com.fiveLink.linkOffice.schedule.domain.ScheduleExceptionParticipant;
import com.fiveLink.linkOffice.schedule.domain.ScheduleExceptionParticipantDto;
import com.fiveLink.linkOffice.schedule.domain.ScheduleParticipant;
import com.fiveLink.linkOffice.schedule.domain.ScheduleParticipantDto;
import com.fiveLink.linkOffice.schedule.domain.ScheduleRepeat;
import com.fiveLink.linkOffice.schedule.domain.ScheduleRepeatDto;
import com.fiveLink.linkOffice.schedule.repository.ScheduleCheckRepository;
import com.fiveLink.linkOffice.schedule.repository.ScheduleExceptionParticipantRepository;
import com.fiveLink.linkOffice.schedule.repository.ScheduleExceptionRepository;
import com.fiveLink.linkOffice.schedule.repository.ScheduleParticipantRepository;
import com.fiveLink.linkOffice.schedule.repository.ScheduleRepeatRepository;
import com.fiveLink.linkOffice.schedule.repository.ScheduleRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class ScheduleService { 
    private final ScheduleRepository scheduleRepository; 
    private final ScheduleRepeatRepository scheduleRepeatRepository;
    private final ScheduleExceptionRepository scheduleExceptionRepository;
    private final ScheduleCheckRepository scheduleCheckRepository;
    private final MemberRepository memberRepository;
    private final ScheduleParticipantRepository scheduleParticipantRepository;
    private final ScheduleParticipantService scheduleParticipantService;
    private final ScheduleExceptionParticipantRepository scheduleExceptionParticipantRepository;
    
	@Autowired
	public ScheduleService(ScheduleRepository scheduleRepository, ScheduleRepeatRepository scheduleRepeatRepository, ScheduleExceptionRepository scheduleExceptionRepositor, ScheduleExceptionRepository scheduleExceptionRepository, 
			ScheduleCheckRepository scheduleCheckRepository, MemberRepository memberRepository, ScheduleParticipantRepository scheduleParticipantRepository, ScheduleParticipantService scheduleParticipantService, ScheduleExceptionParticipantRepository scheduleExceptionParticipantRepository) { 
	    this.scheduleRepository = scheduleRepository; 
	    this.scheduleRepeatRepository = scheduleRepeatRepository; 
	    this.scheduleExceptionRepository = scheduleExceptionRepository; 
	    this.scheduleCheckRepository = scheduleCheckRepository; 
	    this.memberRepository = memberRepository;
	    this.scheduleParticipantRepository = scheduleParticipantRepository;
	    this.scheduleParticipantService = scheduleParticipantService;
	    this.scheduleExceptionParticipantRepository = scheduleExceptionParticipantRepository;
	} 
  
	public void saveCompanySchedule(ScheduleDto scheduleDto, ScheduleRepeatDto scheduleRepeatDto) {
	    Schedule schedule = Schedule.builder()
	    		.memberNo(scheduleDto.getMember_no())
	            .scheduleTitle(scheduleDto.getSchedule_title())
	            .scheduleComment(scheduleDto.getSchedule_comment())
	            .scheduleStartDate(scheduleDto.getSchedule_start_date())
	            .scheduleAllday(scheduleDto.getSchedule_allday())
	            .scheduleEndDate(scheduleDto.getSchedule_end_date())
	            .scheduleStartTime(scheduleDto.getSchedule_start_time())
	            .scheduleEndTime(scheduleDto.getSchedule_end_time())
	            .scheduleCategoryNo(scheduleDto.getSchedule_category_no())
	            .scheduleRepeat(scheduleDto.getSchedule_repeat())
	            .scheduleType(3L)
	            .scheduleStatus(0L)
	            .build();

	    scheduleRepository.save(schedule);

	    // 반복 일정 저장
	    if (scheduleDto.getSchedule_repeat() != 0) {
	        ScheduleRepeat repeat = ScheduleRepeat.builder()
	                .scheduleNo(schedule.getScheduleNo())
	                .scheduleRepeatType(scheduleRepeatDto.getSchedule_repeat_type())
	                .scheduleRepeatDay(determineRepeatDay(scheduleRepeatDto.getSchedule_repeat_type(), scheduleRepeatDto.getSchedule_repeat_day())) // 요일
	                .scheduleRepeatWeek(determineRepeatWeek(scheduleRepeatDto.getSchedule_repeat_type(), scheduleRepeatDto.getSchedule_repeat_week())) // 주차
	                .scheduleRepeatDate(determineRepeatDate(scheduleRepeatDto.getSchedule_repeat_type(), scheduleRepeatDto.getSchedule_repeat_date())) // 일자
	                .scheduleRepeatMonth(determineRepeatMonth(scheduleRepeatDto.getSchedule_repeat_type(), scheduleRepeatDto.getSchedule_repeat_month())) // 월
	                .scheduleRepeatEndDate(scheduleRepeatDto.getSchedule_repeat_end_date()) 
	                .build();

	        scheduleRepeatRepository.save(repeat);
	    }
	}

	private Long determineRepeatDay(long repeatOption, Long dayOfWeek) {
	    // 2 (매주) - 요일  
	    if ((repeatOption == 2 || repeatOption == 4) && dayOfWeek != null) {
	        return dayOfWeek; 
	    }
	    return null; 
	}

	private Long determineRepeatWeek(long repeatOption, Long week) {
	    // 4 (매월 주)- 주차 
	    if (repeatOption == 4 && week != null) {
	        return week;  
	    }
	    return null;  
	}

	private Long determineRepeatDate(long repeatOption, Long date) {
	    // 3 (매월 일), 5 (매년)- 일  
	    if ((repeatOption == 3 || repeatOption == 5) && date != null) {
	        return date;  
	    }
	    return null; 
	}

	private Long determineRepeatMonth(long repeatOption, Long month) {
	    // 5 (매년) - 월  
	    if (repeatOption == 5 && month != null) {
	        return month;  
	    }
	    return null;  
	}
 
	// 관리자 - 월간 일정 출력
	public List<Schedule> getAllSchedules() { 
	    return scheduleRepository.findByScheduleTypeAndScheduleStatus(3L, 0L);
	}
	
	// 관리자 - 반복 일정  
	public List<ScheduleRepeat> getAllRepeatSchedules() { 
        return scheduleRepeatRepository.findAllScheduleRepeats();
    }

	// 관리자 - 수정 
	public ScheduleDto getScheduleById(Long eventNo) {
        Optional<Schedule> scheduleOpt = scheduleRepository.findById(eventNo);
        if (scheduleOpt.isPresent()) {
            return convertToDto(scheduleOpt.get());
        }
        return null;
    }
 
	public ScheduleRepeatDto getScheduleRepeatById(Long eventNo) { 
	    List<ScheduleRepeat> scheduleRepeatList = scheduleRepeatRepository.findByScheduleNo(eventNo);
	    if (!scheduleRepeatList.isEmpty()) {  
	        return convertToRepeatDto(scheduleRepeatList.get(0)); 
	    }
	    return null;  
	}

    private ScheduleRepeatDto convertToRepeatDto(ScheduleRepeat scheduleRepeat) {
        ScheduleRepeatDto dto = new ScheduleRepeatDto(); 
        dto.setSchedule_repeat_type(scheduleRepeat.getScheduleRepeatType());
        dto.setSchedule_no(scheduleRepeat.getScheduleNo()); 
        dto.setSchedule_repeat_type(scheduleRepeat.getScheduleRepeatType()); 
        dto.setSchedule_repeat_day(scheduleRepeat.getScheduleRepeatDay()); 
        dto.setSchedule_repeat_week(scheduleRepeat.getScheduleRepeatWeek()); 
        dto.setSchedule_repeat_date(scheduleRepeat.getScheduleRepeatDate()); 
        dto.setSchedule_repeat_month(scheduleRepeat.getScheduleRepeatMonth()); 
        dto.setSchedule_repeat_end_date(scheduleRepeat.getScheduleRepeatEndDate()); 
        return dto;
    }

    private ScheduleDto convertToDto(Schedule schedule) { 
        ScheduleDto dto = new ScheduleDto(); 
        dto.setSchedule_no(schedule.getScheduleNo());
        dto.setMember_no(schedule.getMemberNo());
        dto.setSchedule_title(schedule.getScheduleTitle());
        dto.setSchedule_comment(schedule.getScheduleComment());
        dto.setSchedule_start_date(schedule.getScheduleStartDate());
        dto.setSchedule_end_date(schedule.getScheduleEndDate());
        dto.setSchedule_start_time(schedule.getScheduleStartTime());
        dto.setSchedule_end_time(schedule.getScheduleEndTime());
        dto.setSchedule_allday(schedule.getScheduleAllday());
        dto.setSchedule_category_no(schedule.getScheduleCategoryNo());
        dto.setSchedule_type(schedule.getScheduleType());
        dto.setSchedule_repeat(schedule.getScheduleRepeat());
        dto.setSchedule_create_date(schedule.getScheduleCreateDate());
        return dto;
    }
    
    // 관리자 - 일반 일정 수정
    public void updateCompanySchedule(Long eventId, ScheduleDto scheduleDto, ScheduleRepeatDto scheduleRepeatDto) {
        Schedule existingSchedule = scheduleRepository.findById(eventId)
            .orElseThrow(() -> new RuntimeException("일정을 찾을 수 없습니다."));
         
        existingSchedule.setScheduleTitle(scheduleDto.getSchedule_title());
        existingSchedule.setScheduleComment(scheduleDto.getSchedule_comment());
        existingSchedule.setScheduleStartDate(scheduleDto.getSchedule_start_date());
        existingSchedule.setScheduleEndDate(scheduleDto.getSchedule_end_date());
        existingSchedule.setScheduleAllday(scheduleDto.getSchedule_allday());
        existingSchedule.setScheduleStartTime(scheduleDto.getSchedule_start_time());
        existingSchedule.setScheduleEndTime(scheduleDto.getSchedule_end_time());
        existingSchedule.setScheduleRepeat(scheduleDto.getSchedule_repeat());
         
        scheduleRepository.save(existingSchedule);
 
        if (scheduleDto.getSchedule_repeat() != 0) { 
            ScheduleRepeat existingRepeat = scheduleRepeatRepository.getByScheduleNo(eventId);
            
            if (existingRepeat != null) { 
                existingRepeat.setScheduleRepeatType(scheduleRepeatDto.getSchedule_repeat_type());
                existingRepeat.setScheduleRepeatDay(determineRepeatDay(scheduleRepeatDto.getSchedule_repeat_type(), scheduleRepeatDto.getSchedule_repeat_day())); // 요일
                existingRepeat.setScheduleRepeatWeek(determineRepeatWeek(scheduleRepeatDto.getSchedule_repeat_type(), scheduleRepeatDto.getSchedule_repeat_week())); // 주차
                existingRepeat.setScheduleRepeatDate(determineRepeatDate(scheduleRepeatDto.getSchedule_repeat_type(), scheduleRepeatDto.getSchedule_repeat_date())); // 일자
                existingRepeat.setScheduleRepeatMonth(determineRepeatMonth(scheduleRepeatDto.getSchedule_repeat_type(), scheduleRepeatDto.getSchedule_repeat_month())); // 월
                existingRepeat.setScheduleRepeatEndDate(scheduleRepeatDto.getSchedule_repeat_end_date()); 
 
                scheduleRepeatRepository.save(existingRepeat);
            } else { 
                ScheduleRepeat repeat = ScheduleRepeat.builder()
                        .scheduleNo(eventId)
                        .scheduleRepeatType(scheduleRepeatDto.getSchedule_repeat_type())
                        .scheduleRepeatDay(determineRepeatDay(scheduleRepeatDto.getSchedule_repeat_type(), scheduleRepeatDto.getSchedule_repeat_day())) // 요일
                        .scheduleRepeatWeek(determineRepeatWeek(scheduleRepeatDto.getSchedule_repeat_type(), scheduleRepeatDto.getSchedule_repeat_week())) // 주차
                        .scheduleRepeatDate(determineRepeatDate(scheduleRepeatDto.getSchedule_repeat_type(), scheduleRepeatDto.getSchedule_repeat_date())) // 일자
                        .scheduleRepeatMonth(determineRepeatMonth(scheduleRepeatDto.getSchedule_repeat_type(), scheduleRepeatDto.getSchedule_repeat_month())) // 월
                        .scheduleRepeatEndDate(scheduleRepeatDto.getSchedule_repeat_end_date()) 
                        .build();
 
                scheduleRepeatRepository.save(repeat);
            }
        }
    }
    
    
    // 관리자 - 반복 일정 수정
    // 이 일정만 수정
    public void updateSingleEvent(Long eventId, ScheduleDto scheduleDto, ScheduleRepeatDto scheduleRepeatDto, String pickStartDate, String pickEndDate) {
        Schedule schedule = scheduleRepository.findById(eventId)
            .orElseThrow(() -> new EntityNotFoundException("일정을 찾을 수 없습니다."));

        // 예외 일정으로 저장 
        ScheduleException scheduleException = ScheduleException.builder()
                .scheduleNo(schedule.getScheduleNo())
                .memberNo(scheduleDto.getMember_no())
                .scheduleExceptionDate(pickStartDate)
                .scheduleExceptionTitle(scheduleDto.getSchedule_title())
                .scheduleExceptionComment(scheduleDto.getSchedule_comment())
                .scheduleExceptionStartDate(scheduleDto.getSchedule_start_date())
                .scheduleExceptionEndDate(scheduleDto.getSchedule_end_date())
                .scheduleExceptionStartTime(scheduleDto.getSchedule_start_time())
                .scheduleExceptionEndTime(scheduleDto.getSchedule_end_time())
                .scheduleExceptionAllday(scheduleDto.getSchedule_allday())
                .scheduleCategoryNo(scheduleDto.getSchedule_category_no()) 
                .scheduleExceptionType(3L) 
                .scheduleExceptionAllday(scheduleDto.getSchedule_allday())
                .scheduleExceptionStatus(0L)
                .build();
        scheduleExceptionRepository.save(scheduleException);  
    }

    // 이 일정 및 향후 일정 수정
    public void updateFutureEvents(Long eventId, ScheduleDto scheduleDto, ScheduleRepeatDto scheduleRepeatDto, String pickStartDate) {
        Schedule schedule = scheduleRepository.findById(eventId)
            .orElseThrow(() -> new EntityNotFoundException("일정을 찾을 수 없습니다."));
        
        ScheduleRepeat scheduleRepeat = scheduleRepeatRepository.getByScheduleNo(eventId);
        
        LocalDate adjustedEndDate = LocalDate.parse(pickStartDate).minusDays(1);
        String adjustedEndDateString = adjustedEndDate.format(DateTimeFormatter.ISO_LOCAL_DATE); 
        scheduleRepeat.setScheduleRepeatEndDate(adjustedEndDateString); 
        scheduleRepeatRepository.save(scheduleRepeat);
        
        saveCompanySchedule(scheduleDto, scheduleRepeatDto);  
    }

    // 모든 일정 수정 
    public void updateAllEvents(Long eventId, ScheduleDto scheduleDto, ScheduleRepeatDto scheduleRepeatDto) {  
        Schedule schedule = scheduleRepository.findById(eventId)
            .orElseThrow(() -> new EntityNotFoundException("일정을 찾을 수 없습니다."));
 
        schedule.setScheduleTitle(scheduleDto.getSchedule_title());
        schedule.setScheduleComment(scheduleDto.getSchedule_comment()); 
        schedule.setScheduleAllday(scheduleDto.getSchedule_allday());
        schedule.setScheduleCategoryNo(scheduleDto.getSchedule_category_no());
        schedule.setScheduleStartTime(scheduleDto.getSchedule_start_time());
        schedule.setScheduleEndTime(scheduleDto.getSchedule_end_time());
        schedule.setScheduleRepeat(scheduleDto.getSchedule_repeat());
        scheduleRepository.save(schedule); 
 
        if (scheduleDto.getSchedule_repeat() != 0) { 
            ScheduleRepeat existingRepeat = scheduleRepeatRepository.getByScheduleNo(eventId);
            
            if (existingRepeat != null) { 
                existingRepeat.setScheduleRepeatType(scheduleRepeatDto.getSchedule_repeat_type());
                existingRepeat.setScheduleRepeatDay(determineRepeatDay(scheduleRepeatDto.getSchedule_repeat_type(), scheduleRepeatDto.getSchedule_repeat_day())); // 요일
                existingRepeat.setScheduleRepeatWeek(determineRepeatWeek(scheduleRepeatDto.getSchedule_repeat_type(), scheduleRepeatDto.getSchedule_repeat_week())); // 주차
                existingRepeat.setScheduleRepeatDate(determineRepeatDate(scheduleRepeatDto.getSchedule_repeat_type(), scheduleRepeatDto.getSchedule_repeat_date())); // 일자
                existingRepeat.setScheduleRepeatMonth(determineRepeatMonth(scheduleRepeatDto.getSchedule_repeat_type(), scheduleRepeatDto.getSchedule_repeat_month())); // 월
                existingRepeat.setScheduleRepeatEndDate(scheduleRepeatDto.getSchedule_repeat_end_date()); 
 
                scheduleRepeatRepository.save(existingRepeat);
            } else { 
                ScheduleRepeat repeat = ScheduleRepeat.builder()
                        .scheduleNo(eventId)
                        .scheduleRepeatType(scheduleRepeatDto.getSchedule_repeat_type())
                        .scheduleRepeatDay(determineRepeatDay(scheduleRepeatDto.getSchedule_repeat_type(), scheduleRepeatDto.getSchedule_repeat_day())) // 요일
                        .scheduleRepeatWeek(determineRepeatWeek(scheduleRepeatDto.getSchedule_repeat_type(), scheduleRepeatDto.getSchedule_repeat_week())) // 주차
                        .scheduleRepeatDate(determineRepeatDate(scheduleRepeatDto.getSchedule_repeat_type(), scheduleRepeatDto.getSchedule_repeat_date())) // 일자
                        .scheduleRepeatMonth(determineRepeatMonth(scheduleRepeatDto.getSchedule_repeat_type(), scheduleRepeatDto.getSchedule_repeat_month())) // 월
                        .scheduleRepeatEndDate(scheduleRepeatDto.getSchedule_repeat_end_date()) 
                        .build();
 
                scheduleRepeatRepository.save(repeat);
            }
        }
    }  
    
    // 예외 일정 가져오기
    public List<ScheduleException> getAllExceptionSchedules() { 
	    return scheduleExceptionRepository.findAll(); 
	}
    
    // 예외 일정 상세
    public ScheduleExceptionDto getScheduleExceptionById(Long eventNo) {
        Optional<ScheduleException> scheduleExceptionOpt = scheduleExceptionRepository.findById(eventNo);
        if (scheduleExceptionOpt.isPresent()) {
            return convertToExceptionDto(scheduleExceptionOpt.get());
        }
        return null;
    }
    
    private ScheduleExceptionDto convertToExceptionDto(ScheduleException scheduleException) { 
        ScheduleExceptionDto dto = new ScheduleExceptionDto(); 
        dto.setSchedule_no(scheduleException.getScheduleNo()); 
        dto.setMember_no(scheduleException.getMemberNo());
        dto.setSchedule_exception_title(scheduleException.getScheduleExceptionTitle());
        dto.setSchedule_exception_comment(scheduleException.getScheduleExceptionComment());
        dto.setSchedule_exception_start_date(scheduleException.getScheduleExceptionStartDate());
        dto.setSchedule_exception_end_date(scheduleException.getScheduleExceptionEndDate());
        dto.setSchedule_exception_start_time(scheduleException.getScheduleExceptionStartTime());
        dto.setSchedule_exception_end_time(scheduleException.getScheduleExceptionEndTime());
        dto.setSchedule_exception_allday(scheduleException.getScheduleExceptionAllday());
        dto.setSchedule_category_no(scheduleException.getScheduleCategoryNo());  
        dto.setSchedule_exception_type(scheduleException.getScheduleExceptionType()); 
        dto.setDepartment_no(scheduleException.getDepartmentNo()); 
        dto.setSchedule_exception_create_date(scheduleException.getScheduleExceptionCreateDate());
        return dto;
    }
    
    // 예외 일정 수정
    public void updateCompanyExceptionSchedule(Long eventId, ScheduleExceptionDto scheduleExceptionDto) {
        ScheduleException existingSchedule = scheduleExceptionRepository.findById(eventId)
            .orElseThrow(() -> new RuntimeException("일정을 찾을 수 없습니다."));
         
        existingSchedule.setScheduleExceptionTitle(scheduleExceptionDto.getSchedule_exception_title());
        existingSchedule.setScheduleExceptionComment(scheduleExceptionDto.getSchedule_exception_comment());
        existingSchedule.setScheduleExceptionStartDate(scheduleExceptionDto.getSchedule_exception_start_date());
        existingSchedule.setScheduleExceptionEndDate(scheduleExceptionDto.getSchedule_exception_end_date());
        existingSchedule.setScheduleExceptionAllday(scheduleExceptionDto.getSchedule_exception_allday());
        existingSchedule.setScheduleExceptionStartTime(scheduleExceptionDto.getSchedule_exception_start_time());
        existingSchedule.setScheduleExceptionEndTime(scheduleExceptionDto.getSchedule_exception_end_time()); 
         
        scheduleExceptionRepository.save(existingSchedule); 
    }
    
    // 기본 일정 삭제
    @Transactional
    public boolean deleteBasicSchedule(Long eventId) {
        try {
            Optional<Schedule> scheduleOptional = scheduleRepository.findById(eventId);
            if (scheduleOptional.isPresent()) {
                Schedule schedule = scheduleOptional.get();
                schedule.setScheduleStatus(1L);  
                scheduleRepository.save(schedule); 
                return true;  
            }
        } catch (Exception e) {
            e.printStackTrace();  
        }
        return false;  
    }
    
    // 예외 일정 삭제
    @Transactional
    public boolean deleteExceptionSchedule(Long eventId) {
        try {
            Optional<ScheduleException> scheduleOptional = scheduleExceptionRepository.findById(eventId);
            if (scheduleOptional.isPresent()) {
            	ScheduleException scheduleException = scheduleOptional.get();
            	scheduleException.setScheduleExceptionStatus(1L);  
            	scheduleExceptionRepository.save(scheduleException); 
                return true;  
            }
        } catch (Exception e) {
            e.printStackTrace();  
        }
        return false;  
    }
    
    // 관리자 - 모든 일정 삭제
    public boolean deleteAllEvents(Long eventId) {   
        try {
            Optional<Schedule> scheduleOptional = scheduleRepository.findById(eventId);
            if (scheduleOptional.isPresent()) {
                Schedule schedule = scheduleOptional.get();
                schedule.setScheduleStatus(1L);  
                scheduleRepository.save(schedule); 
                return true;  
            }
        } catch (Exception e) {
            e.printStackTrace();  
        }
        return false;   
    }  
    
    // 관리자 - 이 일정 및 향후 일정 삭제
    public boolean deleteFutureEvents(Long eventId, ScheduleDto scheduleDto, ScheduleRepeatDto scheduleRepeatDto, String pickStartDate, String pickEndDate) {
    	Schedule schedule = scheduleRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("일정을 찾을 수 없습니다."));
            
            ScheduleRepeat scheduleRepeat = scheduleRepeatRepository.getByScheduleNo(eventId);
            
            LocalDate adjustedEndDate = LocalDate.parse(pickStartDate).minusDays(1);
            String adjustedEndDateString = adjustedEndDate.format(DateTimeFormatter.ISO_LOCAL_DATE); 
            scheduleRepeat.setScheduleRepeatEndDate(adjustedEndDateString); 
            scheduleRepeatRepository.save(scheduleRepeat);
            
            LocalDate EndDate = LocalDate.parse(pickEndDate).minusDays(1);
            String EndDateString = adjustedEndDate.format(DateTimeFormatter.ISO_LOCAL_DATE); 
            
            
            scheduleDto.setSchedule_start_date(adjustedEndDateString);
            scheduleDto.setSchedule_end_date(EndDateString); 
            
            Long newScheduleId = savedeleteCompanySchedule(scheduleDto, scheduleRepeatDto);  
             
             
        try {
            Optional<Schedule> newScheduleOptional = scheduleRepository.findById(newScheduleId);
            if (newScheduleOptional.isPresent()) {
                Schedule newSchedule  = newScheduleOptional.get();
                newSchedule.setScheduleStatus(1L);  
                scheduleRepository.save(schedule); 
                return true;  
            }
        } catch (Exception e) {
            e.printStackTrace();  
        }
        return false;   
    }
    
    // 관리자 - 이 일정만 삭제
    public boolean deleteSingleEvent(Long eventId, ScheduleDto scheduleDto, ScheduleRepeatDto scheduleRepeatDto, String pickStartDate, String pickEndDate) {
        Schedule schedule = scheduleRepository.findById(eventId)
            .orElseThrow(() -> new EntityNotFoundException("일정을 찾을 수 없습니다."));

        // 예외 일정으로 저장 
        ScheduleException scheduleException = ScheduleException.builder()
                .scheduleNo(schedule.getScheduleNo())
                .scheduleExceptionDate(pickStartDate)
                .scheduleExceptionTitle(scheduleDto.getSchedule_title())
                .scheduleExceptionComment(scheduleDto.getSchedule_comment())
                .scheduleExceptionStartDate(pickStartDate)
                .scheduleExceptionEndDate(pickEndDate)
                .scheduleExceptionStartTime(scheduleDto.getSchedule_start_time())
                .scheduleExceptionEndTime(scheduleDto.getSchedule_end_time())
                .scheduleCategoryNo(scheduleDto.getSchedule_category_no()) 
                .scheduleExceptionAllday(scheduleDto.getSchedule_allday())
                .scheduleExceptionStatus(0L)
                .build();
        scheduleExceptionRepository.save(scheduleException);  
        
        Long scheduleExceptionNo = scheduleException.getScheduleExceptionNo(); 
        
        try {
            // 예외 일정의 상태를 삭제(1)로 변경
            Optional<ScheduleException> scheduleExceptionOptional = scheduleExceptionRepository.findById(scheduleExceptionNo);
            if (scheduleExceptionOptional.isPresent()) {
                ScheduleException exception = scheduleExceptionOptional.get();
                exception.setScheduleExceptionStatus(1L);   
                scheduleExceptionRepository.save(exception);  
                return true; 
            }
        } catch (Exception e) {
            e.printStackTrace();   
        }
        return false;   
    }
    
	public Long savedeleteCompanySchedule(ScheduleDto scheduleDto, ScheduleRepeatDto scheduleRepeatDto) {
	    Schedule schedule = Schedule.builder()
	    		.memberNo(scheduleDto.getMember_no())
	            .scheduleTitle(scheduleDto.getSchedule_title())
	            .scheduleComment(scheduleDto.getSchedule_comment())
	            .scheduleStartDate(scheduleDto.getSchedule_start_date())
	            .scheduleAllday(scheduleDto.getSchedule_allday())
	            .scheduleEndDate(scheduleDto.getSchedule_end_date())
	            .scheduleStartTime(scheduleDto.getSchedule_start_time())
	            .scheduleEndTime(scheduleDto.getSchedule_end_time())
	            .scheduleCategoryNo(scheduleDto.getSchedule_category_no())
	            .scheduleRepeat(scheduleDto.getSchedule_repeat())
	            .scheduleType(3L)
	            .scheduleStatus(0L)
	            .build();

	    scheduleRepository.save(schedule);

	    // 반복 일정 저장
	    if (scheduleDto.getSchedule_repeat() != 0) {
	        ScheduleRepeat repeat = ScheduleRepeat.builder()
	                .scheduleNo(schedule.getScheduleNo())
	                .scheduleRepeatType(scheduleRepeatDto.getSchedule_repeat_type())
	                .scheduleRepeatDay(determineRepeatDay(scheduleRepeatDto.getSchedule_repeat_type(), scheduleRepeatDto.getSchedule_repeat_day())) // 요일
	                .scheduleRepeatWeek(determineRepeatWeek(scheduleRepeatDto.getSchedule_repeat_type(), scheduleRepeatDto.getSchedule_repeat_week())) // 주차
	                .scheduleRepeatDate(determineRepeatDate(scheduleRepeatDto.getSchedule_repeat_type(), scheduleRepeatDto.getSchedule_repeat_date())) // 일자
	                .scheduleRepeatMonth(determineRepeatMonth(scheduleRepeatDto.getSchedule_repeat_type(), scheduleRepeatDto.getSchedule_repeat_month())) // 월
	                .scheduleRepeatEndDate(scheduleRepeatDto.getSchedule_repeat_end_date()) 
	                .build();

	        scheduleRepeatRepository.save(repeat);
	    }
	    return schedule.getScheduleNo();
	}
	
	// 사원
	// 사원 개인 일정 
	public List<Schedule> getAllpersonalSchedules(Long memberNo) { 
	    return scheduleRepository.findByScheduleTypeAndScheduleStatusAndMemberNo(0L, 0L, memberNo);
	}
	
	// 부서 일정
	// 사원 개인 일정 
	public List<Schedule> getAlldepartmentSchedules() { 
	    return scheduleRepository.findByScheduleTypeAndScheduleStatus(1L, 0L);
	}
	
	// 참여자 일정
	public List<Schedule> getAllparticipateSchedules() { 
	    return scheduleRepository.findByScheduleTypeAndScheduleStatus(2L, 0L);
	}  
	
	// 사원 부서 체크박스 상태 
	public List<ScheduleCheckDto> getScheduleChecksByMemberNo(Long memberNo) {
	    List<ScheduleCheck> scheduleChecks = scheduleCheckRepository.findByMemberNoAndScheduleCheckStatus(memberNo, 0L);
	    return scheduleChecks.stream()
	            .map(ScheduleCheckDto::toDto)  
	            .collect(Collectors.toList());
	} 

    // 사원 부서 체크박스 상태 저장
	public void updateScheduleCheck(Long memberNo, Long departmentNo, Long scheduleCheckStatus) { 

	    ScheduleCheck existingCheckStatusOne = scheduleCheckRepository.findByMemberNoAndDepartmentNoAndScheduleCheckStatus(memberNo, departmentNo, 1L);

	    ScheduleCheck existingCheckStatusZero = scheduleCheckRepository.findByMemberNoAndDepartmentNoAndScheduleCheckStatus(
	            memberNo, departmentNo, 0L);

	    if (scheduleCheckStatus.equals(0L)) { 
	        if (existingCheckStatusOne != null) {
	            existingCheckStatusOne.setScheduleCheckStatus(0L);
	            scheduleCheckRepository.save(existingCheckStatusOne);
	        } else if (existingCheckStatusZero == null) {
	            ScheduleCheck newScheduleCheck = ScheduleCheck.builder()
	                    .memberNo(memberNo)
	                    .departmentNo(departmentNo)
	                    .scheduleCheckStatus(0L)
	                    .build();
	            scheduleCheckRepository.save(newScheduleCheck);
	        }
	    } else { 
	        if (existingCheckStatusZero != null) {
	            existingCheckStatusZero.setScheduleCheckStatus(1L);
	            scheduleCheckRepository.save(existingCheckStatusZero);
	        }
	    }
	}
 
	// 일정 참여자 
	public List<ScheduleParticipantDto> getParticipantsByReservationNo(Long scheduleNo) { 
        List<ScheduleParticipant> participants = scheduleParticipantRepository.findParticipantsByScheduleNo(scheduleNo);
 
        return participants.stream().map(participant -> { 
            String memberName = memberRepository.findById(participant.getMemberNo())
                                               .map(Member::getMemberName)
                                               .orElse("사원");
            String positionName = "직위";
            String departmentName = "부서";
            
            Long memberNo = participant.getMemberNo();
            
            List<Object[]> memberInfo = memberRepository.findMemberWithDepartmentAndPosition(memberNo); 
            
            Object[] row = memberInfo.get(0);  
            positionName = (String) row[1];   
            departmentName = (String) row[2]; 
             
             
            return ScheduleParticipantDto.builder()
                    .schedule_participant_no(participant.getScheduleParticipantNo())
                    .schedule_no(participant.getScheduleNo())
                    .member_no(participant.getMemberNo())
                    .schedule_participant_status(participant.getScheduleParticipantStatus())
                    .memberName(memberName)  
                    .positionName(positionName)   
                    .departmentName(departmentName)
                    .build();
        }).collect(Collectors.toList());
    }
    
	// 참여자 일정 저장
    @Transactional
    public void saveScheduleAndParticipants(ScheduleDto scheduleDto, ScheduleRepeatDto scheduleRepeatDto, List<ScheduleParticipantDto> participants) { 
	    Schedule schedule = Schedule.builder()
	    		.memberNo(scheduleDto.getMember_no())
	            .scheduleTitle(scheduleDto.getSchedule_title())
	            .scheduleComment(scheduleDto.getSchedule_comment())
	            .scheduleStartDate(scheduleDto.getSchedule_start_date())
	            .scheduleAllday(scheduleDto.getSchedule_allday())
	            .scheduleEndDate(scheduleDto.getSchedule_end_date())
	            .scheduleStartTime(scheduleDto.getSchedule_start_time())
	            .scheduleEndTime(scheduleDto.getSchedule_end_time())
	            .scheduleCategoryNo(scheduleDto.getSchedule_category_no())
	            .scheduleRepeat(scheduleDto.getSchedule_repeat())
	            .scheduleType(scheduleDto.getSchedule_type())
	            .departmentNo(scheduleDto.getDepartment_no())
	            .scheduleStatus(0L)
	            .build();

	    scheduleRepository.save(schedule);

	    // 반복 일정 저장
	    if (scheduleDto.getSchedule_repeat() != 0) {
	        ScheduleRepeat repeat = ScheduleRepeat.builder()
	                .scheduleNo(schedule.getScheduleNo())
	                .scheduleRepeatType(scheduleRepeatDto.getSchedule_repeat_type())
	                .scheduleRepeatDay(determineRepeatDay(scheduleRepeatDto.getSchedule_repeat_type(), scheduleRepeatDto.getSchedule_repeat_day())) // 요일
	                .scheduleRepeatWeek(determineRepeatWeek(scheduleRepeatDto.getSchedule_repeat_type(), scheduleRepeatDto.getSchedule_repeat_week())) // 주차
	                .scheduleRepeatDate(determineRepeatDate(scheduleRepeatDto.getSchedule_repeat_type(), scheduleRepeatDto.getSchedule_repeat_date())) // 일자
	                .scheduleRepeatMonth(determineRepeatMonth(scheduleRepeatDto.getSchedule_repeat_type(), scheduleRepeatDto.getSchedule_repeat_month())) // 월
	                .scheduleRepeatEndDate(scheduleRepeatDto.getSchedule_repeat_end_date()) 
	                .build();

	        scheduleRepeatRepository.save(repeat);
	    }
	    
        // 참가자 정보 저장
        participants.forEach(participantDto -> {
            participantDto.setSchedule_no(schedule.getScheduleNo()); 
            scheduleParticipantRepository.save(participantDto.toEntity());  
        });
         
    }
	
    // 사원 일정 저장
    public void saveEmployeeSchedule(ScheduleDto scheduleDto, ScheduleRepeatDto scheduleRepeatDto) {
    	Schedule schedule = Schedule.builder()
	    		.memberNo(scheduleDto.getMember_no())
	            .scheduleTitle(scheduleDto.getSchedule_title())
	            .scheduleComment(scheduleDto.getSchedule_comment())
	            .scheduleStartDate(scheduleDto.getSchedule_start_date())
	            .scheduleAllday(scheduleDto.getSchedule_allday())
	            .scheduleEndDate(scheduleDto.getSchedule_end_date())
	            .scheduleStartTime(scheduleDto.getSchedule_start_time())
	            .scheduleEndTime(scheduleDto.getSchedule_end_time())
	            .scheduleCategoryNo(scheduleDto.getSchedule_category_no())
	            .scheduleRepeat(scheduleDto.getSchedule_repeat())
	            .scheduleType(scheduleDto.getSchedule_type())
	            .departmentNo(scheduleDto.getDepartment_no())
	            .scheduleStatus(0L)
	            .build();

	    scheduleRepository.save(schedule);

	    // 반복 일정 저장
	    if (scheduleDto.getSchedule_repeat() != 0) {
	        ScheduleRepeat repeat = ScheduleRepeat.builder()
	                .scheduleNo(schedule.getScheduleNo())
	                .scheduleRepeatType(scheduleRepeatDto.getSchedule_repeat_type())
	                .scheduleRepeatDay(determineRepeatDay(scheduleRepeatDto.getSchedule_repeat_type(), scheduleRepeatDto.getSchedule_repeat_day())) // 요일
	                .scheduleRepeatWeek(determineRepeatWeek(scheduleRepeatDto.getSchedule_repeat_type(), scheduleRepeatDto.getSchedule_repeat_week())) // 주차
	                .scheduleRepeatDate(determineRepeatDate(scheduleRepeatDto.getSchedule_repeat_type(), scheduleRepeatDto.getSchedule_repeat_date())) // 일자
	                .scheduleRepeatMonth(determineRepeatMonth(scheduleRepeatDto.getSchedule_repeat_type(), scheduleRepeatDto.getSchedule_repeat_month())) // 월
	                .scheduleRepeatEndDate(scheduleRepeatDto.getSchedule_repeat_end_date()) 
	                .build();

	        scheduleRepeatRepository.save(repeat);
	    }
    }

    // 참여자 정보 
    public List<ScheduleParticipantDto> getParticipantsByScheduleNo(Long scheduleNo) { 
        List<ScheduleParticipant> participants = scheduleParticipantRepository.findParticipantsByScheduleNo(scheduleNo);
 
        return participants.stream().map(participant -> { 
            String memberName = memberRepository.findById(participant.getMemberNo())
                                               .map(Member::getMemberName)
                                               .orElse("사원");
            String positionName = "직위";
            String departmentName = "부서";
            
            Long memberNo = participant.getMemberNo();
            
            List<Object[]> memberInfo = memberRepository.findMemberWithDepartmentAndPosition(memberNo); 
            
            Object[] row = memberInfo.get(0);  
            positionName = (String) row[1];   
            departmentName = (String) row[2]; 
              
            return ScheduleParticipantDto.builder()
                    .schedule_participant_no(participant.getScheduleParticipantNo())
                    .schedule_no(participant.getScheduleNo())
                    .member_no(participant.getMemberNo())
                    .schedule_participant_status(participant.getScheduleParticipantStatus())
                    .memberName(memberName)  
                    .positionName(positionName)   
                    .departmentName(departmentName)
                    .build();
        }).collect(Collectors.toList());
    }
    
    // 사원 개인 일정 수정
    public void updateEmployeeSchedule(Long eventId, ScheduleDto scheduleDto, ScheduleRepeatDto scheduleRepeatDto) {
        Schedule existingSchedule = scheduleRepository.findById(eventId)
            .orElseThrow(() -> new RuntimeException("일정을 찾을 수 없습니다."));
         
        existingSchedule.setScheduleTitle(scheduleDto.getSchedule_title());
        existingSchedule.setScheduleComment(scheduleDto.getSchedule_comment());
        existingSchedule.setScheduleStartDate(scheduleDto.getSchedule_start_date());
        existingSchedule.setScheduleEndDate(scheduleDto.getSchedule_end_date());
        existingSchedule.setScheduleAllday(scheduleDto.getSchedule_allday());
        existingSchedule.setScheduleStartTime(scheduleDto.getSchedule_start_time());
        existingSchedule.setScheduleEndTime(scheduleDto.getSchedule_end_time());
        existingSchedule.setScheduleRepeat(scheduleDto.getSchedule_repeat());
        existingSchedule.setScheduleCategoryNo(scheduleDto.getSchedule_category_no());
        existingSchedule.setScheduleType(scheduleDto.getSchedule_type());
        existingSchedule.setDepartmentNo(scheduleDto.getDepartment_no());
        
        scheduleRepository.save(existingSchedule);
 
        if (scheduleDto.getSchedule_repeat() != 0) { 
            ScheduleRepeat existingRepeat = scheduleRepeatRepository.getByScheduleNo(eventId);
            
            if (existingRepeat != null) { 
                existingRepeat.setScheduleRepeatType(scheduleRepeatDto.getSchedule_repeat_type());
                existingRepeat.setScheduleRepeatDay(determineRepeatDay(scheduleRepeatDto.getSchedule_repeat_type(), scheduleRepeatDto.getSchedule_repeat_day())); // 요일
                existingRepeat.setScheduleRepeatWeek(determineRepeatWeek(scheduleRepeatDto.getSchedule_repeat_type(), scheduleRepeatDto.getSchedule_repeat_week())); // 주차
                existingRepeat.setScheduleRepeatDate(determineRepeatDate(scheduleRepeatDto.getSchedule_repeat_type(), scheduleRepeatDto.getSchedule_repeat_date())); // 일자
                existingRepeat.setScheduleRepeatMonth(determineRepeatMonth(scheduleRepeatDto.getSchedule_repeat_type(), scheduleRepeatDto.getSchedule_repeat_month())); // 월
                existingRepeat.setScheduleRepeatEndDate(scheduleRepeatDto.getSchedule_repeat_end_date()); 
 
                scheduleRepeatRepository.save(existingRepeat);
            } else { 
                ScheduleRepeat repeat = ScheduleRepeat.builder()
                        .scheduleNo(eventId)
                        .scheduleRepeatType(scheduleRepeatDto.getSchedule_repeat_type())
                        .scheduleRepeatDay(determineRepeatDay(scheduleRepeatDto.getSchedule_repeat_type(), scheduleRepeatDto.getSchedule_repeat_day())) // 요일
                        .scheduleRepeatWeek(determineRepeatWeek(scheduleRepeatDto.getSchedule_repeat_type(), scheduleRepeatDto.getSchedule_repeat_week())) // 주차
                        .scheduleRepeatDate(determineRepeatDate(scheduleRepeatDto.getSchedule_repeat_type(), scheduleRepeatDto.getSchedule_repeat_date())) // 일자
                        .scheduleRepeatMonth(determineRepeatMonth(scheduleRepeatDto.getSchedule_repeat_type(), scheduleRepeatDto.getSchedule_repeat_month())) // 월
                        .scheduleRepeatEndDate(scheduleRepeatDto.getSchedule_repeat_end_date()) 
                        .build();
 
                scheduleRepeatRepository.save(repeat);
            }
        }
    }
    
    // 사원 참여자 수정
    @Transactional
    public void updateParticipants(ScheduleDto scheduleDto, String selectedMembers) {
    	
    	// 참여자 정보 
        if (selectedMembers != null && !selectedMembers.isEmpty()) { 
            List<ScheduleParticipantDto> existingParticipants = scheduleParticipantService.getParticipantsByscheduleNo(scheduleDto.getSchedule_no());
            List<String> newMemberList = new ArrayList<>(Arrays.asList(selectedMembers.split(","))); 
            
            Long ownerMemberNo = scheduleDto.getMember_no();
            if (ownerMemberNo != null && !newMemberList.contains(String.valueOf(ownerMemberNo))) {
                newMemberList.add(String.valueOf(ownerMemberNo));  
            } 
            
            for (ScheduleParticipantDto participant : existingParticipants) {
                if (!newMemberList.contains(String.valueOf(participant.getMember_no()))) {
                    participant.setSchedule_participant_status(1L);  
                    scheduleParticipantService.updateParticipantStatus(participant);
                }
            }
 
            for (String memberId : newMemberList) {
                Long memberIdLong = Long.parseLong(memberId.trim());
                boolean isExisting = existingParticipants.stream()
                    .anyMatch(participant -> participant.getMember_no().equals(memberIdLong));
 
                if (!isExisting) {
                	ScheduleParticipantDto newParticipant = ScheduleParticipantDto.builder()
                        .schedule_no(scheduleDto.getSchedule_no())
                        .member_no(memberIdLong)
                        .schedule_participant_status(0L) 
                        .build();
                	scheduleParticipantService.save(newParticipant);
                }
            }
        } else {
        	List<ScheduleExceptionParticipantDto> existingParticipants = scheduleParticipantService.getExceptionParticipantsByscheduleNo(scheduleDto.getSchedule_no());
        	List<String> newMemberList = new ArrayList<>(Arrays.asList(selectedMembers.split(","))); 
        	for (ScheduleExceptionParticipantDto participant : existingParticipants) {
                if (!newMemberList.contains(String.valueOf(participant.getMember_no()))) {
                    participant.setSchedule_exception_participant_status(1L);  
                    scheduleParticipantService.updateExceptionParticipantStatus(participant);
                }
            }
        }
    }
    
    // 예외 일정 참여자 
 	public List<ScheduleExceptionParticipantDto> getParticipantsByExceptionReservationNo(Long scheduleExceptionNo) { 
         List<ScheduleExceptionParticipant> participants = scheduleExceptionParticipantRepository.findExceptionParticipantsByScheduleNo(scheduleExceptionNo);
  
         return participants.stream().map(participant -> { 
             String memberName = memberRepository.findById(participant.getMemberNo())
                                                .map(Member::getMemberName)
                                                .orElse("사원");
             String positionName = "직위";
             String departmentName = "부서";
             
             Long memberNo = participant.getMemberNo();
             
             List<Object[]> memberInfo = memberRepository.findMemberWithDepartmentAndPosition(memberNo); 
             
             Object[] row = memberInfo.get(0);  
             positionName = (String) row[1];   
             departmentName = (String) row[2]; 
              
              
             return ScheduleExceptionParticipantDto.builder()
                     .schedule_exception_participant_no(participant.getScheduleExceptionParticipantNo())
                     .schedule_exception_no(participant.getScheduleExceptionNo())
                     .member_no(participant.getMemberNo())
                     .schedule_exception_participant_status(participant.getScheduleExceptionParticipantStatus())
                     .memberName(memberName)  
                     .positionName(positionName)   
                     .departmentName(departmentName)
                     .build();
         }).collect(Collectors.toList());
     }
 	
 	// 사원 - 이 일정 및 향후 일정 수정
    // 이 일정 및 향후 일정 수정
    public Long updateFutureEmployeeSchedule(Long eventId, ScheduleDto scheduleDto, ScheduleRepeatDto scheduleRepeatDto, String pickStartDate) {
        Schedule schedule = scheduleRepository.findById(eventId)
            .orElseThrow(() -> new EntityNotFoundException("일정을 찾을 수 없습니다."));
        
        ScheduleRepeat scheduleRepeat = scheduleRepeatRepository.getByScheduleNo(eventId);
        
        LocalDate adjustedEndDate = LocalDate.parse(pickStartDate).minusDays(1);
        String adjustedEndDateString = adjustedEndDate.format(DateTimeFormatter.ISO_LOCAL_DATE); 
        scheduleRepeat.setScheduleRepeatEndDate(adjustedEndDateString); 
        scheduleRepeatRepository.save(scheduleRepeat);
        
        return saveFutureEmployeeSchedule(scheduleDto, scheduleRepeatDto);   
    }
    
    public Long saveFutureEmployeeSchedule(ScheduleDto scheduleDto, ScheduleRepeatDto scheduleRepeatDto) { 
        Schedule schedule = Schedule.builder()
                .memberNo(scheduleDto.getMember_no())
                .scheduleTitle(scheduleDto.getSchedule_title())
                .scheduleComment(scheduleDto.getSchedule_comment())
                .scheduleStartDate(scheduleDto.getSchedule_start_date())
                .scheduleAllday(scheduleDto.getSchedule_allday())
                .scheduleEndDate(scheduleDto.getSchedule_end_date())
                .scheduleStartTime(scheduleDto.getSchedule_start_time())
                .scheduleEndTime(scheduleDto.getSchedule_end_time())
                .scheduleCategoryNo(scheduleDto.getSchedule_category_no())
                .scheduleRepeat(scheduleDto.getSchedule_repeat())
                .scheduleType(scheduleDto.getSchedule_type())
                .departmentNo(scheduleDto.getDepartment_no())
                .scheduleStatus(0L)
                .build();
 
        scheduleRepository.save(schedule);
 
        if (scheduleDto.getSchedule_repeat() != 0) {
            ScheduleRepeat repeat = ScheduleRepeat.builder()
                    .scheduleNo(schedule.getScheduleNo())  
                    .scheduleRepeatType(scheduleRepeatDto.getSchedule_repeat_type())
                    .scheduleRepeatDay(determineRepeatDay(scheduleRepeatDto.getSchedule_repeat_type(), scheduleRepeatDto.getSchedule_repeat_day())) // 요일
                    .scheduleRepeatWeek(determineRepeatWeek(scheduleRepeatDto.getSchedule_repeat_type(), scheduleRepeatDto.getSchedule_repeat_week())) // 주차
                    .scheduleRepeatDate(determineRepeatDate(scheduleRepeatDto.getSchedule_repeat_type(), scheduleRepeatDto.getSchedule_repeat_date())) // 일자
                    .scheduleRepeatMonth(determineRepeatMonth(scheduleRepeatDto.getSchedule_repeat_type(), scheduleRepeatDto.getSchedule_repeat_month())) // 월
                    .scheduleRepeatEndDate(scheduleRepeatDto.getSchedule_repeat_end_date()) 
                    .build();

            scheduleRepeatRepository.save(repeat);
        } 
        return schedule.getScheduleNo();
    }

    // 사원 - 이 일정만 수정 
    public Long updateEmployeeSingleEvent(Long eventId, ScheduleDto scheduleDto, ScheduleRepeatDto scheduleRepeatDto, String pickStartDate, String pickEndDate) {
        Schedule schedule = scheduleRepository.findById(eventId)
            .orElseThrow(() -> new EntityNotFoundException("일정을 찾을 수 없습니다."));

        // 예외 일정으로 저장 
        ScheduleException scheduleException = ScheduleException.builder()
                .scheduleNo(schedule.getScheduleNo())
                .memberNo(scheduleDto.getMember_no())
                .scheduleExceptionDate(pickStartDate)
                .scheduleExceptionTitle(scheduleDto.getSchedule_title())
                .scheduleExceptionComment(scheduleDto.getSchedule_comment())
                .scheduleExceptionStartDate(scheduleDto.getSchedule_start_date())
                .scheduleExceptionEndDate(scheduleDto.getSchedule_end_date())
                .scheduleExceptionStartTime(scheduleDto.getSchedule_start_time())
                .scheduleExceptionEndTime(scheduleDto.getSchedule_end_time())
                .scheduleExceptionAllday(scheduleDto.getSchedule_allday())
                .scheduleCategoryNo(scheduleDto.getSchedule_category_no()) 
                .scheduleExceptionType(scheduleDto.getSchedule_type()) 
                .departmentNo(scheduleDto.getDepartment_no()) 
                .scheduleExceptionStatus(0L)
                .build();
        scheduleExceptionRepository.save(scheduleException); 
        
        return scheduleException.getScheduleExceptionNo();
    }
    
    // 사원 - 예외 참여자 추가
    @Transactional
    public void saveExceptionParticipants(ScheduleDto scheduleDto, String selectedMembers) {
    	
    	// 참여자 정보 
        if (selectedMembers != null && !selectedMembers.isEmpty()) { 
            List<ScheduleExceptionParticipantDto> existingParticipants = scheduleParticipantService.getExceptionParticipantsByscheduleNo(scheduleDto.getSchedule_no());
            List<String> newMemberList = new ArrayList<>(Arrays.asList(selectedMembers.split(","))); 
            
            Long ownerMemberNo = scheduleDto.getMember_no();
            if (ownerMemberNo != null && !newMemberList.contains(String.valueOf(ownerMemberNo))) {
                newMemberList.add(String.valueOf(ownerMemberNo));  
            } 
            
            for (ScheduleExceptionParticipantDto participant : existingParticipants) {
                if (!newMemberList.contains(String.valueOf(participant.getMember_no()))) {
                    participant.setSchedule_exception_participant_status(1L);  
                    scheduleParticipantService.updateExceptionParticipantStatus(participant);
                }
            }
 
            for (String memberId : newMemberList) {
                Long memberIdLong = Long.parseLong(memberId.trim());
                boolean isExisting = existingParticipants.stream()
                    .anyMatch(participant -> participant.getMember_no().equals(memberIdLong));
 
                if (!isExisting) {
                	ScheduleExceptionParticipantDto newParticipant = ScheduleExceptionParticipantDto.builder()
                        .schedule_exception_no(scheduleDto.getSchedule_no())
                        .member_no(memberIdLong)
                        .schedule_exception_participant_status(0L) 
                        .build();
                	scheduleParticipantService.saveException(newParticipant);
                }
            }
        } else {
        	List<ScheduleExceptionParticipantDto> existingParticipants = scheduleParticipantService.getExceptionParticipantsByscheduleNo(scheduleDto.getSchedule_no());
        	List<String> newMemberList = new ArrayList<>(Arrays.asList(selectedMembers.split(","))); 
        	for (ScheduleExceptionParticipantDto participant : existingParticipants) {
                if (!newMemberList.contains(String.valueOf(participant.getMember_no()))) {
                    participant.setSchedule_exception_participant_status(1L);  
                    scheduleParticipantService.updateExceptionParticipantStatus(participant);
                }
            }
        }
    }
    
    // 예외 일정 수정
    public void updateExceptionEmployeeSchedule(Long eventId, ScheduleExceptionDto scheduleExceptionDto) {
        ScheduleException existingSchedule = scheduleExceptionRepository.findById(eventId)
            .orElseThrow(() -> new RuntimeException("일정을 찾을 수 없습니다."));
         
        existingSchedule.setScheduleExceptionTitle(scheduleExceptionDto.getSchedule_exception_title());
        existingSchedule.setScheduleExceptionComment(scheduleExceptionDto.getSchedule_exception_comment());
        existingSchedule.setScheduleExceptionStartDate(scheduleExceptionDto.getSchedule_exception_start_date());
        existingSchedule.setScheduleExceptionEndDate(scheduleExceptionDto.getSchedule_exception_end_date());
        existingSchedule.setScheduleExceptionAllday(scheduleExceptionDto.getSchedule_exception_allday());
        existingSchedule.setScheduleExceptionStartTime(scheduleExceptionDto.getSchedule_exception_start_time());
        existingSchedule.setScheduleExceptionEndTime(scheduleExceptionDto.getSchedule_exception_end_time()); 
        existingSchedule.setScheduleCategoryNo(scheduleExceptionDto.getSchedule_category_no());
        existingSchedule.setScheduleExceptionType(scheduleExceptionDto.getSchedule_exception_type());
        existingSchedule.setDepartmentNo(scheduleExceptionDto.getDepartment_no());
        
        scheduleExceptionRepository.save(existingSchedule);
 
    }
    
    // 사원 예외 참여자 수정
    @Transactional
    public void updateExceptionParticipants(Long eventId, ScheduleExceptionDto scheduleExceptionDto, String selectedMembers) {
    	
    	// 참여자 정보 
        if (selectedMembers != null && !selectedMembers.isEmpty()) { 
            List<ScheduleExceptionParticipantDto> existingParticipants = scheduleParticipantService.getExceptionParticipantsByscheduleNo(eventId);
            List<String> newMemberList = new ArrayList<>(Arrays.asList(selectedMembers.split(","))); 
            
            Long ownerMemberNo = scheduleExceptionDto.getMember_no();
            if (ownerMemberNo != null && !newMemberList.contains(String.valueOf(ownerMemberNo))) {
                newMemberList.add(String.valueOf(ownerMemberNo));  
            } 
            
            for (ScheduleExceptionParticipantDto participant : existingParticipants) {
                if (!newMemberList.contains(String.valueOf(participant.getMember_no()))) {
                    participant.setSchedule_exception_participant_status(1L);  
                    scheduleParticipantService.updateExceptionParticipantStatus(participant);
                }
            }
 
            for (String memberId : newMemberList) {
                Long memberIdLong = Long.parseLong(memberId.trim());
                boolean isExisting = existingParticipants.stream()
                    .anyMatch(participant -> participant.getMember_no().equals(memberIdLong));
 
                if (!isExisting) {
                	ScheduleExceptionParticipantDto newParticipant = ScheduleExceptionParticipantDto.builder()
                        .schedule_exception_no(scheduleExceptionDto.getSchedule_exception_no())
                        .member_no(memberIdLong)
                        .schedule_exception_participant_status(0L) 
                        .build();
                	scheduleParticipantService.saveException(newParticipant);
                }
            }
        } else {
        	List<ScheduleExceptionParticipantDto> existingParticipants = scheduleParticipantService.getExceptionParticipantsByscheduleNo(eventId);
        	List<String> newMemberList = new ArrayList<>(Arrays.asList(selectedMembers.split(","))); 
        	for (ScheduleExceptionParticipantDto participant : existingParticipants) {
                if (!newMemberList.contains(String.valueOf(participant.getMember_no()))) {
                    participant.setSchedule_exception_participant_status(1L);  
                    scheduleParticipantService.updateExceptionParticipantStatus(participant);
                }
            }
        }
    } 
    
    public List<ScheduleExceptionParticipantDto> getParticipantsByExceptionReservationNo(Long scheduleExceptionNo, Long memberNo) { 
        List<ScheduleExceptionParticipant> participants = scheduleExceptionParticipantRepository.findExceptionParticipantsByScheduleNo(scheduleExceptionNo, memberNo);
 
        return participants.stream().map(participant -> { 
            String memberName = memberRepository.findById(participant.getMemberNo())
                                               .map(Member::getMemberName)
                                               .orElse("사원");
            String positionName = "직위";
            String departmentName = "부서";
            
            Long member_No = participant.getMemberNo();
            
            List<Object[]> memberInfo = memberRepository.findMemberWithDepartmentAndPosition(member_No); 
            
            Object[] row = memberInfo.get(0);  
            positionName = (String) row[1];   
            departmentName = (String) row[2]; 
             
             
            return ScheduleExceptionParticipantDto.builder()
                    .schedule_exception_participant_no(participant.getScheduleExceptionParticipantNo())
                    .schedule_exception_no(participant.getScheduleExceptionNo())
                    .member_no(participant.getMemberNo())
                    .schedule_exception_participant_status(participant.getScheduleExceptionParticipantStatus())
                    .memberName(memberName)  
                    .positionName(positionName)   
                    .departmentName(departmentName)
                    .build();
        }).collect(Collectors.toList());
    }
    
    public List<ScheduleParticipantDto> getParticipantsByReservationNoOwn(Long scheduleNo, Long memberNo) { 
        List<ScheduleParticipant> participants = scheduleParticipantRepository.findParticipantsByScheduleNo(scheduleNo, memberNo);
 
        return participants.stream().map(participant -> { 
            String memberName = memberRepository.findById(participant.getMemberNo())
                                               .map(Member::getMemberName)
                                               .orElse("사원");
            String positionName = "직위";
            String departmentName = "부서";
            
            Long member_No = participant.getMemberNo();
            
            List<Object[]> memberInfo = memberRepository.findMemberWithDepartmentAndPosition(member_No); 
            
            Object[] row = memberInfo.get(0);  
            positionName = (String) row[1];   
            departmentName = (String) row[2]; 
             
             
            return ScheduleParticipantDto.builder()
                    .schedule_participant_no(participant.getScheduleParticipantNo())
                    .schedule_no(participant.getScheduleNo())
                    .member_no(participant.getMemberNo())
                    .schedule_participant_status(participant.getScheduleParticipantStatus())
                    .memberName(memberName)  
                    .positionName(positionName)   
                    .departmentName(departmentName)
                    .build();
        }).collect(Collectors.toList());
    }
}
