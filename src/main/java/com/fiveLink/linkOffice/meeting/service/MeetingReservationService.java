package com.fiveLink.linkOffice.meeting.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fiveLink.linkOffice.meeting.domain.Meeting;
import com.fiveLink.linkOffice.meeting.domain.MeetingParticipant;
import com.fiveLink.linkOffice.meeting.domain.MeetingParticipantDto;
import com.fiveLink.linkOffice.meeting.domain.MeetingReservation;
import com.fiveLink.linkOffice.meeting.domain.MeetingReservationDto;
import com.fiveLink.linkOffice.meeting.repository.MeetingParticipantRepository;
import com.fiveLink.linkOffice.meeting.repository.MeetingRepository;
import com.fiveLink.linkOffice.meeting.repository.MeetingReservationRepository;
import com.fiveLink.linkOffice.member.domain.Member;
import com.fiveLink.linkOffice.member.repository.MemberRepository;
import com.fiveLink.linkOffice.member.service.MemberService;
import com.fiveLink.linkOffice.schedule.domain.ScheduleParticipant;
import com.fiveLink.linkOffice.schedule.domain.ScheduleParticipantDto;
import com.fiveLink.linkOffice.vacationapproval.domain.VacationApprovalDto;

@Service
public class MeetingReservationService {

    private final MeetingReservationRepository meetingReservationRepository;
    private final MeetingService meetingService; 
    private final MemberService memberService; 
    private final MeetingParticipantRepository meetingParticipantRepository;
    private final MemberRepository memberRepository;
    private final MeetingParticipantService meetingParticipantService;
    private final MeetingRepository meetingRepository;
    
    @Autowired
    public MeetingReservationService(MeetingReservationRepository meetingReservationRepository, MeetingService meetingService, MemberService memberService, 
    		                         MeetingParticipantRepository meetingParticipantRepository, MemberRepository memberRepository, MeetingParticipantService meetingParticipantService, MeetingRepository meetingRepository) {
        this.meetingReservationRepository = meetingReservationRepository;
        this.meetingService = meetingService;
        this.memberService = memberService;
        this.meetingParticipantRepository = meetingParticipantRepository;
        this.memberRepository = memberRepository;
        this.meetingParticipantService = meetingParticipantService;
        this.meetingRepository = meetingRepository; 
    }

    // 해당 날짜 예약 정보 
    public List<MeetingReservationDto> getReservationsByDate(String date) {
        List<Object[]> reservations = meetingReservationRepository.findReservations(date, 0L);
        List<MeetingReservationDto> dtoList = new ArrayList<>();
        
        for (Object[] row : reservations) {
            MeetingReservation reservation = (MeetingReservation) row[0];  
            Member member = (Member) row[1];   
            String positionName = (String) row[2];  
            String departmentName = (String) row[3]; 
            String meetingName = meetingService.getMeetingNameById(reservation.getMeetingNo());
            
            MeetingReservationDto dto = MeetingReservationDto.builder()
                    .meeting_reservation_no(reservation.getMeetingReservationNo())
                    .meeting_no(reservation.getMeetingNo())
                    .member_no(reservation.getMemberNo())
                    .meeting_reservation_date(reservation.getMeetingReservationDate())
                    .meeting_reservation_start_time(reservation.getMeetingReservationStartTime())
                    .meeting_reservation_end_time(reservation.getMeetingReservationEndTime())
                    .meeting_reservation_purpose(reservation.getMeetingReservationPurpose())
                    .meeting_reservation_create_date(reservation.getMeetingReservationCreateDate())
                    .meeting_reservation_update_date(reservation.getMeetingReservationUpdateDate())
                    .meeting_reservation_status(reservation.getMeetingReservationStatus())
                    .meeting_name(meetingName)  
                    .member_name(member.getMemberName()) 
                    .position_name(positionName) 
                    .department_name(departmentName) 
                    .build();
            
            dtoList.add(dto);
        } 
        return dtoList;
    } 
    
