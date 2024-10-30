package com.fiveLink.linkOffice.survey.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fiveLink.linkOffice.survey.domain.Survey;

@Repository
public interface SurveyRepository extends JpaRepository<Survey, Long> {
	
	 List<Survey> findBySurveyStatus(int surveyStatus);

    // 검색어(조건, 제목 또는 내용) + 로그인한 사용자의 설문조사만 조회
    @Query("SELECT DISTINCT s, sp.surveyParticipantStatus FROM Survey s " +
           "LEFT JOIN SurveyParticipant sp ON s.surveyNo = sp.survey.surveyNo " +
           "WHERE (s.surveyTitle LIKE %:searchText% OR s.surveyDescription LIKE %:searchText%) " +
           "AND s.member.memberNo = :memberNo")
    Page<Object[]> findSurveyByTitleOrContent(@Param("searchText") String searchText, @Param("memberNo") Long memberNo, Pageable pageable);

    // 검색어(조건, 제목) + 로그인한 사용자의 설문조사만 조회
    @Query("SELECT DISTINCT s, sp.surveyParticipantStatus FROM Survey s " +
           "LEFT JOIN SurveyParticipant sp ON s.surveyNo = sp.survey.surveyNo " +
           "WHERE s.surveyTitle LIKE %:searchText% AND s.member.memberNo = :memberNo")
    Page<Object[]> findSurveyByTitle(@Param("searchText") String searchText, @Param("memberNo") Long memberNo, Pageable pageable);

    // 검색어(조건, 내용) + 로그인한 사용자의 설문조사만 조회
    @Query("SELECT DISTINCT s, sp.surveyParticipantStatus FROM Survey s " +
           "LEFT JOIN SurveyParticipant sp ON s.surveyNo = sp.survey.surveyNo " +
           "WHERE s.surveyDescription LIKE %:searchText% AND s.member.memberNo = :memberNo")
    Page<Object[]> findSurveyByDescription(@Param("searchText") String searchText, @Param("memberNo") Long memberNo, Pageable pageable);

    // 전체 조회 + 로그인한 사용자의 설문조사만 조회
    @Query("SELECT DISTINCT s, sp.surveyParticipantStatus FROM Survey s " +
           "LEFT JOIN SurveyParticipant sp ON s.surveyNo = sp.survey.surveyNo " +
           "WHERE s.member.memberNo = :memberNo")
    Page<Object[]> findSurveyAll(@Param("memberNo") Long memberNo, Pageable pageable);

    // 제목 또는 내용 또는 작성자 검색 + 로그인한 사용자가 참여한 마감된 설문조사만 조회
    @Query("SELECT DISTINCT s, sp.surveyParticipantStatus FROM Survey s " +
           "JOIN SurveyParticipant sp ON s.surveyNo = sp.survey.surveyNo " +
           "JOIN Member m ON s.member.memberNo = m.memberNo " +
           "WHERE (s.surveyTitle LIKE %:searchText% OR s.surveyDescription LIKE %:searchText% OR m.memberName LIKE %:searchText%) " +
           "AND sp.member.memberNo = :memberNo AND s.surveyStatus = 1")
    Page<Object[]> findSurveyByTitleOrContentForEndList(@Param("searchText") String searchText, @Param("memberNo") Long memberNo, Pageable pageable);

    // 제목 검색 + 로그인한 사용자가 참여한 마감된 설문조사만 조회
    @Query("SELECT DISTINCT s, sp.surveyParticipantStatus FROM Survey s " +
           "JOIN SurveyParticipant sp ON s.surveyNo = sp.survey.surveyNo " +
           "WHERE s.surveyTitle LIKE %:searchText% AND sp.member.memberNo = :memberNo AND s.surveyStatus = 1")
    Page<Object[]> findSurveyByTitleForEndList(@Param("searchText") String searchText, @Param("memberNo") Long memberNo, Pageable pageable);

    // 내용 검색 + 로그인한 사용자가 참여한 마감된 설문조사만 조회
    @Query("SELECT DISTINCT s, sp.surveyParticipantStatus FROM Survey s " +
           "JOIN SurveyParticipant sp ON s.surveyNo = sp.survey.surveyNo " +
           "WHERE s.surveyDescription LIKE %:searchText% AND sp.member.memberNo = :memberNo AND s.surveyStatus = 1")
    Page<Object[]> findSurveyByDescriptionForEndList(@Param("searchText") String searchText, @Param("memberNo") Long memberNo, Pageable pageable);

