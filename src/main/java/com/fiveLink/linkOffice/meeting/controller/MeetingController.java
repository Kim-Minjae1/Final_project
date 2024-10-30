package com.fiveLink.linkOffice.meeting.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.fiveLink.linkOffice.meeting.domain.Meeting;
import com.fiveLink.linkOffice.meeting.domain.MeetingDto;
import com.fiveLink.linkOffice.meeting.service.MeetingFileService;
import com.fiveLink.linkOffice.meeting.service.MeetingService;
import com.fiveLink.linkOffice.member.domain.MemberDto;
import com.fiveLink.linkOffice.member.service.MemberService; 

@Controller
public class MeetingController {
    
    private final MeetingService meetingService;
    private final MeetingFileService meetingFileService;
    private final MemberService memberService; 
    
    @Autowired
    public MeetingController(MeetingService meetingService, MeetingFileService meetingFileService, MemberService memberService) {
        this.meetingService = meetingService;
        this.meetingFileService = meetingFileService;
        this.memberService = memberService; 
    }
 
    @GetMapping("/meetingroomList")
    public String listMeetings(Model model,
            @RequestParam(value = "searchText", defaultValue = "") String searchText,
            @PageableDefault(page = 0, size = 10, sort = "meetingName", direction = Sort.Direction.ASC) Pageable pageable) { 

        Page<MeetingDto> meetings = meetingService.searchMeetingRooms(searchText, pageable);
        
        Long memberNo = memberService.getLoggedInMemberNo();  
        List<MemberDto> memberDto = memberService.getMembersByNo(memberNo);

        model.addAttribute("memberdto", memberDto);
        model.addAttribute("meetings", meetings);
        model.addAttribute("searchText", searchText);
         
        return "/admin/meeting/meetingroomList";
    } 
 
