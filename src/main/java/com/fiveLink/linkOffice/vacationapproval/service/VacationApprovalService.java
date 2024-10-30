package com.fiveLink.linkOffice.vacationapproval.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fiveLink.linkOffice.mapper.NoficationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.fiveLink.linkOffice.member.domain.Member;
import com.fiveLink.linkOffice.member.repository.MemberRepository;
import com.fiveLink.linkOffice.vacation.domain.VacationType;
import com.fiveLink.linkOffice.vacation.repository.VacationTypeRepository;
import com.fiveLink.linkOffice.vacationapproval.domain.VacationApproval;
import com.fiveLink.linkOffice.vacationapproval.domain.VacationApprovalDto;
import com.fiveLink.linkOffice.vacationapproval.domain.VacationApprovalFile;
import com.fiveLink.linkOffice.vacationapproval.domain.VacationApprovalFileDto;
import com.fiveLink.linkOffice.vacationapproval.domain.VacationApprovalFlow;
import com.fiveLink.linkOffice.vacationapproval.domain.VacationApprovalFlowDto;
import com.fiveLink.linkOffice.vacationapproval.repository.VacationApprovalFileRepository;
import com.fiveLink.linkOffice.vacationapproval.repository.VacationApprovalFlowRepository;
import com.fiveLink.linkOffice.vacationapproval.repository.VacationApprovalRepository;

import jakarta.transaction.Transactional;

@Service
public class VacationApprovalService {
	
	private final VacationApprovalRepository vacationApprovalRepository;
	private final MemberRepository memberRepository;
	private final VacationTypeRepository vacationTypeRepository;
	private final VacationApprovalFileRepository vacationApprovalFileRepository;
	private final VacationApprovalFlowRepository vacationApprovalFlowRepository;
	//[김채영] 테스트 알림 매퍼
	private final NoficationMapper noficationMapper;
	
	@Autowired
	public VacationApprovalService(VacationApprovalRepository vacationApprovalRepository,MemberRepository memberRepository,VacationTypeRepository vacationTypeRepository, VacationApprovalFileRepository vacationApprovalFileRepository, VacationApprovalFlowRepository vacationApprovalFlowRepository, NoficationMapper noficationMapper) {
        this.vacationApprovalRepository = vacationApprovalRepository;
        this.memberRepository = memberRepository;
        this.vacationTypeRepository = vacationTypeRepository;
        this.vacationApprovalFileRepository = vacationApprovalFileRepository;
        this.vacationApprovalFlowRepository = vacationApprovalFlowRepository;
		this.noficationMapper = noficationMapper;
    }
	
	// 사용자 휴가신청함 목록 조회
	public Page<VacationApprovalDto> getVacationApprovalByNo(Long memberNo,VacationApprovalDto searchdto, Pageable pageable) {
    	
        Page<VacationApproval> forms = null;
        
        String searchText = searchdto.getSearch_text();
        if(searchText != null && "".equals(searchText) == false) {
        	int searchType = searchdto.getSearch_type();
        	
        	switch(searchType) {
        	// 전체 검색
        		case 1 :
        			forms = vacationApprovalRepository.findByMemberMemberNoAndVacationApprovalTitleAndApprovalStatus(memberNo,searchText, pageable);;
        			break;
        	// 제목 검색
        		case 2 :
        			forms = vacationApprovalRepository.findByMemberMemberNoAndVacationApprovalTitleContaining(memberNo,searchText, pageable);
        			break;
        	// 상태 검색
        		case 3 :
        			forms = vacationApprovalRepository.findByMemberNoAndApprovalStatus(memberNo,searchText, pageable);;
        			break;
        	}
        } else {
        	forms = vacationApprovalRepository.findAllByMemberMemberNo(memberNo,pageable);
        }
        
        List<VacationApprovalDto> approvalApprovalDtoList = new ArrayList<VacationApprovalDto>();
        for(VacationApproval va : forms) {
        	VacationApprovalDto dto = va.toDto();
        	approvalApprovalDtoList.add(dto);
        }
        
        return new PageImpl<>(approvalApprovalDtoList, pageable, forms.getTotalElements());
    }
	
