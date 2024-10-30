package com.fiveLink.linkOffice.survey.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.fiveLink.linkOffice.member.repository.MemberRepository;
import com.fiveLink.linkOffice.notice.controller.NoticeApiController;
import com.fiveLink.linkOffice.survey.domain.Survey;
import com.fiveLink.linkOffice.survey.domain.SurveyAnswerOption;
import com.fiveLink.linkOffice.survey.domain.SurveyAnswerOptionDto;
import com.fiveLink.linkOffice.survey.domain.SurveyDto;
import com.fiveLink.linkOffice.survey.domain.SurveyOption;
import com.fiveLink.linkOffice.survey.domain.SurveyParticipant;
import com.fiveLink.linkOffice.survey.domain.SurveyQuestion;
import com.fiveLink.linkOffice.survey.domain.SurveyQuestionDto;
import com.fiveLink.linkOffice.survey.domain.SurveyText;
import com.fiveLink.linkOffice.survey.domain.SurveyTextDto;
import com.fiveLink.linkOffice.survey.repository.SurveyAnswerOptionRepository;
import com.fiveLink.linkOffice.survey.repository.SurveyOptionRepository;
import com.fiveLink.linkOffice.survey.repository.SurveyParticipantRepository;
import com.fiveLink.linkOffice.survey.repository.SurveyQuestionRepository;
import com.fiveLink.linkOffice.survey.repository.SurveyRepository;
import com.fiveLink.linkOffice.survey.repository.SurveyTextRepository;

import jakarta.transaction.Transactional;