    // 본인 예약 정보  
    public Page<MeetingReservationDto> searchReservations(Long memberNo, String meetingNo, String searchText, String startDate, String endDate, String sortBy, Pageable pageable) { 

        Page<MeetingReservation> reservationPage = meetingReservationRepository.searchReservations(
									                memberNo, 
									                meetingNo.isEmpty() ? null : Long.parseLong(meetingNo), 
									                searchText.isEmpty() ? null : searchText, 
									                startDate.isEmpty() ? null : startDate, 
									                endDate.isEmpty() ? null : endDate, 
									                sortBy, 
									                pageable);
 
        return reservationPage.map(reservation -> {
            String meetingName = meetingService.getMeetingNameById(reservation.getMeetingNo());
            String memberName = memberService.getMemberNameById(reservation.getMemberNo());
            long participantCount = meetingParticipantRepository.countByMeetingReservationNoAndStatus(reservation.getMeetingReservationNo(), 0L);

            return MeetingReservationDto.builder()
                    .meeting_reservation_no(reservation.getMeetingReservationNo())
                    .meeting_no(reservation.getMeetingNo())
                    .member_no(reservation.getMemberNo())
                    .meeting_reservation_date(reservation.getMeetingReservationDate())
                    .meeting_reservation_start_time(reservation.getMeetingReservationStartTime())
                    .meeting_reservation_end_time(reservation.getMeetingReservationEndTime())
                    .meeting_reservation_purpose(reservation.getMeetingReservationPurpose())
                    .meeting_reservation_create_date(reservation.getMeetingReservationCreateDate())
                    .meeting_reservation_update_date(reservation.getMeetingReservationUpdateDate())
                    .meeting_reservation_status(reservation.getMeetingReservationStatus())
                    .meeting_name(meetingName)
                    .member_name(memberName)
                    .participant_count(participantCount)
                    .build();
        });
    } 
    
    // 상세 보기
    public MeetingReservationDto getReservationById(Long meetingreservationId) {
 
        Optional<MeetingReservation> optionalReservation = meetingReservationRepository.findById(meetingreservationId);
  
        MeetingReservation reservation = optionalReservation.get();
 
        String meetingName = meetingService.getMeetingNameById(reservation.getMeetingNo());
        String memberName = memberService.getMemberNameById(reservation.getMemberNo());
        long participantCount = meetingParticipantRepository.countByMeetingReservationNoAndStatus(reservation.getMeetingReservationNo(), 0L);

        List<Object[]> memberInfo = memberRepository.findMemberWithDepartmentAndPosition(reservation.getMemberNo()); 
        String positionName = "직위";
        String departmentName = "부서";
        
        Object[] row = memberInfo.get(0);   
        positionName = (String) row[1];
        departmentName = (String) row[2];
         
        return MeetingReservationDto.builder()
                .meeting_reservation_no(reservation.getMeetingReservationNo())
                .meeting_no(reservation.getMeetingNo())
                .member_no(reservation.getMemberNo())
                .meeting_reservation_date(reservation.getMeetingReservationDate())
                .meeting_reservation_start_time(reservation.getMeetingReservationStartTime())
                .meeting_reservation_end_time(reservation.getMeetingReservationEndTime())
                .meeting_reservation_purpose(reservation.getMeetingReservationPurpose())
                .meeting_reservation_create_date(reservation.getMeetingReservationCreateDate())
                .meeting_reservation_update_date(reservation.getMeetingReservationUpdateDate())
                .meeting_reservation_status(reservation.getMeetingReservationStatus())
                .meeting_name(meetingName)
                .member_name(memberName)
                .position_name(positionName)
                .department_name(departmentName)
                .participant_count(participantCount)
                .build();
    }
    
