package com.fiveLink.linkOffice.member.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.fiveLink.linkOffice.member.domain.MemberDto;
import com.fiveLink.linkOffice.member.service.MemberService;
import com.fiveLink.linkOffice.organization.domain.DepartmentDto;
import com.fiveLink.linkOffice.organization.domain.PositionDto;
import com.fiveLink.linkOffice.organization.service.DepartmentService;
import com.fiveLink.linkOffice.organization.service.PositionService;
import com.fiveLink.linkOffice.util.AESUtil;


@Controller
public class MemberViewController {
	
	private final MemberService memberService;
	private final DepartmentService departmentService;
	private final PositionService positionService;
	
	@Autowired
	public MemberViewController(MemberService memberService, DepartmentService departmentService, PositionService positionService) {
		this.memberService = memberService;
		this.departmentService = departmentService;
		this.positionService = positionService;
	}
	
	// 내정보 페이지
	@GetMapping("/employee/member/mypage")
	public String myPage(Model model) throws Exception {
		Long member_no = memberService.getLoggedInMemberNo();
		List<MemberDto> memberdto = memberService.getMembersByNo(member_no);
		
	    String memberNational = memberdto.get(0).getMember_national();
	    
	    String decryptedNational = null;
	    
		if (!memberdto.isEmpty()) {
		     decryptedNational = AESUtil.decrypt(memberNational);
		}
		memberdto.get(0).setMember_national(decryptedNational);	
		
	    model.addAttribute("memberdto", memberdto);
	    return "employee/member/mypage";
	}
	// 정보 수정 페이지
	@GetMapping("/employee/member/myedit")
	public String myedit(Model model) throws Exception {
		Long member_no = memberService.getLoggedInMemberNo();
		List<MemberDto> memberdto = memberService.getMembersByNo(member_no);
		
	    String memberNational = memberdto.get(0).getMember_national();
	    
	    String decryptedNational = null;
	    
		if (!memberdto.isEmpty()) {
		     decryptedNational = AESUtil.decrypt(memberNational);
		} 
		
		memberdto.get(0).setMember_national(decryptedNational);	
		
	    model.addAttribute("memberdto", memberdto);
		return "employee/member/myedit";
	}
	
	// 정보 수정 페이지
	@GetMapping("/employee/member/digitalname")
	public String digitalname( Model model) {
		Long member_no = memberService.getLoggedInMemberNo();
		List<MemberDto> memberdto = memberService.getMembersByNo(member_no);
	    model.addAttribute("memberdto", memberdto);
	    return "employee/member/digitalname";
	}

	// 관리자 사원 등록 페이지
	@GetMapping("/admin/member/create")
	public String create(Model model) {
		// 로그인한 사원의 정보
	      Long member_no = memberService.getLoggedInMemberNo();
	      // 번호
	      List<MemberDto> memberdto = memberService.getMembersByNo(member_no);
	      // 부서명 조회 
	      List<DepartmentDto> departments = departmentService.findSubDepartment();
	      // 직위명 조회 
	      List<PositionDto> positions = positionService.getAllPositionsForSelect();
	      model.addAttribute("memberdto", memberdto);
	      model.addAttribute("departments", departments);
	      model.addAttribute("positions", positions);
		return "admin/member/create";
	}
	
	private Sort getSortOption(String sort) {
		if ("latest".equals(sort)) {
			return Sort.by(Sort.Order.desc("memberHireDate")); 
		} else if ("oldest".equals(sort)) {
			return Sort.by(Sort.Order.asc("memberHireDate")); 
		}
		return Sort.by(Sort.Order.desc("memberHireDate")); 
	}
	
	
	//[전주영] 사원 목록 조회 
	@GetMapping("/admin/member/list")
	public String list(
	    @PageableDefault(size = 10, sort = "memberHireDate", direction = Sort.Direction.DESC) Pageable pageable,
	    @RequestParam(value = "sort", defaultValue = "latest") String sort,
	    Model model, 
	    MemberDto searchdto) {
	    
	    Long member_no = memberService.getLoggedInMemberNo();
	    List<MemberDto> memberdto = memberService.getMembersByNo(member_no);

	    // 정렬 설정
	    Sort sortOption = getSortOption(sort);
	    Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortOption);

	    // 페이징 + 정렬
	    Page<MemberDto> memberPage = memberService.getAllMemberPage(sortedPageable, searchdto);
	    
	    model.addAttribute("memberdto", memberdto);
	    // 페이징 안에 content
	    model.addAttribute("memberList", memberPage.getContent());
	    model.addAttribute("page", memberPage); 
	    model.addAttribute("searchDto", searchdto);
	    model.addAttribute("currentSort", sort); 
	    