	// 사용자 휴가신청함 상세 조회 
	@Transactional
	public VacationApprovalDto selectVacationApprovalOne(Long vacationApprovalNo) {
	    VacationApproval origin = vacationApprovalRepository.findByVacationApprovalNo(vacationApprovalNo);
	    
	    VacationApprovalDto dto = origin.toDto();
	    
	    // 사원의 서명 정보를 dto에 추가
	    if (origin.getMember() != null) {
	        dto.setDigitalname(origin.getMember().getMemberNewDigitalImg());
	    }

	    List<VacationApprovalFile> files = vacationApprovalFileRepository.findByVacationApproval(origin);
	    List<VacationApprovalFlow> flows = vacationApprovalFlowRepository.findByVacationApproval(origin);
	    
	    List<VacationApprovalFileDto> fileDtos = files.stream()
	        .map(VacationApprovalFile::toDto)
	        .collect(Collectors.toList());
	    
	    List<VacationApprovalFlowDto> flowsDtos = flows.stream()
	        .map(VacationApprovalFlow::toDto)
	        .collect(Collectors.toList());
	    
	    dto.setFiles(fileDtos);
	    dto.setFlows(flowsDtos);
	    
	    return dto;
	}

	
	// 사용자 휴가 신청 (파일 X)
	public VacationApproval createVacationApproval(VacationApprovalDto vappdto, List<VacationApprovalFlowDto> approvalFlowDtos) {
	    
	    Member member = memberRepository.findByMemberNo(vappdto.getMember_no());
	    
	    VacationType vacationType = vacationTypeRepository.findByvacationTypeNo(vappdto.getVacation_type_no());
	    
	    VacationApproval vapp = vappdto.toEntity(member, vacationType);
	    VacationApproval savedVapp = vacationApprovalRepository.save(vapp); 
	    
	    for (VacationApprovalFlowDto flowDto : approvalFlowDtos) {
	    	
	        Long approverMemberNo = flowDto.getMember_no();
	        
	        Member memberFlow = memberRepository.findByMemberNo(approverMemberNo);
	    	
	    	VacationApprovalFlow vaf = flowDto.toEntity(vapp, memberFlow);
	    	
	    	vacationApprovalFlowRepository.save(vaf);
	    }
	    
	    return savedVapp; 
	}

	// 사용자 휴가 신청 (파일 O)
	public VacationApproval createVacationApprovalFile(VacationApprovalDto vappdto, VacationApprovalFileDto vaFiledto, List<VacationApprovalFlowDto> approvalFlowDtos) {
		
		Member member = memberRepository.findByMemberNo(vappdto.getMember_no());
		VacationType vacationType = vacationTypeRepository.findByvacationTypeNo(vappdto.getVacation_type_no());
		
		VacationApproval vapp = vappdto.toEntity(member, vacationType);
		
		VacationApproval savedVapp = vacationApprovalRepository.save(vapp);
	    for (VacationApprovalFlowDto flowDto : approvalFlowDtos) {
	    	
	    	Long approverMemberNo = flowDto.getMember_no();
	        
	        Member memberFlow = memberRepository.findByMemberNo(approverMemberNo);
	        
	    	VacationApprovalFlow vaf = flowDto.toEntity(vapp, memberFlow);
	    	
	    	vacationApprovalFlowRepository.save(vaf);
	    }
	    
		 if (vaFiledto != null) {
		        VacationApprovalFile vaFile = vaFiledto.toEntity(savedVapp);
		        vacationApprovalFileRepository.save(vaFile); 
		    }
		return vacationApprovalRepository.save(vapp);
		
	}
	
	// 휴가 결재 기안 취소
	public VacationApproval cancelVacationApproval(VacationApprovalDto dto) {
		
		Member member = memberRepository.findByMemberNo(dto.getMember_no());
		VacationType vacationType = vacationTypeRepository.findByvacationTypeNo(dto.getVacation_type_no());
		
		VacationApproval va = dto.toEntity(member, vacationType);
		
		VacationApproval result = vacationApprovalRepository.save(va);
		
		return result;
	}

	// 휴가 결재 수정 (파일 있을 때)
	@Transactional
	public VacationApproval updateVacationApprovalFile(
	        VacationApprovalDto vappdto,
	        VacationApprovalFileDto vaFiledto,
	        List<VacationApprovalFlowDto> approvalFlowDtos) {

	    VacationApproval existingVapp = vacationApprovalRepository.findByVacationApprovalNo(vappdto.getVacation_approval_no());

	    existingVapp.setVacationApprovalTitle(vappdto.getVacation_approval_title());
	    existingVapp.setVacationType(vacationTypeRepository.findByvacationTypeNo(vappdto.getVacation_type_no()));
	    existingVapp.setVacationApprovalStartDate(vappdto.getVacation_approval_start_date());
	    existingVapp.setVacationApprovalEndDate(vappdto.getVacation_approval_end_date());
	    existingVapp.setVacationApprovalTotalDays(vappdto.getVacation_approval_total_days());
	    existingVapp.setVacationApprovalContent(vappdto.getVacation_approval_content());
	    existingVapp.setMember(memberRepository.findByMemberNo(vappdto.getMember_no()));

	    vacationApprovalRepository.save(existingVapp);
	    
	    if (!approvalFlowDtos.isEmpty()) {
	        vacationApprovalFlowRepository.deleteByVacationApproval(existingVapp);

	        for (VacationApprovalFlowDto flowDto : approvalFlowDtos) {
	            Long approverMemberNo = flowDto.getMember_no();
	            Member memberFlow = memberRepository.findByMemberNo(approverMemberNo);
	            VacationApprovalFlow vaf = flowDto.toEntity(existingVapp, memberFlow);
	            vacationApprovalFlowRepository.save(vaf);
	        }
	    }

	    if (vaFiledto != null) {
	        List<VacationApprovalFile> existingFiles = vacationApprovalFileRepository.findByVacationApproval(existingVapp);
	        if (!existingFiles.isEmpty()) {
	            for (VacationApprovalFile file : existingFiles) {
	                vacationApprovalFileRepository.delete(file);
	            }
	        }
	        VacationApprovalFile vaFile = vaFiledto.toEntity(existingVapp);
	        vacationApprovalFileRepository.save(vaFile);
	    }

	    return existingVapp;
	}
	
