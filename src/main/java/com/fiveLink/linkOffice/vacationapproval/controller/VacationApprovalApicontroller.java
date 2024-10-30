package com.fiveLink.linkOffice.vacationapproval.controller;

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

import com.fiveLink.linkOffice.member.domain.MemberDto;
import com.fiveLink.linkOffice.member.service.MemberService;
import com.fiveLink.linkOffice.vacation.service.VacationService;
import com.fiveLink.linkOffice.vacationapproval.domain.VacationApprovalDto;
import com.fiveLink.linkOffice.vacationapproval.domain.VacationApprovalFileDto;
import com.fiveLink.linkOffice.vacationapproval.domain.VacationApprovalFlowDto;
import com.fiveLink.linkOffice.vacationapproval.service.VacationApprovalFileService;
import com.fiveLink.linkOffice.vacationapproval.service.VacationApprovalService;

@Controller
public class VacationApprovalApicontroller {

	private final VacationApprovalService vacationApprovalService;
	private final VacationApprovalFileService vacationApprovalFileService;
	private final MemberService memberService;
	private final VacationService vacationService;
	
	@Autowired
	public VacationApprovalApicontroller (VacationApprovalService vacationApprovalService, 
			VacationApprovalFileService vacationApprovalFileService,
			MemberService memberService,
			VacationService vacationService) {
		this.vacationApprovalService = vacationApprovalService;
		this.vacationApprovalFileService = vacationApprovalFileService;
		this.memberService = memberService;
		this.vacationService = vacationService;
	}
	
	// 휴가 결재 등록
	@ResponseBody
	@PostMapping("/employee/vacationapproval/create")
	public Map<String, String> createVacationApproval(
			@RequestParam(value = "vacationFile", required = false) MultipartFile file,
	        @RequestParam("vacationapprovalTitle") String vacationapprovalTitle,
	        @RequestParam("vacationtype") Long vacationtype,
	        @RequestParam("memberNo") Long memberNo,
	        @RequestParam("startDate") String startDate,
	        @RequestParam("endDate") String endDate,
	        @RequestParam("dateCount") String dateCount,
	        @RequestParam("vacationapprovalContent") String vacationapprovalContent,
	        @RequestParam("approvers") List<Long> approvers,
	        @RequestParam("references") List<Long> references,
	        @RequestParam("reviewers") List<Long> reviewers) {
		

	    Map<String, String> response = new HashMap<>();
	    response.put("res_code", "404");
	    response.put("res_msg", "휴가 신청 중 오류가 발생했습니다.");
	    
	    // 휴가 갯수 감소
        Long member_no = memberService.getLoggedInMemberNo();
        MemberDto memberdto = new MemberDto();
        
        Double vacation_count = vacationService.vacationType(vacationtype);

        int dateCountInt = Integer.parseInt(dateCount);
        
        double totalVacationCount = (vacation_count * dateCountInt);

        memberdto.setMember_no(member_no);
        memberdto.setMember_vacation_count(totalVacationCount);
        
	    if(memberService.updateVacation(memberdto) != null) {
	    	response.put("res_code", "200");
	    	response.put("res_msg", "휴가 개수가 감소되었습니다.");
	    }
	    
	    VacationApprovalDto vappdto = new VacationApprovalDto();
	    vappdto.setVacation_approval_title(vacationapprovalTitle);
	    vappdto.setVacation_type_no(vacationtype);
	    vappdto.setMember_no(memberNo);
	    vappdto.setVacation_approval_start_date(startDate);
	    vappdto.setVacation_approval_end_date(endDate);
	    vappdto.setVacation_approval_total_days(dateCount);
	    vappdto.setVacation_approval_content(vacationapprovalContent);
	    vappdto.setVacation_approval_status(0L); 

	    
	    List<VacationApprovalFlowDto> approvalFlowDtos = new ArrayList<>();
	    int order = 1;

	    // 1. 합의자 (flow_role = 1)
	    for (Long referenceId : references) {
	        VacationApprovalFlowDto flowDto = new VacationApprovalFlowDto();
	        flowDto.setMember_no(referenceId);
	        flowDto.setVacation_approval_flow_role(1L); 
	        flowDto.setVacation_approval_flow_order((long) order++);
	        flowDto.setVacation_approval_flow_status(order == 2 ? 1L : 0L); 
	        approvalFlowDtos.add(flowDto);
	    }

	    // 2. 결재자 (flow_role = 2)
	    for (Long approverId : approvers) {
	        VacationApprovalFlowDto flowDto = new VacationApprovalFlowDto();
	        flowDto.setMember_no(approverId);
	        flowDto.setVacation_approval_flow_role(2L); 
	        flowDto.setVacation_approval_flow_order((long) order++);
	        flowDto.setVacation_approval_flow_status(order == 2 && references.isEmpty() ? 1L : 0L); 
	        approvalFlowDtos.add(flowDto);
	    }

	    // 3. 참조자 (flow_role = 0)
	    for (Long reviewerId : reviewers) {
	        VacationApprovalFlowDto flowDto = new VacationApprovalFlowDto();
	        flowDto.setMember_no(reviewerId);
	        flowDto.setVacation_approval_flow_role(0L); 
	        flowDto.setVacation_approval_flow_order(null); 
	        flowDto.setVacation_approval_flow_status(4L); 
	        approvalFlowDtos.add(flowDto);
	    }

	    boolean isFileUploaded = false;
	    
	    // 파일이 있을 떄 
	    if (file != null && !file.isEmpty()) {
	        VacationApprovalFileDto vaFiledto = new VacationApprovalFileDto();
	        
	        String saveVacationFileName = vacationApprovalFileService.uploadVacation(file);
	        
	        if (saveVacationFileName != null) {
	            vaFiledto.setVacation_approval_file_ori_name(file.getOriginalFilename());
	            vaFiledto.setVacation_approval_file_new_name(saveVacationFileName);
	            vaFiledto.setVacation_approval_file_size(file.getSize());

	            if (vacationApprovalService.createVacationApprovalFile(vappdto, vaFiledto, approvalFlowDtos) != null) {
					Long vacationApprovalPk = vacationApprovalService.getVacationApprovalPk();
					response.put("approvalPk", String.valueOf(vacationApprovalPk));
	                response.put("res_code", "200");
	                response.put("res_msg", "휴가 신청이 완료되었습니다.");
	                isFileUploaded = true;
	            }
	        } 
	    }
	    // 파일이 없을 떄 
	    if (!isFileUploaded) {
	        if (vacationApprovalService.createVacationApproval(vappdto,approvalFlowDtos) != null) {
				Long vacationApprovalPk = vacationApprovalService.getVacationApprovalPk();
				response.put("approvalPk", String.valueOf(vacationApprovalPk));
	            response.put("res_code", "200");
	            response.put("res_msg", "휴가 신청이 완료되었습니다.");
	        }
	    }

	    return response;
	}
	
