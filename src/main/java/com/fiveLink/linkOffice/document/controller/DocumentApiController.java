package com.fiveLink.linkOffice.document.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fiveLink.linkOffice.document.domain.DocumentFile;
import com.fiveLink.linkOffice.document.domain.DocumentFolder;
import com.fiveLink.linkOffice.document.repository.DocumentFileRepository;
import com.fiveLink.linkOffice.document.repository.DocumentFolderRepository;
import com.fiveLink.linkOffice.document.service.DocumentFileService;
import com.fiveLink.linkOffice.document.service.DocumentFolderService;
import com.fiveLink.linkOffice.member.domain.Member;
import com.fiveLink.linkOffice.member.repository.MemberRepository;
import com.fiveLink.linkOffice.member.service.MemberService;
import com.fiveLink.linkOffice.organization.domain.Department;
import com.fiveLink.linkOffice.organization.repository.DepartmentRepository;

@Controller
public class DocumentApiController {
	
	private final DocumentFolderService documentFolderService;
	private final MemberService memberService;
	private final DocumentFileService documentFileService;
	private MemberRepository memberRepository;
	private DepartmentRepository departmentRepository;
	private DocumentFolderRepository documentFolderRepository;
	private DocumentFileRepository documentFileRepository;
	
	private static final Logger LOGGER
		= LoggerFactory.getLogger(DocumentApiController.class);
	
	@Autowired
	public DocumentApiController(DocumentFolderService documentFolderService,
			MemberService memberService,
			DocumentFileService documentFileService,
			MemberRepository memberRepository, 
			DepartmentRepository departmentRepository,
			DocumentFolderRepository documentFolderRepository,
			DocumentFileRepository documentFileRepository) {
		this.documentFolderService = documentFolderService;
		this.memberService = memberService;
		this.documentFileService = documentFileService;
		this.memberRepository = memberRepository;
		this.departmentRepository = departmentRepository;
		this.documentFolderRepository = documentFolderRepository;
		this.documentFileRepository = documentFileRepository;
	}
	
   // 개인 첫 폴더 
   @PostMapping("/personal/first/folder")
   @ResponseBody
   public Map<String, Object> personalFirstFolder(@RequestBody Map<String, Object> payload){
	  Map<String, Object> resultMap = new HashMap<>();
	  resultMap.put("res_code", "404");
	  resultMap.put("res_msg", "경로 오류");
	
	  String folderName = (String) payload.get("folderName");
	  String memberNoStr = (String) payload.get("memberNo");
	  String deptNoStr = (String) payload.get("deptNo");
    
	  Long memberNo = Long.valueOf(memberNoStr);
	  Long deptNo = Long.valueOf(deptNoStr);
      Long folderLevel = 1L;
      Long docBoxType = 0L;
      Long folderStatus = 0L;
      Long parentNo = null;
      
      Member member = memberRepository.findByMemberNo(memberNo);
      Department department = departmentRepository.findByDepartmentNo(deptNo);

      DocumentFolder documentFolder = DocumentFolder.builder()
            .documentFolderName(folderName)
            .documentFolderLevel(folderLevel)
            .department(department)
            .documentBoxType(docBoxType)
            .member(member)
            .documentFolderStatus(folderStatus)
            .build();

      int result = documentFolderService.personalFirstFolder(documentFolder);
      DocumentFolder documentFirstFolder = 
    		  documentFolderRepository.findByMemberMemberNoAndDocumentBoxTypeAndDocumentFolderParentNoAndDocumentFolderStatus(memberNo, docBoxType, parentNo, folderStatus);

      if (result > 0) {
    	  resultMap.put("res_code", "200");
    	  resultMap.put("res_msg", "폴더가 생성되었습니다.");
    	  resultMap.put("folderNo", documentFirstFolder.getDocumentFolderNo());
      } 
      return resultMap;
   }	
   // 개인 폴더 생성  
   @PostMapping("/personal/create/folder")
   @ResponseBody
   public Map<String, Object> personalCreateFolder(@RequestBody Map<String, Object> payload){
	  Map<String, Object> resultMap = new HashMap<>();
	  resultMap.put("res_code", "404");
	  resultMap.put("res_msg", "경로 오류");
	
	  String folderName = (String) payload.get("folderName");
	  String folderNoStr = (String) payload.get("parentFolderNo");  
	  String memberNoStr = (String) payload.get("memberNo");
	  String deptNoStr = (String) payload.get("deptNo");
    
	  Long memberNo = Long.valueOf(memberNoStr);
	  Long deptNo = Long.valueOf(deptNoStr);
	  Long folderNo = Long.valueOf(folderNoStr);
      Long docBoxType = 0L;
      Long folderStatus = 0L;
      
      Member member = memberRepository.findByMemberNo(memberNo);
      Department department = departmentRepository.findByDepartmentNo(deptNo);
      DocumentFolder documentFolder = documentFolderRepository.findByDocumentFolderNo(folderNo);
      
      Long folderLevel = documentFolder.getDocumentFolderLevel() + 1;
      
      DocumentFolder newDocumentFolder = DocumentFolder.builder()
            .documentFolderName(folderName)
            .documentFolderParentNo(folderNo)            
            .documentFolderLevel(folderLevel)
            .department(department)
            .documentBoxType(docBoxType)
            .member(member)
            .documentFolderStatus(folderStatus)
            .build();

      int result = documentFolderService.personalCreateFolder(newDocumentFolder);

      if (result > 0) {
    	  List<DocumentFolder> folderList = documentFolderRepository.findByDocumentFolderParentNo(folderNo);
    	  DocumentFolder createdFolder = folderList.get(folderList.size() - 1);
    	  resultMap.put("res_code", "200");
    	  resultMap.put("res_msg", "폴더가 생성되었습니다.");
    	  resultMap.put("folderNo" ,createdFolder.getDocumentFolderNo());
      } 
      return resultMap;
   }
   
