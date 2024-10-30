package com.fiveLink.linkOffice.schedule.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fiveLink.linkOffice.member.domain.Member;
import com.fiveLink.linkOffice.member.repository.MemberRepository;
import com.fiveLink.linkOffice.schedule.domain.ScheduleExceptionParticipant;
import com.fiveLink.linkOffice.schedule.domain.ScheduleExceptionParticipantDto;
import com.fiveLink.linkOffice.schedule.domain.ScheduleParticipant;
import com.fiveLink.linkOffice.schedule.domain.ScheduleParticipantDto;
import com.fiveLink.linkOffice.schedule.repository.ScheduleExceptionParticipantRepository;
import com.fiveLink.linkOffice.schedule.repository.ScheduleParticipantRepository;

@Service
public class ScheduleParticipantService {
	
    private final ScheduleParticipantRepository scheduleParticipantRepository;
    private final MemberRepository memberRepository;
    private final ScheduleExceptionParticipantRepository scheduleExceptionParticipantRepository;
    
    @Autowired
    public ScheduleParticipantService(ScheduleParticipantRepository scheduleParticipantRepository, MemberRepository memberRepository, ScheduleExceptionParticipantRepository scheduleExceptionParticipantRepository) {
        this.scheduleParticipantRepository = scheduleParticipantRepository;
        this.memberRepository = memberRepository;
        this.scheduleExceptionParticipantRepository = scheduleExceptionParticipantRepository;
    }
    
    // 일반 - 참여자 수정
    public List<ScheduleParticipantDto> getParticipantsByscheduleNo(Long scheduleNo) { 
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
    
    // 일반 - 참여자 수정
    @Transactional
    public void updateParticipantStatus(ScheduleParticipantDto participant) {
    	ScheduleParticipant entity = scheduleParticipantRepository.findById(participant.getSchedule_participant_no())
                .orElseThrow(() -> new IllegalArgumentException("참여자를 찾을 수 없습니다." + participant.getSchedule_participant_no()));
        entity.setScheduleParticipantStatus(participant.getSchedule_participant_status());
        scheduleParticipantRepository.save(entity);
    }
    
    // 일반 - 참여자 수정
    public void save(ScheduleParticipantDto participant) {
    	scheduleParticipantRepository.save(participant.toEntity());
    } 
    
    
    // 일반 - 예외 참여자 저장 
    public List<ScheduleExceptionParticipantDto> getExceptionParticipantsByscheduleNo(Long scheduleNo) { 
        List<ScheduleExceptionParticipant> participants = scheduleExceptionParticipantRepository.findExceptionParticipantsByScheduleNo(scheduleNo);
 
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
    
    @Transactional
    public void updateExceptionParticipantStatus(ScheduleExceptionParticipantDto participant) {
    	ScheduleExceptionParticipant entity = scheduleExceptionParticipantRepository.findById(participant.getSchedule_exception_participant_no())
                .orElseThrow(() -> new IllegalArgumentException("참여자를 찾을 수 없습니다." + participant.getSchedule_exception_participant_no()));
        entity.setScheduleExceptionParticipantStatus(participant.getSchedule_exception_participant_status());
        scheduleExceptionParticipantRepository.save(entity);
    } 
    
    public void saveException(ScheduleExceptionParticipantDto participant) {
    	scheduleExceptionParticipantRepository.save(participant.toEntity());
    } 
}