	// 휴가 결재 기안 취소
	@ResponseBody
	@PutMapping("/employee/vacationapproval/cancel/{vacationapproval_no}")
	public Map<String,String> employeeVacationApprovalDelete(@PathVariable("vacationapproval_no") Long vapNo,
			@RequestBody VacationApprovalDto vacationApprovalDto){
		Map<String, String> response = new HashMap<>();
	    response.put("res_code", "404");
	    response.put("res_msg", "기안 취소 중 오류가 발생하였습니다.");
	    
	    VacationApprovalDto dto = vacationApprovalService.selectVacationApprovalOne(vapNo);
	    dto.setVacation_approval_status(3L);
	    dto.setVacation_approval_cancel_reason(vacationApprovalDto.getVacation_approval_cancel_reason());
	    
	    Long vacationtype = dto.getVacation_type_no();
	    String dateCount = dto.getVacation_approval_total_days();
	    // 휴가 갯수 복구
        Long member_no = memberService.getLoggedInMemberNo();
        MemberDto memberdto = new MemberDto();
        
        Double vacation_count = vacationService.vacationType(vacationtype);

        int dateCountInt = Integer.parseInt(dateCount);
        
        double totalVacationCount = (vacation_count * dateCountInt);

        memberdto.setMember_no(member_no);
        memberdto.setMember_vacation_count(totalVacationCount);
        
	    if(memberService.updateOriginVacation(memberdto) != null) {
	    	response.put("res_code", "200");
	    	response.put("res_msg", "휴가 개수가 복구되었습니다.");
	    }
	    
	    if(vacationApprovalService.cancelVacationApproval(dto) != null) {
	    	response.put("res_code", "200");
		    response.put("res_msg", "휴가 신청이 취소되었습니다.");			 
	    }
	    return response; 
	}
	
