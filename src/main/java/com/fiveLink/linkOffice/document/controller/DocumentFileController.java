package com.fiveLink.linkOffice.document.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.fiveLink.linkOffice.document.domain.DocumentFile;
import com.fiveLink.linkOffice.document.domain.DocumentFolder;
import com.fiveLink.linkOffice.document.repository.DocumentFileRepository;
import com.fiveLink.linkOffice.document.repository.DocumentFolderRepository;
import com.fiveLink.linkOffice.document.service.DocumentFileService;
import com.fiveLink.linkOffice.member.domain.Member;
import com.fiveLink.linkOffice.member.repository.MemberRepository;
import com.fiveLink.linkOffice.member.service.MemberService;

@Controller
public class DocumentFileController {

	private final MemberService memberService;
	private final DocumentFileService documentFileService;
	private MemberRepository memberRepository;
	private DocumentFileRepository documentFileRepository;
	private DocumentFolderRepository documentFolderRepository;
	
	private static final Logger LOGGER
		= LoggerFactory.getLogger(DocumentFileController.class);
	
	@Autowired
	public DocumentFileController(
			MemberService memberService,
			DocumentFileService documentFileService,
			MemberRepository memberRepository, 
			DocumentFileRepository documentFileRepository,
			DocumentFolderRepository documentFolderRepository) {
		this.memberService = memberService;
		this.documentFileService = documentFileService;
		this.memberRepository = memberRepository;
		this.documentFileRepository = documentFileRepository;
		this.documentFolderRepository = documentFolderRepository;
		}
	// 파일 업로드 
	@PostMapping("/document/file/upload")
	@ResponseBody
	 public Map<String, Object> uploadFile(
	            @RequestParam("file") MultipartFile file,
	            @RequestParam("folderNo") Long folderNo,
	            @RequestParam("memberNo") Long memberNo) {

	        Map<String, Object> resultMap = new HashMap<>();
	        resultMap.put("res_code", "404");
	        resultMap.put("res_msg", "파일 업로드에 실패했습니다.");
	        // 파일 사이즈 
	        double fileSizeBytes = file.getSize() / 1024.0;
	        double fileSizeKB = Math.round(fileSizeBytes * 100.0) / 100.0;
	        double fileSizeGB = file.getSize() / (1024 * 1024 * 1024);
	        String fileSize = String.format("%.2f", fileSizeKB) + "KB";
	        
	        Member member = memberRepository.findByMemberNo(memberNo);
	        DocumentFolder folder = documentFolderRepository.findByDocumentFolderNo(folderNo);
	        String savedFileName = documentFileService.fileUpload(file);
	        
	        if(savedFileName != null) {
	        	DocumentFile documentFile = DocumentFile.builder()
	        			.documentOriFileName(file.getOriginalFilename())
	        			.documentNewFileName(savedFileName)
	        			.documentFolder(folder)
	        			.member(member)
	        			.documentFileSize(fileSize)  
	        			.documentFileUploadDate(LocalDateTime.now())
	        			.documentFileUpdateDate(LocalDateTime.now())
	        			.documentFileStatus(0L)
	        			.build();
	        	// 문서함 총 용량 확인 
	        	if(folder.getDocumentBoxType() == 0L) {
	        		double allFileSize = documentFileService.getPersonalFileSize(memberNo);
	        		double addedFileSize = allFileSize + fileSizeGB;
	        		if(addedFileSize > 10) {
	        			resultMap.put("res_code", "404");
		    	        resultMap.put("res_msg", "문서함의 저장용량이 초과되었습니다.");
	        		} else {
	        			int result = documentFileService.saveFile(documentFile);
	    	        	if(result > 0) {
	    	        		resultMap.put("res_code", "200");
	    	    	        resultMap.put("res_msg", "업로드 되었습니다.");
	    	        	}
	        		}
	        	} else if(folder.getDocumentBoxType() == 1L) {
	        		double allFileSize = documentFileService.getDeparmentFileSize(folder.getDepartment().getDepartmentNo());       		
	        		double addedFileSize = allFileSize + fileSizeGB;
	        		if(addedFileSize > 50) {
	        			resultMap.put("res_code", "404");
		    	        resultMap.put("res_msg", "문서함의 저장용량이 초과되었습니다.");
	        		} else {
	        			int result = documentFileService.saveFile(documentFile);
	    	        	if(result > 0) {
	    	        		resultMap.put("res_code", "200");
	    	    	        resultMap.put("res_msg", "파일이 업로드되었습니다.");
	    	        	}
	        		}
	        	} else if(folder.getDocumentBoxType() == 2L) {
	        		double allFileSize = documentFileService.getCompanyFileSize();
	        		double addedFileSize = allFileSize + fileSizeGB;
	        		if(addedFileSize > 100) {
	        			resultMap.put("res_code", "404");
		    	        resultMap.put("res_msg", "문서함의 저장용량이 초과되었습니다.");
	        		} else {
	        			int result = documentFileService.saveFile(documentFile);
	    	        	if(result > 0) {
	    	        		resultMap.put("res_code", "200");
	    	    	        resultMap.put("res_msg", "파일이 업로드되었습니다.");
	    	        	}
	        		}
	        	}
	        	
	        }
	        return resultMap;
	    }
	// 파일 삭제
	@PostMapping("/document/file/delete")
	@ResponseBody
	public Map<String, Object> deleteFile(@RequestParam("fileNo") Long fileNo){
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("res_code", "404");
        resultMap.put("res_msg", "파일을 삭제하지 못했습니다.");
		
        DocumentFile file = documentFileRepository.findByDocumentFileNo(fileNo);
        file.setDocumentFileStatus(1L);
        file.setDocumentFileUpdateDate(LocalDateTime.now());
        int result = documentFileService.saveFile(file);
    	if(result > 0) {
    		resultMap.put("res_code", "200");
	        resultMap.put("res_msg", "삭제되었습니다.");
    	}
		return resultMap;
	}
	// 여러 파일 삭제 
	@PostMapping("/document/fileList/delete")
	@ResponseBody
	public Map<String, Object> deleteFiles(@RequestBody Map<String, List<Long>> payload) {
		List<Long> fileNos = payload.get("fileNos");
		Map<String, Object> resultMap = new HashMap<>();
	    resultMap.put("res_code", "404");
	    resultMap.put("res_msg", "파일을 삭제하지 못했습니다.");
	    
	    int deletedCount = 0;

	    // 리스트로 가져온 파일들을 하나씩 삭제 
	    for (Long fileNo : fileNos) {
	        DocumentFile file = documentFileRepository.findByDocumentFileNo(fileNo);
	        if (file != null) {
	            file.setDocumentFileStatus(1L); 
	            documentFileService.saveFile(file); 
	            deletedCount++;
	        }
	    }
	    
	    if (deletedCount == fileNos.size()) {
	        resultMap.put("res_code", "200");
	        resultMap.put("res_msg", "삭제되었습니다.");
	    } 
	    return resultMap;
	}
	