   // 부서 첫 폴더 
   @PostMapping("/department/first/folder")
   @ResponseBody
   public Map<String, Object> departmentFirstFolder(@RequestBody Map<String, Object> payload){
	  Map<String, Object> resultMap = new HashMap<>();
	  resultMap.put("res_code", "404");
	  resultMap.put("res_msg", "경로 오류");
	
	  String folderName = (String) payload.get("folderName");
	  String memberNoStr = (String) payload.get("memberNo");
	  String deptNoStr = (String) payload.get("deptNo");
    
	  Long memberNo = Long.valueOf(memberNoStr);
	  Long deptNo = Long.valueOf(deptNoStr);
      Long folderLevel = 1L;
      Long docBoxType = 1L;
      Long folderStatus = 0L;
      Long parentNo = null;
      
      Member member = memberRepository.findByMemberNo(memberNo);
      Department department = departmentRepository.findByDepartmentNo(deptNo);

      DocumentFolder documentFolder = DocumentFolder.builder()
            .documentFolderName(folderName)
            .documentFolderLevel(folderLevel)
            .department(department)
            .documentBoxType(docBoxType)
            .member(member)
            .documentFolderStatus(folderStatus)
            .build();

      int result = documentFolderService.departmentFirstFolder(documentFolder);
      DocumentFolder documentFirstFolder = 
    		  documentFolderRepository.findByDepartmentDepartmentNoAndDocumentBoxTypeAndDocumentFolderParentNoAndDocumentFolderStatus(deptNo, docBoxType, parentNo, folderStatus);

      if (result > 0) {
    	  resultMap.put("res_code", "200");
    	  resultMap.put("res_msg", "폴더가 생성되었습니다.");
    	  resultMap.put("folderNo", documentFirstFolder.getDocumentFolderNo());
      } 
      return resultMap;
   }	
 
