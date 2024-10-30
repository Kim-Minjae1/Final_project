package com.fiveLink.linkOffice.survey.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;


import com.fiveLink.linkOffice.member.domain.MemberDto;
import com.fiveLink.linkOffice.member.service.MemberService;
import com.fiveLink.linkOffice.notice.controller.NoticeViewController;
import com.fiveLink.linkOffice.survey.domain.SurveyDto;
import com.fiveLink.linkOffice.survey.domain.SurveyQuestionDto;
import com.fiveLink.linkOffice.survey.service.SurveyService;

@Controller
public class SurveyViewController {
	private static final Logger LOGGER = LoggerFactory.getLogger(NoticeViewController.class);
    private final SurveyService surveyService;
    private final MemberService memberService;
    
    @Autowired
    public SurveyViewController(MemberService memberService, SurveyService surveyService) {
        this.memberService = memberService;
        this.surveyService = surveyService;
    }
    
    // 정렬 방식 처리
    private Sort getSortOption(String sort) {
        if ("latest".equals(sort)) {
            return Sort.by(Sort.Order.desc("surveyStartDate"));
        } else if ("oldest".equals(sort)) {
            return Sort.by(Sort.Order.asc("surveyStartDate"));
        }
        return Sort.by(Sort.Order.desc("surveyStartDate"));
    }
    
    // 내가 만든 설문 목록 페이지
    @GetMapping("/employee/survey/myList/{member_no}")
    public String surveyMyList(
        @PageableDefault(size = 10, sort = "surveyStartDate", direction = Sort.Direction.DESC) Pageable pageable,
        @RequestParam(value = "sort", defaultValue = "latest") String sort,
        Model model,
        SurveyDto searchDto) {

        Long memberNo = memberService.getLoggedInMemberNo(); 
        List<MemberDto> memberdto = memberService.getMembersByNo(memberNo);
        model.addAttribute("memberdto", memberdto);
        model.addAttribute("currentSort", sort);

        // 정렬 방식 설정
        Sort sortOption = getSortOption(sort);
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortOption);

        // 설문 목록 페이징 처리
        Page<SurveyDto> surveyPage = surveyService.getAllSurveyPage(sortedPageable, searchDto, memberNo);
        model.addAttribute("surveyList", surveyPage.getContent());
        model.addAttribute("page", surveyPage);
        model.addAttribute("searchDto", searchDto);

