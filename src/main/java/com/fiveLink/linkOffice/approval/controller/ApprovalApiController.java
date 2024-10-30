package com.fiveLink.linkOffice.approval.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.fiveLink.linkOffice.approval.domain.ApprovalDto;
import com.fiveLink.linkOffice.approval.domain.ApprovalFileDto;
import com.fiveLink.linkOffice.approval.domain.ApprovalFlowDto;
import com.fiveLink.linkOffice.approval.domain.ApprovalFormDto;
import com.fiveLink.linkOffice.approval.service.ApprovalFileService;
import com.fiveLink.linkOffice.approval.service.ApprovalFormService;
import com.fiveLink.linkOffice.approval.service.ApprovalService;
import com.fiveLink.linkOffice.member.service.MemberService;
import com.fiveLink.linkOffice.vacationapproval.domain.VacationApprovalFlowDto;

@Controller
public class ApprovalApiController {
	
	private final ApprovalFormService approvalFormService;
	private final MemberService memberService;
	private final ApprovalFileService approvalFileService;
	private final ApprovalService approvalService;
	
	
	@Autowired
	public ApprovalApiController( ApprovalFormService approvalFormService, MemberService memberService, ApprovalFileService approvalFileService, ApprovalService approvalService) {
		this.approvalFormService = approvalFormService;
		this.memberService = memberService;
		this.approvalFileService = approvalFileService;
		this.approvalService = approvalService;
	}
	
	// 관리자 전자결재 양식 등록
	@ResponseBody
	@PostMapping("/admin/approval/create")
	public Map<String,String> adminApprovalCreate(@RequestParam("approval_title") String approvalTitle, 
			@RequestParam("editor_content") String editorContent) {
		Map<String, String> response = new HashMap<>();
	    response.put("res_code", "404");
	    response.put("res_msg", "양식 등록 중 오류가 발생하였습니다.");
		 ApprovalFormDto dto = new ApprovalFormDto();
		 dto.setApproval_form_title(approvalTitle);  
		 dto.setApproval_form_content(editorContent);
		 dto.setApproval_form_status(0L);
		    
		 if(approvalFormService.saveApprovalForm(dto) != null) {
				response.put("res_code", "200");
			    response.put("res_msg", "양식이 등록되었습니다.");			 
		 }
	    return response; 
	}
	
	// 관리자 전자결재 양식 수정
	@ResponseBody
	@PutMapping("/admin/approval/edit")
	public Map<String,String> adminApprovalEdit(@RequestParam("form_no") String formNo, 
			@RequestParam("approval_title") String approvalTitle, 
			@RequestParam("editor_content") String editorContent) {
		Map<String, String> response = new HashMap<>();
	    response.put("res_code", "404");
	    response.put("res_msg", "양식 수정 중 오류가 발생하였습니다.");
		 ApprovalFormDto dto = new ApprovalFormDto();
		 
		 dto.setApproval_form_no(Long.parseLong(formNo));  
		 dto.setApproval_form_title(approvalTitle);  
		 dto.setApproval_form_content(editorContent);
		 dto.setApproval_form_status(0L);
		    
		 if(approvalFormService.editApprovalForm(dto) != null) {
				response.put("res_code", "200");
			    response.put("res_msg", "양식을 수정하였습니다.");			 
		 }
	    return response; 
	}
	
	// 관리자 전자결재 양식 삭제
	@ResponseBody
	@PutMapping("/admin/approval/delete/{form_no}")
	public Map<String,String> adminApprovalDelete(@PathVariable("form_no") Long formNo){
		Map<String, String> response = new HashMap<>();
	    response.put("res_code", "404");
	    response.put("res_msg", "양식 삭제 중 오류가 발생하였습니다.");
	    
	    ApprovalFormDto dto = approvalFormService.getApprovalFormOne(formNo);
	    dto.setApproval_form_status(1L);
	    
	    if(approvalFormService.deleteApprovalForm(dto) != null) {
	    	response.put("res_code", "200");
		    response.put("res_msg", "양식을 삭제하였습니다.");			 
	    }
	    return response; 
	}
	