    // 회의실 등록
    @PostMapping("/meetingroomList/add")
    @ResponseBody
    public Map<String, Object> addMeeting(
        @RequestParam("meetingName") String meetingName,
        @RequestParam("meetingMax") Long meetingMax,
        @RequestParam("meetingAvailableStart") String meetingAvailableStart,
        @RequestParam("meetingAvailableEnd") String meetingAvailableEnd,
        @RequestParam("meetingComment") String meetingComment,
        @RequestParam("meetingImage") MultipartFile meetingImage) {

        Map<String, Object> resultMap = new HashMap<>();

        try { 
            if (meetingService.isMeetingNameExists(meetingName)) {
                resultMap.put("res_code", "400");
                resultMap.put("res_msg", "동일한 회의실명이 존재합니다.");
                return resultMap;
            }
 
            String newImageName = meetingFileService.upload(meetingImage);
            if (newImageName == null) {
                resultMap.put("res_code", "404");
                resultMap.put("res_msg", "이미지 업로드 중 오류가 발생했습니다.");
                return resultMap;
            }
 
            MeetingDto meetingDto = MeetingDto.builder()
                .meeting_name(meetingName)
                .meeting_max(meetingMax)
                .meeting_available_start(meetingAvailableStart)
                .meeting_available_end(meetingAvailableEnd)
                .meeting_ori_image(meetingImage.getOriginalFilename())
                .meeting_new_image(newImageName)
                .meeting_comment(meetingComment)
                .meeting_status(0L) 
                .build();
 
            Meeting meeting = meetingService.saveMeeting(meetingDto);
            if (meeting != null) {
                resultMap.put("res_code", "200");
                resultMap.put("res_msg", "회의실 정보가 등록되었습니다.");
            } else {
                resultMap.put("res_code", "500");
                resultMap.put("res_msg", "회의실 정보 등록 중 오류가 발생했습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace(); 
        } 
        return resultMap;
    } 
    
    // 상세
    @GetMapping("/meetingroomList/edit/{meetingId}")
    @ResponseBody
    public Map<String, Object> getMeetingById(@PathVariable("meetingId") Long meetingId) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            MeetingDto meetingDto = meetingService.getMeetingById(meetingId);
            if (meetingDto != null) {
                resultMap.put("res_code", "200");
                resultMap.put("meeting", meetingDto);
            } else {
                resultMap.put("res_code", "404");
                resultMap.put("res_msg", "회의실을 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            resultMap.put("res_code", "500");
            resultMap.put("res_msg", "서버 오류가 발생했습니다.");
            e.printStackTrace();
        } 
        return resultMap;
    } 

    // 회의실 수정
    @PostMapping("/meetingroomList/edit")
    @ResponseBody
    public Map<String, Object> editMeeting(
        @RequestParam("meetingId") Long meetingId,
        @RequestParam("meetingName") String meetingName,
        @RequestParam("meetingMax") Long meetingMax,
        @RequestParam("meetingAvailableStart") String meetingAvailableStart,
        @RequestParam("meetingAvailableEnd") String meetingAvailableEnd,
        @RequestParam("meetingComment") String meetingComment,
        @RequestParam(value = "meetingImage", required = false) MultipartFile meetingImage) {

        Map<String, Object> resultMap = new HashMap<>();

        try {
            MeetingDto existingMeeting = meetingService.getMeetingById(meetingId);
            if (existingMeeting == null) {
                resultMap.put("res_code", "404");
                resultMap.put("res_msg", "회의실을 찾을 수 없습니다.");
                return resultMap;
            }
            
            if (meetingService.isMeetingNameExistsEdit(meetingName, meetingId)) { 
                resultMap.put("res_code", "400");
                resultMap.put("res_msg", "동일한 회의실명이 존재합니다.");
                return resultMap;
            }
            
            String newImageName = existingMeeting.getMeeting_new_image();
            if (meetingImage != null && !meetingImage.isEmpty()) {
                newImageName = meetingFileService.upload(meetingImage);
                if (newImageName == null) {
                    resultMap.put("res_code", "404");
                    resultMap.put("res_msg", "이미지 업로드 중 오류가 발생했습니다.");
                    return resultMap;
                }
            }

            MeetingDto updatedMeetingDto = MeetingDto.builder()
                .meeting_no(meetingId)
                .meeting_name(meetingName)
                .meeting_max(meetingMax)
                .meeting_available_start(meetingAvailableStart)
                .meeting_available_end(meetingAvailableEnd)
                .meeting_ori_image(meetingImage != null ? meetingImage.getOriginalFilename() : existingMeeting.getMeeting_ori_image())
                .meeting_new_image(newImageName)
                .meeting_comment(meetingComment)
                .meeting_status(existingMeeting.getMeeting_status())
                .meeting_create_date(existingMeeting.getMeeting_create_date())
                .meeting_update_date(LocalDateTime.now())
                .build();

            Meeting updatedMeeting = meetingService.saveMeeting(updatedMeetingDto);
            if (updatedMeeting != null) {
                resultMap.put("res_code", "200");
                resultMap.put("res_msg", "회의실 정보가 수정되었습니다.");
            } else {
                resultMap.put("res_code", "500");
                resultMap.put("res_msg", "회의실 정보 수정 중 오류가 발생했습니다.");
            }
        } catch (Exception e) {
            resultMap.put("res_code", "500");
            resultMap.put("res_msg", "서버 오류가 발생했습니다.");
            e.printStackTrace();
        }
        return resultMap;
    }
    
    // 삭제
    @PostMapping("/meetingroomList/delete")
    @ResponseBody
    public Map<String, Object> deleteMeetings(@RequestBody List<Long> meetingIds) {
        Map<String, Object> resultMap = new HashMap<>();
 
        try {
            boolean deleted = meetingService.deleteMeetings(meetingIds);

            if (deleted) {
                resultMap.put("res_code", "200");
                resultMap.put("res_msg", "회의실이 삭제되었습니다.");
            } else {
                resultMap.put("res_code", "404");
                resultMap.put("res_msg", "회의실 삭제 중 오류가 발생했습니다.");
            }
        } catch (Exception e) {
            resultMap.put("res_code", "500");
            resultMap.put("res_msg", "서버 오류가 발생했습니다.");
            e.printStackTrace();
        }
        
        return resultMap;
    }
     
    
}
