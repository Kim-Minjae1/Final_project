package com.fiveLink.linkOffice.approval.controller;

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

import com.fiveLink.linkOffice.approval.domain.ApprovalDto;
import com.fiveLink.linkOffice.approval.domain.ApprovalFileDto;
import com.fiveLink.linkOffice.approval.domain.ApprovalFlowDto;
import com.fiveLink.linkOffice.approval.domain.ApprovalFormDto;
import com.fiveLink.linkOffice.approval.service.ApprovalFileService;
import com.fiveLink.linkOffice.approval.service.ApprovalFormService;
import com.fiveLink.linkOffice.approval.service.ApprovalService;
import com.fiveLink.linkOffice.member.domain.MemberDto;
import com.fiveLink.linkOffice.member.service.MemberService;
import com.fiveLink.linkOffice.vacationapproval.domain.VacationApprovalDto;
import com.fiveLink.linkOffice.vacationapproval.domain.VacationApprovalFileDto;
import com.fiveLink.linkOffice.vacationapproval.domain.VacationApprovalFlowDto;
import com.fiveLink.linkOffice.vacationapproval.service.VacationApprovalService;

@Controller
public class ApprovalViewController {

	private final MemberService memberService;
	private final ApprovalFormService approvalFormService;
	private final VacationApprovalService vacationApprovalService;
	private final ApprovalService approvalService;
	private final ApprovalFileService approvalFileService;

	@Autowired
	public ApprovalViewController(MemberService memberService, ApprovalFormService approvalFormService,
			VacationApprovalService vacationApprovalService, ApprovalService approvalService, ApprovalFileService approvalFileService) {
		this.memberService = memberService;
		this.approvalFormService = approvalFormService;
		this.vacationApprovalService = vacationApprovalService;
		this.approvalService = approvalService;
		this.approvalFileService = approvalFileService;
	}

	// 관리자 전자결재 양식 등록 페이지
	@GetMapping("/admin/approval/create")
	public String adminApprovalCreate(Model model) {
		Long member_no = memberService.getLoggedInMemberNo();
		List<MemberDto> memberdto = memberService.getMembersByNo(member_no);

		model.addAttribute("memberdto", memberdto);

		return "admin/approval/approval_create";
	}
	
	// 양식 정렬
	private Sort getSortOption(String sort) {
		if ("latest".equals(sort)) {
			return Sort.by(Sort.Order.desc("approvalFormCreateDate"));
		} else if ("oldest".equals(sort)) {
			return Sort.by(Sort.Order.asc("approvalFormCreateDate"));
		}
		return Sort.by(Sort.Order.desc("approvalFormCreateDate"));
	}

	// 관리자 전자결재 양식함 페이지
	@GetMapping("/admin/approval/form")
	public String adminApprovalForm(Model model, ApprovalFormDto searchdto,
			@PageableDefault(size = 10, sort = "approvalFormCreateDate", direction = Sort.Direction.DESC) Pageable pageable,
			@RequestParam(value = "sort", defaultValue = "latest") String sort) {
		Long member_no = memberService.getLoggedInMemberNo();
		List<MemberDto> memberdto = memberService.getMembersByNo(member_no);

		Sort sortOption = getSortOption(sort);
		Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortOption);

		Page<ApprovalFormDto> formList = approvalFormService.getAllApprovalForms(sortedPageable, searchdto);

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		formList.forEach(form -> {
			if (form.getApproval_form_create_date() != null) {
				String fomattedCreateDate = form.getApproval_form_create_date().format(formatter);
				form.setFormat_create_date(fomattedCreateDate);
			}
		});

		model.addAttribute("memberdto", memberdto);
		model.addAttribute("formList", formList.getContent());
		model.addAttribute("page", formList);
		model.addAttribute("searchDto", searchdto);
		model.addAttribute("currentSort", sort);