        return "employee/survey/survey_my_list";
    }

    
    // 설문 결과 페이지 (주관식 및 객관식 응답 포함)
    @GetMapping("/employee/survey/result/{survey_no}")
    public String surveyResult(@PathVariable("survey_no") Long surveyNo, Model model) {
        // 설문 상세 정보
        SurveyDto dto = surveyService.selectSurveyOne(surveyNo);

        // 설문에 대한 질문들 (객관식, 주관식)
        List<SurveyQuestionDto> questions = surveyService.getSurveyQuestions(surveyNo);
        
        
        
        // 참여자 통계
        int totalParticipants = surveyService.getTotalParticipants(surveyNo);
        int completedParticipants = surveyService.getCompletedParticipants(surveyNo);
        int notParticipatedParticipants = totalParticipants - completedParticipants;

        // 각 질문에 대한 참여율 및 응답 통계
        Map<Long, Integer> participationRates = surveyService.calculateParticipationRates(questions, totalParticipants);
        Map<Long, List<Object[]>> optionAnswerCounts = surveyService.getOptionAnswerCountsBySurvey(surveyNo);
        Map<Long, List<List<Object>>> chartData = prepareChartData(optionAnswerCounts);
        
        Long memberNo = memberService.getLoggedInMemberNo(); 
        List<MemberDto> memberdto = memberService.getMembersByNo(memberNo);
        model.addAttribute("memberdto", memberdto);
        // 주관식 답변도 포함
        Map<Long, List<Object[]>> textAnswers = surveyService.getTextAnswersBySurvey(surveyNo);
        
        // 주관식 답변 로그 확인
        textAnswers.forEach((questionNo, answers) -> {
            answers.forEach(answer -> {
                LOGGER.info("Survey No: {}, Question No: {}, Participant: {}, Answer: {}", surveyNo, questionNo, answer[0], answer[1]);
            });
        });
        model.addAttribute("dto", dto);
        model.addAttribute("questions", questions);
        model.addAttribute("totalParticipants", totalParticipants);
        model.addAttribute("completedParticipants", completedParticipants);
        model.addAttribute("notParticipatedParticipants", notParticipatedParticipants);
        model.addAttribute("participationRates", participationRates);
        model.addAttribute("chartData", chartData);
        model.addAttribute("textAnswers", textAnswers);
        return "employee/survey/survey_question_myResult";
    }
    
    @GetMapping("/employee/survey/detail/{survey_no}")
    public String selectSurveyOne(Model model, @PathVariable("survey_no") Long surveyNo) {
        Long memberNo = memberService.getLoggedInMemberNo();

        // 설문 기본 정보 가져오기
        SurveyDto dto = surveyService.selectSurveyOne(surveyNo);
        List<SurveyQuestionDto> questions = surveyService.getSurveyQuestions(surveyNo);
        Long member_no = memberService.getLoggedInMemberNo();
        List<MemberDto> memberdto = memberService.getMembersByNo(member_no);
        model.addAttribute("memberdto", memberdto);

        // 설문 상태 확인 (진행중 or 마감)
        int surveyStatus = dto.getSurvey_status(); // 0: 진행중, 1: 마감
        model.addAttribute("dto", dto);
        model.addAttribute("questions", questions);

        if (surveyStatus == 1) {
            // 설문이 마감된 경우 결과 페이지로 이동
            int totalParticipants = surveyService.getTotalParticipants(surveyNo);
            int completedParticipants = surveyService.getCompletedParticipants(surveyNo);
            int notParticipatedParticipants = totalParticipants - completedParticipants;

            // 참여율 계산 및 추가
            Map<Long, Integer> participationRates = surveyService.calculateParticipationRates(questions, totalParticipants);
            model.addAttribute("participationRates", participationRates);

            // 각 질문별 응답 수 가져오기
            Map<Long, List<Object[]>> optionAnswerCounts = surveyService.getOptionAnswerCountsBySurvey(surveyNo);
            model.addAttribute("optionAnswerCounts", optionAnswerCounts);

            // 차트 데이터 준비 (각 질문별 옵션 응답 수를 차트 형식으로 변환)
            Map<Long, List<List<Object>>> chartData = prepareChartData(optionAnswerCounts);
            model.addAttribute("chartData", chartData);

            // 통계 데이터 추가
            model.addAttribute("totalParticipants", totalParticipants);
            model.addAttribute("completedParticipants", completedParticipants);
            model.addAttribute("notParticipatedParticipants", notParticipatedParticipants);

            return "employee/survey/survey_question_result"; // 설문 결과 페이지로 이동
        } else if (surveyStatus == 0) {
            // 설문이 진행 중인 경우에만 참여 여부를 확인
            boolean hasParticipated = surveyService.hasUserParticipated(surveyNo, memberNo);

            if (hasParticipated) {
                // 설문 참여자가 있을 경우
                int totalParticipants = surveyService.getTotalParticipants(surveyNo);
                int completedParticipants = surveyService.getCompletedParticipants(surveyNo);
                int notParticipatedParticipants = totalParticipants - completedParticipants;

                // 참여율 계산 및 추가
                Map<Long, Integer> participationRates = surveyService.calculateParticipationRates(questions, totalParticipants);
                model.addAttribute("participationRates", participationRates);

                // 각 질문별 응답 수 가져오기
                Map<Long, List<Object[]>> optionAnswerCounts = surveyService.getOptionAnswerCountsBySurvey(surveyNo);
                model.addAttribute("optionAnswerCounts", optionAnswerCounts);

                // 차트 데이터 준비 (각 질문별 옵션 응답 수를 차트 형식으로 변환)
                Map<Long, List<List<Object>>> chartData = prepareChartData(optionAnswerCounts);
                model.addAttribute("chartData", chartData);

                // 통계 데이터 추가
                model.addAttribute("totalParticipants", totalParticipants);
                model.addAttribute("completedParticipants", completedParticipants);
                model.addAttribute("notParticipatedParticipants", notParticipatedParticipants);

                return "employee/survey/survey_question_result"; 
            } else {
                return "employee/survey/survey_question_detail"; 
            }
        }

        return "employee/survey/survey_question_detail";
    }
    
    @GetMapping("/employee/survey/update/{survey_no}")
    public String updateSurveyPage(Model model, @PathVariable("survey_no") Long surveyNo) {
        Long memberNo = memberService.getLoggedInMemberNo();
        List<MemberDto> memberdto = memberService.getMembersByNo(memberNo);
        
        model.addAttribute("memberdto", memberdto);
        
        // 설문 데이터와 질문 데이터 가져오기
        SurveyDto dto = surveyService.selectSurveyOne(surveyNo);
        List<SurveyQuestionDto> questions = surveyService.getSurveyQuestions(surveyNo);
        
        // 로그로 데이터 확인
        LOGGER.info("Survey DTO: {}", dto);
        LOGGER.info("Survey Questions: {}", questions);

        model.addAttribute("dto", dto);
        model.addAttribute("questions", questions); 

        return "employee/survey/survey_question_update"; 
    }

    
    
    @GetMapping("/employee/survey/endList/{member_no}")
    public String surveyEndList(
        @PageableDefault(size = 10, sort = "surveyStartDate", direction = Sort.Direction.DESC) Pageable pageable,
        @RequestParam(value = "sort", defaultValue = "latest") String sort,
        Model model,
        SurveyDto searchDto) {

        Long memberNo = memberService.getLoggedInMemberNo();
        List<MemberDto> memberdto = memberService.getMembersByNo(memberNo);

        if (searchDto == null) {
            searchDto = new SurveyDto();
        }

        model.addAttribute("memberdto", memberdto);
        model.addAttribute("currentSort", sort);

        Sort sortOption = getSortOption(sort);
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortOption);

        // 서비스에서 페이징 처리된 마감된 설문조사 리스트 조회
        Page<SurveyDto> surveyPage = surveyService.getEndAllSurveyPage(sortedPageable, searchDto, memberNo);

        model.addAttribute("surveyEndList", surveyPage.getContent());
        model.addAttribute("page", surveyPage);
        model.addAttribute("searchDto", searchDto);

        return "employee/survey/survey_end_list";
    }
    
    @GetMapping("/employee/survey/ingList/{member_no}")
    public String surveyIngList(
            @PageableDefault(size = 10, sort = "surveyStartDate", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(value = "sort", defaultValue = "latest") String sort,
            Model model,
            SurveyDto searchDto) {

        Long memberNo = memberService.getLoggedInMemberNo();
        List<MemberDto> memberdto = memberService.getMembersByNo(memberNo);
        
        model.addAttribute("memberdto", memberdto);
        model.addAttribute("currentSort", sort);

        Sort sortOption = getSortOption(sort);
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortOption);

        Page<SurveyDto> surveyPage = surveyService.getIngAllSurveyPage(sortedPageable, searchDto, memberNo);

        model.addAttribute("surveyIngList", surveyPage.getContent());
        model.addAttribute("page", surveyPage);
        model.addAttribute("searchDto", searchDto);

        return "employee/survey/survey_ing_list";
    }
    

    

    // 차트 데이터를 준비하는 메서드
    private Map<Long, List<List<Object>>> prepareChartData(Map<Long, List<Object[]>> optionAnswerCounts) {
        Map<Long, List<List<Object>>> chartData = new HashMap<>();

        for (Map.Entry<Long, List<Object[]>> entry : optionAnswerCounts.entrySet()) {
            Long questionNo = entry.getKey();
            List<Object[]> options = entry.getValue();
            List<List<Object>> data = new ArrayList<>();
            data.add(Arrays.asList("Option", "Votes")); 

            for (Object[] option : options) {
                String optionAnswer = (String) option[0];
                Long answerCount = (Long) option[1];
                data.add(Arrays.asList(optionAnswer, answerCount));
            }
            chartData.put(questionNo, data); 
        }
        return chartData;
    }



}