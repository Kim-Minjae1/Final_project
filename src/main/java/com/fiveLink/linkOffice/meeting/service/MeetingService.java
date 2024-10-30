package com.fiveLink.linkOffice.meeting.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fiveLink.linkOffice.meeting.domain.Meeting;
import com.fiveLink.linkOffice.meeting.domain.MeetingDto;
import com.fiveLink.linkOffice.meeting.repository.MeetingRepository; 

@Service
public class MeetingService {

	private final MeetingRepository meetingRepository;  
	
	@Autowired
	public MeetingService (MeetingRepository meetingRepository) {
		this.meetingRepository = meetingRepository;  
	}
	
	public org.springframework.data.domain.Page<MeetingDto> searchMeetingRooms(String searchText, Pageable pageable) {
	    return meetingRepository.findByMeetingNameContainingIgnoreCaseAndMeetingStatus(searchText, 0L, pageable)
	            .map(MeetingDto::toDto);
	} 
	
	public List<MeetingDto> getAllMeetings() {
	    List<Meeting> meetings = meetingRepository.findByMeetingStatusOrderByMeetingNameAsc(0L);
	    List<MeetingDto> meetingDtos = new ArrayList();

	    for (Meeting meeting : meetings) {
	        MeetingDto meetingDto = MeetingDto.toDto(meeting);
	        meetingDtos.add(meetingDto);
	    } 
	    return meetingDtos;
	} 
	
	public MeetingDto getMeetingById(Long meetingId) {
        return meetingRepository.findById(meetingId)
                .map(MeetingDto::toDto)
                .orElse(null);
    }

	 
	// 등록
	public boolean isMeetingNameExists(String meetingName) {
		 return meetingRepository.existsByMeetingNameAndMeetingStatus(meetingName, 0L);
	}
  
	@Transactional
	public Meeting saveMeeting(MeetingDto meetingDto) {
	    Meeting meeting = meetingDto.toEntity();
	    return meetingRepository.save(meeting);
	}
	
	// 수정 
	public boolean isMeetingNameExistsEdit(String meetingName, Long meetingId) {
		 return meetingRepository.existsByMeetingNameAndMeetingStatusAndMeetingNoNot(meetingName, 0L, meetingId);
	}
	 
	// 삭제
	public boolean deleteMeetings(List<Long> meetingNos) {
        try {
            for (Long meetingNo : meetingNos) {
                meetingRepository.findById(meetingNo).ifPresent(meeting -> {
                    meeting.setMeetingStatus(1L);  
                    meetingRepository.save(meeting); 
                });
            }
            return true; 
        } catch (Exception e) {
            e.printStackTrace();
            return false; 
        }
    }
	
	// 검색
	public List<MeetingDto> searchMeetingRoomsByName(String searchText) {
        List<Meeting> meetings = meetingRepository.findByMeetingNameContainingIgnoreCaseAndMeetingStatusOrderByMeetingNameAsc(searchText, 0L);
        return meetings.stream()
                       .map(MeetingDto::toDto)
                       .collect(Collectors.toList());
    }
	
	// 회의실 이름 
    public String getMeetingNameById(Long meetingNo) {
        return meetingRepository.findById(meetingNo)
                .map(Meeting::getMeetingName)
                .orElse("회의실");
    }
}