	// 파일 다운
	@GetMapping("/document/file/download/{no}")
	public ResponseEntity<Object> documentFileDownload(
			@PathVariable("no") Long fileNo){
		return documentFileService.fileDownload(fileNo);
	}
	
	// 파일 미리보기
	@GetMapping("/document/file/view/{no}")
	public ResponseEntity<Object> documentFileView(
			@PathVariable("no") Long fileNo){
		return documentFileService.fileView(fileNo);
	}
	
	// 파일 영구 삭제  
	@PostMapping("/document/file/permanent/delete")
	@ResponseBody
	public Map<String, Object> deleteFilePermanent(@RequestParam("fileNo") Long fileNo){
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("res_code", "404");
        resultMap.put("res_msg", "파일을 영구 삭제하지 못했습니다.");
		
        DocumentFile file = documentFileRepository.findByDocumentFileNo(fileNo);
        file.setDocumentFileStatus(2L);
        file.setDocumentFileUpdateDate(LocalDateTime.now());
        int dbResult = documentFileService.saveFile(file);
        int fileResult = documentFileService.documentFilePermanentDelete(fileNo);
        
    	if(dbResult > 0 && fileResult > 0) {
    		resultMap.put("res_code", "200");
	        resultMap.put("res_msg", "영구 삭제되었습니다.");
    	}
		return resultMap;
	}
	// 파일 선택 영구 삭제 
	@PostMapping("/document/fileList/permanent/delete")
	@ResponseBody
	public Map<String, Object> deleteFilespermanent(@RequestBody Map<String, List<Long>> payload) {
		List<Long> fileNos = payload.get("fileNos");
		Map<String, Object> resultMap = new HashMap<>();
	    resultMap.put("res_code", "404");
	    resultMap.put("res_msg", "파일을 영구 삭제하지 못했습니다.");
	    
	    int deletedCount = 0;

	    // 리스트로 가져온 파일들을 하나씩 삭제 
	    for (Long fileNo : fileNos) {
	        DocumentFile file = documentFileRepository.findByDocumentFileNo(fileNo);
	        file.setDocumentFileStatus(2L);
	        file.setDocumentFileUpdateDate(LocalDateTime.now());
	        int dbResult = documentFileService.saveFile(file);
	        int fileResult = documentFileService.documentFilePermanentDelete(fileNo);
	        
	    	if(dbResult > 0 && fileResult > 0) {
	    	    deletedCount++;
	    	}
	    }	    
	    if (deletedCount == fileNos.size()) {
	        resultMap.put("res_code", "200");
	        resultMap.put("res_msg", "파일이 영구 삭제되었습니다.");
	    } 
	    return resultMap;
	}
	// 파일 복구 
	@PostMapping("/document/file/update")
	@ResponseBody
	public Map<String, Object> documentFileUpdate(@RequestBody Map<String, Long> payload){
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("res_code", "404");
        resultMap.put("res_msg", "파일을 복구하지 못했습니다.");
		
        Long fileNo = payload.get("fileNo");
        DocumentFile file = documentFileRepository.findByDocumentFileNo(fileNo);
        Long memberNo = file.getMember().getMemberNo();
        Long docBoxType = file.getDocumentFolder().getDocumentBoxType();
        Long docParentNo = null;
        Long folderStatus = 0L;
        int result = 0;
        
        if(file.getDocumentFolder().getDocumentFolderStatus() == 0L) {
        	file.setDocumentFileStatus(0L);
        	file.setDocumentFileUpdateDate(LocalDateTime.now());
        	result = documentFileService.saveFile(file);  
        } else {
        	// 최상위 폴더 찾기
        	DocumentFolder parentFolder = documentFolderRepository.findByMemberMemberNoAndDocumentBoxTypeAndDocumentFolderParentNoAndDocumentFolderStatus(memberNo, docBoxType, docParentNo, folderStatus);
        	if(parentFolder != null) {
        		file.setDocumentFileStatus(0L);
        		file.setDocumentFolder(parentFolder);
        		file.setDocumentFileUpdateDate(LocalDateTime.now());
        		result = documentFileService.saveFile(file);  
        	} else {
        		resultMap.put("res_msg", "복구할 수 있는 폴더가 존재하지 않습니다.");
        	}
        }       
    	if(result > 0) {
    		resultMap.put("res_code", "200");
	        resultMap.put("res_msg", "파일이 복구되었습니다.");
    	}
		return resultMap;
	}
	// 파일 선택 복구 
	@PostMapping("/document/fileList/update")
	@ResponseBody
	public Map<String, Object> documentFileListUpdate(@RequestBody Map<String, List<Long>> payload){
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("res_code", "404");
        resultMap.put("res_msg", "파일을 복구하지 못했습니다.");
		
        List<Long> fileNos = payload.get("fileNos");
        Long docParentNo = null;
        Long folderStatus = 0L;
        int result = 0;
	    int updateCount = 0;

	    // 리스트로 가져온 파일들을 하나씩 복구
	    for (Long fileNo : fileNos) {
	    	DocumentFile file = documentFileRepository.findByDocumentFileNo(fileNo);	    	
	    	Long memberNo = file.getMember().getMemberNo();
	    	Long docBoxType = file.getDocumentFolder().getDocumentBoxType();
	        if(file.getDocumentFolder().getDocumentFolderStatus() == 0L) {
	        	file.setDocumentFileStatus(0L);
	        	file.setDocumentFileUpdateDate(LocalDateTime.now());
	        	result = documentFileService.saveFile(file);  
	        	if(result > 0) {
	        		updateCount++;
	        	}
	        } else {
	        	// 최상위 폴더 찾기
	        	DocumentFolder parentFolder = documentFolderRepository.findByMemberMemberNoAndDocumentBoxTypeAndDocumentFolderParentNoAndDocumentFolderStatus(memberNo, docBoxType, docParentNo, folderStatus);
	        	if(parentFolder != null) {
	        		file.setDocumentFileStatus(0L);
	        		file.setDocumentFolder(parentFolder);
	        		file.setDocumentFileUpdateDate(LocalDateTime.now());
	        		result = documentFileService.saveFile(file);  
		        	if(result > 0) {
		        		updateCount++;
		        	}
	        	} else {
	        		resultMap.put("res_msg", "복구할 수 있는 폴더가 존재하지 않습니다.");
	        	}
	        }       
	    	if(result > 0) {
	    		if(updateCount == fileNos.size()) {
	    			resultMap.put("res_code", "200");
	    			resultMap.put("res_status", "0");	    			
	    			resultMap.put("res_msg", "파일이 복구되었습니다.");	    			
	    		} else {
	    			resultMap.put("res_code", "200");
	    			resultMap.put("res_status", "1");	
	    		}
	    	} 
	    }	    
		return resultMap;
	}
}
