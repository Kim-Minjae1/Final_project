package com.fiveLink.linkOffice.vacationapproval.controller;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fiveLink.linkOffice.member.domain.Member;
import com.fiveLink.linkOffice.member.domain.MemberDto;
import com.fiveLink.linkOffice.member.service.MemberService;
import com.fiveLink.linkOffice.vacation.domain.VacationTypeDto;
import com.fiveLink.linkOffice.vacation.service.VacationService;
import com.fiveLink.linkOffice.vacationapproval.domain.VacationApprovalDto;
import com.fiveLink.linkOffice.vacationapproval.domain.VacationApprovalFileDto;
import com.fiveLink.linkOffice.vacationapproval.domain.VacationApprovalFlowDto;
import com.fiveLink.linkOffice.vacationapproval.service.VacationApprovalFileService;
import com.fiveLink.linkOffice.vacationapproval.service.VacationApprovalService;

@Controller
public class VacationApprovalViewController {
	
	private final MemberService memberService;
    private final VacationService vacationService;
    private final VacationApprovalService vacationApprovalService;
    private final VacationApprovalFileService vacationApprovalFileService;
	
    @Autowired
    public VacationApprovalViewController(MemberService memberService, VacationService vacationService, VacationApprovalService vacationApprovalService, VacationApprovalFileService vacationApprovalFileService) {
        this.memberService = memberService;
        this.vacationService = vacationService;
        this.vacationApprovalService = vacationApprovalService;
        this.vacationApprovalFileService = vacationApprovalFileService;
    }
    
 // 사용자 휴가 결재 등록 페이지
 	@GetMapping("/employee/vacationapproval/create")
 	public String employeeVacationApprovalCreate(Model model) {
 		Long member_no = memberService.getLoggedInMemberNo();
 		List<MemberDto> memberdto = memberService.getMembersByNo(member_no);
 		
 		Member mem = memberService.memberNo(member_no);
 		
 		List<VacationTypeDto> vacationTypeList = vacationService.selectVacationTypeList();
 		System.out.println(mem.getMemberVacationCount());
 		model.addAttribute("mem",mem);
 		model.addAttribute("memberdto", memberdto);
 		model.addAttribute("vacationTypeList", vacationTypeList);
 		
 		return "employee/vacationapproval/vacationapproval_create";
 	}
 	// 휴가 정렬
 	private Sort getSortOption(String sort) {
		if ("latest".equals(sort)) {
			return Sort.by(Sort.Order.desc("vacationApprovalCreateDate")); 
		} else if ("oldest".equals(sort)) {
			return Sort.by(Sort.Order.asc("vacationApprovalCreateDate")); 
		}
		return Sort.by(Sort.Order.desc("vacationApprovalCreateDate")); 
	}
 	
 	//  사용자 휴가 결재함 페이지
 	@GetMapping("/employee/vacationapproval/list")
 	public String employeevacationapprovalList(Model model, VacationApprovalDto searchdto, @PageableDefault(size = 10, sort = "vacationApprovalCreateDate", direction = Sort.Direction.DESC) Pageable pageable, @RequestParam(value = "sort", defaultValue = "latest") String sort) {
 		Long member_no = memberService.getLoggedInMemberNo();
 		List<MemberDto> memberdto = memberService.getMembersByNo(member_no);
 		
 		Sort sortOption = getSortOption(sort);
		Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortOption);
 		
 		Page<VacationApprovalDto> vacationapprovalList = vacationApprovalService.getVacationApprovalByNo(member_no,searchdto,sortedPageable);
 		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
 		vacationapprovalList.forEach(vapp -> {
 			if(vapp.getVacation_approval_create_date() != null) {
 				String fomattedCreateDate = vapp.getVacation_approval_create_date().format(formatter);
 				vapp.setFormat_vacation_approval_create_date(fomattedCreateDate);
 			}
 		});
 		
 		model.addAttribute("memberdto", memberdto);
 		model.addAttribute("vacationapprovalList", vacationapprovalList.getContent());
		model.addAttribute("page", vacationapprovalList);
		model.addAttribute("searchDto", searchdto);
		model.addAttribute("currentSort", sort);
 		