	// 휴가 결재 수정 (파일 없을 때)
	@Transactional
	public VacationApproval updateVacationApproval(
			VacationApprovalDto vappdto,
			List<VacationApprovalFlowDto> approvalFlowDtos) {
		
		VacationApproval existingVapp = vacationApprovalRepository.findByVacationApprovalNo(vappdto.getVacation_approval_no());
		
		existingVapp.setVacationApprovalTitle(vappdto.getVacation_approval_title());
		existingVapp.setVacationType(vacationTypeRepository.findByvacationTypeNo(vappdto.getVacation_type_no()));
		existingVapp.setVacationApprovalStartDate(vappdto.getVacation_approval_start_date());
		existingVapp.setVacationApprovalEndDate(vappdto.getVacation_approval_end_date());
		existingVapp.setVacationApprovalTotalDays(vappdto.getVacation_approval_total_days());
		existingVapp.setVacationApprovalContent(vappdto.getVacation_approval_content());
		existingVapp.setMember(memberRepository.findByMemberNo(vappdto.getMember_no()));
		
		vacationApprovalRepository.save(existingVapp);
		
	    if (!approvalFlowDtos.isEmpty()) {
	        vacationApprovalFlowRepository.deleteByVacationApproval(existingVapp);

	        for (VacationApprovalFlowDto flowDto : approvalFlowDtos) {
	            Long approverMemberNo = flowDto.getMember_no();
	            Member memberFlow = memberRepository.findByMemberNo(approverMemberNo);
	            VacationApprovalFlow vaf = flowDto.toEntity(existingVapp, memberFlow);
	            vacationApprovalFlowRepository.save(vaf);
	        }
	    }
		
		return existingVapp;
	}
	
	// 휴가 결재 승인 
	 @Transactional
	    public VacationApproval employeeVacationApprovalFlowUpdate(Long vacationApprovalNo, Long memberNo) {
	        VacationApproval vacationApproval = vacationApprovalRepository.findById(vacationApprovalNo).orElse(null);

	        List<VacationApprovalFlow> approvalFlows = vacationApprovalFlowRepository.findByVacationApproval(vacationApproval);

	        VacationApprovalFlow currentFlow = approvalFlows.stream()
	            .filter(flow -> flow.getMember().getMemberNo().equals(memberNo))
	            .findFirst()
	            .orElse(null);

	        currentFlow.setVacationApprovalFlowStatus(2L); 
	        vacationApprovalFlowRepository.save(currentFlow);

	        VacationApprovalFlow nextFlow = approvalFlows.stream()
	            .filter(flow -> flow.getVacationApprovalFlowOrder() != null)
	            .filter(flow -> flow.getVacationApprovalFlowOrder() > currentFlow.getVacationApprovalFlowOrder())
	            .findFirst()
	            .orElse(null);

	        if (nextFlow != null) {
	            nextFlow.setVacationApprovalFlowStatus(1L);
	            vacationApprovalFlowRepository.save(nextFlow);
	        } else {
	            vacationApproval.setVacationApprovalStatus(1L);
	            vacationApprovalRepository.save(vacationApproval);
	        }

	        return vacationApproval;
	    }
	 
