package com.fiveLink.linkOffice.nofication.controller;

import com.fiveLink.linkOffice.nofication.domain.NoficationDto;
import com.fiveLink.linkOffice.nofication.service.NoficationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLOutput;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class NoficationController {
    private final NoficationService noficationService;

    @Autowired
    public NoficationController(NoficationService noficationService){
        this.noficationService = noficationService;
    }

    //현재 사용자의 안읽음 개수 가져오기
    @GetMapping("/api/nofication/unread/{headerCurrentMember}")
    @ResponseBody
    public Map<String, Object> bellUnreadCount(@PathVariable("headerCurrentMember") Long headerCurrentMember) {
        int unreadCount =  noficationService.bellCount(headerCurrentMember);

        Map<String, Object> response = new HashMap<>();

        response.put("unreadCount", unreadCount);
        return response;

    }

    //현재 사용자의 안읽음 개수 가져오기
    @GetMapping("/api/nofication/unread/list/{headerCurrentMember}")
    @ResponseBody
    public Map<String, Object> selectUnreadList(@PathVariable("headerCurrentMember") Long headerCurrentMember) {
        List<NoficationDto> unreadList =  noficationService.selectUnreadList(headerCurrentMember);

        Map<String, Object> response = new HashMap<>();
        response.put("unreadList", unreadList);

        return response;

    }

    //읽음 처리
    @PostMapping("/api/notification/read")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> readNotification(@RequestBody Map<String, Object> requestBody) {
        Long memberNo = Long.parseLong(requestBody.get("memberNo").toString());
        List<Long> notificationNos = ((List<?>) requestBody.get("notificationNos")).stream()
                .map(member -> Long.parseLong(member.toString()))
                .toList();

        boolean read = noficationService.readNotification(memberNo, notificationNos);

        Map<String, Object> response = new HashMap<>();
        response.put("read", read);

        return ResponseEntity.ok(response);
    }

    //타입별 읽음 처리
    @GetMapping("/api/nofication/type/read/{currentMember}/{functionType}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> readTypeNotification(@PathVariable("currentMember") Long currentMember, @PathVariable("functionType") int functionType) {


        boolean read = noficationService.readTypeNotification(currentMember, functionType);

        Map<String, Object> response = new HashMap<>();
        response.put("read", read);

        return ResponseEntity.ok(response);
    }

    //휴가 결재 테스트
    @GetMapping("/api/nofication/type/Approval/read/{currentMember}/{functionType}/{noficationTypePk}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> readTypePkNotification(@PathVariable("currentMember") Long currentMember, @PathVariable("functionType") int functionType, @PathVariable("noficationTypePk") Long noficationTypePk) {


        boolean read = noficationService.readTypePkNotification(currentMember, functionType, noficationTypePk);

        Map<String, Object> response = new HashMap<>();
        response.put("read", read);

        return ResponseEntity.ok(response);
    }
}
