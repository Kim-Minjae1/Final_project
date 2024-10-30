package com.fiveLink.linkOffice.survey.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.fiveLink.linkOffice.survey.domain.Survey;
import com.fiveLink.linkOffice.survey.repository.SurveyRepository;

import jakarta.transaction.Transactional;

@Service
public class SurveySchedulerService {

    private final SurveyRepository surveyRepository;

    // 생성자 주입
    public SurveySchedulerService(SurveyRepository surveyRepository) {
        this.surveyRepository = surveyRepository;
    }

    // 매일 자정(00:00)에 스케줄러 실행 (cron: 0 0 0 * * ?)
    @Scheduled(cron = "0 0 0 * * ?")  // 매일 00:00에 실행
    //@Scheduled(cron = "*/5 * * * * *")
    @Transactional  // 데이터베이스 업데이트를 위한 트랜잭션 처리
    public void updateSurveyStatus() {
        LocalDate today = LocalDate.now();  // 오늘 날짜를 가져옵니다.
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd"); 

        // 설문 리스트를 가져와서 직접 날짜 비교를 수행
        List<Survey> ongoingSurveys = surveyRepository.findBySurveyStatus(0); 

        for (Survey survey : ongoingSurveys) {
            LocalDate endDate = LocalDate.parse(survey.getSurveyEndDate(), formatter); 
            
            // 종료일이 오늘 날짜보다 이전이면 설문 상태를 마감(1)으로 변경
            if (endDate.isBefore(today)) {
                survey.setSurveyStatus(1);  // 마감 상태로 변경
            }
        }

        // 변경된 설문들을 저장
        surveyRepository.saveAll(ongoingSurveys);
    }
}