package com.fiveLink.linkOffice.organization.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.fiveLink.linkOffice.member.domain.MemberDto;
import com.fiveLink.linkOffice.member.service.MemberService;
import com.fiveLink.linkOffice.organization.domain.DepartmentDto;
import com.fiveLink.linkOffice.organization.service.DepartmentService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class DepartmentController {

    private final DepartmentService departmentService;
    private final MemberService memberService;

    @Autowired
    public DepartmentController(DepartmentService departmentService, MemberService memberService) {
        this.departmentService = departmentService;
        this.memberService = memberService;
    }

    @GetMapping("/department")
    public String listDepartments(Model model, @RequestParam(value = "id", required = false) Long id) {
        List<DepartmentDto> departments = departmentService.getAllDepartments();
        Long memberNo = memberService.getLoggedInMemberNo();
        List<MemberDto> memberDto = memberService.getMembersByNo(memberNo); 
        
        model.addAttribute("memberdto", memberDto);
        model.addAttribute("departments", departments);
        model.addAttribute("topLevelDepartments", departmentService.getTopLevelDepartments()); 

        if (id != null) {
            departmentService.getDepartmentById(id).ifPresent(department -> {
                List<MemberDto> members = memberService.getMembersByDepartmentNo(department.getDepartment_no());
                department.setMembers(members);
                model.addAttribute("department", department);  
            });
        } 
        return "/admin/organization/department_list";
    } 

    @PostMapping("/department/add")
    @ResponseBody
    public Map<String, String> addDepartment(@RequestBody Map<String, Object> payload) {
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("res_code", "404");
        resultMap.put("res_msg", "부서 추가 중 오류가 발생했습니다.");

        try {
            String departmentName = (String) payload.get("departmentName");
            Long departmentHigh = Long.valueOf(payload.get("departmentHigh").toString());

            departmentService.addDepartment(departmentName, departmentHigh);
            resultMap.put("res_code", "200");
            resultMap.put("res_msg", "부서 정보가 추가되었습니다.");
        } catch (NumberFormatException e) {
            resultMap.put("res_msg", "상위 부서를 찾을 수 없습니다. " + e.getMessage());
        } catch (Exception e) {
            resultMap.put("res_msg", e.getMessage());
        }
        return resultMap;
    }

    @GetMapping("/department/get")
    @ResponseBody
    public Map<String, Object> getDepartment(@RequestParam("id") Long id) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Optional<DepartmentDto> departmentDtoOptional = departmentService.getDepartmentById(id);
            if (departmentDtoOptional.isPresent()) {
                resultMap.put("res_code", "200");
                resultMap.put("department", departmentDtoOptional.get());
            } else {
                resultMap.put("res_code", "404");
                resultMap.put("res_msg", "부서를 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            resultMap.put("res_code", "500");
            resultMap.put("res_msg", "서버 오류: " + e.getMessage());
        }
        return resultMap;
    }

    @PostMapping("/department/update")
    @ResponseBody
    public Map<String, String> updateDepartment(@RequestBody Map<String, Object> payload) {
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("res_code", "404");
        resultMap.put("res_msg", "부서 수정 중 오류가 발생했습니다.");

        try {
            Long departmentId = Long.valueOf(payload.get("departmentId").toString());
            String departmentName = (String) payload.get("departmentName");
            Long departmentHigh = Long.valueOf(payload.get("departmentHigh").toString());

            departmentService.updateDepartment(departmentId, departmentName, departmentHigh);
            resultMap.put("res_code", "200");
            resultMap.put("res_msg", "부서 정보가 수정되었습니다.");
        } catch (Exception e) {
            resultMap.put("res_msg", e.getMessage());
        }
        return resultMap;
    }

    @GetMapping("/department/member-count")
    @ResponseBody
    public long getMemberCountByDepartmentNo(@RequestParam Long departmentNo) {
        return departmentService.getMemberCountByDepartmentNo(departmentNo);
    }

    @PostMapping("/department/delete")
    @ResponseBody
    public Map<String, String> deleteDepartment(@RequestParam("id") Long departmentId) {
        Map<String, String> resultMap = new HashMap<>();
        boolean success = departmentService.deleteDepartment(departmentId);
        
        if (success) {
            resultMap.put("res_code", "200");
            resultMap.put("res_msg", "부서 정보가 삭제되었습니다.");
        } else {
            resultMap.put("res_code", "404");
            resultMap.put("res_msg", "부서에 소속 사원이 존재하여 삭제가 불가능합니다.");
        }
        return resultMap;
    }
}
