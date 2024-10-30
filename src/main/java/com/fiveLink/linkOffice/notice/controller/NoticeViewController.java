package com.fiveLink.linkOffice.notice.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.fiveLink.linkOffice.member.domain.MemberDto;
import com.fiveLink.linkOffice.member.service.MemberService;
import com.fiveLink.linkOffice.notice.domain.Notice;
import com.fiveLink.linkOffice.notice.domain.NoticeDto;
import com.fiveLink.linkOffice.notice.service.NoticeService;

@Controller
public class NoticeViewController {
    private static final Logger LOGGER = LoggerFactory.getLogger(NoticeViewController.class);
    private final NoticeService noticeService;
    private final MemberService memberService;

    @Autowired
    public NoticeViewController(MemberService memberService, NoticeService noticeService) {
        this.memberService = memberService;
        this.noticeService = noticeService;
    }

    // 공지사항 생성 페이지
    @GetMapping("/admin/notice/create/{member_no}")
    public String noticeCreate(@PathVariable("member_no") Long memberNo, Model model) {
        // memberNo로부터 memberDto 가져오기
        List<MemberDto> memberDto = memberService.getMembersByNo(memberNo);
        model.addAttribute("memberDto", memberDto);
    	
    	Long member_no = memberService.getLoggedInMemberNo();
		List<MemberDto> memberdto = memberService.getMembersByNo(member_no);
		model.addAttribute("memberdto", memberdto);
		
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String noticeNumber = authentication.getName();
        String noticeName = noticeService.findNoticeNameByNumber(noticeNumber);
        model.addAttribute("manager", noticeName);  

        return "admin/notice/notice_create"; 
    }

    
    private Sort getSortOption(String sort) {
        if ("latest".equals(sort)) {
            return Sort.by(Sort.Order.desc("noticeCreateDate"));
        } else if ("oldest".equals(sort)) {
            return Sort.by(Sort.Order.asc("noticeCreateDate"));
        }
        return Sort.by(Sort.Order.desc("noticeCreateDate"));
    }

    @GetMapping("/admin/notice/list")
    public String noticeList(
        @PageableDefault(size = 10, sort = "noticeCreateDate", direction = Sort.Direction.DESC) Pageable pageable,
        @RequestParam(value = "sort", defaultValue = "latest") String sort,
        Model model,
        NoticeDto searchDto) {
    	
    	Long member_no = memberService.getLoggedInMemberNo();
		List<MemberDto> memberdto = memberService.getMembersByNo(member_no);
		model.addAttribute("memberdto", memberdto);
		
        Pageable sortedPageable = PageRequest.of(
            pageable.getPageNumber(),
            pageable.getPageSize(),
            getSortOption(sort)
        );

        Page<NoticeDto> noticePage = noticeService.getAllNoticePage(sortedPageable, sort, searchDto);

        model.addAttribute("noticeList", noticePage.getContent());
        model.addAttribute("page", noticePage);
        model.addAttribute("searchDto", searchDto);
        model.addAttribute("currentSort", sort);

        return "admin/notice/notice_list";
    }
    
    // 공지사항 상세 페이지
    @GetMapping("/admin/notice/list/{notice_no}")
    public String selectNoticeOne(Model model,
    		@PathVariable("notice_no")Long notice_no) {
    	
    	Long member_no = memberService.getLoggedInMemberNo();
		List<MemberDto> memberdto = memberService.getMembersByNo(member_no);
		model.addAttribute("memberdto", memberdto);
    	
    	NoticeDto dto = noticeService.selectNoticeOne(notice_no);
    	model.addAttribute("dto",dto);
    	
    	
    	return "admin/notice/notice_detail";
    }
    
    // 공지사항 수정 페이지
    @GetMapping("/admin/notice/update/{notice_no}")
    public String updateNoticePage(Model model,
    		@PathVariable("notice_no")Long notice_no) {
    	
    	Long member_no = memberService.getLoggedInMemberNo();
		List<MemberDto> memberdto = memberService.getMembersByNo(member_no);
		model.addAttribute("memberdto", memberdto);
    	NoticeDto dto = noticeService.selectNoticeOne(notice_no);
    	model.addAttribute("dto",dto);
    	
    	
    	return "admin/notice/notice_update";
    }
    
    
    
}