	// 사용자 전자결재 기안
	@ResponseBody
	@PostMapping("/employee/approval/create")
	public Map<String,String> createApproval(
			@RequestParam(value = "file", required = false) MultipartFile file,
			@RequestParam("approvalTitle") String approvalTitle,
			@RequestParam("approvalContent")String approvalContent,
	        @RequestParam("approvers") List<Long> approvers,
	        @RequestParam("references") List<Long> references,
	        @RequestParam("reviewers") List<Long> reviewers){
		
	    Map<String, String> response = new HashMap<>();
	    response.put("res_code", "404");
	    response.put("res_msg", "결재 중 오류가 발생했습니다.");
	    
	    Long member_no = memberService.getLoggedInMemberNo();
	    
	    ApprovalDto appdto = new ApprovalDto();
	    appdto.setApproval_title(approvalTitle);
	    appdto.setMember_no(member_no);
	    appdto.setApproval_content(approvalContent);
	    appdto.setApproval_status(0L);
	    
	    List<ApprovalFlowDto> approvalFlowdto =  new ArrayList<>();
	    
	    int order = 1;
	    
	 // 1. 합의자 (flow_role = 1)
	    for (Long referenceId : references) {
	        ApprovalFlowDto flowDto = new ApprovalFlowDto();
	        flowDto.setMember_no(referenceId);
	        flowDto.setApproval_flow_role(1L); 
	        flowDto.setApproval_flow_order((long) order++);
	        flowDto.setApproval_flow_status(order == 2 ? 1L : 0L); 
	        approvalFlowdto.add(flowDto);
	    }

	    // 2. 결재자 (flow_role = 2)
	    for (Long approverId : approvers) {
	        ApprovalFlowDto flowDto = new ApprovalFlowDto();
	        flowDto.setMember_no(approverId);
	        flowDto.setApproval_flow_role(2L); 
	        flowDto.setApproval_flow_order((long) order++);
	        flowDto.setApproval_flow_status(order == 2 && references.isEmpty() ? 1L : 0L); 
	        approvalFlowdto.add(flowDto);
	    }

	    // 3. 참조자 (flow_role = 0)
	    for (Long reviewerId : reviewers) {
	        ApprovalFlowDto flowDto = new ApprovalFlowDto();
	        flowDto.setMember_no(reviewerId);
	        flowDto.setApproval_flow_role(0L); 
	        flowDto.setApproval_flow_order(null); 
	        flowDto.setApproval_flow_status(4L); 
	        approvalFlowdto.add(flowDto);
	    }
	    

	    
	    boolean isFileUploaded = false;
	    
	    // 파일이 있을 떄 
	    if (file != null && !file.isEmpty()) {
	        ApprovalFileDto filedto = new ApprovalFileDto();
	        
	        String saveFileName = approvalFileService.upload(file);
	        
	        if (saveFileName != null) {
	        	filedto.setApproval_file_ori_name(file.getOriginalFilename());
	        	filedto.setApproval_file_new_name(saveFileName);
	        	filedto.setApproval_file_size(file.getSize());

	            if (approvalService.createApprovalFile(appdto, filedto, approvalFlowdto) != null) {
					Long approvalPk = approvalService.getApprovalPk();
					response.put("approvalPk", String.valueOf(approvalPk));	            	
	                response.put("res_code", "200");
	                response.put("res_msg", "결재를 기안하였습니다.");
	                isFileUploaded = true;
	            }
	        } 
	    }

	    // 파일이 없을 때
	    if (!isFileUploaded) {
	        if (approvalService.createApproval(appdto, approvalFlowdto) != null) {
				Long approvalPk = approvalService.getApprovalPk();
				response.put("approvalPk", String.valueOf(approvalPk));		        	
	            response.put("res_code", "200");
	            response.put("res_msg", "결재를 기안하였습니다."); 
	        }
	    }
	    
	    return response;
	}
	
	// 사용자 전자 결재 기안 취소
	@ResponseBody
	@PutMapping("/employee/approval/cancel/{approval_no}")
	public Map<String,String> employeeApprovalDelete(@PathVariable("approval_no") Long aapNo,
			@RequestBody ApprovalDto approvalDto){
		Map<String, String> response = new HashMap<>();
	    response.put("res_code", "404");
	    response.put("res_msg", "기안 취소 중 오류가 발생하였습니다.");
	    
	    ApprovalDto dto = approvalService.selectApprovalOne(aapNo);
	    dto.setApproval_status(3L);
	    dto.setApproval_cancel_reason(approvalDto.getApproval_cancel_reason());
	    
	    if(approvalService.cancelApproval(dto) != null) {
	    	response.put("res_code", "200");
		    response.put("res_msg", "결재 기안이 취소되었습니다.");			 
	    }
	    return response; 
	} 
	