	// 휴가 결재 수정 
	@ResponseBody
	@PutMapping("/employee/vacationapproval/edit/{vacationapproval_no}")
	public Map<String,String> employeeVacationApprovalEdit(@PathVariable("vacationapproval_no") Long vapNo, 
			@RequestParam(value = "vacationFile", required = false) MultipartFile file,
	        @RequestParam("vacationapprovalTitle") String vacationapprovalTitle,
	        @RequestParam("vacationtype") Long vacationtype,
	        @RequestParam("startDate") String startDate,
	        @RequestParam("endDate") String endDate,
	        @RequestParam("dateCount") String dateCount,
	        @RequestParam("vacationapprovalContent") String vacationapprovalContent,
	        @RequestParam("approvers") List<Long> approvers,
	        @RequestParam("references") List<Long> references,
	        @RequestParam("reviewers") List<Long> reviewers){
		Map<String, String> response = new HashMap<>();
	    response.put("res_code", "404");
	    response.put("res_msg", "수정 중 오류가 발생하였습니다.");
	    
	    VacationApprovalDto vacationapprovaldto = vacationApprovalService.selectVacationApprovalOne(vapNo);
 		
	    vacationapprovaldto.setVacation_approval_title(vacationapprovalTitle);
	    vacationapprovaldto.setVacation_type_no(vacationtype);
	    vacationapprovaldto.setVacation_approval_start_date(startDate);
	    vacationapprovaldto.setVacation_approval_end_date(endDate);
	    vacationapprovaldto.setVacation_approval_total_days(dateCount);
	    vacationapprovaldto.setVacation_approval_content(vacationapprovalContent);
	    
	    List<VacationApprovalFlowDto> approvalFlowDtos = new ArrayList<>();
	    int order = 1;

	    // 1. 참조자 (flow_role = 1)
	    for (Long referenceId : references) {
	        VacationApprovalFlowDto flowDto = new VacationApprovalFlowDto();
	        flowDto.setMember_no(referenceId);
	        flowDto.setVacation_approval_flow_role(1L);
	        flowDto.setVacation_approval_flow_order((long) order++);
	        flowDto.setVacation_approval_flow_status(order == 2 ? 1L : 0L);
	        approvalFlowDtos.add(flowDto);
	    }
	    // 2. 결재자 (flow_role = 2)
	    for (Long approverId : approvers) {
	        VacationApprovalFlowDto flowDto = new VacationApprovalFlowDto();
	        flowDto.setMember_no(approverId);
	        flowDto.setVacation_approval_flow_role(2L);
	        flowDto.setVacation_approval_flow_order((long) order++);
	        flowDto.setVacation_approval_flow_status(order == 2 && references.isEmpty() ? 1L : 0L);
	        approvalFlowDtos.add(flowDto);
	    }
	    // 3. 참조자 (flow_role = 0)
	    for (Long reviewerId : reviewers) {
	        VacationApprovalFlowDto flowDto = new VacationApprovalFlowDto();
	        flowDto.setMember_no(reviewerId);
	        flowDto.setVacation_approval_flow_role(0L);
	        flowDto.setVacation_approval_flow_order(null);
	        flowDto.setVacation_approval_flow_status(4L);
	        approvalFlowDtos.add(flowDto);
	    }
	    
	    boolean isFileUploaded = false;
	    
	    // 파일이 있을 때 
	    if (file != null && !file.isEmpty()) {
	        VacationApprovalFileDto vaFiledto = new VacationApprovalFileDto();
	        String saveFileName = vacationApprovalFileService.uploadVacation(file);
	        
	        if (saveFileName != null) {
	            vaFiledto.setVacation_approval_file_ori_name(file.getOriginalFilename());
	            vaFiledto.setVacation_approval_file_new_name(saveFileName);
	            vaFiledto.setVacation_approval_file_size(file.getSize());
	            
	            if (vacationApprovalFileService.existsFileForVacationApproval(vapNo)) {
	                if (vacationApprovalFileService.delete(vapNo) > 0) {
	                    response.put("res_msg", "기존 파일이 삭제 되었습니다.");
	                } else {
	                    response.put("res_msg", "기존 파일 삭제 중 오류가 발생하였습니다.");
	                    return response;
	                }
	            }
	            
	            if (vacationApprovalService.updateVacationApprovalFile(vacationapprovaldto, vaFiledto, approvalFlowDtos) != null) {
	                response.put("res_code", "200");
	                response.put("res_msg", "휴가 문서를 수정하였습니다.");
	                isFileUploaded = true;
	            } else {
	                response.put("res_msg", "파일 정보 업데이트 실패");
	            }
	        } else {
	            response.put("res_msg", "파일 업로드 실패");
	        }
	    }
	    
	    // 파일이 없을 때
	    if (!isFileUploaded) {
	        if (vacationApprovalService.updateVacationApproval(vacationapprovaldto, approvalFlowDtos) != null) {
	            response.put("res_code", "200");
	            response.put("res_msg", "휴가 문서를 수정하였습니다."); 
	        }
	    }
		 
	    return response;
	}
	
