package com.fiveLink.linkOffice.permission.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fiveLink.linkOffice.member.domain.MemberDto;
import com.fiveLink.linkOffice.member.service.MemberService;
import com.fiveLink.linkOffice.organization.domain.DepartmentDto;
import com.fiveLink.linkOffice.organization.service.DepartmentService;
import com.fiveLink.linkOffice.permission.domain.MenuDto;
import com.fiveLink.linkOffice.permission.service.PermissionService;

@Controller
public class PermissionController {
 
    private final PermissionService permissionService;
    private final MemberService memberService;
    private final DepartmentService departmentService;

    @Autowired
    public PermissionController(PermissionService permissionService, MemberService memberService, DepartmentService departmentService) {
        this.permissionService = permissionService;
        this.memberService = memberService;
        this.departmentService = departmentService;
    }
    
    @GetMapping("/permission")
    public String listPermissions(Model model) {
        List<MenuDto> permissionList = permissionService.getPermissionList();
        Long memberNo = memberService.getLoggedInMemberNo();  
        List<MemberDto> memberDto = memberService.getMembersByNo(memberNo);

        model.addAttribute("memberdto", memberDto);
        model.addAttribute("permissionList", permissionList);
         
        return "/admin/permission/permission";
    }
    
    @GetMapping("/permission/members")
    @ResponseBody
    public List<Object[]> getPermissionMembers(@RequestParam("menuNo") Long menuNo) {
        // menu_permission_no 조회
        Long menuPermissionNos = permissionService.findMenuPermissionNosByMenuNo(menuNo);
        
        // menu_permission_no를 기반으로 사원 목록 조회
        List<Object[]> members = permissionService.findMembersByMenuNo(menuPermissionNos);
         
        return members;
    }
 
    // 권한자 등록
    @PostMapping("/permission/addMembers")
    public @ResponseBody Map<String, String> addMembers(@RequestBody Map<String, Object> requestBody) {
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("res_code", "404");
        resultMap.put("res_msg", "사원 등록 중 오류가 발생했습니다.");
        
        try {
            String menuNo = (String) requestBody.get("menuNo");
            List<String> memberNos = (List<String>) requestBody.get("memberNos");

            permissionService.saveSelectedMembers(menuNo, memberNos);

            for (String memberNoStr : memberNos) {
                Long memberNo = Long.parseLong(memberNoStr);   
                permissionService.updateMemberAdditionalStatus(memberNo);
            }
            
            resultMap.put("res_code", "200");
            resultMap.put("res_msg", "권한자 등록이 완료되었습니다.");
        } catch (Exception e) { 
            resultMap.put("res_msg", "사원 등록 중 오류가 발생했습니다");
        }

        return resultMap;
    }

    // 조직도 데이터 불러오기
    @GetMapping("/permission/chart")
    @ResponseBody
    public Map<String, Object> getOrganizationChart(@RequestParam(value = "selectedMemberNos", required = false) List<Long> selectedMemberNos,
                                                    @RequestParam("menuNo") Long menuNo) {
        List<DepartmentDto> departments = departmentService.getAllDepartments();
        List<MemberDto> members = memberService.getAllMembersChart();
        
        if (selectedMemberNos == null || selectedMemberNos.isEmpty()) {
            List<Object[]> permissionMembers = permissionService.findMembersByMenuNo(menuNo);
            selectedMemberNos = permissionMembers.stream()
                .map(member -> Long.parseLong(member[0].toString()))
                .collect(Collectors.toList());
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("chartData", buildTree(departments, members));
        result.put("selectedMemberNos", selectedMemberNos);
        
        return result;
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
                    
                    // 하위 부서 추가
                    children.add(subDeptNode);
                    hasSubDepartments = true;
                }
            }
		}

		// 현재 부서의 구성원 확인 및 추가
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
    
    // 등록된 사원 정보
    @GetMapping("/permission/assigned-members")
    @ResponseBody
    public List<Long> getAssignedMembers(@RequestParam("menuNo") Long menuNo) {
        return permissionService.getAssignedMembersByMenuNo(menuNo);
    } 
    
    // 권한자 삭제
    @PostMapping("/permission/deleteMembers")
    public @ResponseBody Map<String, String> deleteMembers(@RequestBody Map<String, Object> requestBody) {
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("res_code", "500");
        resultMap.put("res_msg", "삭제 중 오류가 발생했습니다.");

        try { 
            List<Long> memberNos = (List<Long>) requestBody.get("memberNos");
            if (memberNos == null || memberNos.isEmpty()) {
                resultMap.put("res_msg", "삭제할 권한자가 선택되지 않았거나 메뉴 번호가 없습니다.");
                return resultMap;
            }
 
            Long menuNo = null;
            Object menuNoObj = requestBody.get("menuNo");
            menuNo = ((Number) menuNoObj).longValue();

            permissionService.deleteSelectedMembers(memberNos, menuNo);

            resultMap.put("res_code", "200");
            resultMap.put("res_msg", "권한자가 삭제되었습니다.");
        } catch (IllegalArgumentException e) {
            resultMap.put("res_msg", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("res_msg", "삭제 처리 중 오류가 발생했습니다: " + e.getMessage());
        } 
        return resultMap;
    }
 
}
                   