	    return "admin/member/list";
	}
	
	//[전주영] 관리자 사원 목록 조회 엑셀 다운로드 
	@GetMapping("/admin/member/excelDownload")
	public ResponseEntity<byte[]> downloadExcel(MemberDto searchDto) {
	    
	    Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
	    Page<MemberDto> memberPage = memberService.getAllMemberPage(pageable, searchDto);

	    // 엑셀 파일 생성
	    Workbook workbook = new XSSFWorkbook();
	     // 엑셀 파일 안 시트의 이름
	    Sheet sheet = workbook.createSheet("사원리스트");
	    int rowNum = 0;
	    
	    //행을 생성 후 +1
	    Row headerRow = sheet.createRow(rowNum++);
	    String[] headers = {"사번", "사원명", "부서명", "직위", "입사일", "상태"};
	    // 생성 후 배열의 값 넣기
	    for (int i = 0; i < headers.length; i++) {
	        Cell cell = headerRow.createCell(i);
	        cell.setCellValue(headers[i]);
	    }
	    
	    // 행 안에 내용 
	    for (MemberDto member : memberPage.getContent()) {
	        Row row = sheet.createRow(rowNum++);
	        row.createCell(0).setCellValue(member.getMember_number());
	        row.createCell(1).setCellValue(member.getMember_name());
	        row.createCell(2).setCellValue(member.getDepartment_name());
	        row.createCell(3).setCellValue(member.getPosition_name());
	        row.createCell(4).setCellValue(member.getMember_hire_date());
	        row.createCell(5).setCellValue(member.getMember_status() == 1 ? "퇴사" : "재직");
	    }

	    // 엑셀 파일을 바이트 배열로 변환
	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	    try {
	    	// 파일 출력
	        workbook.write(out);
	        workbook.close(); 
	    } catch (IOException e) {
	        e.printStackTrace();
	    }

	    byte[] bytes = out.toByteArray();

	    HttpHeaders headersResponse = new HttpHeaders();
	    headersResponse.setContentType(MediaType.APPLICATION_OCTET_STREAM);
	    headersResponse.setContentDispositionFormData("attachment", "사원리스트.xlsx");

	    return ResponseEntity
	            .ok()
	            .headers(headersResponse)
	            .body(bytes);
	}



	// [전주영] 관리자 사원 상세 조회 
	@GetMapping("/admin/member/detail/{member_no}")
	public String detail(@PathVariable("member_no") Long memberNo, Model model) throws Exception {
		Long member_no = memberService.getLoggedInMemberNo();
		List<MemberDto> memberdto = memberService.getMembersByNo(member_no);
		
		List<MemberDto> memberDtoList = memberService.getMembersByNo(memberNo);
		
	    String memberNational = memberDtoList.get(0).getMember_national();
	    
	    String decryptedNational = null;
	    
		if (!memberDtoList.isEmpty()) {
		     decryptedNational = AESUtil.decrypt(memberNational);
		} 
		
		memberDtoList.get(0).setMember_national(decryptedNational);

		  DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	        // 날짜를 문자열로 변환
	        memberDtoList.forEach(member -> {
	            if (member.getMember_end_date() != null) {
	                String formattedEndDate = member.getMember_end_date().format(formatter);
	                member.setFormat_end_date(formattedEndDate);
	            }
	        });
		
		model.addAttribute("memberdto", memberdto);
	    model.addAttribute("memberDtoList", memberDtoList);
	    
	    return "admin/member/detail";
	}
	
	// [전주영] 관리자 사원 수정
	@GetMapping("/admin/member/edit/{member_no}")
	public String edit(@PathVariable("member_no") Long memberNo, Model model) {
	    try {
	        Long member_no = memberService.getLoggedInMemberNo();
	        List<MemberDto> memberdto = memberService.getMembersByNo(member_no);
	        List<MemberDto> memberDtoList = memberService.getMembersByNo(memberNo);

	        List<DepartmentDto> departments = departmentService.getAllDepartments();
	        List<PositionDto> positions = positionService.getAllPositionsForSelect();

		    String memberNational = memberDtoList.get(0).getMember_national();
		    
		    String decryptedNational = null;
		    
			if (!memberDtoList.isEmpty()) {
			     decryptedNational = AESUtil.decrypt(memberNational);
			} else {
			}
			
			memberDtoList.get(0).setMember_national(decryptedNational);
			
	        model.addAttribute("memberdto", memberdto);
	        model.addAttribute("memberDtoList", memberDtoList);
	        model.addAttribute("departments", departments);
	        model.addAttribute("positions", positions);

	    }catch(Exception e) {
	    	e.printStackTrace();
	    }

	    return "admin/member/edit";
	}
	// [전주영] 사용자 주소록
	@GetMapping("/employee/member/list")
	public String memberList(Model model, MemberDto searchdto, @PageableDefault(size = 10, sort = "positionLevel", direction = Sort.Direction.DESC) Pageable pageable, @RequestParam(value = "sort", defaultValue = "latest") String sort) {
		Long member_no = memberService.getLoggedInMemberNo();
		List<MemberDto> memberdto = memberService.getMembersByNo(member_no);
	    
	    Sort sortOption = getSortOption(sort);
	    Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortOption);
	    
	    Page<MemberDto> memberPage = memberService.getAllMemberPosition(sortedPageable,searchdto);
	    
		model.addAttribute("memberdto", memberdto);
		model.addAttribute("memberList", memberPage.getContent());
		model.addAttribute("page", memberPage);
		model.addAttribute("searchDto", searchdto);
		model.addAttribute("currentSort", sort);
		
		
		return "employee/member/list";
	}
	
	// [전주영] 사용자 사원 상세 조회 
	@GetMapping("/employee/member/detail/{member_no}")
	public String memberDetail(@PathVariable("member_no") Long memberNo, Model model) throws Exception {
		Long member_no = memberService.getLoggedInMemberNo();
		List<MemberDto> memberdto = memberService.getMembersByNo(member_no);
		List<MemberDto> memberDtoList = memberService.getMembersByNo(memberNo);
		
		  DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	        // 날짜를 문자열로 변환
	        memberDtoList.forEach(member -> {
	            if (member.getMember_end_date() != null) {
	                String formattedEndDate = member.getMember_end_date().format(formatter);
	                member.setFormat_end_date(formattedEndDate);
	            }
	        });
		
		model.addAttribute("memberdto", memberdto);
	    model.addAttribute("memberDtoList", memberDtoList);
	    
	    return "employee/member/detail";
	}
	
}