	// 사용자 전자결재 기안 수정
	@ResponseBody
	@PutMapping("/employee/approval/edit/{approval_no}")
	public Map<String,String> editApproval(@PathVariable("approval_no") Long aapNo,
			@RequestParam(value = "file", required = false) MultipartFile file,
			@RequestParam("approvalTitle") String approvalTitle,
			@RequestParam("approvalContent")String approvalContent,
	        @RequestParam("approvers") List<Long> approvers,
	        @RequestParam("references") List<Long> references,
	        @RequestParam("reviewers") List<Long> reviewers){
		
	    Map<String, String> response = new HashMap<>();
	    response.put("res_code", "404");
	    response.put("res_msg", "결재 수정 중 오류가 발생했습니다.");
	    
	    ApprovalDto appdto = approvalService.selectApprovalOne(aapNo);
	    
	    appdto.setApproval_title(approvalTitle);
	    appdto.setApproval_content(approvalContent);
	    
	    List<ApprovalFlowDto> approvalFlowdto =  new ArrayList<>();
	    
	    int order = 1;
	    
	 // 1. 합의자 (flow_role = 1)
	    for (Long referenceId : references) {
	        ApprovalFlowDto flowDto = new ApprovalFlowDto();
	        flowDto.setMember_no(referenceId);
	        flowDto.setApproval_flow_role(1L); 
	        flowDto.setApproval_flow_order((long) order++);
	        flowDto.setApproval_flow_status(order == 2 ? 1L : 0L); 
	        approvalFlowdto.add(flowDto);
	    }

	    // 2. 결재자 (flow_role = 2)
	    for (Long approverId : approvers) {
	        ApprovalFlowDto flowDto = new ApprovalFlowDto();
	        flowDto.setMember_no(approverId);
	        flowDto.setApproval_flow_role(2L); 
	        flowDto.setApproval_flow_order((long) order++);
	        flowDto.setApproval_flow_status(order == 2 && references.isEmpty() ? 1L : 0L); 
	        approvalFlowdto.add(flowDto);
	    }

	    // 3. 참조자 (flow_role = 0)
	    for (Long reviewerId : reviewers) {
	        ApprovalFlowDto flowDto = new ApprovalFlowDto();
	        flowDto.setMember_no(reviewerId);
	        flowDto.setApproval_flow_role(0L); 
	        flowDto.setApproval_flow_order(null); 
	        flowDto.setApproval_flow_status(4L); 
	        approvalFlowdto.add(flowDto);
	    }
	    

	    
	    boolean isFileUploaded = false;
	    
	    // 파일이 있을 떄 
	    if (file != null && !file.isEmpty()) {
	        ApprovalFileDto filedto = new ApprovalFileDto();
	        
	        String saveFileName = approvalFileService.upload(file);
	        
	        if (saveFileName != null) {
	        	filedto.setApproval_file_ori_name(file.getOriginalFilename());
	        	filedto.setApproval_file_new_name(saveFileName);
	        	filedto.setApproval_file_size(file.getSize());
	        	
	            if (approvalFileService.existsFileForVacationApproval(aapNo)) {
	                if (approvalFileService.delete(aapNo) > 0) {
	                    response.put("res_msg", "기존 파일이 삭제 되었습니다.");
	                } else {
	                    response.put("res_msg", "기존 파일 삭제 중 오류가 발생하였습니다.");
	                    return response;
	                }
	            }
	        	
	        	
	            if (approvalService.updateApprovalFile(appdto, filedto, approvalFlowdto) != null) {
	                response.put("res_code", "200");
	                response.put("res_msg", "결재 문서를 수정하였습니다.");
	                isFileUploaded = true;
	            }
	        } 
	    }

	    // 파일이 없을 때
	    if (!isFileUploaded) {
	        if (approvalService.updateApproval(appdto, approvalFlowdto) != null) {
	            response.put("res_code", "200");
	            response.put("res_msg", "결재 문서를 수정하였습니다."); 
	        }
	    }
	    
	    return response;
	}
	
	// 사용자 (결재자) 전자결재 내역함 - 승인
	@ResponseBody
	@PutMapping("/employee/approval/approve/{approval_no}")
	public Map<String,String> employeeApprovalFlowUpdate(@PathVariable("approval_no") Long appNo){
		Map<String, String> response = new HashMap<>();
	    response.put("res_code", "404");
	    response.put("res_msg", "승인 중 오류가 발생하였습니다.");
	    
	    Long memberNo = memberService.getLoggedInMemberNo();
	    
	    if(approvalService.employeeApprovalFlowUpdate(appNo, memberNo) != null) {
	    	
            response.put("res_code", "200");
            response.put("res_msg", "결재 승인이 완료되었습니다."); 	    	
	    }
	    
	    return response;
	}
	
	// 사용자 (결재자) 전자결재 내역함 - 승인 취소
	@ResponseBody
	@PutMapping("/employee/approval/approvecancel/{approval_no}")
	public Map<String,String> employeeApprovalFlowCancel(@PathVariable("approval_no") Long appNo){
		Map<String, String> response = new HashMap<>();
	    response.put("res_code", "404");
	    response.put("res_msg", "승인 취소 중 오류가 발생하였습니다.");
	    
	    Long memberNo = memberService.getLoggedInMemberNo();
	    
	    if(approvalService.employeeApprovalFlowApproveCancel(appNo, memberNo) != null) {
	    	
            response.put("res_code", "200");
            response.put("res_msg", "결재 승인이 취소되었습니다."); 	    	
	    }
	    
	    return response;
	}
	
	// 사용자 (결재자) 전자결재 내역함 - 반려
	@ResponseBody
	@PutMapping("/employee/approval/reject/{approval_no}")
	public Map<String,String> employeeApprovalFlowReject(@PathVariable("approval_no") Long appNo,
			@RequestBody ApprovalFlowDto approvalFlowDto){
		Map<String, String> response = new HashMap<>();
	    response.put("res_code", "404");
	    response.put("res_msg", "반려 중 오류가 발생하였습니다.");
	    
	    Long memberNo = memberService.getLoggedInMemberNo();
	    
	    approvalFlowDto.setApproval_no(appNo);
	    
	    if(approvalService.employeeApprovalFlowReject(approvalFlowDto, memberNo) != null) {
	    	
            response.put("res_code", "200");
            response.put("res_msg", "결재 문서를 반려하였습니다."); 	    	
	    }
	    
	    return response;
	}
	
}
