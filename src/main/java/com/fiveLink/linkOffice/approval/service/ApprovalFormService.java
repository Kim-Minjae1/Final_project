package com.fiveLink.linkOffice.approval.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.fiveLink.linkOffice.approval.domain.ApprovalForm;
import com.fiveLink.linkOffice.approval.domain.ApprovalFormDto;
import com.fiveLink.linkOffice.approval.repository.ApprovalFormRepository;

@Service
public class ApprovalFormService {
    private final ApprovalFormRepository approvalFormRepository;

    public ApprovalFormService(ApprovalFormRepository approvalFormRepository) {
        this.approvalFormRepository = approvalFormRepository;
    }

    // 관리자 전자결재 양식함 목록 조회 
    public Page<ApprovalFormDto> getAllApprovalForms(Pageable pageable, ApprovalFormDto searchdto) {
    	
        Page<ApprovalForm> forms = null;
        
        String searchText = searchdto.getSearch_text();
        if(searchText != null && "".equals(searchText) == false) {
        	forms = approvalFormRepository.findByaprovalFormTitleContaining(searchText, pageable, 1L);
        } else {
        	forms = approvalFormRepository.findAllByApprovalFormStatusNot(pageable,1L);
        }
        
        List<ApprovalFormDto> approvalFormDtoList = new ArrayList<ApprovalFormDto>();
        for(ApprovalForm af : forms) {
        	ApprovalFormDto dto = af.toDto();
        	approvalFormDtoList.add(dto);
        }
        
        return new PageImpl<>(approvalFormDtoList, pageable, forms.getTotalElements());
    }
    
    // 관리자 전자결재 양식함 상세 조회 
    public ApprovalFormDto getApprovalFormOne(Long formNo){
    	ApprovalForm origin = approvalFormRepository.findByApprovalFormNo(formNo);
    	return origin.toDto();
    }
    
    // 관리자 전자결재 양식 등록
    public ApprovalForm saveApprovalForm(ApprovalFormDto dto) {
        ApprovalForm approvalForm = dto.toEntity();
        return approvalFormRepository.save(approvalForm);
    }
    
    // 관리자 전자결재 양식 수정
    public ApprovalForm editApprovalForm(ApprovalFormDto dto) {
    	ApprovalFormDto temp = getApprovalFormOne(dto.getApproval_form_no());
    	temp.setApproval_form_title(dto.getApproval_form_title());
    	temp.setApproval_form_content(dto.getApproval_form_content());
    	temp.setApproval_form_status(0L);
    	
    	ApprovalForm approvalform = temp.toEntity();
    	ApprovalForm result = approvalFormRepository.save(approvalform);
    	
    	return result;
    }
    
    // 관리자 전자결재 양식 삭제(update)
    public ApprovalForm deleteApprovalForm(ApprovalFormDto dto) {
    	ApprovalForm approvalform = dto.toEntity();
    	ApprovalForm result = approvalFormRepository.save(approvalform);
    	return result;
    }
    
    // 양식 조회 (사용자 전자결재 등록 페이지) 
    public List<ApprovalFormDto> findAllByApprovalStatusNot(Long approvalStatus) {
    	
        List<ApprovalForm> approvals = approvalFormRepository.findAllByApprovalFormStatusNot(approvalStatus);
        
        return approvals.stream()
                        .map(ApprovalForm::toDto)
                        .collect(Collectors.toList());
    }
    
}