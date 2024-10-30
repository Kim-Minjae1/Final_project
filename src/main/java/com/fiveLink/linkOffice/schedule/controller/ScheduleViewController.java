package com.fiveLink.linkOffice.schedule.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fiveLink.linkOffice.member.domain.MemberDto;
import com.fiveLink.linkOffice.member.service.MemberService;
import com.fiveLink.linkOffice.schedule.domain.ScheduleCategoryDto;
import com.fiveLink.linkOffice.schedule.service.ScheduleCategoryService;

@Controller
public class ScheduleViewController {
	private final MemberService memberService; 
	private final ScheduleCategoryService scheduleCategoryService;
	
	@Autowired
	public ScheduleViewController(MemberService memberService, ScheduleCategoryService scheduleCategoryService) { 
	    this.memberService = memberService; 
	    this.scheduleCategoryService = scheduleCategoryService; 
	}
	
	// 관리자 - 일정 범주
	@GetMapping("/schedule/category")
	public String listPermissions(Model model, @RequestParam(value = "id", required = false) Long id) { 
	    Long memberNo = memberService.getLoggedInMemberNo();  
	    List<MemberDto> memberDto = memberService.getMembersByNo(memberNo);
	    List<ScheduleCategoryDto> scheduleCategories = scheduleCategoryService.getAllScheduleCategories();
 
	    if (id == null && !scheduleCategories.isEmpty()) {
	        id = scheduleCategories.get(0).getSchedule_category_no();
	    }

	    ScheduleCategoryDto selectedCategory = null;
	    if (id != null) {
	        selectedCategory = scheduleCategoryService.getScheduleCategoryById(id);
	    }

	    model.addAttribute("memberdto", memberDto); 
	    model.addAttribute("scheduleCategories", scheduleCategories);
	    model.addAttribute("selectedCategory", selectedCategory); 
	    
	    return "/admin/schedule/scheduleCategory";
	}
	
	// 관리자 - 사내 일정 
	@GetMapping("/schedule/company")
	public String companySchedule(Model model) { 
	    Long memberNo = memberService.getLoggedInMemberNo();  
	    List<MemberDto> memberDto = memberService.getMembersByNo(memberNo); 
   
	    model.addAttribute("memberdto", memberDto);  
	    
	    return "/admin/schedule/companySchedule";
	}
	
	
	// 사원
	// 사원 일정
	@GetMapping("/employee/schedule")
	public String employeeSchedule(Model model) { 
	    Long memberNo = memberService.getLoggedInMemberNo();  
	    List<MemberDto> memberDto = memberService.getMembersByNo(memberNo);  

	    model.addAttribute("memberdto", memberDto);  
	    model.addAttribute("memberNo", memberNo); 
	     
	    return "/employee/schedule/employeeSchedule";
	}
}