   // 부서 폴더 생성  
   @PostMapping("/department/create/folder")
   @ResponseBody
   public Map<String, Object> departmentCreateFolder(@RequestBody Map<String, Object> payload){
	  Map<String, Object> resultMap = new HashMap<>();
	  resultMap.put("res_code", "404");
	  resultMap.put("res_msg", "경로 오류");
	
	  String folderName = (String) payload.get("folderName");
	  String folderNoStr = (String) payload.get("parentFolderNo");  
	  String memberNoStr = (String) payload.get("memberNo");
	  String deptNoStr = (String) payload.get("deptNo");
    
	  Long memberNo = Long.valueOf(memberNoStr);
	  Long deptNo = Long.valueOf(deptNoStr);
	  Long folderNo = Long.valueOf(folderNoStr);
      Long docBoxType = 1L;
      Long folderStatus = 0L;
      
      Member member = memberRepository.findByMemberNo(memberNo);
      Department department = departmentRepository.findByDepartmentNo(deptNo);
      DocumentFolder documentFolder = documentFolderRepository.findByDocumentFolderNo(folderNo);
      
      Long folderLevel = documentFolder.getDocumentFolderLevel() + 1;
      
      DocumentFolder newDocumentFolder = DocumentFolder.builder()
            .documentFolderName(folderName)
            .documentFolderParentNo(folderNo)            
            .documentFolderLevel(folderLevel)
            .department(department)
            .documentBoxType(docBoxType)
            .member(member)
            .documentFolderStatus(folderStatus)
            .build();

      int result = documentFolderService.departmentCreateFolder(newDocumentFolder);

      if (result > 0) {
    	  List<DocumentFolder> folderList = documentFolderRepository.findByDocumentFolderParentNo(folderNo);
    	  DocumentFolder createdFolder = folderList.get(folderList.size() - 1);
    	  resultMap.put("res_code", "200");
    	  resultMap.put("res_msg", "폴더가 생성되었습니다.");
    	  resultMap.put("folderNo" ,createdFolder.getDocumentFolderNo());
      } 
      return resultMap;
   }
   
   // 사내 첫 폴더 
   @PostMapping("/company/first/folder")
   @ResponseBody
   public Map<String, Object> companyFirstFolder(@RequestBody Map<String, Object> payload){
	  Map<String, Object> resultMap = new HashMap<>();
	  resultMap.put("res_code", "404");
	  resultMap.put("res_msg", "경로 오류");
	
	  String folderName = (String) payload.get("folderName");
	  String memberNoStr = (String) payload.get("memberNo");
	  String deptNoStr = (String) payload.get("deptNo");
    
	  Long memberNo = Long.valueOf(memberNoStr);
	  Long deptNo = Long.valueOf(deptNoStr);
      Long folderLevel = 1L;
      Long docBoxType = 2L;
      Long folderStatus = 0L;
      Long parentNo = null;
      
      Member member = memberRepository.findByMemberNo(memberNo);
      Department department = departmentRepository.findByDepartmentNo(deptNo);

      DocumentFolder documentFolder = DocumentFolder.builder()
            .documentFolderName(folderName)
            .documentFolderLevel(folderLevel)
            .department(department)
            .documentBoxType(docBoxType)
            .member(member)
            .documentFolderStatus(folderStatus)
            .build();

      int result = documentFolderService.departmentFirstFolder(documentFolder);
      DocumentFolder documentFirstFolder = 
    		  documentFolderRepository.findByDocumentBoxTypeAndDocumentFolderParentNoAndDocumentFolderStatus(docBoxType, parentNo, folderStatus);

      if (result > 0) {
    	  resultMap.put("res_code", "200");
    	  resultMap.put("res_msg", "폴더가 생성되었습니다.");
    	  resultMap.put("folderNo", documentFirstFolder.getDocumentFolderNo());
      } 
      return resultMap;
   }
   
