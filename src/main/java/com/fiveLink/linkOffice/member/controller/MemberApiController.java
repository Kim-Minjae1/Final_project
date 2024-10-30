package com.fiveLink.linkOffice.member.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.fiveLink.linkOffice.member.domain.MemberDto;
import com.fiveLink.linkOffice.member.service.MemberFileService;
import com.fiveLink.linkOffice.member.service.MemberService;
import com.fiveLink.linkOffice.util.AESUtil;

@Controller
public class MemberApiController {

	private final MemberService memberService;
	private final MemberFileService memberFileService;
	private final PasswordEncoder passwordEncoder;
	
	@Autowired
	public MemberApiController(MemberService memberService,MemberFileService memberFileService, PasswordEncoder passwordEncoder) {
		this.memberService = memberService;
		this.memberFileService = memberFileService;
		this.passwordEncoder = passwordEncoder;
	}
	
	// [전주영] 전자결재 서명 등록
	@ResponseBody
	@PostMapping("/employee/member/digitalname/{member_no}")
	public Map<String, String> digitalnameUpdate( @PathVariable("member_no") Long memberNo,@RequestParam("signatureData") String signatureData) {
	    Map<String, String> response = new HashMap<>();
	    response.put("res_code", "404");
	    response.put("res_msg", "서명 등록 중 오류가 발생하였습니다.");
	    
	    MemberDto memberdto = memberService.selectMemberOne(memberNo); 
	     
	    try {
	        if (signatureData != null && !signatureData.isEmpty()) {
	            String newDigitalName = memberFileService.uploadDigital(signatureData);
	            if (newDigitalName != null) {
	                memberdto.setMember_ori_digital_img(signatureData);
	                memberdto.setMember_new_digital_img(newDigitalName);
	                
                    if(memberFileService.delete(memberdto.getMember_no()) > 0) {
                    	response.put("res_msg", "기존 서명이 삭제 완료되었습니다.");
                    }else {
                    	response.put("res_msg", "기존 서명 삭제 중 오류가 발생되었습니다.");
                    }

	            }
	            
	            if (memberService.updateMemberDigital(memberdto) != null) {
                    response.put("res_code", "200");
                    response.put("res_msg", "서명이 등록되었습니다.");
                 
                }
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        response.put("res_msg", "서버 오류가 발생하였습니다.");
	    }
	    return response;
	}
	
	// [전주영] 비밀번호 확인
	@ResponseBody
	@PostMapping("/myedit/pwVerify/{member_no}")
	public Map<String, String> pwVerify(@PathVariable("member_no") Long memberNo, @RequestBody String pwVerify) {
	    Map<String, String> response = new HashMap<>();
	    response.put("res_code", "404"); 
	    response.put("res_msg", "비밀번호 확인 중 오류가 발생하였습니다."); 
	    
	    // 회원 정보 조회
	    MemberDto memberDto = memberService.selectMemberOne(memberNo);
	    
	    if (memberDto != null) {
	        // 비밀번호 비교
	        if (passwordEncoder.matches(pwVerify, memberDto.getMember_pw())) {
	            response.put("res_code", "200"); 
	            response.put("res_msg", "비밀번호가 일치합니다."); 
	        }
	    }
	    return response;
	}
	
	// [전주영] 정보수정 
	@ResponseBody
	@PostMapping("/employee/member/myedit/{member_no}")
	public Map<String,String> profileUpdate(@PathVariable("member_no") Long memberNo,
            @RequestParam(name = "file", required = false) MultipartFile file, 
            @RequestParam(name = "roadAddress") String roadAddress,  
            @RequestParam(name = "detailAddress") String detailAddress, 
            @RequestParam(name = "newPassword") String newPassword){
		 Map<String, String> response = new HashMap<>();
		    response.put("res_code", "404");
		    response.put("res_msg", "정보 수정 중 오류가 발생하였습니다.");
		    
		    MemberDto memberdto = memberService.selectMemberOne(memberNo); 
		     
		    String newAdr = roadAddress + " " + detailAddress; 
		    
		    if(newAdr.trim().isEmpty()) {
		    	memberdto.setMember_address(memberdto.getMember_address());
		    } else {
		    	memberdto.setMember_address(newAdr);
		    }
		    
		    if(!newPassword.isEmpty()) {
		    	memberdto.setMember_pw(passwordEncoder.encode(newPassword));
		    }
		    if(file != null && "".equals(file.getOriginalFilename()) == false) {
		    	String saveFileName = memberFileService.uploadProfile(file);
		    	if(saveFileName != null) {
		    		memberdto.setMember_ori_profile_img(file.getOriginalFilename());
		    		memberdto.setMember_new_profile_img(saveFileName);
		    		
		    		if(memberFileService.profileDelete(memberNo) > 0) {
		    			response.put("res_msg", "기존 파일이 삭제 되었습니다.");
		    		}else {
		    			response.put("res_msg", "기존 파일이 삭제 중 오류가 발생하었습니다.");
		    		}
		    	} else {
		    		response.put("res_msg", "파일 업로드 실패");
		    	}
		    	
		    }
		    
		    if(memberService.updateMemberProfile(memberdto) != null) {
		    	response.put("res_code", "200");
		    	response.put("res_msg", "정보 수정이 완료되었습니다.");
		    }
		    return response;
	}
	
	// [전주영] 관리자 사원 등록 
	@ResponseBody
	@PostMapping("/admin/member/create")
	public Map<String,String> memberCreate(@RequestParam("profile_image") MultipartFile profileImage,
            @RequestParam("name") String name,
            @RequestParam("national_number_front") String nationalNumberFront,
            @RequestParam("national_number_back") String nationalNumberBack,
            @RequestParam("hire_date") String hireDate,
            @RequestParam("mobile1") String mobile1,
            @RequestParam("mobile2") String mobile2,
            @RequestParam("mobile3") String mobile3,
            @RequestParam("internal") String internal,
            @RequestParam("department") String department,
            @RequestParam("position") String position){
		
		 Map<String, String> response = new HashMap<>();
		    response.put("res_code", "404");
		    response.put("res_msg", "사원 등록 중 오류가 발생하였습니다.");
		
		    MemberDto dto = new MemberDto();
		    
		try {
			
			String saveProfileName = memberFileService.uploadProfile(profileImage);
			if(saveProfileName != null) {
				dto.setMember_ori_profile_img(profileImage.getOriginalFilename());
				dto.setMember_new_profile_img(saveProfileName);
			}
			
			dto.setMember_name(name);
			
			dto.setMember_pw("1111");

			List<MemberDto> memberDtoList = memberService.getAllMembers();
			
			String national = nationalNumberFront + "-" + nationalNumberBack;
			String encryptedNational = AESUtil.encrypt(national);
			
			String decryptedNational = null;
			for (MemberDto memberDto : memberDtoList) {
			    decryptedNational = AESUtil.decrypt(memberDto.getMember_national());

			    if (decryptedNational.equals(national)) {
			        response.put("res_code", "400");
			        response.put("res_msg", "일치하는 주민번호가 있습니다.");
			        return response;
			    }
			}

			dto.setMember_national(encryptedNational);
			
			
			dto.setMember_hire_date(hireDate);
			String mobile = mobile1 + "-" + mobile2 + "-" + mobile3;
			dto.setMember_mobile(mobile);
			
			dto.setMember_internal(internal);
			
			long departmentNo = Long.parseLong(department);
		    long positionNo = Long.parseLong(position);		
			dto.setDepartment_no(departmentNo);
			dto.setPosition_no(positionNo);
			
			if(memberService.createMember(dto) != null) {
				response.put("res_code", "200");
			    response.put("res_msg", "사원을 등록하였습니다.");
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return response;
	}
	
	// [전주영] 비밀번호 변경
	@ResponseBody
	@PutMapping("/pwchange")
	public Map<String,String> pwchange(
			 @RequestParam("user_id") String userId,
			 @RequestParam("national_number_front") String national1,
			 @RequestParam("national_number_mid") String national2,
			 @RequestParam("national_number_back") String national3,
			 @RequestParam("new_password") String newPw) throws Exception{
		 Map<String, String> response = new HashMap<>();
		    response.put("res_code", "404");
		    response.put("res_msg", "비밀번호 변경 중 오류가 발생하였습니다.");
		    
		    MemberDto dto = new MemberDto();
		    
		    List<MemberDto> memberDtoList = memberService.getAllMembers();
		    
		    String userNational = national1 +"-" + national2 + national3;
		    
		    boolean memberExists = false; 
		    
		    String decryptedNational = null;
		    
		    for (MemberDto memberDto : memberDtoList) {
		    	
		        String memberNumber = memberDto.getMember_number();
		        String memberNational = memberDto.getMember_national();
		        decryptedNational = AESUtil.decrypt(memberNational);

		        if (memberNumber.equals(userId)) {
		            memberExists = true; 
		            dto.setMember_number(userId);

		            if (!decryptedNational.equals(userNational)) {
		                response.put("res_code", "409");
		                response.put("res_msg", "등록된 주민번호와 일치하지 않습니다.");
		                return response; 
		            } else {
		                dto.setMember_pw(newPw); 
		            }
		        }
		    }

		    if (!memberExists) {
		        response.put("res_code", "404");
		        response.put("res_msg", "등록된 아이디가 아닙니다.");
		        return response;
		    }

		    if (memberService.pwchange(dto) != null) {
		        response.put("res_code", "200");
		        response.put("res_msg", "비밀번호가 변경되었습니다.");
		    }

		    return response;

	}
	
	// [전주영] 사원 상태 변경
	@ResponseBody
	@PutMapping("/admin/member/status/{member_no}")
	public Map<String,String> statusUpdate(@PathVariable("member_no") Long memberNo){
		 Map<String, String> response = new HashMap<>();
		    response.put("res_code", "404");
		    response.put("res_msg", "퇴사 처리 중 오류가 발생하였습니다.");
		    
		    MemberDto memberdto = memberService.selectMemberOne(memberNo);
		    
		    if(memberService.statusUpdate(memberdto) != null) {
		    	response.put("res_code", "200");
			    response.put("res_msg", "퇴사 처리하였습니다.");
			}
		    
		    return response;
	}
	
	// [전주영] 관리자 사원 정보 수정
	@ResponseBody
	@PutMapping("/admin/member/edit/{member_no}")
	public Map<String,String> edit(@PathVariable("member_no") Long memberNo,
			@RequestParam("profile_img") MultipartFile profileImage,
            @RequestParam("hire_date") String hireDate,
            @RequestParam("mobile1") String mobile1,
            @RequestParam("mobile2") String mobile2,
            @RequestParam("mobile3") String mobile3,
            @RequestParam("internal") String internal,
            @RequestParam("department") String department,
            @RequestParam("position") String position){
		Map<String, String> response = new HashMap<>();
	    response.put("res_code", "404");
	    response.put("res_msg", "정보 수정 중 오류가 발생하였습니다.");
	    
	    MemberDto memberdto = memberService.selectMemberOne(memberNo);
	    
	    if(profileImage != null && "".equals(profileImage.getOriginalFilename()) == false) {
	    	String saveFileName = memberFileService.uploadProfile(profileImage);
	    	if(saveFileName != null) {
	    		memberdto.setMember_ori_profile_img(profileImage.getOriginalFilename());
	    		memberdto.setMember_new_profile_img(saveFileName);
	    		
	    		if(memberFileService.profileDelete(memberNo) > 0) {
	    			response.put("res_msg", "기존 파일이 삭제 되었습니다.");
	    		} else {
	    			response.put("res_msg", "기존 파일이 삭제 중 오류가 발생하었습니다.");
	    		}
	    	} else {
	    		response.put("res_msg", "파일 업로드 실패");
	    	}
	    }
	    
		
		memberdto.setMember_hire_date(hireDate);
		
		String mobile = mobile1 + "-" + mobile2 + "-" + mobile3;
		memberdto.setMember_mobile(mobile);
		
		memberdto.setMember_internal(internal);
		
		long departmentNo = Long.parseLong(department);
	    long positionNo = Long.parseLong(position);		
	    memberdto.setDepartment_no(departmentNo);
	    memberdto.setPosition_no(positionNo);
	    
	    if(memberService.memberEdit(memberdto) != null) {
	    	response.put("res_code", "200");
	    	response.put("res_msg", "정보를 수정하였습니다.");

	    }
	    
	    return response;
	}

}
