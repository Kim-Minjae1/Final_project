package com.fiveLink.linkOffice.notice.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.fiveLink.linkOffice.member.domain.Member;
import com.fiveLink.linkOffice.member.repository.MemberRepository;
import com.fiveLink.linkOffice.notice.controller.NoticeApiController;
import com.fiveLink.linkOffice.notice.domain.Notice;
import com.fiveLink.linkOffice.notice.domain.NoticeDto;
import com.fiveLink.linkOffice.notice.repository.NoticeRepository;

import jakarta.transaction.Transactional;

@Service
public class NoticeService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NoticeApiController.class);
    private final NoticeRepository noticeRepository;
    private final MemberRepository memberRepository;

    @Autowired
    public NoticeService(NoticeRepository noticeRepository, MemberRepository memberRepository) {
        this.noticeRepository = noticeRepository;
        this.memberRepository = memberRepository;
    }
    
    
    
    
    // 중요 공지사항 개수 확인 메서드
    public int countImportantNotices() {
        return noticeRepository.countImportantNotices(); 
    }
    
    public Notice createNotice(NoticeDto dto) {
        Long noticeWriter = dto.getMember_no(); 
        Member member = memberRepository.findBymemberNo(noticeWriter);
        
        Notice notice = Notice.builder()
                .noticeTitle(dto.getNotice_title())
                .noticeContent(dto.getNotice_content())
                .noticeCreateDate(dto.getNotice_create_date())
                .noticeImportance(dto.getNotice_importance())
                .noticeOriImg(dto.getNotice_ori_img())
                .noticeNewImg(dto.getNotice_new_img())
                .member(member)
                .build();
        return noticeRepository.save(notice);
    }

    // managerName을 통해 member_no 조회하는 메서드
    public Long findMemberNoByManagerName(String managerName) {
        Member member = memberRepository.findByMemberName(managerName);
        return member.getMemberNo();
    }

    // 멤버 넘버로 멤버 이름 조회
    public String findNoticeNameByNumber(String memberNumber) {
        return noticeRepository.findMemberNameByMemberNumber(memberNumber);
    }
    
    public Page<NoticeDto> getAllNoticePage(Pageable pageable, String sort, NoticeDto searchDto) {
        Page<Object[]> results = null;
        
        String searchText = searchDto.getSearch_text();
        if (searchText != null && !searchText.isEmpty()) {
            int searchType = searchDto.getSearch_type();
            switch (searchType) {
                // 제목 또는 내용 검색
                case 1:
                    results = noticeRepository.findNoticesByTitleOrContentContainingWithMember(searchText, sort, pageable);
                    break;
                // 제목 검색
                case 2:
                    results = noticeRepository.findNoticesByTitleWithMember(searchText, sort, pageable);
                    break;
                // 내용 검색
                case 3:
                    results = noticeRepository.findNoticesByContentWithMember(searchText, sort, pageable);
                    break;
            }
        } else {
            results = noticeRepository.findNoticesAllWithMember(sort, pageable);
        }

        List<NoticeDto> noticeDtoList = convertToDtoList(results.getContent());
        return new PageImpl<>(noticeDtoList, pageable, results.getTotalElements());
    }
    
    private List<NoticeDto> convertToDtoList(List<Object[]> results) {
        return results.stream().map(result -> {
            Notice notice = (Notice) result[0];
            String memberName = (String) result[1];
            
            // 멤버 이름과 포지션 정보 가져오기
            Long memberNo = notice.getMember().getMemberNo();
            List<Object[]> memberDetails = memberRepository.findMemberWithDepartmentAndPosition(memberNo);
            String positionName = (String) memberDetails.get(0)[1]; 

            return NoticeDto.builder()
                    .notice_no(notice.getNoticeNo())
                    .notice_title(notice.getNoticeTitle())
                    .notice_content(notice.getNoticeContent())
                    .notice_create_date(notice.getNoticeCreateDate())
                    .notice_update_date(notice.getNoticeUpdateDate())
                    .notice_importance(notice.getNoticeImportance())
                    .notice_new_img(notice.getNoticeNewImg())
                    .notice_ori_img(notice.getNoticeOriImg())
                    .member_name(memberName)
                    .position_name(positionName) 
                    .build();
        }).collect(Collectors.toList());
    }

    public List<NoticeDto> getNoticeByNo(Long noticeNo) {
        List<Object[]> results = noticeRepository.findNoticesWithMemberName(noticeNo);
        return convertToDtoList(results);
    }
    
    public NoticeDto selectNoticeOne(Long notice_no) {
        Notice notice = noticeRepository.findBynoticeNo(notice_no);
        
        NoticeDto dto = NoticeDto.builder()
                .notice_no(notice.getNoticeNo())
                .notice_title(notice.getNoticeTitle())
                .notice_content(notice.getNoticeContent())
                .notice_create_date(notice.getNoticeCreateDate())
                .notice_update_date(notice.getNoticeUpdateDate())
                .notice_new_img(notice.getNoticeNewImg())
                .notice_ori_img(notice.getNoticeOriImg())
                .notice_importance(notice.getNoticeImportance())
                .member_no(notice.getMember().getMemberNo())
                .member_name(notice.getMember().getMemberName())
                .build();
        return dto;
    }
    
    @Transactional
    public Notice updateNotice(NoticeDto dto) {
        // 기존 공지사항 불러오기
        Notice notice = noticeRepository.findBynoticeNo(dto.getNotice_no());
           

        // 공지사항 정보 수정
        notice.setNoticeTitle(dto.getNotice_title());
        notice.setNoticeContent(dto.getNotice_content());
        notice.setNoticeImportance(dto.getNotice_importance());
        notice.setNoticeUpdateDate(dto.getNotice_update_date());

        // 파일 업데이트 처리
        if (dto.getNotice_ori_img() != null && !dto.getNotice_ori_img().isEmpty()) {
            notice.setNoticeOriImg(dto.getNotice_ori_img());
            notice.setNoticeNewImg(dto.getNotice_new_img());
        }

        return noticeRepository.save(notice);
    }

    
    public int deleteNotice(Long notice_no) {
    	int result =0;
    	try {
    		noticeRepository.deleteById(notice_no);
    		result= 1;
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
    	return result;
    }
}