   // 사내 폴더 생성  
   @PostMapping("/company/create/folder")
   @ResponseBody
   public Map<String, Object> companyCreateFolder(@RequestBody Map<String, Object> payload){
	  Map<String, Object> resultMap = new HashMap<>();
	  resultMap.put("res_code", "404");
	  resultMap.put("res_msg", "경로 오류");
	
	  String folderName = (String) payload.get("folderName");
	  String folderNoStr = (String) payload.get("parentFolderNo");  
	  String memberNoStr = (String) payload.get("memberNo");
	  String deptNoStr = (String) payload.get("deptNo");
    
	  Long memberNo = Long.valueOf(memberNoStr);
	  Long deptNo = Long.valueOf(deptNoStr);
	  Long folderNo = Long.valueOf(folderNoStr);
      Long docBoxType = 2L;
      Long folderStatus = 0L;

      Member member = memberRepository.findByMemberNo(memberNo);
      Department department = departmentRepository.findByDepartmentNo(deptNo);
      DocumentFolder documentFolder = documentFolderRepository.findByDocumentFolderNo(folderNo);
      
      Long folderLevel = documentFolder.getDocumentFolderLevel() + 1;
      
      DocumentFolder newDocumentFolder = DocumentFolder.builder()
            .documentFolderName(folderName)
            .documentFolderParentNo(folderNo)            
            .documentFolderLevel(folderLevel)
            .department(department)
            .documentBoxType(docBoxType)
            .member(member)
            .documentFolderStatus(folderStatus)
            .build();

      int result = documentFolderService.companyCreateFolder(newDocumentFolder);
      if (result > 0) {
    	  List<DocumentFolder> folderList = documentFolderRepository.findByDocumentFolderParentNo(folderNo);
    	  DocumentFolder createdFolder = folderList.get(folderList.size() - 1);
    	  resultMap.put("res_code", "200");
    	  resultMap.put("res_msg", "폴더가 생성되었습니다.");
    	  resultMap.put("folderNo" ,createdFolder.getDocumentFolderNo());
      } 
      return resultMap;
   }
   
   // 폴더 이름 변경
   @PostMapping("/change/folder/name")
   @ResponseBody
   public Map<String, String> changedFolderName(@RequestBody Map<String, Object> payload){
		  Map<String, String> resultMap = new HashMap<>();
		  resultMap.put("res_code", "404");
		  resultMap.put("res_msg", "경로 오류");
		
		  String newFolderName = (String) payload.get("folderName");
		  String folderNoStr = (String) payload.get("folderNo");
	    
		  Long folderNo = Long.valueOf(folderNoStr);
		  
		  DocumentFolder oridocumentfolder = documentFolderRepository.findByDocumentFolderNo(folderNo);

	      DocumentFolder newDocumentFolder = DocumentFolder.builder()
	    		.documentFolderNo(oridocumentfolder.getDocumentFolderNo())
	            .documentFolderName(newFolderName)
	            .documentFolderParentNo(oridocumentfolder.getDocumentFolderParentNo())
	            .documentFolderLevel(oridocumentfolder.getDocumentFolderLevel())
	            .department(oridocumentfolder.getDepartment())
	            .documentBoxType(oridocumentfolder.getDocumentBoxType())
	            .member(oridocumentfolder.getMember())
	            .documentFolderCreateDate(oridocumentfolder.getDocumentFolderCreateDate())
	            .documentFolderUpdateDate(LocalDateTime.now())
	            .documentFolderStatus(oridocumentfolder.getDocumentFolderStatus())
	            .build();
	      if(documentFolderService.changeFolderName(newDocumentFolder) > 0) {
          	resultMap.put("res_code", "200");
          	resultMap.put("res_msg", "폴더명이 변경되었습니다.");  
	      }
	      return resultMap;
	   }

   // 폴더 삭제 전 부모 폴더 존재 여부 확인
   @PostMapping("/document/parent/existence")
   @ResponseBody
   public Map<String, Object> parentFolderExistence(@RequestBody Map<String, Object> payload){
   	  Map<String, Object> resultMap = new HashMap<>();
   	  resultMap.put("res_code", "404");
   	  resultMap.put("res_msg", "경로 오류");
      resultMap.put("res_result", 0); 	  
      String folderNoStr = (String) payload.get("folderNo");
   	  Long folderNo = Long.valueOf(folderNoStr);
             
      DocumentFolder documentfolder = documentFolderRepository.findByDocumentFolderNo(folderNo);

     if (documentfolder.getDocumentFolderParentNo() != null) {
    	 resultMap.put("res_code", "200");
     } else{
         resultMap.put("res_code", "200");
         resultMap.put("res_result", 1);
     }
     return resultMap;
   }
   
