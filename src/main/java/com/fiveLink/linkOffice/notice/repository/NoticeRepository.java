package com.fiveLink.linkOffice.notice.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fiveLink.linkOffice.notice.domain.Notice;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
	
    
    Notice findBynoticeNo(Long notice_no);

    @Query("SELECT m.memberName FROM Member m WHERE m.memberNumber = :memberNumber")
    String findMemberNameByMemberNumber(@Param("memberNumber") String memberNumber);

    @Query("SELECT n, m.memberName " +
           "FROM Notice n " +
           "JOIN n.member m " + 
           "WHERE n.noticeTitle LIKE %:searchText% " +
           "OR n.noticeContent LIKE %:searchText% " +
           "ORDER BY CASE WHEN n.noticeImportance = 1 THEN 0 ELSE 1 END, " +
           "CASE WHEN :sort = 'latest' THEN n.noticeCreateDate END DESC, " +
           "CASE WHEN :sort = 'oldest' THEN n.noticeCreateDate END ASC")
    Page<Object[]> findNoticesByTitleOrContentContainingWithMember(@Param("searchText") String searchText, @Param("sort") String sort, Pageable pageable);

    @Query("SELECT n, m.memberName " +
           "FROM Notice n " +
           "JOIN n.member m " + 
           "WHERE n.noticeTitle LIKE %:searchText% " +
           "ORDER BY CASE WHEN n.noticeImportance = 1 THEN 0 ELSE 1 END, " +
           "CASE WHEN :sort = 'latest' THEN n.noticeCreateDate END DESC, " +
           "CASE WHEN :sort = 'oldest' THEN n.noticeCreateDate END ASC")
    Page<Object[]> findNoticesByTitleWithMember(@Param("searchText") String searchText, @Param("sort") String sort, Pageable pageable);

    @Query("SELECT n, m.memberName " +
           "FROM Notice n " +
           "JOIN n.member m " + 
           "WHERE n.noticeContent LIKE %:searchText% " +
           "ORDER BY CASE WHEN n.noticeImportance = 1 THEN 0 ELSE 1 END, " +
           "CASE WHEN :sort = 'latest' THEN n.noticeCreateDate END DESC, " +
           "CASE WHEN :sort = 'oldest' THEN n.noticeCreateDate END ASC")
    Page<Object[]> findNoticesByContentWithMember(@Param("searchText") String searchText, @Param("sort") String sort, Pageable pageable);

  
    @Query("SELECT n, m.memberName " +
           "FROM Notice n " +
           "JOIN n.member m " +
           "ORDER BY CASE WHEN n.noticeImportance = 1 THEN 0 ELSE 1 END, " +
           "CASE WHEN :sort = 'latest' THEN n.noticeCreateDate END DESC, " +
           "CASE WHEN :sort = 'oldest' THEN n.noticeCreateDate END ASC")
    Page<Object[]> findNoticesAllWithMember(@Param("sort") String sort, Pageable pageable);

    @Query("SELECT n, m.memberName " +
           "FROM Notice n " +
           "JOIN n.member m " +
           "WHERE n.noticeNo = :noticeNo")
    List<Object[]> findNoticesWithMemberName(@Param("noticeNo") Long noticeNo);
    
    @Query("SELECT COUNT(n) FROM Notice n WHERE n.noticeImportance = 1")
    int countImportantNotices();
}