	 // 휴가 결재 반려
	 @Transactional
	    public VacationApproval employeeVacationApprovalFlowReject(VacationApprovalFlowDto vacationApprovalFlowDto, Long memberNo) {
	        VacationApproval vacationApproval = vacationApprovalRepository.findById(vacationApprovalFlowDto.getVacation_approval_no()).orElse(null);

	        List<VacationApprovalFlow> approvalFlows = vacationApprovalFlowRepository.findByVacationApproval(vacationApproval);
	        
	        VacationApprovalFlow currentFlow = approvalFlows.stream()
	            .filter(flow -> flow.getMember().getMemberNo().equals(memberNo))
	            .findFirst()
	            .orElse(null);

	        if (currentFlow != null) {
	            currentFlow.setVacationApprovalFlowStatus(3L);
	            currentFlow.setVacationApprovalFlowRejectReason(vacationApprovalFlowDto.getVacation_approval_flow_reject_reason()); 
	            vacationApprovalFlowRepository.save(currentFlow); 
	        }

	        vacationApproval.setVacationApprovalStatus(2L); 
	        vacationApprovalRepository.save(vacationApproval); 

	        return vacationApproval;
	    }
	 
	 // 휴가 결재 승인 취소
	 @Transactional
	 public VacationApproval employeeVacationApprovalFlowApproveCancel(Long vacationApprovalNo, Long memberNo) {
		 
		 VacationApproval vacationApproval = vacationApprovalRepository.findById(vacationApprovalNo).orElse(null);

	        List<VacationApprovalFlow> approvalFlows = vacationApprovalFlowRepository.findByVacationApproval(vacationApproval);

	        VacationApprovalFlow currentFlow = approvalFlows.stream()
	            .filter(flow -> flow.getMember().getMemberNo().equals(memberNo))
	            .findFirst()
	            .orElse(null);

	        currentFlow.setVacationApprovalFlowStatus(1L); 
	        vacationApprovalFlowRepository.save(currentFlow);

	        VacationApprovalFlow nextFlow = approvalFlows.stream()
	            .filter(flow -> flow.getVacationApprovalFlowOrder() != null)
	            .filter(flow -> flow.getVacationApprovalFlowOrder() > currentFlow.getVacationApprovalFlowOrder())
	            .findFirst()
	            .orElse(null);

	        if (nextFlow != null) {
	            nextFlow.setVacationApprovalFlowStatus(0L);
	            vacationApprovalFlowRepository.save(nextFlow);
	        } 

	        return vacationApproval;
	 }
	 // 결재 흐름 조회
	 @Transactional
	 public List<VacationApprovalFlowDto> getVacationApprovalFlows(Long vaAppNo){
		 List<VacationApprovalFlow> approvalFlows = vacationApprovalFlowRepository.findByVacationApprovalVacationApprovalNo(vaAppNo);
		 List<VacationApprovalFlowDto> approvalFlowsdto = new ArrayList<>();
		 
		 for(VacationApprovalFlow appFlow : approvalFlows) {
			 VacationApprovalFlowDto dto = appFlow.toDto();
			 approvalFlowsdto.add(dto);
		 }
		 return approvalFlowsdto;
	 }
  
  // [서혜원] 사원 일정 휴가 조회
	 public List<VacationApprovalDto> getApprovedVacationSchedules() {
	        List<Object[]> vacationApprovals = vacationApprovalRepository.findApprovedVacationSchedules(1);

	        // Object[] 데이터를 VacationApprovalDto로 변환
	        return vacationApprovals.stream()
	                .map(this::convertToDto)
	                .collect(Collectors.toList());
	    }

	// [서혜원] 사원 일정 휴가 조회
	 private VacationApprovalDto convertToDto(Object[] row) {
	    VacationApprovalDto dto = new VacationApprovalDto();
	    dto.setMember_no(((Number) row[0]).longValue());  
	    dto.setVacation_type_no(((Number) row[1]).longValue()); 
	    dto.setVacation_approval_start_date((String) row[2]);  
	    dto.setVacation_approval_end_date((String) row[3]);  
	    dto.setVacation_type_name((String) row[4]);  
	    dto.setDepartment_no(((Number) row[5]).longValue());   
	    dto.setVacation_approval_no(((Number) row[6]).longValue());  
 
	    Long memberNo = ((Number) row[0]).longValue();
	    String memberName = memberRepository.findById(memberNo)
	                                         .map(Member::getMemberName)
	                                         .orElse("사원");  

	    dto.setMember_name(memberName);  
	    
		String positionName = "직위";
		String departmentName = "부서";
		
		List<Object[]> memberInfo = memberRepository.findMemberWithDepartmentAndPosition(memberNo); 
		
		Object[] memberpositionDepartment = memberInfo.get(0);  
		positionName = (String) memberpositionDepartment[1];   
		departmentName = (String) memberpositionDepartment[2]; 
		dto.setPosition_name(positionName);
		dto.setDepartment_name(departmentName);

	    return dto;
	}

	//[김채영] 테스트 실시간 휴가 결재 pk값
	public Long getVacationApprovalPk(){
		return noficationMapper.getVacationApprovalPk();
	}
}