   // 자식 폴더를 삭제하는 메소드 
   public void deleteChildFolders(Long parentFolderNo, DocumentFolder parentFolder) {
	    List<DocumentFolder> childFolders = documentFolderRepository.findByDocumentFolderParentNo(parentFolderNo);
	    
	    for (DocumentFolder childFolder : childFolders) {
	        // 자식 폴더의 파일리스트 가져오기
	        List<DocumentFile> childFileList = documentFileRepository.findByDocumentFolderDocumentFolderNoAndDocumentFileStatus(childFolder.getDocumentFolderNo(), 0L);
	        
	        // 자식 폴더의 파일을 최상위 폴더로 옮김
	        if (childFileList != null && !childFileList.isEmpty()) {
	            for (DocumentFile file : childFileList) {
	                file.setDocumentFolder(parentFolder);
	                file.setDocumentFileUpdateDate(LocalDateTime.now());
	                // 파일 저장
	                documentFileRepository.save(file);
	            }
	        }
	        
	        // 자식 폴더도 재귀적으로 삭제
	        deleteChildFolders(childFolder.getDocumentFolderNo(), parentFolder);
	        
	        // 자식 폴더 삭제
	        childFolder.setDocumentFolderStatus(1L);
	        childFolder.setDocumentFolderUpdateDate(LocalDateTime.now());
	        documentFolderRepository.save(childFolder);
	    }
	}   
	// 자식 폴더를 재귀적으로 삭제하는 메소드 
	public void deleteChildFolders(Long parentFolderNo) {
	    List<DocumentFolder> childFolders = documentFolderRepository.findByDocumentFolderParentNo(parentFolderNo);
	    
	    if (childFolders != null && !childFolders.isEmpty()) {
	        for (DocumentFolder childFolder : childFolders) {
	            // 자식 폴더의 파일 리스트를 휴지통으로 옮김
	            List<DocumentFile> childFileList = documentFileRepository.findByDocumentFolderDocumentFolderNoAndDocumentFileStatus(childFolder.getDocumentFolderNo(), 0L);
	            if (childFileList != null && !childFileList.isEmpty()) {
	                for (DocumentFile file : childFileList) {
	                    file.setDocumentFileStatus(1L);
	                    file.setDocumentFileUpdateDate(LocalDateTime.now());
	                    // 파일 저장
	                    documentFileRepository.save(file);
	                }
	            }
	            // 자식 폴더도 재귀적으로 삭제
	            deleteChildFolders(childFolder.getDocumentFolderNo());
	            // 자식 폴더 삭제
	            childFolder.setDocumentFolderStatus(1L);
	            childFolder.setDocumentFolderUpdateDate(LocalDateTime.now());
	            documentFolderRepository.save(childFolder);
	        }
	    }
	}
   // 최상위 폴더가 존재하는 개인 폴더 삭제 
   @PostMapping("/document/personal/folder/delete")
   @ResponseBody
   public Map<String, Object> personalfolderDelete(@RequestBody Map<String, Object> payload){
	   Map<String, Object> resultMap = new HashMap<>();
   	   resultMap.put("res_code", "404");
   	   resultMap.put("res_msg", "경로 오류");
   	  
   	   String memberNoStr = (String) payload.get("memberNo");
       String folderNoStr = (String) payload.get("folderNo");
       Long memberNo = Long.valueOf(memberNoStr);  	   
       Long folderNo = Long.valueOf(folderNoStr);
   	   Long fileStatus = 0L;
   	   Long docBoxType = 0L;
   	   Long docParentNo = null;
   	   Long folderStatus = 0L;
       
   	   // 현재 폴더 
       DocumentFolder documentfolder = documentFolderRepository.findByDocumentFolderNo(folderNo);
       // 현재 폴더의 파일리스트 
       List<DocumentFile> documentFileList = documentFileRepository.findByDocumentFolderDocumentFolderNoAndDocumentFileStatus(folderNo, fileStatus);
       // 최상위 폴더 
       DocumentFolder parentFolder = documentFolderRepository.findByMemberMemberNoAndDocumentBoxTypeAndDocumentFolderParentNoAndDocumentFolderStatus(memberNo, docBoxType, docParentNo, folderStatus);     
       
       // 최상위 폴더로 파일리스트를 옮김 
       Long parentFolderNo = parentFolder.getDocumentFolderNo();
       if (documentFileList != null && !documentFileList.isEmpty()) {
           for (DocumentFile file : documentFileList) {
               file.setDocumentFolder(parentFolder);
               file.setDocumentFileUpdateDate(LocalDateTime.now());              
               // 파일 저장
               documentFileRepository.save(file);
           }
       }
       
       // 자식 폴더를 재귀적으로 삭제하는 메소드
       deleteChildFolders(folderNo, parentFolder);  
       
       documentfolder.setDocumentFolderStatus(1L);
       documentfolder.setDocumentFolderUpdateDate(LocalDateTime.now());
       if(documentFolderService.deleteFolder(documentfolder) > 0) {
    	   resultMap.put("res_code", "200");
    	   resultMap.put("res_msg", "삭제되었습니다.");
    	   resultMap.put("parentNo", parentFolderNo);
       }
       
       return resultMap;       
   }
   