		return "/admin/approval/approval_form";
	}

	// 관리자 전자결재 양식 상세 페이지
	@GetMapping("/admin/approval/detail/{form_no}")
	public String adminApprovalDetail(Model model, @PathVariable("form_no") Long formNo) {
		Long member_no = memberService.getLoggedInMemberNo();
		List<MemberDto> memberdto = memberService.getMembersByNo(member_no);

		ApprovalFormDto formList = approvalFormService.getApprovalFormOne(formNo);

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		if (formList.getApproval_form_create_date() != null) {
			String formattedCreateDate = formList.getApproval_form_create_date().format(formatter);
			formList.setFormat_create_date(formattedCreateDate);
		}

		model.addAttribute("memberdto", memberdto);
		model.addAttribute("formList", formList);
		return "/admin/approval/approval_detail";
	}

	// 관리자 전자결재 양식 수정 페이지
	@GetMapping("/admin/approval/edit/{form_no}")
	public String adminApprovalEdit(Model model, @PathVariable("form_no") Long formNo) {
		Long member_no = memberService.getLoggedInMemberNo();
		List<MemberDto> memberdto = memberService.getMembersByNo(member_no);

		ApprovalFormDto formList = approvalFormService.getApprovalFormOne(formNo);
		
		model.addAttribute("memberdto", memberdto);
		model.addAttribute("formList", formList);

		return "admin/approval/approval_edit";
	}
	
	// 사용자
	private Sort getSortApprovalReferences(String sort) {
		if ("latest".equals(sort)) {
			return Sort.by(Sort.Order.desc("approval_date"));
		} else if ("oldest".equals(sort)) {
			return Sort.by(Sort.Order.asc("approval_date"));
		}
		return Sort.by(Sort.Order.desc("approval_date"));
	}
	
	// 사용자 전자결재 내역함 
	@GetMapping("/employee/approval/history")
	public String approvalHistory(Model model,ApprovalDto searchdto, @RequestParam(value = "sort", defaultValue = "latest") String sort, @PageableDefault(size = 10, sort = "approval_date", direction = Sort.Direction.DESC) Pageable pageable) {
	    Long memberNo = memberService.getLoggedInMemberNo();
	    List<MemberDto> memberdto = memberService.getMembersByNo(memberNo);
	    
	    Sort sortOption = getSortApprovalReferences(sort);
	    Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortOption);
	    
	    Page<ApprovalDto> approvals = approvalService.getAllApprovalHistory(memberNo, searchdto, sortedPageable);
	    
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		approvals.forEach(vapp -> {
			if (vapp.getApproval_create_date() != null) {
				String formattedCreateDate = vapp.getApproval_create_date().format(formatter);
				vapp.setFormat_approval_create_date(formattedCreateDate);
			}
		});
		
	    model.addAttribute("approvals", approvals.getContent());
	    model.addAttribute("page", approvals);
	    model.addAttribute("searchDto", searchdto);
	    model.addAttribute("memberdto", memberdto);
	    model.addAttribute("currentSort", sort);
	    return "employee/approval/approval_history_list";
	}

	
	// 사용자 전자결재 참조함
	@GetMapping("/employee/approval/references")
	public String approvalReferences(Model model,ApprovalDto searchdto,@RequestParam(value = "sort", defaultValue = "latest") String sort, @PageableDefault(size = 10, sort = "approval_date", direction = Sort.Direction.DESC) Pageable pageable) {
	    Long memberNo = memberService.getLoggedInMemberNo();
	    List<MemberDto> memberdto = memberService.getMembersByNo(memberNo);
	    
	    Sort sortOption = getSortApprovalReferences(sort);
	    Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortOption);
	    
	    Page<ApprovalDto> approvals = approvalService.getAllApprovalReferences(memberNo, searchdto, sortedPageable);

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		approvals.forEach(vapp -> {
			if (vapp.getApproval_create_date() != null) {
				String formattedCreateDate = vapp.getApproval_create_date().format(formatter);
				vapp.setFormat_approval_create_date(formattedCreateDate);
			}
		});
		
	    model.addAttribute("approvals", approvals.getContent());
	    model.addAttribute("page", approvals);
	    model.addAttribute("searchDto", searchdto);
	    model.addAttribute("memberdto", memberdto);
	    model.addAttribute("currentSort", sort);
	    return "employee/approval/approval_references_list";
	}
	
	// 사용자 결재 진행, 반려 정렬
	private Sort getSortApproval(String sort) {
		if ("latest".equals(sort)) {
			return Sort.by(Sort.Order.desc("approvalCreateDate"));
		} else if ("oldest".equals(sort)) {
			return Sort.by(Sort.Order.asc("approvalCreateDate"));
		}
		return Sort.by(Sort.Order.desc("approvalCreateDate"));
	}

	
	// 사용자 결재 진행함 페이지
	@GetMapping("/employee/approval/progress")
	public String approvalProgress(Model model, ApprovalDto searchdto,
			@PageableDefault(size = 10, sort = "approvalCreateDate", direction = Sort.Direction.DESC) Pageable pageable,
			@RequestParam(value = "sort", defaultValue = "latest") String sort) {
		
		Long member_no = memberService.getLoggedInMemberNo();
		List<MemberDto> memberdto = memberService.getMembersByNo(member_no);
		
		Sort sortOption = getSortApproval(sort);
		Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortOption);

		Page<ApprovalDto> ApprovalDtoPage = approvalService.getAllApproval(member_no, searchdto, sortedPageable);
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		ApprovalDtoPage.getContent().forEach(vapp -> {
		    if (vapp.getApproval_create_date() != null) {  
		        String formattedCreateDate = vapp.getApproval_create_date().format(formatter); 
		        vapp.setFormat_approval_create_date(formattedCreateDate); 
		    }
		});
		
		model.addAttribute("memberdto", memberdto);
		model.addAttribute("approvalDtoList", ApprovalDtoPage.getContent());
		model.addAttribute("page", ApprovalDtoPage);
		model.addAttribute("searchDto", searchdto);
		model.addAttribute("currentSort", sort);

		return "employee/approval/approval_progress_list";
	}

	// 사용자 결재 반려함 페이지
	@GetMapping("/employee/approval/reject")
	public String approvalReject(Model model, ApprovalDto searchdto,
			@PageableDefault(size = 10, sort = "approvalCreateDate", direction = Sort.Direction.DESC) Pageable pageable,
			@RequestParam(value = "sort", defaultValue = "latest") String sort) {
		Long member_no = memberService.getLoggedInMemberNo();
		List<MemberDto> memberdto = memberService.getMembersByNo(member_no);
		
		Sort sortOption = getSortApproval(sort);
		Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortOption);

		Page<ApprovalDto> ApprovalDtoPage = approvalService.getAllReject(member_no, searchdto, sortedPageable);
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		ApprovalDtoPage.getContent().forEach(vapp -> {
		    if (vapp.getApproval_create_date() != null) {  
		        String formattedCreateDate = vapp.getApproval_create_date().format(formatter); 
		        vapp.setFormat_approval_create_date(formattedCreateDate); 
		    }
		});
		
		model.addAttribute("memberdto", memberdto);
		model.addAttribute("approvalDtoList", ApprovalDtoPage.getContent());
		model.addAttribute("page", ApprovalDtoPage);
		model.addAttribute("searchDto", searchdto);
		model.addAttribute("currentSort", sort);
		model.addAttribute("memberdto", memberdto);

		return "employee/approval/approval_reject_list";
	}

	// 사용자 결재 (휴가) 내역함 상세 페이지
	@GetMapping("/employee/approval/approval_history_vacation_detail/{vacationapproval_no}")
	public String approvalHistoryVacationDetail(Model model, @PathVariable("vacationapproval_no") Long vacationApprovalNo) {

		Long member_no = memberService.getLoggedInMemberNo();
		List<MemberDto> memberdto = memberService.getMembersByNo(member_no);

		VacationApprovalDto vacationapprovaldto = vacationApprovalService.selectVacationApprovalOne(vacationApprovalNo);

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		if (vacationapprovaldto.getVacation_approval_create_date() != null) {
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
		
		model.addAttribute("memberdto", memberdto);
		model.addAttribute("vacationapprovaldto", vacationapprovaldto);
		model.addAttribute("currentUserMemberNo", member_no);

		return "employee/approval/approval_history_vacation_detail";
	}
	
	// 사용자 결재 (결재) 내역함 상세 페이지
		@GetMapping("/employee/approval/approval_history_detail/{approval_no}")
		public String approvalHistoryDetail(Model model, @PathVariable("approval_no") Long appNo) {

			Long member_no = memberService.getLoggedInMemberNo();
			List<MemberDto> memberdto = memberService.getMembersByNo(member_no);

			ApprovalDto approvaldto = approvalService.selectApprovalOne(appNo);

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			if (approvaldto.getApproval_create_date() != null) {
				String formattedCreateDate = approvaldto.getApproval_create_date().format(formatter);
				approvaldto.setFormat_approval_create_date(formattedCreateDate);
			}

			if (approvaldto.getFlows() != null) {
				for (ApprovalFlowDto flow : approvaldto.getFlows()) {
					if (flow.getApproval_flow_complete_date() != null) {
						String formattedCompleteDate = flow.getApproval_flow_complete_date().format(formatter);
						flow.setFormat_approval_flow_complete_date(formattedCompleteDate);
					}

					MemberDto currentMember = memberService.selectMemberOne(flow.getMember_no());
					flow.setDigital_name(currentMember.getMember_new_digital_img());

				}
			}
			
			if(approvaldto.getFiles() != null) {
				for(ApprovalFileDto file : approvaldto.getFiles()) {
					Long fileSize = file.getApproval_file_size();
					file.setApproval_file_size(fileSize / 1024);
				}
			}
			
			model.addAttribute("memberdto", memberdto);
			model.addAttribute("approvaldto", approvaldto);
			model.addAttribute("currentUserMemberNo", member_no);

			return "employee/approval/approval_history_detail";
		}
	
	// 사용자 결재 참조함 상세 페이지
	@GetMapping("/employee/approval/approval_references_vacation_detail/{vacationapproval_no}")
	public String approvalReferencesVacationDetail(Model model, @PathVariable("vacationapproval_no") Long vacationApprovalNo) {

		Long member_no = memberService.getLoggedInMemberNo();
		List<MemberDto> memberdto = memberService.getMembersByNo(member_no);

		VacationApprovalDto vacationapprovaldto = vacationApprovalService.selectVacationApprovalOne(vacationApprovalNo);

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		if (vacationapprovaldto.getVacation_approval_create_date() != null) {
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

		model.addAttribute("memberdto", memberdto);
		model.addAttribute("vacationapprovaldto", vacationapprovaldto);
		model.addAttribute("currentUserMemberNo", member_no);

		return "employee/approval/approval_references_vacation_detail";
	}
	
	// 사용자 결재 (결재) 내역함 상세 페이지
			@GetMapping("/employee/approval/approval_references_detail/{approval_no}")
			public String approvalReferencesDetail(Model model, @PathVariable("approval_no") Long appNo) {

				Long member_no = memberService.getLoggedInMemberNo();
				List<MemberDto> memberdto = memberService.getMembersByNo(member_no);

				ApprovalDto approvaldto = approvalService.selectApprovalOne(appNo);

				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
				if (approvaldto.getApproval_create_date() != null) {
					String formattedCreateDate = approvaldto.getApproval_create_date().format(formatter);
					approvaldto.setFormat_approval_create_date(formattedCreateDate);
				}

				if (approvaldto.getFlows() != null) {
					for (ApprovalFlowDto flow : approvaldto.getFlows()) {
						if (flow.getApproval_flow_complete_date() != null) {
							String formattedCompleteDate = flow.getApproval_flow_complete_date().format(formatter);
							flow.setFormat_approval_flow_complete_date(formattedCompleteDate);
						}

						MemberDto currentMember = memberService.selectMemberOne(flow.getMember_no());
						flow.setDigital_name(currentMember.getMember_new_digital_img());

					}
				}
				
				if(approvaldto.getFiles() != null) {
					for(ApprovalFileDto file : approvaldto.getFiles()) {
						Long fileSize = file.getApproval_file_size();
						file.setApproval_file_size(fileSize / 1024);
					}
				}

				model.addAttribute("memberdto", memberdto);
				model.addAttribute("approvaldto", approvaldto);
				model.addAttribute("currentUserMemberNo", member_no);

				return "employee/approval/approval_references_detail";
			}
	
	// 사용자 결재 진행함 상세 페이지
	@GetMapping("/employee/approval/approval_progress_detail/{approval_no}")
	public String approvalProgressDetail(Model model, @PathVariable("approval_no") Long appNo) {

		Long member_no = memberService.getLoggedInMemberNo();
		List<MemberDto> memberdto = memberService.getMembersByNo(member_no);

		ApprovalDto approvaldto = approvalService.selectApprovalOne(appNo);

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		if (approvaldto.getApproval_create_date() != null) {
			String formattedCreateDate = approvaldto.getApproval_create_date().format(formatter);
			approvaldto.setFormat_approval_create_date(formattedCreateDate);
		}

		if (approvaldto.getFlows() != null) {
			for (ApprovalFlowDto flow : approvaldto.getFlows()) {
				if (flow.getApproval_flow_complete_date() != null) {
					String formattedCompleteDate = flow.getApproval_flow_complete_date().format(formatter);
					flow.setFormat_approval_flow_complete_date(formattedCompleteDate);
				}

				MemberDto currentMember = memberService.selectMemberOne(flow.getMember_no());
				flow.setDigital_name(currentMember.getMember_new_digital_img());

			}
		}
		
		if(approvaldto.getFiles() != null) {
			for(ApprovalFileDto file : approvaldto.getFiles()) {
				Long fileSize = file.getApproval_file_size();
				file.setApproval_file_size(fileSize / 1024);
			}
		}

		model.addAttribute("memberdto", memberdto);
		model.addAttribute("approvaldto", approvaldto);
		model.addAttribute("currentUserMemberNo", member_no);

		return "employee/approval/approval_progress_detail";
	}
	
	// 사용자 결재 반려함 상세 페이지
	@GetMapping("/employee/approval/approval_reject_detail/{approval_no}")
	public String approvalRejectDetail(Model model, @PathVariable("approval_no") Long appNo) {

		Long member_no = memberService.getLoggedInMemberNo();
		List<MemberDto> memberdto = memberService.getMembersByNo(member_no);

		ApprovalDto approvaldto = approvalService.selectApprovalOne(appNo);

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		if (approvaldto.getApproval_create_date() != null) {
			String formattedCreateDate = approvaldto.getApproval_create_date().format(formatter);
			approvaldto.setFormat_approval_create_date(formattedCreateDate);
		}

		if (approvaldto.getFlows() != null) {
			for (ApprovalFlowDto flow : approvaldto.getFlows()) {
				if (flow.getApproval_flow_complete_date() != null) {
					String formattedCompleteDate = flow.getApproval_flow_complete_date().format(formatter);
					flow.setFormat_approval_flow_complete_date(formattedCompleteDate);
				}

				MemberDto currentMember = memberService.selectMemberOne(flow.getMember_no());
				flow.setDigital_name(currentMember.getMember_new_digital_img());

			}
		}
		
		if(approvaldto.getFiles() != null) {
			for(ApprovalFileDto file : approvaldto.getFiles()) {
				Long fileSize = file.getApproval_file_size();
				file.setApproval_file_size(fileSize / 1024);
			}
		}

		model.addAttribute("memberdto", memberdto);
		model.addAttribute("approvaldto", approvaldto);
		model.addAttribute("currentUserMemberNo", member_no);

		return "employee/approval/approval_reject_detail";
	}
	
	// 사용자 전자결재 작성 페이지 
	@GetMapping("/employee/approval/create")
	public String approvalCreate(Model model) {
		
		Long member_no = memberService.getLoggedInMemberNo();
		List<MemberDto> memberdto = memberService.getMembersByNo(member_no);
		
		List<ApprovalFormDto> formList = approvalFormService.findAllByApprovalStatusNot(1L);
		model.addAttribute("memberdto", memberdto);
		model.addAttribute("formList", formList);
		
		return "employee/approval/approval_create";
	}
	
	// 사용자 전자결재 수정 페이지
	@GetMapping("/employee/approval/approval_edit/{approval_no}")
	public String approvalEdit(Model model, @PathVariable("approval_no") Long appNo) {
		Long member_no = memberService.getLoggedInMemberNo();
		List<MemberDto> memberdto = memberService.getMembersByNo(member_no);
		
		ApprovalDto approvaldto = approvalService.selectApprovalOne(appNo);
		model.addAttribute("memberdto", memberdto);
		model.addAttribute("approvaldto", approvaldto);
		
		return "employee/approval/approval_edit";
	}
	
	// 전자결재 수정 결재자 값 (js)
	@GetMapping("/employee/approval/approve/{approval_no}")
	@ResponseBody
	public  Map<String, Object> approvalEdit(@PathVariable("approval_no") Long appNo) {
		Map<String, Object> response = new HashMap<>();
		
		ApprovalDto approvaldto = approvalService.selectApprovalOne(appNo);
		response.put("approvaldto", approvaldto);
		
		return response;
	}
	
	// 전자결재 파일 다운로드 
	@GetMapping("/download_file/{approval_no}")
	public ResponseEntity<Object> noticeImgDownload(@PathVariable("approval_no")Long appNo){
		return approvalFileService.download(appNo);
	}
	
}