@Service
public class SurveyService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NoticeApiController.class);
    private final SurveyRepository surveyRepository;
    private final MemberRepository memberRepository;
    private final SurveyOptionRepository surveyOptionRepository;
    private final SurveyQuestionRepository surveyQuestionRepository;
    private final SurveyTextRepository surveyTextRepository;
    private final SurveyParticipantRepository surveyParticipantRepository;
    private final SurveyAnswerOptionRepository surveyAnswerOptionRepository;
    
    @Autowired
    public SurveyService(SurveyRepository surveyRepository, MemberRepository memberRepository, SurveyOptionRepository surveyOptionRepository,
                         SurveyQuestionRepository surveyQuestionRepository, SurveyTextRepository surveyTextRepository, 
                         SurveyParticipantRepository surveyParticipantRepository,
                         SurveyAnswerOptionRepository surveyAnswerOptionRepository) {
        this.surveyRepository = surveyRepository;
        this.memberRepository = memberRepository;
        this.surveyOptionRepository = surveyOptionRepository;
        this.surveyQuestionRepository = surveyQuestionRepository;
        this.surveyTextRepository = surveyTextRepository;
        this.surveyParticipantRepository = surveyParticipantRepository;
        this.surveyAnswerOptionRepository = surveyAnswerOptionRepository;
    }
    
    public void deleteSurvey(Long surveyNo) {
        surveyRepository.deleteById(surveyNo);
    }
    
    @Transactional
    public Survey updateCompleteSurvey(SurveyDto dto) {
        LOGGER.info("Survey update process started for survey ID: {}", dto.getSurvey_no());

        // 1. 기존 설문 불러오기
        Survey existingSurvey = surveyRepository.findById(dto.getSurvey_no())
                .orElseThrow(() -> new RuntimeException("Survey not found for ID: " + dto.getSurvey_no()));

        // 2. 설문 기본 정보 업데이트
        existingSurvey.setSurveyTitle(dto.getSurvey_title());
        existingSurvey.setSurveyDescription(dto.getSurvey_description());
        existingSurvey.setSurveyStartDate(dto.getSurvey_start_date());
        existingSurvey.setSurveyEndDate(dto.getSurvey_end_date());
        surveyRepository.save(existingSurvey);

        // 3. 기존 질문 불러오기
        List<SurveyQuestion> existingQuestions = surveyQuestionRepository.findBySurveyNo(dto.getSurvey_no());

        // 4. 기존 질문과 새로운 질문 비교 및 처리
        updateSurveyQuestions(existingSurvey, existingQuestions, dto.getQuestions());

        // 5. 기존 참여자와 새로운 참여자 비교 및 처리
        if (dto.getParticipantMemberNos() != null) {
            updateSurveyParticipants(existingSurvey, dto.getParticipantMemberNos());
        }

        LOGGER.info("Survey update complete for survey ID: {}", existingSurvey.getSurveyNo());
        return existingSurvey;
    }

    private void updateSurveyQuestions(Survey survey, List<SurveyQuestion> existingQuestions, List<SurveyQuestionDto> newQuestions) {
        Set<Long> updatedQuestionIds = new HashSet<>();

        for (SurveyQuestionDto newQuestionDto : newQuestions) {
            if (newQuestionDto.getSurvey_question_no() != null) {
                // 기존 질문 업데이트
                SurveyQuestion existingQuestion = existingQuestions.stream()
                        .filter(q -> q.getSurveyQuestionNo().equals(newQuestionDto.getSurvey_question_no()))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Question not found for update: " + newQuestionDto.getSurvey_question_no()));

                existingQuestion.setSurveyQuestionText(newQuestionDto.getSurvey_question_text());
                existingQuestion.setSurveyQuestionType(newQuestionDto.getSurvey_question_type());
                surveyQuestionRepository.save(existingQuestion);

                // 선택형 질문일 경우 보기(옵션) 업데이트
                if (newQuestionDto.getSurvey_question_type() == 0) {
                    updateSurveyOptions(existingQuestion, newQuestionDto.getOptions());
                }

                updatedQuestionIds.add(existingQuestion.getSurveyQuestionNo());
            } else {
                // 새로운 질문 추가
                SurveyQuestion newQuestion = SurveyQuestion.builder()
                        .survey(survey)
                        .surveyQuestionText(newQuestionDto.getSurvey_question_text())
                        .surveyQuestionType(newQuestionDto.getSurvey_question_type())
                        .build();

                SurveyQuestion savedQuestion = surveyQuestionRepository.save(newQuestion);
                if (newQuestionDto.getSurvey_question_type() == 0) {
                    saveSurveyOptions(savedQuestion, newQuestionDto.getOptions());
                }
            }
        }

        // 기존 질문 중에서 삭제된 질문 처리
        List<SurveyQuestion> questionsToDelete = existingQuestions.stream()
                .filter(q -> !updatedQuestionIds.contains(q.getSurveyQuestionNo()))
                .collect(Collectors.toList());

        for (SurveyQuestion questionToDelete : questionsToDelete) {
            surveyQuestionRepository.delete(questionToDelete);
        }
    }

    private void updateSurveyOptions(SurveyQuestion question, List<String> newOptions) {
        List<SurveyOption> existingOptions = surveyOptionRepository.findByQuestionNo(question.getSurveyQuestionNo());
        Set<Long> updatedOptionIds = new HashSet<>();

        for (String optionText : newOptions) {
            SurveyOption existingOption = existingOptions.stream()
                    .filter(opt -> opt.getSurveyOptionAnswer().equals(optionText))
                    .findFirst()
                    .orElse(null);

            if (existingOption != null) {
                updatedOptionIds.add(existingOption.getSurveyOptionNo());
            } else {
                SurveyOption newOption = SurveyOption.builder()
                        .surveyQuestion(question)
                        .surveyOptionAnswer(optionText)
                        .build();
                surveyOptionRepository.save(newOption);
            }
        }

        // 기존 옵션 중 삭제된 옵션 처리
        List<SurveyOption> optionsToDelete = existingOptions.stream()
                .filter(opt -> !updatedOptionIds.contains(opt.getSurveyOptionNo()))
                .collect(Collectors.toList());

        for (SurveyOption optionToDelete : optionsToDelete) {
            surveyOptionRepository.delete(optionToDelete);
        }
    }

    private void updateSurveyParticipants(Survey savedSurvey, List<Long> participantMemberNos) {
        List<SurveyParticipant> existingParticipants = surveyParticipantRepository.findBySurvey(savedSurvey);
        Set<Long> updatedParticipantIds = new HashSet<>();

        // 새로운 참여자 업데이트 또는 추가
        for (Long memberNo : participantMemberNos) {
            SurveyParticipant existingParticipant = existingParticipants.stream()
                    .filter(p -> p.getMember().getMemberNo().equals(memberNo))
                    .findFirst()
                    .orElse(null);

            if (existingParticipant == null) {
                // 새로운 참여자 추가
                memberRepository.findById(memberNo).ifPresent(member -> {
                    surveyParticipantRepository.save(SurveyParticipant.builder()
                            .survey(savedSurvey)
                            .member(member)
                            .surveyParticipantStatus(0)
                            .build());
                });
            } else {
                // 기존 참여자 유지
                updatedParticipantIds.add(existingParticipant.getSurveyParticipantNo());
            }
        }

        // 기존 참여자 중에서 삭제된 참여자 처리
        List<SurveyParticipant> participantsToDelete = existingParticipants.stream()
                .filter(p -> !updatedParticipantIds.contains(p.getSurveyParticipantNo()))
                .collect(Collectors.toList());

        for (SurveyParticipant participantToDelete : participantsToDelete) {
            surveyParticipantRepository.delete(participantToDelete);
        }
    }

    
    
    
    
    
    @Transactional
    public void saveSurveyAnswerOption(SurveyAnswerOptionDto surveyAnswerOptionDto) {
        Long memberNo = surveyAnswerOptionDto.getSurvey_participant_no();
        Long surveyOptionNo = surveyAnswerOptionDto.getSurvey_option_no(); 

        LOGGER.info("Saving survey answer option for memberNo: {}", memberNo);

        SurveyOption surveyOption = surveyOptionRepository.findById(surveyOptionNo)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 선택지입니다."));

        SurveyQuestion surveyQuestion = surveyOption.getSurveyQuestion();  
        Survey survey = surveyQuestion.getSurvey();

        SurveyParticipant participant = surveyParticipantRepository
            .findByMember_MemberNoAndSurvey_SurveyNo(memberNo, survey.getSurveyNo())
            .orElseThrow(() -> new IllegalArgumentException("해당 참여자를 찾을 수 없습니다."));

        SurveyAnswerOption surveyAnswerOption = SurveyAnswerOption.builder()
            .surveyParticipant(participant)
            .surveyOption(surveyOption)
            .build();
        surveyAnswerOptionRepository.save(surveyAnswerOption);
    }

    @Transactional
    public void saveSurveyTextAnswer(SurveyTextDto surveyTextDto) {
        Long memberNo = surveyTextDto.getSurvey_participant_no();
        Long surveyQuestionNo = surveyTextDto.getSurvey_question_no();    

        LOGGER.info("Saving survey text answer for memberNo: {}", memberNo);

        SurveyQuestion surveyQuestion = surveyQuestionRepository.findById(surveyQuestionNo)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 질문입니다."));

        SurveyParticipant participant = surveyParticipantRepository
            .findByMember_MemberNoAndSurvey_SurveyNo(memberNo, surveyQuestion.getSurvey().getSurveyNo())
            .orElseThrow(() -> new IllegalArgumentException("해당 참여자를 찾을 수 없습니다."));

        LOGGER.info("SurveyTextDto: {}", surveyTextDto);

        if (surveyTextDto.getSurvey_text_answer() != null) {
            String textAnswer = surveyTextDto.getSurvey_text_answer(); 
            SurveyText surveyText = SurveyText.builder()
                .surveyParticipant(participant)
                .surveyQuestion(surveyQuestion)
                .surveyTextAnswer(textAnswer)
                .build();
            surveyTextRepository.save(surveyText);
        }

        if (participant.getSurveyParticipantStatus() == 0) {
            participant.setSurveyParticipantStatus(1);
            surveyParticipantRepository.save(participant);
        }
    }

    
    @Transactional
    public void updateSurveyAnswerOption(SurveyAnswerOptionDto surveyAnswerOptionDto) {
        Long memberNo = surveyAnswerOptionDto.getSurvey_participant_no();
        Long surveyOptionNo = surveyAnswerOptionDto.getSurvey_option_no(); 

        LOGGER.info("Updating survey answer option for memberNo: {}", memberNo);

        SurveyOption surveyOption = surveyOptionRepository.findById(surveyOptionNo)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 선택지입니다."));

        SurveyQuestion surveyQuestion = surveyOption.getSurveyQuestion();
        Survey survey = surveyQuestion.getSurvey();

        // 참가자 찾기
        SurveyParticipant participant = surveyParticipantRepository
            .findByMember_MemberNoAndSurvey_SurveyNo(memberNo, survey.getSurveyNo())
            .orElseThrow(() -> new IllegalArgumentException("해당 참여자를 찾을 수 없습니다."));

        // 기존의 해당 설문 응답을 모두 찾아서 삭제
        List<SurveyAnswerOption> existingAnswers = surveyAnswerOptionRepository
            .findAllBySurveyParticipantAndSurveyOption_SurveyQuestion(participant, surveyQuestion);

        // 기존 응답 삭제
        if (!existingAnswers.isEmpty()) {
            surveyAnswerOptionRepository.deleteAll(existingAnswers);
        }

        // 새로운 응답 저장 (필요할 경우)
        SurveyAnswerOption newAnswerOption = SurveyAnswerOption.builder()
            .surveyParticipant(participant)
            .surveyOption(surveyOption)
            .build();
        surveyAnswerOptionRepository.save(newAnswerOption);
    }


    
    @Transactional
    public void updateSurveyTextAnswer(SurveyTextDto surveyTextDto) {
        Long memberNo = surveyTextDto.getSurvey_participant_no();
        Long surveyQuestionNo = surveyTextDto.getSurvey_question_no();    

        LOGGER.info("Updating survey text answer for memberNo: {}", memberNo);

        SurveyQuestion surveyQuestion = surveyQuestionRepository.findById(surveyQuestionNo)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 질문입니다."));

        SurveyParticipant participant = surveyParticipantRepository
            .findByMember_MemberNoAndSurvey_SurveyNo(memberNo, surveyQuestion.getSurvey().getSurveyNo())
            .orElseThrow(() -> new IllegalArgumentException("해당 참여자를 찾을 수 없습니다."));

        List<SurveyText> existingTextAnswers = surveyTextRepository
            .findAllBySurveyParticipantAndSurveyQuestion(participant, surveyQuestion);

        if (!existingTextAnswers.isEmpty()) {
            // 중복된 모든 기존 주관식 답변 삭제 후 새로 저장
            surveyTextRepository.deleteAll(existingTextAnswers);
        }

        // 새로운 텍스트 답변 저장
        SurveyText newTextAnswer = SurveyText.builder()
            .surveyParticipant(participant)
            .surveyQuestion(surveyQuestion)
            .surveyTextAnswer(surveyTextDto.getSurvey_text_answer())
            .build();
        surveyTextRepository.save(newTextAnswer);

        // 참가자 상태 업데이트
        if (participant.getSurveyParticipantStatus() == 0) {
            participant.setSurveyParticipantStatus(1);
            surveyParticipantRepository.save(participant);
        }
    }



    
    private List<SurveyDto> convertToDtoList(List<Object[]> surveys) {
        Set<Long> seenSurveyNos = new HashSet<>();

        return surveys.stream()
            .filter(objects -> {
                Survey survey = (Survey) objects[0];
                return seenSurveyNos.add(survey.getSurveyNo()); 
            })
            .map(objects -> {
                Survey survey = (Survey) objects[0];
                Integer participantStatus = objects[1] != null ? (Integer) objects[1] : 0;


                Long memberNo = survey.getMember().getMemberNo();
                List<Object[]> memberDetails = memberRepository.findMemberWithDepartmentAndPosition(memberNo);


                String positionName = memberDetails.isEmpty() || memberDetails.get(0)[1] == null ? "Unknown" : (String) memberDetails.get(0)[1];

                return SurveyDto.builder()
                    .survey_no(survey.getSurveyNo())
                    .survey_title(survey.getSurveyTitle())
                    .survey_start_date(survey.getSurveyStartDate())
                    .survey_end_date(survey.getSurveyEndDate())
                    .survey_status(survey.getSurveyStatus())
                    .member_name(survey.getMember().getMemberName()) 
                    .position_name(positionName) 
                    .survey_participant_status(participantStatus) 
                    .build();
            }).collect(Collectors.toList());
    }


    public Page<SurveyDto> getAllSurveyPage(Pageable pageable, SurveyDto searchDto, Long memberNo) {
        Page<Object[]> results = null;  

        String searchText = searchDto.getSearch_text();
        if (searchText != null && !searchText.isEmpty()) {
            int searchType = searchDto.getSearch_type();
            switch (searchType) {
                case 1:
                    results = surveyRepository.findSurveyByTitleOrContent(searchText, memberNo, pageable);
                    break;
                case 2:
                    results = surveyRepository.findSurveyByTitle(searchText, memberNo, pageable);
                    break;
                case 3:
                    results = surveyRepository.findSurveyByDescription(searchText, memberNo, pageable);
                    break;
            }
        } else {
            results = surveyRepository.findSurveyAll(memberNo, pageable); 
        }

        List<Object[]> filteredResults = results.getContent().stream()
            .filter(objArr -> {
                Survey survey = (Survey) objArr[0];
                return survey.getSurveyStatus() == 0 || survey.getSurveyStatus() == 1;
            })
            .collect(Collectors.toList());

        List<SurveyDto> surveyDtoList = convertToDtoList(filteredResults); 
        return new PageImpl<>(surveyDtoList, pageable, results.getTotalElements());
    }

    public Page<SurveyDto> getEndAllSurveyPage(Pageable pageable, SurveyDto searchDto, Long memberNo) {
        Page<Object[]> results = null; 

        String searchText = searchDto.getSearch_text();
        if (searchText != null && !searchText.isEmpty()) {
            int searchType = searchDto.getSearch_type();
            switch (searchType) {
                case 1:
                    results = surveyRepository.findSurveyByTitleOrContentForEndList(searchText, memberNo, pageable);
                    break;
                case 2:
                    results = surveyRepository.findSurveyByTitleForEndList(searchText, memberNo, pageable);
                    break;
                case 3:
                    results = surveyRepository.findSurveyByDescriptionForEndList(searchText, memberNo, pageable);
                    break;
                case 4:
                    results = surveyRepository.findSurveyByAuthorForEndList(searchText, memberNo, pageable);
                    break;
            }
        } else {
            results = surveyRepository.findAllEndedSurveysForMember(memberNo, pageable);
        }

        List<SurveyDto> surveyDtoList = convertToDtoList(results.getContent());
        return new PageImpl<>(surveyDtoList, pageable, results.getTotalElements());
    }

    
    public Page<SurveyDto> getIngAllSurveyPage(Pageable pageable, SurveyDto searchDto, Long memberNo) {
        Page<Object[]> results = null; 
        String searchText = searchDto.getSearch_text();

        if (searchText != null && !searchText.isEmpty()) {
            int searchType = searchDto.getSearch_type();
            switch (searchType) {
                case 1:
                    results = surveyRepository.findSurveyByTitleOrContentForIngList(searchText, memberNo, pageable);
                    break;
                case 2:
                    results = surveyRepository.findSurveyByTitleForIngList(searchText, memberNo, pageable);
                    break;
                case 3:
                    results = surveyRepository.findSurveyByDescriptionForIngList(searchText, memberNo, pageable);
                    break;
                case 4:
                    results = surveyRepository.findSurveyByAuthorForIngList(searchText, memberNo, pageable);
                    break;
            }
        } else {
            results = surveyRepository.findAllOngoingSurveysForMember(memberNo, pageable);
        }

        List<SurveyDto> surveyDtoList = convertToDtoList(results.getContent()); 
        return new PageImpl<>(surveyDtoList, pageable, results.getTotalElements());
    }
    
    public Page<SurveyDto> getIngAllSurveyPage1(Pageable pageable, SurveyDto searchDto, Long memberNo) {
        Page<Object[]> results = null;
        String searchText = searchDto.getSearch_text();

        // 검색 조건에 따른 결과 가져오기
        if (searchText != null && !searchText.isEmpty()) {
            int searchType = searchDto.getSearch_type();
            switch (searchType) {
                case 1:
                    results = surveyRepository.findSurveyByTitleOrContentForIngList(searchText, memberNo, pageable);
                    break;
                case 2:
                    results = surveyRepository.findSurveyByTitleForIngList(searchText, memberNo, pageable);
                    break;
                case 3:
                    results = surveyRepository.findSurveyByDescriptionForIngList(searchText, memberNo, pageable);
                    break;
                case 4:
                    results = surveyRepository.findSurveyByAuthorForIngList(searchText, memberNo, pageable);
                    break;
            }
        } else {
            results = surveyRepository.findAllOngoingSurveysForMember(memberNo, pageable);
        }

        // Object[]를 SurveyDto로 변환
        List<SurveyDto> surveyDtoList = convertToDtoList(results.getContent());

        // 오늘 날짜와 비교하여 필터링
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate today = LocalDate.now();  

     
        List<SurveyDto> filteredSurveyDtoList = surveyDtoList.stream()
            .filter(survey -> {
                try {
                    String startDateStr = survey.getSurvey_start_date();
                    if (startDateStr == null || startDateStr.isEmpty()) {
                        return false;  
                    }
                    LocalDate startDate = LocalDate.parse(startDateStr, formatter);
                    return !startDate.isAfter(today); 
                } catch (Exception e) {
                    return false;  
                }
            })
            .collect(Collectors.toList());

       
        if (filteredSurveyDtoList.size() < 5) {
            int remainingCount = 5 - filteredSurveyDtoList.size();

           
            List<SurveyDto> additionalSurveys = surveyDtoList.stream()
                .filter(survey -> {
                    try {
                        String startDateStr = survey.getSurvey_start_date();
                        if (startDateStr == null || startDateStr.isEmpty()) {
                            return false;  
                        }
                        LocalDate startDate = LocalDate.parse(startDateStr, formatter);
                     
                        return startDate.isBefore(today) && !filteredSurveyDtoList.contains(survey);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .limit(remainingCount)  
                .collect(Collectors.toList());

 
            filteredSurveyDtoList.addAll(additionalSurveys);
        }

      
        List<SurveyDto> finalSurveyList = filteredSurveyDtoList.stream()
            .limit(5)
            .collect(Collectors.toList());

      
        return new PageImpl<>(finalSurveyList, pageable, results.getTotalElements());
    }


    
    

    public SurveyDto selectSurveyOne(Long surveyNo) {

        Survey survey = surveyRepository.findBysurveyNo(surveyNo);
        

        Long memberNo = survey.getMember().getMemberNo();
        List<Object[]> memberDetails = memberRepository.findMemberWithDepartmentAndPosition(memberNo);
        
  
        String positionName = memberDetails.get(0)[1] != null ? (String) memberDetails.get(0)[1] : "";

        
        return SurveyDto.builder()
            .survey_no(survey.getSurveyNo())
            .survey_title(survey.getSurveyTitle())
            .survey_create_date(survey.getSurveyCreateDate())
            .survey_start_date(survey.getSurveyStartDate())
            .survey_end_date(survey.getSurveyEndDate())
            .survey_status(survey.getSurveyStatus())
            .survey_description(survey.getSurveyDescription())
            .member_name(survey.getMember().getMemberName())  
            .position_name(positionName)                      
            .build();
    }

 // 설문 질문 (객관식, 주관식) 조회
    public List<SurveyQuestionDto> getSurveyQuestions(Long surveyNo) {
        LOGGER.info("Fetching questions for surveyNo: {}", surveyNo);

        // 설문에 해당하는 질문 가져오기
        List<SurveyQuestion> questions = surveyQuestionRepository.findBySurveyNo(surveyNo);
        LOGGER.info("Fetched questions: {}", questions);

        return questions.stream().map(question -> {

            // 선택형 질문에 대한 옵션 번호 가져오기
            List<Long> optionNo = surveyOptionRepository.findByQuestionNo(question.getSurveyQuestionNo())
                    .stream()
                    .map(option -> option.getSurveyOptionNo())
                    .collect(Collectors.toList());

            // 선택형 질문에 대한 옵션 답변 가져오기
            List<String> optionAnswers = surveyOptionRepository.findByQuestionNo(question.getSurveyQuestionNo())
                    .stream()
                    .map(option -> option.getSurveyOptionAnswer())
                    .collect(Collectors.toList());

            // 각 옵션에 대한 선택된 답변 가져오기 (선택된 옵션 번호)
            List<Long> selectedOptionNos = surveyAnswerOptionRepository.findAllBySurveyOption_SurveyOptionNoIn(optionNo)
                    .stream()
                    .map(answer -> answer.getSurveyOption().getSurveyOptionNo())
                    .collect(Collectors.toList());

            // 주관식 질문에 대한 텍스트 번호 가져오기
            List<Long> textNo = surveyTextRepository.findByQuestionNo(question.getSurveyQuestionNo())
                    .stream()
                    .map(SurveyText::getSurveyTextNo)
                    .collect(Collectors.toList());

            // 주관식 질문에 대한 텍스트 답변 가져오기
            List<String> textAnswers = surveyTextRepository.findByQuestionNo(question.getSurveyQuestionNo())
                    .stream()
                    .map(SurveyText::getSurveyTextAnswer)
                    .collect(Collectors.toList());

            // SurveyQuestionDto로 변환하여 리턴
            return SurveyQuestionDto.builder()
                    .survey_question_no(question.getSurveyQuestionNo())
                    .survey_no(question.getSurvey().getSurveyNo())
                    .survey_question_text(question.getSurveyQuestionText())
                    .survey_question_type(question.getSurveyQuestionType())
                    .survey_question_essential(question.getSurveyQuestionEssential())
                    .survey_option_no(optionNo)
                    .survey_option_answer(optionAnswers)
                    .selected_option_no(selectedOptionNos) // 선택된 옵션 번호 추가
                    .survey_text_no(textNo)  // 주관식 질문 텍스트 번호
                    .survey_text_answer(textAnswers) // 주관식 질문 답변
                    .build();
        }).collect(Collectors.toList());
    }

    public boolean hasUserParticipated(Long surveyNo, Long memberNo) {
        SurveyParticipant participant = surveyParticipantRepository.findBySurveyNoAndMemberNo(surveyNo, memberNo);
        return participant != null && participant.getSurveyParticipantStatus() == 1;
    }

    public int getTotalParticipants(Long surveyNo) {
        return surveyParticipantRepository.countBySurveyNo(surveyNo);
    }

    public int getCompletedParticipants(Long surveyNo) {
        return surveyParticipantRepository.countCompletedBySurveyNo(surveyNo);
    }

    public int getParticipantsByQuestion(Long questionNo) {
        return surveyParticipantRepository.countByQuestionAndStatus(questionNo);
    }

    public Map<Long, Integer> calculateParticipationRates(List<SurveyQuestionDto> questions, int totalParticipants) {
        Map<Long, Integer> participationRates = new HashMap<>();

        if (totalParticipants == 0) {
            // 참여자가 없을 때 참여율을 0%로 처리
            for (SurveyQuestionDto question : questions) {
                participationRates.put(question.getSurvey_question_no(), 0);
            }
        } else {
            for (SurveyQuestionDto question : questions) {
                int participantsForQuestion = getParticipantsByQuestion(question.getSurvey_question_no());
                int participationRate = (int) ((participantsForQuestion / (double) totalParticipants) * 100);
                participationRates.put(question.getSurvey_question_no(), participationRate);
            }
        }

        return participationRates;
    }
    
    // 주관식 답변과 참여자 정보(이름, 직위) 가져오기
    public Map<Long, List<Object[]>> getTextAnswersBySurvey(Long surveyNo) {
        List<SurveyQuestion> questions = surveyQuestionRepository.findBySurveyNo(surveyNo);
        Map<Long, List<Object[]>> textAnswers = new HashMap<>();

        for (SurveyQuestion question : questions) {
            if (question.getSurveyQuestionType() == 1) { 
                List<Object[]> answersWithParticipants = surveyTextRepository.findTextAnswersWithParticipant(question.getSurveyQuestionNo());
                textAnswers.put(question.getSurveyQuestionNo(), answersWithParticipants);
            }
        }

        return textAnswers;
    }
    
    // 설문에 대한 옵션별 응답 수 계산
    public Map<Long, List<Object[]>> getOptionAnswerCountsBySurvey(Long surveyNo) {
       

        // 설문에 대한 옵션별 응답 수를 레포지토리에서 조회
        List<Object[]> result = surveyOptionRepository.countAnswersByOptionWithAnswer(surveyNo);

        // 각 질문별로 응답 수를 정리하여 맵으로 저장
        Map<Long, List<Object[]>> optionAnswerCounts = new HashMap<>();
        for (Object[] row : result) {
            Long questionNo = (Long) row[0];
            String optionAnswer = (String) row[1];
            Long answerCount = (Long) row[2];
            optionAnswerCounts.computeIfAbsent(questionNo, k -> new ArrayList<>())
                              .add(new Object[]{optionAnswer, answerCount});
        }

        // 정리된 데이터의 내용을 쉽게 확인할 수 있도록 로그로 출력
        optionAnswerCounts.forEach((questionNo, options) -> {
            StringBuilder sb = new StringBuilder();
            sb.append("질문 번호: ").append(questionNo).append(" -> ");
            options.forEach(option -> sb.append(Arrays.toString(option)).append(" "));
        });

        return optionAnswerCounts;
    }
    
    @Transactional
    public Survey createCompleteSurvey(SurveyDto dto) {
        LOGGER.info("Survey creation process started for: {}", dto);

        Survey survey = Survey.builder()
                .surveyTitle(dto.getSurvey_title())
                .surveyDescription(dto.getSurvey_description())
                .surveyStartDate(dto.getSurvey_start_date())
                .surveyEndDate(dto.getSurvey_end_date())
                .member(memberRepository.findById(dto.getMember_no())
                        .orElseThrow(() -> new RuntimeException("Invalid member number.")))
                .surveyStatus(dto.getSurvey_status() != null ? dto.getSurvey_status() : 0) 
                .build();

        LOGGER.info("Generated Survey Entity: {}", survey);

        Survey savedSurvey = surveyRepository.save(survey);
        LOGGER.info("Survey saved with ID: {}", savedSurvey.getSurveyNo());

        // 참여자 저장 로직
        if (dto.getParticipantMemberNos() != null && !dto.getParticipantMemberNos().isEmpty()) {
            saveSurveyParticipants(savedSurvey, dto.getParticipantMemberNos());
        }

        // 질문 저장 로직
        if (dto.getQuestions() != null && !dto.getQuestions().isEmpty()) {
            saveSurveyQuestions(savedSurvey, dto.getQuestions());
        }

        LOGGER.info("Survey creation complete for survey ID: {}", savedSurvey.getSurveyNo());
        return savedSurvey;
    }

    private void saveSurveyParticipants(Survey savedSurvey, List<Long> participantMemberNos) {
        for (Long memberNo : participantMemberNos) {
            LOGGER.info("Saving participant with memberNo: {}", memberNo);

            memberRepository.findById(memberNo).ifPresent(member -> {
                surveyParticipantRepository.save(SurveyParticipant.builder()
                        .survey(savedSurvey)
                        .member(member)
                        .surveyParticipantStatus(0)  
                        .build());
                LOGGER.info("Participant saved for survey ID: {}", savedSurvey.getSurveyNo());
            });
        }
    }

    private void saveSurveyQuestions(Survey savedSurvey, List<SurveyQuestionDto> questionDtos) {
        for (SurveyQuestionDto questionDto : questionDtos) {
            LOGGER.info("Saving question: {}", questionDto.getSurvey_question_text());

            SurveyQuestion savedQuestion = surveyQuestionRepository.save(SurveyQuestion.builder()
                    .survey(savedSurvey)
                    .surveyQuestionText(questionDto.getSurvey_question_text())
                    .surveyQuestionType(questionDto.getSurvey_question_type())
                    .surveyQuestionEssential(questionDto.getSurvey_question_essential())
                    .build());

            LOGGER.info("Question saved with ID: {}", savedQuestion.getSurveyQuestionNo());

            // 선택형 질문에 대해서만 옵션 저장
            if (questionDto.getSurvey_question_type() == 0 && questionDto.getOptions() != null) {
                saveSurveyOptions(savedQuestion, questionDto.getOptions());
            }

            // 텍스트형 질문 처리 로직 추가
            if (questionDto.getSurvey_question_type() == 1 && questionDto.getSurvey_text_answer() != null) {
                saveSurveyTextAnswers(savedQuestion, questionDto.getSurvey_text_answer());
            }
        }
    }

    private void saveSurveyOptions(SurveyQuestion savedQuestion, List<String> options) {
        for (String option : options) {
            LOGGER.info("Saving option: {}", option);
            surveyOptionRepository.save(SurveyOption.builder()
                    .surveyQuestion(savedQuestion)
                    .surveyOptionAnswer(option)
                    .build());
            LOGGER.info("Option saved for question ID: {}", savedQuestion.getSurveyQuestionNo());
        }
    }

    // 텍스트형 질문(주관식) 답변 저장 메서드
    private void saveSurveyTextAnswers(SurveyQuestion savedQuestion, List<String> textAnswers) {
        LOGGER.info("Saving text answers for question ID: {}", savedQuestion.getSurveyQuestionNo());

        // 설문에 참여한 사람들을 찾아서 매핑 (이전 작업에서 참여자와 질문은 연결됨)
        List<SurveyParticipant> participants = surveyParticipantRepository.findBySurvey(savedQuestion.getSurvey());

        // 각각의 텍스트 답변과 참여자 매핑 후 저장
        for (int i = 0; i < textAnswers.size(); i++) {
            SurveyParticipant participant = participants.get(i); // 참여자 매핑
            String answer = textAnswers.get(i); // 텍스트 답변 매핑

            SurveyText surveyText = SurveyText.builder()
                    .surveyQuestion(savedQuestion)  // 질문 객체
                    .surveyParticipant(participant)  // 참여자 객체
                    .surveyTextAnswer(answer)  // 주관식 답변
                    .build();

            surveyTextRepository.save(surveyText);
            LOGGER.info("Text answer saved for participant ID: {}, answer: {}", participant.getSurveyParticipantNo(), answer);
        }
    }


    
    @Transactional
    public void updateSurveyParticipants(SurveyAnswerOptionDto surveyAnswerOptionDto, SurveyTextDto surveyTextDto) {
        Long memberNo = surveyAnswerOptionDto.getSurvey_participant_no();
        Long surveyOptionNo = surveyAnswerOptionDto.getSurvey_option_no();
        Long surveyQuestionNo = surveyTextDto.getSurvey_question_no();  // 주관식 질문 번호 추가

        LOGGER.info("Updating survey responses for memberNo: {}", memberNo);

        SurveyParticipant participant = findParticipant(memberNo, surveyOptionNo, surveyQuestionNo);

        // 1. 객관식 응답 업데이트 처리
        if (surveyOptionNo != null) {
            updateSurveyAnswerOption(participant, surveyOptionNo);
        }

        // 2. 주관식 응답 업데이트 처리
        if (surveyTextDto.getSurvey_text_answer() != null && surveyQuestionNo != null) {
            updateSurveyTextAnswer(participant, surveyTextDto, surveyQuestionNo);
        }

        // 3. 참가자 상태 업데이트 (미참여 -> 참여)
        updateParticipantStatus(participant);
    }

    private SurveyParticipant findParticipant(Long memberNo, Long surveyOptionNo, Long surveyQuestionNo) {
        SurveyParticipant participant = null;

        if (surveyOptionNo != null) {
            SurveyOption surveyOption = surveyOptionRepository.findById(surveyOptionNo)
                    .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 선택지입니다."));

            SurveyQuestion surveyQuestion = surveyOption.getSurveyQuestion();

            participant = surveyParticipantRepository
                .findByMember_MemberNoAndSurvey_SurveyNo(memberNo, surveyQuestion.getSurvey().getSurveyNo())
                .orElseThrow(() -> new IllegalArgumentException("해당 참여자를 찾을 수 없습니다."));
        } else if (surveyQuestionNo != null) {
            SurveyQuestion surveyQuestion = surveyQuestionRepository.findById(surveyQuestionNo)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 질문입니다."));

            participant = surveyParticipantRepository
                .findByMember_MemberNoAndSurvey_SurveyNo(memberNo, surveyQuestion.getSurvey().getSurveyNo())
                .orElseThrow(() -> new IllegalArgumentException("해당 참여자를 찾을 수 없습니다."));
        }

        return participant;
    }

    private void updateSurveyAnswerOption(SurveyParticipant participant, Long surveyOptionNo) {
        SurveyOption surveyOption = surveyOptionRepository.findById(surveyOptionNo)
            .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 선택지입니다."));

        SurveyAnswerOption surveyAnswerOption = SurveyAnswerOption.builder()
            .surveyParticipant(participant)
            .surveyOption(surveyOption)
            .build();

        surveyAnswerOptionRepository.save(surveyAnswerOption);
    }

    private void updateSurveyTextAnswer(SurveyParticipant participant, SurveyTextDto surveyTextDto, Long surveyQuestionNo) {
        SurveyQuestion surveyQuestion = surveyQuestionRepository.findById(surveyQuestionNo)
            .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 질문입니다."));

        SurveyText surveyText = SurveyText.builder()
            .surveyParticipant(participant)
            .surveyQuestion(surveyQuestion)
            .surveyTextAnswer(surveyTextDto.getSurvey_text_answer())
            .build();

        surveyTextRepository.save(surveyText);
    }

    private void updateParticipantStatus(SurveyParticipant participant) {
        if (participant != null && participant.getSurveyParticipantStatus() == 0) {
            participant.setSurveyParticipantStatus(1);  // 상태를 '참여'로 변경
            surveyParticipantRepository.save(participant);
        }
    }

}

