package com.fiveLink.linkOffice.document.controller;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.fiveLink.linkOffice.document.domain.DocumentFileDto;
import com.fiveLink.linkOffice.document.domain.DocumentFolderDto;
import com.fiveLink.linkOffice.document.service.DocumentFileService;
import com.fiveLink.linkOffice.document.service.DocumentFolderService;
import com.fiveLink.linkOffice.member.domain.MemberDto;
import com.fiveLink.linkOffice.member.service.MemberService;

@Controller
public class DocumentViewController {
	
	private final DocumentFolderService documentFolderService;
	private final MemberService memberService;
	private final DocumentFileService documentFileService;
	
	private static final Logger LOGGER
		= LoggerFactory.getLogger(DocumentViewController.class);
	
	@Autowired
	public DocumentViewController(DocumentFolderService documentFolderService,
			MemberService memberService,
			DocumentFileService documentFileService) {
		this.documentFolderService = documentFolderService;
		this.memberService = memberService;
		this.documentFileService = documentFileService;
	}
	
	// 개인 문서함 : 사원번호를 받아옴 
	@GetMapping("/employee/document/personal")
	public String documentPersonalPage(Model model
			) {		
		Long memberNo = memberService.getLoggedInMemberNo();
		// memberDto 불러오기
	    List<MemberDto> memberdto = memberService.getMembersByNo(memberNo); 
		model.addAttribute("memberdto", memberdto);
		return "employee/document/personal";
	}
   // 개인 폴더 
   @GetMapping("/personal/folder")
   public ResponseEntity<List<DocumentFolderDto>> personalFolderList(
		   @RequestParam("memberNo") Long memberNo) throws IOException {
	   List<DocumentFolderDto> folderList = documentFolderService.selectPersonalFolderList(memberNo);
	   return ResponseEntity.ok(folderList);
   }
   
   // 각 폴더 파일 
   @GetMapping("/folder/file")
   public ResponseEntity<List<DocumentFileDto>> selectPersonalfileList(
		   @RequestParam("folderNo") Long folderNo) throws IOException {
	   List<DocumentFileDto> fileList = documentFileService.selectfileList(folderNo);
       return ResponseEntity.ok(fileList);
   }
   
   // 개인 문서함 파일 사이즈 
   @GetMapping("/personal/fileSize")
   public ResponseEntity<Double> getPersonalFileSize(
		   @RequestParam("memberNo") Long memberNo) throws IOException{
	   double allFileSize = documentFileService.getPersonalFileSize(memberNo);
	   return ResponseEntity.ok(allFileSize);
   }
	
	// 부서 문서함 : 부서 번호를 받아옴 
	@GetMapping("/employee/document/department/{department_no}")
	public String documentDepartmentPage(Model model,
			@PathVariable("department_no") Long departmentNo
			) {
		Long memberNo = memberService.getLoggedInMemberNo();
		// memberDto 불러오기
	    List<MemberDto> memberdto = memberService.getMembersByNo(memberNo);
	    
		model.addAttribute("memberdto", memberdto);
		return "employee/document/department";
	}
   // 부서 폴더 
   @GetMapping("/department/folder")
   public ResponseEntity<List<DocumentFolderDto>> departmentFolderList(
		   @RequestParam("deptNo") Long deptNo) throws IOException {
	   List<DocumentFolderDto> folderList = documentFolderService.selectDepartmentFolderList(deptNo);	  
	   return ResponseEntity.ok(folderList);
   }
   
   // 부서 문서함 파일 사이즈 
   @GetMapping("/department/fileSize")
   public ResponseEntity<Double> getDepartmentFileSize(
		   @RequestParam("deptNo") Long deptNo) throws IOException{
	   double allFileSize = documentFileService.getDeparmentFileSize(deptNo);
	   return ResponseEntity.ok(allFileSize);
   }
	
	// 사내 문서함 
	@GetMapping("/employee/document/company")
	public String documentCompanyPage(Model model) {
		Long memberNo = memberService.getLoggedInMemberNo();
		// memberDto 불러오기
	    List<MemberDto> memberdto = memberService.getMembersByNo(memberNo);

		model.addAttribute("memberdto", memberdto);
		return "employee/document/company";
	}
	
   // 사내 폴더 
   @GetMapping("/company/folder")
   public ResponseEntity<List<DocumentFolderDto>> companyFolderList() throws IOException {
	   List<DocumentFolderDto> folderList = documentFolderService.selectCompanyFolderList();	  
	   return ResponseEntity.ok(folderList);
   }
	
   // 사내 문서함 파일 사이즈 
   @GetMapping("/company/fileSize")
   public ResponseEntity<Double> getCompanyFileSize() throws IOException{
	   double allFileSize = documentFileService.getCompanyFileSize();
	   return ResponseEntity.ok(allFileSize);
   }
   
   // 휴지통 
   @GetMapping("/employee/document/bin")
   public String documentBinPage(Model model) {
	   Long memberNo = memberService.getLoggedInMemberNo();
	   // memberDto 불러오기
	   List<MemberDto> memberdto = memberService.getMembersByNo(memberNo);
	   
	   model.addAttribute("memberdto", memberdto);
	   return "employee/document/bin";	
   }
   // 휴지통 파일 
   @GetMapping("/file/bin")
   public ResponseEntity<List<DocumentFileDto>> selectBinFileList(
		   @RequestParam("memberNo") Long memberNo) throws IOException {
	   List<DocumentFileDto> fileList = documentFileService.documentBinList(memberNo);
       return ResponseEntity.ok(fileList);
   }
   
//   // 휴지통 파일 사이즈 
//   @GetMapping("/bin/fileSize")
//   public ResponseEntity<Double> getBinFileSize(
//		   @RequestParam("memberNo") Long memberNo) throws IOException{
//	   double allFileSize = documentFileService.getBinFileSize(memberNo);
//	   return ResponseEntity.ok(allFileSize);
//   }
}