	// 사용자 휴가결재 승인 
	@ResponseBody
	@PutMapping("/employee/vacationapproval/approve/{vacationapproval_no}")
	public Map<String,String> employeeVacationApprovalFlowUpdate(@PathVariable("vacationapproval_no") Long vacationApprovalNo){
		Map<String, String> response = new HashMap<>();
	    response.put("res_code", "404");
	    response.put("res_msg", "승인 중 오류가 발생하였습니다.");
	    
	    Long memberNo = memberService.getLoggedInMemberNo();
	    
	    if(vacationApprovalService.employeeVacationApprovalFlowUpdate(vacationApprovalNo, memberNo) != null) {
	    	
            response.put("res_code", "200");
            response.put("res_msg", "결재 승인이 완료되었습니다."); 	    	
	    }
	    
	    return response;
	}
	
	// 사용자 휴가결재 반려
	@ResponseBody
	@PutMapping("/employee/vacationapproval/reject/{vacationapproval_no}")
	public Map<String,String> employeeVacationApprovalFlowReject(@PathVariable("vacationapproval_no") Long vacationApprovalNo,
			@RequestBody VacationApprovalFlowDto vacationApprovalFlowDto){
		Map<String, String> response = new HashMap<>();
	    response.put("res_code", "404");
	    response.put("res_msg", "반려 중 오류가 발생하였습니다.");
	    
	    VacationApprovalDto dto = vacationApprovalService.selectVacationApprovalOne(vacationApprovalNo);
	    
	    Long memberNo = memberService.getLoggedInMemberNo();
	    
	    Long vacationtype = dto.getVacation_type_no();
	    String dateCount = dto.getVacation_approval_total_days();
	    // 휴가 갯수 복구
        MemberDto memberdto = new MemberDto();
        
        Double vacation_count = vacationService.vacationType(vacationtype);

        int dateCountInt = Integer.parseInt(dateCount);
        
        double totalVacationCount = (vacation_count * dateCountInt);

        memberdto.setMember_no(dto.getMember_no());
        memberdto.setMember_vacation_count(totalVacationCount);
        
	    if(memberService.updateOriginVacation(memberdto) != null) {
	    	response.put("res_code", "200");
	    	response.put("res_msg", "휴가 개수가 복구되었습니다.");
	    }
	    
	    
	    vacationApprovalFlowDto.setVacation_approval_no(vacationApprovalNo);
	    
	    if(vacationApprovalService.employeeVacationApprovalFlowReject(vacationApprovalFlowDto, memberNo) != null) {
	    	
            response.put("res_code", "200");
            response.put("res_msg", "결재 문서를 반려하였습니다."); 	    	
	    }
	    
	    return response;
	}
	
	// 사용자 휴가결재 승인취소
	@ResponseBody
	@PutMapping("/employee/vacationapproval/approvecancel/{vacationapproval_no}")
	public Map<String,String> employeeVacationApprovalFlowCancel(@PathVariable("vacationapproval_no") Long vacationApprovalNo){
		Map<String, String> response = new HashMap<>();
	    response.put("res_code", "404");
	    response.put("res_msg","승인 취소 중 오류가 발생하였습니다.");
	   
	    Long memberNo = memberService.getLoggedInMemberNo();
	    
	    if(vacationApprovalService.employeeVacationApprovalFlowApproveCancel(vacationApprovalNo, memberNo) != null) {
	    	
            response.put("res_code", "200");
            response.put("res_msg", "결재 승인이 취소되었습니다."); 	    	
	    }
	    
	    return response;
	}
}