    // 작성자 검색 + 로그인한 사용자가 참여한 마감된 설문조사만 조회
    @Query("SELECT DISTINCT s, sp.surveyParticipantStatus FROM Survey s " +
           "JOIN SurveyParticipant sp ON s.surveyNo = sp.survey.surveyNo " +
           "JOIN Member m ON s.member.memberNo = m.memberNo " +
           "WHERE m.memberName LIKE %:searchText AND sp.member.memberNo = :memberNo AND s.surveyStatus = 1")
    Page<Object[]> findSurveyByAuthorForEndList(@Param("searchText") String searchText, @Param("memberNo") Long memberNo, Pageable pageable);

    // 전체 조회 + 로그인한 사용자가 참여한 마감된 설문조사만 조회
    @Query("SELECT DISTINCT s, sp.surveyParticipantStatus FROM Survey s " +
           "JOIN SurveyParticipant sp ON s.surveyNo = sp.survey.surveyNo " +
           "WHERE sp.member.memberNo = :memberNo AND s.surveyStatus = 1")
    Page<Object[]> findAllEndedSurveysForMember(@Param("memberNo") Long memberNo, Pageable pageable);

    // 제목 또는 내용 또는 작성자 검색 + 로그인한 사용자가 참여한 설문조사만 조회 (진행 중인 설문만)
    @Query("SELECT DISTINCT s, sp.surveyParticipantStatus FROM Survey s " +
           "JOIN SurveyParticipant sp ON s.surveyNo = sp.survey.surveyNo " +
           "JOIN Member m ON s.member.memberNo = m.memberNo " +  
           "WHERE (s.surveyTitle LIKE %:searchText% OR s.surveyDescription LIKE %:searchText% OR m.memberName LIKE %:searchText%) " + 
           "AND sp.member.memberNo = :memberNo AND s.surveyStatus = 0")
    Page<Object[]> findSurveyByTitleOrContentForIngList(@Param("searchText") String searchText, @Param("memberNo") Long memberNo, Pageable pageable);

    // 제목 검색 + 로그인한 사용자가 참여한 진행 중인 설문조사만 조회
    @Query("SELECT DISTINCT s, sp.surveyParticipantStatus FROM Survey s " +
           "JOIN SurveyParticipant sp ON s.surveyNo = sp.survey.surveyNo " +
           "WHERE s.surveyTitle LIKE %:searchText AND sp.member.memberNo = :memberNo AND s.surveyStatus = 0")
    Page<Object[]> findSurveyByTitleForIngList(@Param("searchText") String searchText, @Param("memberNo") Long memberNo, Pageable pageable);

    // 내용 검색 + 로그인한 사용자가 참여한 진행 중인 설문조사만 조회
    @Query("SELECT DISTINCT s, sp.surveyParticipantStatus FROM Survey s " +
           "JOIN SurveyParticipant sp ON s.surveyNo = sp.survey.surveyNo " +
           "WHERE s.surveyDescription LIKE %:searchText AND sp.member.memberNo = :memberNo AND s.surveyStatus = 0")
    Page<Object[]> findSurveyByDescriptionForIngList(@Param("searchText") String searchText, @Param("memberNo") Long memberNo, Pageable pageable);

    // 작성자 검색 + 로그인한 사용자가 참여한 진행 중인 설문조사만 조회
    @Query("SELECT DISTINCT s, sp.surveyParticipantStatus FROM Survey s " +
           "JOIN SurveyParticipant sp ON s.surveyNo = sp.survey.surveyNo " +
           "JOIN Member m ON s.member.memberNo = m.memberNo " +
           "WHERE m.memberName LIKE %:searchText AND sp.member.memberNo = :memberNo AND s.surveyStatus = 0")
    Page<Object[]> findSurveyByAuthorForIngList(@Param("searchText") String searchText, @Param("memberNo") Long memberNo, Pageable pageable);

    // 전체 조회 + 로그인한 사용자가 참여한 진행 중인 설문조사만 조회
    @Query("SELECT DISTINCT s, sp.surveyParticipantStatus FROM Survey s " +
           "JOIN SurveyParticipant sp ON s.surveyNo = sp.survey.surveyNo " +
           "WHERE sp.member.memberNo = :memberNo AND s.surveyStatus = 0")
    Page<Object[]> findAllOngoingSurveysForMember(@Param("memberNo") Long memberNo, Pageable pageable);

    // 설문 번호로 설문 조회
    Survey findBysurveyNo(Long survey_no);
}
