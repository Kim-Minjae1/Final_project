package com.fiveLink.linkOffice.member.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fiveLink.linkOffice.mapper.VacationMapper;
import com.fiveLink.linkOffice.meeting.domain.Meeting;
import com.fiveLink.linkOffice.member.domain.Member;
import com.fiveLink.linkOffice.member.domain.MemberDto;
import com.fiveLink.linkOffice.member.repository.MemberRepository;
import com.fiveLink.linkOffice.permission.repository.MemberPermissionRepository;

import jakarta.transaction.Transactional;

@Service
public class MemberService {
	
	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
    //[김채영] 1년미만 재직자
    private final VacationMapper vacationMapper;
    private final MemberPermissionRepository memberPermissionRepository;
    
	@Autowired
	public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder, VacationMapper vacationMapper, MemberPermissionRepository memberPermissionRepository) {
		this.memberRepository = memberRepository;
		this.passwordEncoder = passwordEncoder;
        this.vacationMapper =vacationMapper;
        this.memberPermissionRepository = memberPermissionRepository;
	}
	
	// Object[] 결과를 MemberDto로 변환
    private List<MemberDto> convertToDtoList(List<Object[]> results) {
        return results.stream().map(result -> {
            Member member = (Member) result[0];
            String positionName = (String) result[1];
            String departmentName = (String) result[2];
            return MemberDto.builder()
                    .member_no(member.getMemberNo())
                    .member_number(member.getMemberNumber())
                    .member_pw(member.getMemberPw())
                    .member_name(member.getMemberName())
                    .member_national(member.getMemberNational())
                    .member_internal(member.getMemberInternal())
                    .member_mobile(member.getMemberMobile())
                    .department_no(member.getDepartmentNo())
                    .position_no(member.getPositionNo())
                    .department_name(departmentName)
                    .position_name(positionName)
                    .member_address(member.getMemberAddress())
                    .member_hire_date(member.getMemberHireDate())
                    .member_end_date(member.getMemberEndDate())
                    .member_create_date(member.getMemberCreateDate())
                    .member_update_date(member.getMemberUpdateDate())
                    .member_ori_profile_img(member.getMemberOriProfileImg())
                    .member_new_profile_img(member.getMemberNewProfileImg())
                    .member_ori_digital_img(member.getMemberOriDigitalImg())
                    .member_new_digital_img(member.getMemberNewDigitalImg())
                    .member_status(member.getMemberStatus())
                    .member_additional(member.getMemberAdditional())
                    .build();
        }).collect(Collectors.toList());
    }

    // [전주영] 멤버 조회 (목록 조회) 
    public Page<MemberDto> getAllMemberPage(Pageable pageable, MemberDto searchdto) {
    	Page<Object[]> results = null;
    	
    	String searchText = searchdto.getSearch_text();
    	if (searchText != null && !searchText.isEmpty()) {
    	    int searchType = searchdto.getSearch_type();
    	    switch (searchType) {
    	    	// 전체 검색
    	        case 1:
    	        	results = memberRepository.findMembersByNumberMemberNameDepartmentNamePositionNameStatus(searchText, pageable);
    	            break;
    	        // 사번 검색
    	        case 2:
    	            results = memberRepository.findMembersByNumber(searchText, pageable);
    	            break;
    	        // 사원명 검색
    	        case 3:
    	            results = memberRepository.findMembersByMemberName(searchText, pageable);
    	            break;
    	        // 부서명 검색
    	        case 4:
    	        	results = memberRepository.findMembersByDepartmentName(searchText, pageable);
    	            break;
    	        // 직위명 검색
    	        case 5:
    	            results = memberRepository.findMembersByPositionName(searchText, pageable);
    	            break;
    	         // 상태 검색
    	        case 6:
    	        	  results = memberRepository.findMembersByMemberStatus(searchText, pageable);
    	        	  break;
    	        default:
    	            results = memberRepository.findAllMembersWithDetails(pageable);
    	            break;
    	    }
    	} else {
    	    results = memberRepository.findAllMembersWithDetails(pageable);
    	}
    	
        List<MemberDto> memberDtoList = convertToDtoList(results.getContent());
        return new PageImpl<>(memberDtoList, pageable, results.getTotalElements());
    }
    
    // [전주영] 사번으로 조회
    public List<MemberDto> getMemberByNumber(String memberNumber) {
        List<Object[]> results = memberRepository.findMemberNumber(memberNumber);
        return convertToDtoList(results);
    }

    // [전주영] mypage 정보 조회
    public List<MemberDto> getMembersByNo(Long memberNo) {
        List<Object[]> results = memberRepository.findMemberWithDepartmentAndPosition(memberNo);
        return convertToDtoList(results);
    }
    
    // [전주영] 전자결재 서명 dto 조회 
    public MemberDto selectMemberOne(Long memberNo) {
    	Member member = memberRepository.findByMemberNo(memberNo);
    	MemberDto dto = MemberDto.toDto(member);
    	return dto;
    }
    
    // [전주영]전자결재 서명 update
    @Transactional
    public Member updateMemberDigital(MemberDto dto) {
        MemberDto temp = selectMemberOne(dto.getMember_no());
        if (dto.getMember_ori_digital_img() != null && !dto.getMember_ori_digital_img().isEmpty()) {
            temp.setMember_ori_digital_img(dto.getMember_ori_digital_img());
            temp.setMember_new_digital_img(dto.getMember_new_digital_img());
        }
        return memberRepository.save(temp.toEntity());
    }
    
    // [전주영] 프로필 이미지 및 비밀번호, 주소 변경
    @Transactional
    public Member updateMemberProfile(MemberDto dto) {
        return memberRepository.save(dto.toEntity());
    }
    
    // [전주영] 사원 생성
    public Member createMember(MemberDto dto) {
        dto.setMember_pw(passwordEncoder.encode(dto.getMember_pw()));
        return memberRepository.save(dto.toEntity());
    }
    
    // [전주영] 사번으로 member 조회
    public MemberDto selectMemNumberOne(String memberNumber) {
        Member member = memberRepository.findByMemberNumber(memberNumber);
        return MemberDto.toDto(member);
    }
    
    // [전주영] 사원등록, 비밀번호 변경 멤버 조회
    public List<MemberDto> getAllMembers() {
        List<Object[]> results = memberRepository.findAllMembers();
        return convertToDtoList(results);
    }

    // [전주영] 비밀번호 변경해주기
    @Transactional
    public Member pwchange(MemberDto dto) {
    	
    	MemberDto temp = selectMemNumberOne(dto.getMember_number());
    	
    	Member member = temp.toEntity();
    	// dto 에서 받아온 비밀번호 암호화
    	String encodedPw = passwordEncoder.encode(dto.getMember_pw());
    	
    	// member번호에 인코딩된 비밀번호 암호화
    	member.setMemberPw(encodedPw);
    	Member result = memberRepository.save(member);
    	
    	return result;
    }
    
    // [전주영] 상태값 변경 (퇴사)
    @Transactional
    public Member statusUpdate(MemberDto memberdto) {
        MemberDto temp = selectMemberOne(memberdto.getMember_no());
        temp.setMember_status(1L);
        temp.setMember_end_date(LocalDateTime.now());
        return memberRepository.save(temp.toEntity());
    }
    
    // [전주영] 사원 정보 수정
    @Transactional
    public Member memberEdit(MemberDto memberdto) {
        return memberRepository.save(memberdto.toEntity());
    }
    
    // [전주영] 멤버 조회 (직위 순)
    public Page<MemberDto> getAllMemberPosition(Pageable pageable, MemberDto searchdto) {
        Page<Object[]> results = null;
        
        String searchText = searchdto.getSearch_text();
        
        if(searchText != null && !searchText.isEmpty()) {
        	int searchType = searchdto.getSearch_type();
        	switch(searchType) {
        		// 전체 검색
        		case 1 : 
        			results = memberRepository.findAllMemberStatusByNumberNameDepartmentNamePositionName(searchText,pageable);
        			break;
        		// 사번 검색
        		case 2 : 
        			results = memberRepository.findAllMemberStatusByMemberNumber(searchText,pageable);
        			break;
        		// 사원명 검색
        		case 3 : 
        			results = memberRepository.findAllMemberStatusByMemberName(searchText,pageable);
        			break;
        		// 부서명 검색
        		case 4 : 
        			results = memberRepository.findAllMemberStatusByDepartmentName(searchText,pageable);
        			break;
        		// 직위명 검색
        		case 5 : 
        			results = memberRepository.findAllMemberStatusByPositionName(searchText,pageable);
        			break;
        		default:
        			results = memberRepository.findAllMemberStatusOrderByPosition(pageable);
        			break;
        	}
        	
        } else {
        	results = memberRepository.findAllMemberStatusOrderByPosition(pageable);
        }
        
        List<MemberDto> memberDtoList = convertToDtoList(results.getContent());
        return new PageImpl<>(memberDtoList, pageable, results.getTotalElements());
    }
	 // [서혜원] 부서별 사원
	 public List<MemberDto> getMembersByDepartmentNo(Long departmentNo) {
	    List<Member> members = memberRepository.findByDepartmentNoAndMemberStatus(departmentNo, 0L);
	    return members.stream().map(member -> MemberDto.builder()
	            .memberId(member.getMemberNo())
	            .memberName(member.getMemberName())
	            .departmentNo(member.getDepartment().getDepartmentNo())  
	            .build()
	    ).collect(Collectors.toList());
	}
  
	// [서혜원] 부서 관리 memberdto
	public Long getLoggedInMemberNo() {
    	org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();  
 
        Member member = memberRepository.findByMemberNumber(username);

        if (member != null) {
            return member.getMemberNo();  
        } else {
            throw new RuntimeException("로그인한 사용자 정보를 찾을 수 없습니다.");
        }
    }
     
	// [서혜원] 직위 번호별 사원 조회
	public List<MemberDto> getMembersByPositionNo(Long positionNo) {
	    List<Member> members = memberRepository.findByPositionNo(positionNo);
	    return members.stream()
	        .map(member -> MemberDto.builder()
	            .memberId(member.getMemberNo())
	            .memberName(member.getMemberName())
	            .positionNo(member.getPosition().getPositionNo()) 
	            .build()
	        )
	        .collect(Collectors.toList());
	}


    // [서혜원] 본인 포함 전체 사원 조직도
    public List<MemberDto> getAllMembersChart() {
        List<Member> members = memberRepository.findAllByMemberStatusOrderByPosition_PositionLevelAsc(0L);
        return members.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private MemberDto convertToDto(Member member) {
        return MemberDto.builder()
                .member_no(member.getMemberNo())
                .member_number(member.getMemberNumber())
                .member_pw(member.getMemberPw())
                .member_name(member.getMemberName())
                .member_national(member.getMemberNational())
                .member_internal(member.getMemberInternal())
                .member_mobile(member.getMemberMobile())
                .department_no(member.getDepartmentNo())
                .position_no(member.getPositionNo())
                .member_address(member.getMemberAddress())
                .member_hire_date(member.getMemberHireDate())
                .member_end_date(member.getMemberEndDate())
                .member_create_date(member.getMemberCreateDate())
                .member_update_date(member.getMemberUpdateDate())
                .member_ori_profile_img(member.getMemberOriProfileImg())
                .member_new_profile_img(member.getMemberNewProfileImg())
                .member_ori_digital_img(member.getMemberOriDigitalImg())
                .member_new_digital_img(member.getMemberNewDigitalImg())
                .member_status(member.getMemberStatus())
                .member_additional(member.getMemberAdditional())
                .position_name(member.getPosition() != null ? member.getPosition().getPositionName() : null)  
                .department_name(member.getDepartment() != null ? member.getDepartment().getDepartmentName() : null) 
                .build();
    }

    //[김채영] 1년 미만 재직자 정보
    public List<MemberDto> selectUnderYearMember(int num) { 
        return vacationMapper.selectUnderYearMember(num); 
    }

    // [서혜원] 회의실 예약자
    public String getMemberNameById(Long memberNo) {
        return memberRepository.findById(memberNo)
                .map(Member::getMemberName)
                .orElse("사원");
    } 
    
    // [서혜원] 본인 제외 사원 조직도
    public List<MemberDto> getAllMembersChartOut() { 
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loggedInMemberNumber = authentication.getName();  

        System.out.println(authentication); 
        List<Member> members = memberRepository.findAllByMemberStatusAndMemberNumberNotOrderByPosition_PositionLevelAsc(0L, loggedInMemberNumber);

        return members.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    } 
    
    // [전주영] 휴가 갯수 감소
    @Transactional
    public Member updateVacation(MemberDto dto) {
    	MemberDto temp = selectMemberOne(dto.getMember_no());
    	try {
            
    		double currentVacationCount = temp.getMember_vacation_count();
            
            if (dto.getMember_vacation_count() > 0) {
                double updatedVacationCount = currentVacationCount - dto.getMember_vacation_count();
                temp.setMember_vacation_count(updatedVacationCount);
            }
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
        return memberRepository.save(temp.toEntity());
    }
    
    // [전주영] 휴가 갯수 복구
    @Transactional
    public Member updateOriginVacation(MemberDto dto) {
    	MemberDto temp = selectMemberOne(dto.getMember_no());
    	try {
    		
    		double currentVacationCount = temp.getMember_vacation_count();
    		
    		if (dto.getMember_vacation_count() > 0) {
    			double updatedVacationCount = currentVacationCount + dto.getMember_vacation_count();
    			temp.setMember_vacation_count(updatedVacationCount);
    		}
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
    	return memberRepository.save(temp.toEntity());
    }
    
    // member 조회
    public Member memberNo(Long member_no) {
    	return memberRepository.findByMemberNo(member_no);
    }
    
} 