 		return "employee/vacationapproval/vacationapproval_list";
 	}
 	
 	// 사용자 휴가 결재 상세 페이지
 	@GetMapping("/employee/vacationapproval/detail/{vacation_approval_no}")
 	public String employeevacationapprovalDetail(Model model, @PathVariable("vacation_approval_no") Long vacationApprovalNo) {
 		
 		VacationApprovalDto vacationapprovaldto = vacationApprovalService.selectVacationApprovalOne(vacationApprovalNo);
 		
 		Long member_no = memberService.getLoggedInMemberNo();
 		List<MemberDto> memberdto = memberService.getMembersByNo(member_no);
 		
 		
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        if (vacationapprovaldto.getVacation_approval_create_date() != null ) {
            String formattedCreateDate = vacationapprovaldto.getVacation_approval_create_date().format(formatter);
            vacationapprovaldto.setFormat_vacation_approval_create_date(formattedCreateDate);
        }
        
        if (vacationapprovaldto.getFlows() != null) {
            for (VacationApprovalFlowDto flow : vacationapprovaldto.getFlows()) {
                if (flow.getVacation_approval_flow_complete_date() != null) {
                    String formattedCompleteDate = flow.getVacation_approval_flow_complete_date().format(formatter);
                    flow.setFormat_vacation_approval_flow_complete_date(formattedCompleteDate);
                }
                
                MemberDto currentMember = memberService.selectMemberOne(flow.getMember_no());
                flow.setDigital_name(currentMember.getMember_new_digital_img());
                
            }
        }
        
		if(vacationapprovaldto.getFiles() != null) {
			for(VacationApprovalFileDto file : vacationapprovaldto.getFiles()) {
				Long fileSize = file.getVacation_approval_file_size();
				file.setVacation_approval_file_size(fileSize / 1024);
			}
		}
        
 		model.addAttribute("vacationapprovaldto", vacationapprovaldto);
 		model.addAttribute("memberdto", memberdto);
 		
 		return "employee/vacationapproval/vacationapproval_detail";
 	}
 	
 	// 휴가 결재 수정 페이지
 	@GetMapping("/employee/vacationapproval/edit/{vacation_approval_no}")
 	public String employeevacationapprovalEdit(Model model, @PathVariable("vacation_approval_no") Long vacationApprovalNo) {
 		
 		VacationApprovalDto vacationapprovaldto = vacationApprovalService.selectVacationApprovalOne(vacationApprovalNo);
 		
 		Long member_no = memberService.getLoggedInMemberNo();
 		List<MemberDto> memberdto = memberService.getMembersByNo(member_no);
 		
 		Member mem = memberService.memberNo(member_no);
 		
 		System.out.println(mem.getMemberVacationCount());
 		
 		List<VacationTypeDto> vacationTypeList = vacationService.selectVacationTypeList();
 		
 		model.addAttribute("mem",mem);
 		model.addAttribute("vacationapprovaldto", vacationapprovaldto);
 		model.addAttribute("memberdto", memberdto);
 		model.addAttribute("vacationTypeList", vacationTypeList);
 		
 		return "employee/vacationapproval/vacationapproval_edit";
 	}
 	
	// 전자결재 파일 다운로드 
	@GetMapping("/download_vacation_file/{vacation_approval_no}")
	public ResponseEntity<Object> noticeImgDownload(@PathVariable("vacation_approval_no")Long vacationApprovalNo){
		return vacationApprovalFileService.download(vacationApprovalNo);
	}
 	
	// 휴가결재 수정 값 (js)
	@GetMapping("/employee/vacationapproval/approve/{vacation_approval_no}")
	@ResponseBody
	public  Map<String, Object> approvalEdit(@PathVariable("vacation_approval_no") Long vacationApprovalNo) {
		Map<String, Object> response = new HashMap<>();
		
 		VacationApprovalDto vacationapprovaldto = vacationApprovalService.selectVacationApprovalOne(vacationApprovalNo);
		
 		response.put("vacationapprovaldto", vacationapprovaldto);
		
		return response;
	}
}