    // 수정
    @Transactional
    public void updateReservation(MeetingReservationDto meetingReservationDto, String selectedMembers) {
 
        MeetingReservation existingReservation = meetingReservationRepository.findById(meetingReservationDto.getMeeting_reservation_no())
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다." + meetingReservationDto.getMeeting_reservation_no()));
 
        existingReservation.setMeetingNo(meetingReservationDto.getMeeting_no());
        existingReservation.setMemberNo(meetingReservationDto.getMember_no());
        existingReservation.setMeetingReservationDate(meetingReservationDto.getMeeting_reservation_date());
        existingReservation.setMeetingReservationStartTime(meetingReservationDto.getMeeting_reservation_start_time());
        existingReservation.setMeetingReservationEndTime(meetingReservationDto.getMeeting_reservation_end_time());
        existingReservation.setMeetingReservationPurpose(meetingReservationDto.getMeeting_reservation_purpose());
        existingReservation.setMeetingReservationStatus(meetingReservationDto.getMeeting_reservation_status());
 
        meetingReservationRepository.save(existingReservation);

        // 참여자 정보 
        if (selectedMembers != null && !selectedMembers.isEmpty()) { 
            List<MeetingParticipantDto> existingParticipants = meetingParticipantService.getParticipantsByReservationNo(meetingReservationDto.getMeeting_reservation_no());
            List<String> newMemberList = new ArrayList<>(Arrays.asList(selectedMembers.split(","))); 
 
            for (MeetingParticipantDto participant : existingParticipants) {
                if (!newMemberList.contains(String.valueOf(participant.getMember_no()))) {
                    participant.setMeeting_participant_status(1L);  
                    meetingParticipantService.updateParticipantStatus(participant);
                }
            }
 
            for (String memberId : newMemberList) {
                Long memberIdLong = Long.parseLong(memberId.trim());
                boolean isExisting = existingParticipants.stream()
                    .anyMatch(participant -> participant.getMember_no().equals(memberIdLong));
 
                if (!isExisting) {
                    MeetingParticipantDto newParticipant = MeetingParticipantDto.builder()
                        .meeting_reservation_no(meetingReservationDto.getMeeting_reservation_no())
                        .member_no(memberIdLong)
                        .meeting_participant_status(0L) 
                        .build();
                    meetingParticipantService.save(newParticipant);
                }
            }
        }
    }
    
    // 예약 취소
    public boolean cancelReservation(Long reservationNo) {
        try {
            MeetingReservation reservation = meetingReservationRepository.findById(reservationNo)
                    .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다: " + reservationNo));
            
            reservation.setMeetingReservationStatus(1L); 
            meetingReservationRepository.save(reservation);
            return true;  
        } catch (Exception e) { 
            e.printStackTrace();
            return false;
        }
    }

    // 관리자 전체 예약 목록
    public Page<MeetingReservationDto> allReservations(String meetingNo, String searchText, String startDate, String endDate, String sortBy, Pageable pageable) { 

        Page<MeetingReservation> reservationPage = meetingReservationRepository.allReservations(
									                meetingNo.isEmpty() ? null : Long.parseLong(meetingNo), 
									                searchText.isEmpty() ? null : searchText, 
									                startDate.isEmpty() ? null : startDate, 
									                endDate.isEmpty() ? null : endDate, 
									                sortBy, 
									                pageable);
 
        return reservationPage.map(reservation -> {
            String meetingName = meetingService.getMeetingNameById(reservation.getMeetingNo());
            String memberName = memberService.getMemberNameById(reservation.getMemberNo());
            long participantCount = meetingParticipantRepository.countByMeetingReservationNoAndStatus(reservation.getMeetingReservationNo(), 0L);
            
            List<Object[]> memberInfo = memberRepository.findMemberWithDepartmentAndPosition(reservation.getMemberNo()); 
            String positionName = "직위";
            String departmentName = "부서";
            
            Object[] row = memberInfo.get(0);   
            positionName = (String) row[1];
            departmentName = (String) row[2];

            return MeetingReservationDto.builder()
                    .meeting_reservation_no(reservation.getMeetingReservationNo())
                    .meeting_no(reservation.getMeetingNo())
                    .member_no(reservation.getMemberNo())
                    .meeting_reservation_date(reservation.getMeetingReservationDate())
                    .meeting_reservation_start_time(reservation.getMeetingReservationStartTime())
                    .meeting_reservation_end_time(reservation.getMeetingReservationEndTime())
                    .meeting_reservation_purpose(reservation.getMeetingReservationPurpose())
                    .meeting_reservation_create_date(reservation.getMeetingReservationCreateDate())
                    .meeting_reservation_update_date(reservation.getMeetingReservationUpdateDate())
                    .meeting_reservation_status(reservation.getMeetingReservationStatus())
                    .meeting_name(meetingName)
                    .member_name(memberName)
                    .position_name(positionName)
                    .department_name(departmentName)
                    .participant_count(participantCount)
                    .build();
        });
    } 
    
