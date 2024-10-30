package com.fiveLink.linkOffice.inventory.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.ResponseBody;


import com.fiveLink.linkOffice.inventory.domain.InventoryDto;
import com.fiveLink.linkOffice.inventory.service.InventoryService;
import com.fiveLink.linkOffice.member.domain.MemberDto;
import com.fiveLink.linkOffice.member.service.MemberService;
import com.fiveLink.linkOffice.organization.domain.DepartmentDto;
import com.fiveLink.linkOffice.organization.service.DepartmentService;

@Controller
public class InventoryViewController {

    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryViewController.class);

    private final InventoryService inventoryService;
    private final DepartmentService departmentService;
    private final MemberService memberService;

    @Autowired
    public InventoryViewController(MemberService memberService, DepartmentService departmentService, InventoryService inventoryService) {
    	this.memberService = memberService;
        this.inventoryService = inventoryService;
        this.departmentService = departmentService;
    }
    
 // 비품 목록 페이지
    @GetMapping("/inventory/list/{member_no}")
    public String inventoryList(@PathVariable("member_no") Long memberNo, Model model) {
        // 멤버 정보 조회
        List<MemberDto> memberdto = memberService.getMembersByNo(memberNo); 
        model.addAttribute("memberdto", memberdto);

        // 부서 목록 조회
        List<DepartmentDto> departments = departmentService.findSubDepartment();
        model.addAttribute("departments", departments);

        return "admin/inventory/inventory_list";
    }

    // 부서에 속한 비품 목록 조회
    @GetMapping("/inventory/department/{departmentNo}")
    @ResponseBody
    public List<InventoryDto> selectInventoryByDepartment(@PathVariable("departmentNo") Long departmentNo) {
        return inventoryService.selectInventoryByDepartment(departmentNo);
    }

    // 카테고리와 부서 기준 비품 조회
    @GetMapping("/inventory/category/{inventory_category_no}/department/{department_no}")
    @ResponseBody
    public List<InventoryDto> selectInventoryByCategoryAndDepartment(
            @PathVariable("inventory_category_no") Long inventoryCategoryNo,
            @PathVariable("department_no") Long departmentNo) {
        return inventoryService.selectInventoryByCategoryAndDepartment(inventoryCategoryNo, departmentNo);
    }


    // 비품 등록 페이지 
    @GetMapping("/inventory/create/{member_no}")
    public String inventoryCreate(@PathVariable("member_no") Long memberNo, Model model) {
        // 멤버 정보 조회
        List<MemberDto> memberdto = memberService.getMembersByNo(memberNo);
        model.addAttribute("memberdto", memberdto);

        // 부서 목록 조회
        List<DepartmentDto> departments = departmentService.findSubDepartment();
        model.addAttribute("departments", departments);

        // 로그인한 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String memberNumber = authentication.getName();
        String memberName = inventoryService.findMemberNameByNumber(memberNumber);
        model.addAttribute("manager", memberName);  

        return "admin/inventory/inventory_create";
    }

    // 모든 카테고리 목록 조회
    @GetMapping("/inventory/categories")
    @ResponseBody
    public List<String> getAllCategories() {
        return inventoryService.findAllCategories();
    }

   
}
