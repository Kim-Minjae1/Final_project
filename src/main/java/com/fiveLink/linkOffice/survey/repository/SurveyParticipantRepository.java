package com.fiveLink.linkOffice.survey.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fiveLink.linkOffice.survey.domain.Survey;
import com.fiveLink.linkOffice.survey.domain.SurveyParticipant;

@Repository
public interface SurveyParticipantRepository extends JpaRepository<SurveyParticipant, Long> {

    // 설문 번호와 사용자 번호로 설문 참여 정보를 조회하는 메서드
    @Query("SELECT sp FROM SurveyParticipant sp WHERE sp.survey.surveyNo = :surveyNo AND sp.member.memberNo = :memberNo")
    SurveyParticipant findBySurveyNoAndMemberNo(@Param("surveyNo") Long surveyNo, @Param("memberNo") Long memberNo);

    // 설문 번호로 전체 참여자 수 계산
    @Query("SELECT COUNT(sp) FROM SurveyParticipant sp WHERE sp.survey.surveyNo = :surveyNo")
    int countBySurveyNo(@Param("surveyNo") Long surveyNo);

    // 설문 번호로 참여 완료자 수 계산
    @Query("SELECT COUNT(sp) FROM SurveyParticipant sp WHERE sp.survey.surveyNo = :surveyNo AND sp.surveyParticipantStatus = 1")
    int countCompletedBySurveyNo(@Param("surveyNo") Long surveyNo);

    // 질문별 참여자 수 계산 (수정된 쿼리)
    @Query("SELECT COUNT(sp) FROM SurveyParticipant sp JOIN sp.survey.surveyQuestion sq WHERE sq.surveyQuestionNo = :questionNo AND sp.surveyParticipantStatus = 1")
    int countByQuestionAndStatus(@Param("questionNo") Long questionNo);
    
    Optional<SurveyParticipant> findByMember_MemberNoAndSurvey_SurveyNo(Long memberNo, Long surveyNo);
    
 // 특정 설문에 참여한 모든 참여자를 조회하는 메서드
    List<SurveyParticipant> findBySurvey(Survey survey);
    
    
}