   // 최상위 폴더가 존재하지 않는 폴더 삭제 
   @PostMapping("/document/top/folder/delete")
   @ResponseBody
   public Map<String, Object> personalTopfolderDelete(@RequestBody Map<String, Object> payload){
	   Map<String, Object> resultMap = new HashMap<>();
   	   resultMap.put("res_code", "404");
   	   resultMap.put("res_msg", "경로 오류");
   	  
       String folderNoStr = (String) payload.get("folderNo");
       Long folderNo = Long.valueOf(folderNoStr);
   	   Long fileStatus = 0L;
             
       List<DocumentFile> documentFileList = documentFileRepository.findByDocumentFolderDocumentFolderNoAndDocumentFileStatus(folderNo, fileStatus);
       DocumentFolder documentfolder = documentFolderRepository.findByDocumentFolderNo(folderNo);

       // 자식 폴더를 재귀적으로 삭제하는 메소드 호출
       deleteChildFolders(folderNo);
       
       // 현재 폴더의 파일 리스트를 휴지통으로 옮김 
       if (documentFileList != null && !documentFileList.isEmpty()) {
           for (DocumentFile file : documentFileList) {
               file.setDocumentFileUpdateDate(LocalDateTime.now());
               file.setDocumentFileStatus(1L);
               // 파일 저장
               documentFileRepository.save(file);
           }
       }

       documentfolder.setDocumentFolderStatus(1L);
       documentfolder.setDocumentFolderUpdateDate(LocalDateTime.now());
       if(documentFolderService.deleteFolder(documentfolder) > 0) {
    	   resultMap.put("res_code", "200");
    	   resultMap.put("res_msg", "삭제되었습니다.");
       }
       
       return resultMap;       
   }
   