    public List<MeetingReservationDto> getMeetingSchedules() {
        List<MeetingReservation> meetingReservations = meetingReservationRepository.findMeetingReservationsByStatusZero();

        return meetingReservations.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private MeetingReservationDto convertToDto(MeetingReservation meetingReservation) {
        MeetingReservationDto dto = new MeetingReservationDto();
         
        dto.setMeeting_reservation_no(meetingReservation.getMeetingReservationNo());
        dto.setMeeting_no(meetingReservation.getMeetingNo());
        dto.setMember_no(meetingReservation.getMemberNo());
        dto.setMeeting_reservation_date(meetingReservation.getMeetingReservationDate());
        dto.setMeeting_reservation_start_time(meetingReservation.getMeetingReservationStartTime());
        dto.setMeeting_reservation_end_time(meetingReservation.getMeetingReservationEndTime());
        dto.setMeeting_reservation_purpose(meetingReservation.getMeetingReservationPurpose());
        dto.setMeeting_reservation_create_date(meetingReservation.getMeetingReservationCreateDate());
        dto.setMeeting_reservation_update_date(meetingReservation.getMeetingReservationUpdateDate());
        dto.setMeeting_reservation_status(meetingReservation.getMeetingReservationStatus());
 
        Long memberNo = meetingReservation.getMemberNo();
        String memberName = memberRepository.findById(memberNo)
            .map(Member::getMemberName)
            .orElse("사원");  
        dto.setMember_name(memberName);
 
        List<Object[]> memberInfo = memberRepository.findMemberWithDepartmentAndPosition(memberNo); 
        if (!memberInfo.isEmpty()) {
            Object[] memberPositionDepartment = memberInfo.get(0);
            dto.setPosition_name((String) memberPositionDepartment[1]);
            dto.setDepartment_name((String) memberPositionDepartment[2]);
        } else {
            dto.setPosition_name("직위");  
            dto.setDepartment_name("부서");  
        }
        
        Long meetingNo = meetingReservation.getMeetingNo();
        String meetingName = meetingRepository.findById(meetingNo)
                .map(Meeting::getMeetingName)
                .orElse("회의실");  
        dto.setMeeting_name(meetingName);

        return dto;
    } 
    
    public List<MeetingParticipantDto> getParticipantsByMeetingNoOwn(Long meetingReservationNo, Long memberNo) {
        List<MeetingParticipant> participants = meetingParticipantRepository.findParticipantsByMeetingReservationNoExcludingMember(meetingReservationNo, memberNo);

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
    
    public List<MeetingReservationDto> getAllReservations() { 
        List<MeetingReservation> reservations = meetingReservationRepository.findAllByMeetingReservationStatusOrderByMeetingReservationStartTimeAsc(0L);
 
        return reservations.stream().map(reservation -> {
        	String memberName = memberRepository.findById(reservation.getMemberNo())
                    .map(Member::getMemberName)
                    .orElse("사원");
        	String positionName = "직위";
            String departmentName = "부서"; 

            List<Object[]> memberInfo = memberRepository.findMemberWithDepartmentAndPosition(reservation.getMemberNo());

            Object[] row = memberInfo.get(0);
            positionName = (String) row[1];
            departmentName = (String) row[2];
            String meetingName = meetingService.getMeetingNameById(reservation.getMeetingNo());
            long participantCount = meetingParticipantRepository.countByMeetingReservationNoAndStatus(reservation.getMeetingReservationNo(), 0L);
            
            return MeetingReservationDto.builder()
                .meeting_reservation_no(reservation.getMeetingReservationNo())
                .meeting_no(reservation.getMeetingNo())
                .meeting_name(meetingName)
                .meeting_reservation_date(reservation.getMeetingReservationDate())
                .meeting_reservation_purpose(reservation.getMeetingReservationPurpose())
                .participant_count(participantCount)
                .member_no(reservation.getMemberNo())
                .member_name(memberName)
                .meeting_reservation_start_time(reservation.getMeetingReservationStartTime())
                .meeting_reservation_end_time(reservation.getMeetingReservationEndTime()) 
                .department_name(departmentName)
                .position_name(positionName)
                .build();
        }).collect(Collectors.toList());
    }

}