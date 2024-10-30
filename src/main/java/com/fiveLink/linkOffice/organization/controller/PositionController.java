package com.fiveLink.linkOffice.organization.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fiveLink.linkOffice.member.domain.MemberDto;
import com.fiveLink.linkOffice.member.service.MemberService;
import com.fiveLink.linkOffice.organization.domain.DepartmentDto;
import com.fiveLink.linkOffice.organization.domain.Position;
import com.fiveLink.linkOffice.organization.domain.PositionDto;
import com.fiveLink.linkOffice.organization.service.PositionService;

@Controller
public class PositionController {
    private final PositionService positionService;
    private final MemberService memberService;

    @Autowired
    public PositionController(PositionService positionService, MemberService memberService) {
        this.positionService = positionService;
        this.memberService = memberService;
    }

    @GetMapping("/position")
    public String listPositions(Model model, @RequestParam(value = "id", required = false) Long id) {
        List<PositionDto> positions = positionService.getAllPositionsForSelect();
        List<PositionDto> topLevelPositions = positionService.getAllPositionsForSelect();
        Long memberNo = memberService.getLoggedInMemberNo();
        List<MemberDto> memberDto = memberService.getMembersByNo(memberNo);
        List<PositionDto> activePositions = positionService.getActivePositions(id);
        
        model.addAttribute("memberdto", memberDto);
        model.addAttribute("positions", positions);
        model.addAttribute("topLevelPositions", topLevelPositions);
        model.addAttribute("topLevelPositionsUpdate", activePositions);

        if (id != null) {
            positionService.getPositionById(id).ifPresent(position -> {
                model.addAttribute("position", position);
            });
        }
        return "/admin/organization/position_list";
    } 

    @PostMapping("/position/add")
    @ResponseBody
    public Map<String, String> addPosition(@RequestBody Map<String, Object> payload) {
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("res_code", "404");
        resultMap.put("res_msg", "직위 추가 중 오류가 발생했습니다.");

        try {
            String positionName = (String) payload.get("positionName");
            Long positionHigh = null;
 
            if (payload.get("positionHigh") != null) {
                positionHigh = Long.valueOf(payload.get("positionHigh").toString());
            }

            if (positionService.isPositionNameDuplicate(positionName)) {
                resultMap.put("res_msg", "중복된 직위명이 존재합니다.");
                return resultMap;
            }

            positionService.addPosition(positionName, positionHigh);
            resultMap.put("res_code", "200");
            resultMap.put("res_msg", "직위 정보가 추가되었습니다.");
        } catch (NumberFormatException e) {
            resultMap.put("res_msg", "상위 직위를 찾을 수 없습니다. " + e.getMessage());
        } catch (Exception e) {
            resultMap.put("res_msg", e.getMessage());
        }
        return resultMap;
    }
    
    @GetMapping("/position/get")
    @ResponseBody
    public Map<String, Object> getPosition(@RequestParam("id") Long id) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Optional<PositionDto> positionDtoOptional = positionService.getPositionById(id);
            if (positionDtoOptional.isPresent()) {
                PositionDto positionDto = positionDtoOptional.get();
                Long highestActiveParent = positionService.findHighestActiveParent(positionDto.getPosition_high());
                positionDto.setPosition_high(highestActiveParent);
                
                resultMap.put("res_code", "200");
                resultMap.put("position", positionDto);
            } else {
                resultMap.put("res_code", "404");
                resultMap.put("res_msg", "직위를 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            resultMap.put("res_code", "500");
            resultMap.put("res_msg", "서버 오류: " + e.getMessage());
        }
        return resultMap;
    }
 
    @GetMapping("/position/member-count")
    @ResponseBody
    public long getMemberCountByPositionNo(@RequestParam Long positionNo) {
        return positionService.getMemberCountByPositionNo(positionNo);
    }
    
    @PostMapping("/position/delete")
    @ResponseBody
    public Map<String, String> deletePosition(@RequestParam("id") Long positionId) {
        Map<String, String> resultMap = new HashMap<>();
        boolean success = positionService.deletePosition(positionId);
        
        if (success) {
            resultMap.put("res_code", "200");
            resultMap.put("res_msg", "직위 정보가 삭제되었습니다.");
        } else {
            resultMap.put("res_code", "404");
            resultMap.put("res_msg", "직위에 소속 사원이 존재하여 삭제가 불가능합니다.");
        }
        return resultMap;
    } 
    
    // 수정
    @PostMapping("/position/update")
    @ResponseBody
    public Map<String, String> updatePosition(@RequestBody Map<String, Object> payload) {
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("res_code", "404");
        resultMap.put("res_msg", "직위 수정 중 오류가 발생했습니다.");
         
        
        try {
            Long positionNo = Long.valueOf(payload.get("positionId").toString());  
            String positionName = (String) payload.get("positionName");
            Long positionHigh = null;
            
            if (positionService.isPositionNameDuplicate(positionName, positionNo)) {
                resultMap.put("res_msg", "중복된 직위명이 존재합니다.");
                return resultMap;
            }
            
            if (payload.get("positionHigh") != null) {
                positionHigh = Long.valueOf(payload.get("positionHigh").toString());
            }
 
            PositionDto positionDto = PositionDto.builder()
                    .position_no(positionNo)
                    .position_name(positionName)
                    .position_high(positionHigh)
                    .build();
            
            System.out.println(positionDto);
            boolean success = positionService.updatePosition(positionDto);

            if (success) {
                resultMap.put("res_code", "200");
                resultMap.put("res_msg", "직위 정보가 수정되었습니다.");
            } else {
                resultMap.put("res_msg", "직위 정보를 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            resultMap.put("res_msg", "오류: " + e.getMessage());
        }

        return resultMap;
    } 
}