   // 최상위 폴더가 존재하는 부서 폴더 삭제 
   @PostMapping("/document/department/folder/delete")
   @ResponseBody
   public Map<String, Object> departmentfolderDelete(@RequestBody Map<String, Object> payload){
	   Map<String, Object> resultMap = new HashMap<>();
   	   resultMap.put("res_code", "404");
   	   resultMap.put("res_msg", "경로 오류");
   	  
   	   String memberNoStr = (String) payload.get("memberNo");
       String folderNoStr = (String) payload.get("folderNo");
       Long memberNo = Long.valueOf(memberNoStr);  	   
       Long folderNo = Long.valueOf(folderNoStr);
   	   Long fileStatus = 0L;
   	   Long docBoxType = 1L;
   	   Long docParentNo = null;
   	   Long folderStatus = 0L;
       
   	   // 현재 폴더 
       DocumentFolder documentfolder = documentFolderRepository.findByDocumentFolderNo(folderNo);
       // 현재 폴더의 파일리스트 
       List<DocumentFile> documentFileList = documentFileRepository.findByDocumentFolderDocumentFolderNoAndDocumentFileStatus(folderNo, fileStatus);
       // 최상위 폴더 
       DocumentFolder parentFolder 
       		= documentFolderRepository.findByMemberMemberNoAndDocumentBoxTypeAndDocumentFolderParentNoAndDocumentFolderStatus(memberNo, docBoxType, docParentNo, folderStatus);
       
       // 최상위 폴더로 파일리스트를 옮김 
       Long parentFolderNo = parentFolder.getDocumentFolderNo();
       if (documentFileList != null && !documentFileList.isEmpty()) {
           for (DocumentFile file : documentFileList) {
        	   file.setDocumentFolder(parentFolder);
               // 새 파일 저장
               documentFileRepository.save(file);
           }
       }
       // 자식 폴더를 재귀적으로 삭제하는 메소드
       deleteChildFolders(folderNo, parentFolder);  
       
       documentfolder.setDocumentFolderUpdateDate(LocalDateTime.now());
       documentfolder.setDocumentFolderStatus(1L);
       if(documentFolderService.deleteFolder(documentfolder) > 0) {
    	   resultMap.put("res_code", "200");
    	   resultMap.put("res_msg", "삭제되었습니다.");
    	   resultMap.put("parentNo", parentFolderNo);
       }
       
       return resultMap;       
   }
   
   // 최상위 폴더가 존재하는 사내 폴더 삭제 
   @PostMapping("/document/company/folder/delete")
   @ResponseBody
   public Map<String, Object> companyFolderDelete(@RequestBody Map<String, Object> payload){
	   Map<String, Object> resultMap = new HashMap<>();
   	   resultMap.put("res_code", "404");
   	   resultMap.put("res_msg", "경로 오류");
   	  
   	   String memberNoStr = (String) payload.get("memberNo");
       String folderNoStr = (String) payload.get("folderNo");
       Long memberNo = Long.valueOf(memberNoStr);  	   
       Long folderNo = Long.valueOf(folderNoStr);
   	   Long fileStatus = 0L;
   	   Long docBoxType = 2L;
   	   Long docParentNo = null;
       Long folderStatus = 0L;
   	   // 현재 폴더 
       DocumentFolder documentfolder = documentFolderRepository.findByDocumentFolderNo(folderNo);
       // 현재 폴더의 파일리스트 
       List<DocumentFile> documentFileList = documentFileRepository.findByDocumentFolderDocumentFolderNoAndDocumentFileStatus(folderNo, fileStatus);
       // 최상위 폴더 
       DocumentFolder parentFolder 
       		= documentFolderRepository.findByDocumentBoxTypeAndDocumentFolderParentNoAndDocumentFolderStatus(docBoxType, docParentNo, folderStatus);
      
       // 최상위 폴더로 파일리스트를 옮김 
       Long parentFolderNo = parentFolder.getDocumentFolderNo();
       if (documentFileList != null && !documentFileList.isEmpty()) {
           for (DocumentFile file : documentFileList) {
        	   file.setDocumentFolder(parentFolder);
        	   
               // 새 파일 저장
               documentFileRepository.save(file);
           }
       }
       // 자식 폴더를 재귀적으로 삭제하는 메소드
       deleteChildFolders(folderNo, parentFolder);  
       
       documentfolder.setDocumentFolderStatus(1L);
       documentfolder.setDocumentFolderUpdateDate(LocalDateTime.now());
       if(documentFolderService.deleteFolder(documentfolder) > 0) {
    	   resultMap.put("res_code", "200");
    	   resultMap.put("res_msg", "삭제되었습니다.");
    	   resultMap.put("parentNo", parentFolderNo);
       }
       return resultMap;       
   }   
   
}
