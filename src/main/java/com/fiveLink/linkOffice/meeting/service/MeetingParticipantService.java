package com.fiveLink.linkOffice.meeting.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fiveLink.linkOffice.meeting.domain.MeetingParticipant;
import com.fiveLink.linkOffice.meeting.domain.MeetingParticipantDto;
import com.fiveLink.linkOffice.meeting.domain.MeetingReservation;
import com.fiveLink.linkOffice.meeting.domain.MeetingReservationDto;
import com.fiveLink.linkOffice.meeting.repository.MeetingParticipantRepository;
import com.fiveLink.linkOffice.meeting.repository.MeetingReservationRepository;
import com.fiveLink.linkOffice.member.domain.Member;
import com.fiveLink.linkOffice.member.repository.MemberRepository;

@Service
public class MeetingParticipantService {
    private final MeetingReservationRepository meetingReservationRepository;
    private final MeetingParticipantRepository meetingParticipantRepository;
    private final MemberRepository memberRepository;

    @Autowired
    public MeetingParticipantService(MeetingReservationRepository meetingReservationRepository,
                                     MeetingParticipantRepository meetingParticipantRepository, MemberRepository memberRepository) {
        this.meetingReservationRepository = meetingReservationRepository;
        this.meetingParticipantRepository = meetingParticipantRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public void saveReservationAndParticipants(MeetingReservationDto reservationDto, List<MeetingParticipantDto> participants) {
        MeetingReservation savedReservation = meetingReservationRepository.save(reservationDto.toEntity());

        // 참가자 정보 저장
        participants.forEach(participantDto -> {
            participantDto.setMeeting_reservation_no(savedReservation.getMeetingReservationNo());
            meetingParticipantRepository.save(participantDto.toEntity());  
        });
         
    }
    
    public List<MeetingParticipantDto> getParticipantsByReservationNo(Long reservationNo) { 
        List<MeetingParticipant> participants = meetingParticipantRepository.findParticipantsByReservationNo(reservationNo);
 
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
             
             
            return MeetingParticipantDto.builder()
                    .meeting_participant_no(participant.getMeetingParticipantNo())
                    .meeting_reservation_no(participant.getMeetingReservationNo())
                    .member_no(participant.getMemberNo())
                    .meeting_participant_status(participant.getMeetingParticipantStatus())
                    .memberName(memberName)  
                    .positionName(positionName)   
                    .departmentName(departmentName)
                    .build();
        }).collect(Collectors.toList());
    }
    
    public List<MeetingParticipantDto> findParticipantsByReservationNoAndNotMemberNo(Long reservationNo, Long memberNo) { 
        List<MeetingParticipant> participants = meetingParticipantRepository.findParticipantsByReservationNoExcludingMemberNo(reservationNo, memberNo);
 
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
             
             
            return MeetingParticipantDto.builder()
                    .meeting_participant_no(participant.getMeetingParticipantNo())
                    .meeting_reservation_no(participant.getMeetingReservationNo())
                    .member_no(participant.getMemberNo())
                    .meeting_participant_status(participant.getMeetingParticipantStatus())
                    .memberName(memberName)  
                    .positionName(positionName)   
                    .departmentName(departmentName)
                    .build();
        }).collect(Collectors.toList());
    }
    
    // 예약 수정
    public void save(MeetingParticipantDto participant) {
        meetingParticipantRepository.save(participant.toEntity());
    }

    @Transactional
    public void updateParticipantStatus(MeetingParticipantDto participant) {
        MeetingParticipant entity = meetingParticipantRepository.findById(participant.getMeeting_participant_no())
                .orElseThrow(() -> new IllegalArgumentException("참여자를 찾을 수 없습니다." + participant.getMeeting_participant_no()));
        entity.setMeetingParticipantStatus(participant.getMeeting_participant_status());
        meetingParticipantRepository.save(entity);
    }
    
    public List<MeetingParticipantDto> findParticipantsByReservationNo(Long reservationNo) {
        List<MeetingParticipant> participants = meetingParticipantRepository.findParticipantsByReservationNo(reservationNo);
        return participants.stream().map(MeetingParticipantDto::toDto).collect(Collectors.toList());
    }
}
