package com.fiveLink.linkOffice.survey.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fiveLink.linkOffice.member.domain.MemberDto;
import com.fiveLink.linkOffice.member.service.MemberService;
import com.fiveLink.linkOffice.organization.domain.DepartmentDto;
import com.fiveLink.linkOffice.organization.service.DepartmentService;
import com.fiveLink.linkOffice.survey.domain.SurveyAnswerOptionDto;
import com.fiveLink.linkOffice.survey.domain.SurveyDto;
import com.fiveLink.linkOffice.survey.domain.SurveyQuestionDto;
import com.fiveLink.linkOffice.survey.domain.SurveyTextDto;
import com.fiveLink.linkOffice.survey.service.SurveyService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class SurveyApiController {
	private static final Logger LOGGER = LoggerFactory.getLogger(SurveyApiController.class);
	private final DepartmentService departmentService;
	private final MemberService memberService;
	private final SurveyService surveyService;

	@Autowired
	public SurveyApiController(DepartmentService departmentService, MemberService memberService,
			SurveyService surveyService) {
		this.departmentService = departmentService;
		this.memberService = memberService;
		this.surveyService = surveyService;
	}
	@GetMapping("/employee/survey/organizationChart")
	@ResponseBody
	public List<Map<String, Object>> getOrganizationChart() {
		List<DepartmentDto> departments = departmentService.getAllDepartments();
		List<MemberDto> members = memberService.getAllMembersChartOut();
		return buildTree(departments, members);
	}
	
	
	@GetMapping("/employee/survey/qupdate/{survey_no}")
	public String qupdateSurveyPage(Model model, @PathVariable("survey_no") Long surveyNo) {
	    List<DepartmentDto> departments = departmentService.getAllDepartments();
	    List<MemberDto> members = memberService.getAllMembersChartOut();
	    
	    // 설문 정보 로깅
	    SurveyDto dto = surveyService.selectSurveyOne(surveyNo);
	    LOGGER.info("SurveyDto loaded for surveyNo {}: {}", surveyNo, dto);

	    Long memberNo = memberService.getLoggedInMemberNo();
	    List<MemberDto> memberdto = memberService.getMembersByNo(memberNo);
	    LOGGER.info("Member Info loaded for memberNo {}: {}", memberNo, memberdto);

	    List<SurveyQuestionDto> questions = surveyService.getSurveyQuestions(surveyNo);
	    LOGGER.info("SurveyQuestions loaded for surveyNo {}: {}", surveyNo, questions);
	    
	    model.addAttribute("memberdto", memberdto);
	    model.addAttribute("departments", departments);
	    model.addAttribute("dto", dto);
	    model.addAttribute("members", members);
	    model.addAttribute("questions", questions);

	    return "employee/survey/survey_update";
	}
	
	
	// 설문 데이터 생성 및 업데이트 처리
	@PostMapping("/employee/survey/qupdate/{survey_no}")
	@ResponseBody
	public Map<String, String> updateSurvey(@RequestBody SurveyDto surveyDto, @PathVariable("survey_no") Long surveyNo) {
	    Map<String, String> resultMap = new HashMap<>();

	    Long member_no = memberService.getLoggedInMemberNo();
	    surveyDto.setMember_no(member_no);
	    surveyDto.setSurvey_no(surveyNo);


	    LOGGER.info("Received SurveyDto for update: {}", surveyDto);

	    try {
	       
	        surveyService.updateCompleteSurvey(surveyDto);
	        resultMap.put("res_code", "200");
	        resultMap.put("res_msg", "설문이 업데이트되었습니다.");
	        

	        LOGGER.info("Survey updated successfully for surveyNo: {}", surveyNo);
	    } catch (Exception e) {
	        resultMap.put("res_code", "500");
	        resultMap.put("res_msg", "설문 처리 중 오류가 발생했습니다: " + e.getMessage());
	        

	        LOGGER.error("Survey update error for surveyNo {}: {}", surveyNo, e.getMessage(), e);
	    }

	    return resultMap;
	}

	// 설문 생성 페이지로 이동
	@GetMapping("/employee/survey/create/{member_no}")
	public String showSurveyCreatePage(Model model) {
		List<DepartmentDto> departments = departmentService.getAllDepartments();
		List<MemberDto> members = memberService.getAllMembersChartOut(); 
		Long memberNo = memberService.getLoggedInMemberNo();
        List<MemberDto> memberdto = memberService.getMembersByNo(memberNo);
        
        model.addAttribute("memberdto", memberdto);
		model.addAttribute("departments", departments);
		model.addAttribute("members", members);

		return "employee/survey/survey_create";
	}

	

	// 설문 데이터 생성 및 업데이트 처리
	@PatchMapping("/employee/survey/create")
	@ResponseBody
	public Map<String, String> createSurvey(@RequestBody SurveyDto surveyDto, Model model) {
		Map<String, String> resultMap = new HashMap<>();
		
		Long member_no = memberService.getLoggedInMemberNo();
		surveyDto.setMember_no(member_no);

		try {

			// 설문 생성 또는 업데이트
			surveyService.createCompleteSurvey(surveyDto);
			resultMap.put("res_code", "200");
			resultMap.put("res_msg", "설문이 생성되었습니다.");
		} catch (Exception e) {
			resultMap.put("res_code", "500");
			resultMap.put("res_msg", "설문 처리 중 오류가 발생했습니다 ");
			LOGGER.error("Survey creation error: ", e);
		}

		return resultMap;
	}

	@PostMapping("/employee/survey/questionDetail")
	@ResponseBody
	public Map<String, String> submitSurvey(HttpServletRequest request,
	        @ModelAttribute SurveyAnswerOptionDto surveyAnswerOptionDto, @ModelAttribute SurveyTextDto surveyTextDto) {

	    Map<String, String> resultMap = new HashMap<>();
	    try {
	        Long memberNo = memberService.getLoggedInMemberNo();
	        LOGGER.info("Logged in memberNo: {}", memberNo);

	        // 참가자 정보 설정
	        surveyAnswerOptionDto.setSurvey_participant_no(memberNo);
	        surveyTextDto.setSurvey_participant_no(memberNo);

	        // 객관식 응답 처리
	        String[] optionNos = request.getParameterValues("survey_option_no");
	        String[] questionNos = request.getParameterValues("survey_question_no");

	        if (optionNos != null && questionNos != null) {
				for (int i = 0; i < optionNos.length; i++) {
					surveyAnswerOptionDto.setSurvey_option_no(Long.parseLong(optionNos[i]));
					surveyService.saveSurveyAnswerOption(surveyAnswerOptionDto);
				}
			}

	        // 주관식 응답 처리 - 배열로 여러 응답을 수집하여 각각 저장
	        String[] textAnswers = request.getParameterValues("survey_text_answer");
	        String[] textQuestionNos = request.getParameterValues("survey_question_no"); 

	        if (textAnswers != null && textQuestionNos != null) {
	            for (int i = 0; i < textAnswers.length; i++) {
	                surveyTextDto.setSurvey_text_answer(textAnswers[i]);
	                surveyTextDto.setSurvey_question_no(Long.parseLong(textQuestionNos[i]));
	                surveyService.saveSurveyTextAnswer(surveyTextDto);
	            }
	        }

	        resultMap.put("res_code", "200");
	        resultMap.put("res_msg", "설문 성공적으로 저장되었습니다.");
	    } catch (Exception e) {
	        LOGGER.error("Error during survey submission: {}", e.getMessage());
	        resultMap.put("res_code", "500");
	        resultMap.put("res_msg", "설문 응답 처리 중 오류가 발생했습니다: " + e.getMessage());
	    }

	    return resultMap;
	}

	
	

	@PostMapping("/employee/update/{survey_no}")
	@ResponseBody
	public Map<String, String> updateSurvey(
	        SurveyDto dto, 
	        HttpServletRequest request,
	        @ModelAttribute SurveyAnswerOptionDto surveyAnswerOptionDto, 
	        @ModelAttribute SurveyTextDto surveyTextDto) {

	    Map<String, String> resultMap = new HashMap<>();
	    try {
	        Long memberNo = memberService.getLoggedInMemberNo();
	        LOGGER.info("Logged in memberNo: {}", memberNo);

	        // 참가자 정보 설정
	        surveyAnswerOptionDto.setSurvey_participant_no(memberNo);
	        surveyTextDto.setSurvey_participant_no(memberNo);

	        // 객관식 응답 처리
	        String[] optionNos = request.getParameterValues("survey_option_no");
	        if (optionNos != null) {
	            for (String optionNo : optionNos) {
	                surveyAnswerOptionDto.setSurvey_option_no(Long.parseLong(optionNo));
	                surveyService.updateSurveyAnswerOption(surveyAnswerOptionDto);
	            }
	        }

	        // 주관식 응답 처리
	        if (surveyTextDto.getSurvey_text_answer() != null) {
	            LOGGER.info("Text Answer: {}", surveyTextDto.getSurvey_text_answer());
	            surveyService.updateSurveyTextAnswer(surveyTextDto);
	        }

	        resultMap.put("res_code", "200");
	        resultMap.put("res_msg", "설문 수정되었습니다.");
	    } catch (Exception e) {
	        LOGGER.error("Error during survey update: {}", e.getMessage());
	        resultMap.put("res_code", "500");
	        resultMap.put("res_msg", "설문 수정 중 오류가 발생했습니다: " + e.getMessage());
	    }

	    return resultMap;
	}
	
	@ResponseBody
	@DeleteMapping("/employee/delete/{survey_no}")
	public Map<String, String> deletesurvey(@PathVariable("survey_no") Long surveyNo) {
	    Map<String, String> resultMap = new HashMap<>();
	    try {
	        surveyService.deleteSurvey(surveyNo);
	        resultMap.put("res_code", "200");
	        resultMap.put("res_msg", "설문이 삭제되었습니다.");
	    } catch (Exception e) {
	        resultMap.put("res_code", "500");
	        resultMap.put("res_msg", "삭제 중 오류가 발생했습니다: " + e.getMessage());
	    }
	    return resultMap;
	}


	// 트리 구조 생성 메서드
	private List<Map<String, Object>> buildTree(List<DepartmentDto> departments, List<MemberDto> members) {
		Map<Long, Map<String, Object>> departmentMap = new HashMap<>();
		Map<Long, List<MemberDto>> membersByDepartment = new HashMap<>();

		// 부서별 구성원 그룹화
		for (MemberDto member : members) {
			List<MemberDto> departmentMembers = membersByDepartment.getOrDefault(member.getDepartment_no(),
					new ArrayList<>());
			departmentMembers.add(member);
			membersByDepartment.put(member.getDepartment_no(), departmentMembers);
		}

		for (DepartmentDto dept : departments) {
			Map<String, Object> node = new HashMap<>();
			node.put("id", "dept_" + dept.getDepartment_no());
			node.put("text", dept.getDepartment_name());
			node.put("type", "department");
			node.put("children", new ArrayList<>());
			departmentMap.put(dept.getDepartment_no(), node);
		}

		List<Map<String, Object>> result = new ArrayList<>();
		for (DepartmentDto dept : departments) {
			if (dept.getDepartment_high() == 0) {
				Map<String, Object> departmentNode = buildDepartmentHierarchy(dept, departmentMap, membersByDepartment);
				if (departmentNode != null) {
					result.add(departmentNode);
				}
			}
		}
		return result;
	}

	// 부서와 구성원 계층구조 생성
	private Map<String, Object> buildDepartmentHierarchy(DepartmentDto dept,
			Map<Long, Map<String, Object>> departmentMap, Map<Long, List<MemberDto>> membersByDepartment) {
		Map<String, Object> node = departmentMap.get(dept.getDepartment_no());
		List<Map<String, Object>> children = (List<Map<String, Object>>) node.get("children");

		if (dept.getSubDepartments() != null && !dept.getSubDepartments().isEmpty()) {
			for (DepartmentDto subDept : dept.getSubDepartments()) {
				Map<String, Object> subDeptNode = buildDepartmentHierarchy(subDept, departmentMap, membersByDepartment);
				if (subDeptNode != null) {
					children.add(subDeptNode);
				}
			}
		}

		List<MemberDto> deptMembers = membersByDepartment.get(dept.getDepartment_no());
		if (deptMembers != null) {
			for (MemberDto member : deptMembers) {
				Map<String, Object> memberNode = new HashMap<>();
				memberNode.put("id", "member_" + member.getMember_no());
				memberNode.put("text", member.getMember_name() + " " + member.getPosition_name());
				memberNode.put("type", "member");
				children.add(memberNode);
			}
		}

		if (children.isEmpty()) {
			return null;
		} else {
			return node;
		}
	}

}
