package com.fiveLink.linkOffice.document.service;

import java.io.File;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fiveLink.linkOffice.document.domain.DocumentFile;
import com.fiveLink.linkOffice.document.domain.DocumentFileDto;
import com.fiveLink.linkOffice.document.domain.DocumentFolder;
import com.fiveLink.linkOffice.document.repository.DocumentFileRepository;
import com.fiveLink.linkOffice.document.repository.DocumentFolderRepository;

@Service
public class DocumentFileService {
	
	private String fileDir = "C:\\linkoffice\\upload\\document\\";
	// [박혜선] mac 파일 저장 경로 
	// private String fileDir = "/Users/parkhyeseon/Desktop/fiveLink/upload/";

	private final DocumentFileRepository documentFileRepository;
	private final DocumentFolderRepository documentFolderRepository;
	
	private static final Logger LOGGER
	= LoggerFactory.getLogger(DocumentFileService.class);
	
	@Autowired
	public DocumentFileService(DocumentFileRepository documentFileRepository,
			DocumentFolderRepository documentFolderRepository) {
		this.documentFileRepository = documentFileRepository;
		this.documentFolderRepository = documentFolderRepository;
	}
	
	// 파일 리스트 DocumentFileDto로 바꾸는 메소드 
	private List<DocumentFileDto> changedToDocumentFile(List<DocumentFile> resultList) {
	    return resultList.stream().map(documentFile -> {
	        return DocumentFileDto.builder()
	                .document_file_no(documentFile.getDocumentFileNo()) 
	                .document_ori_file_name(documentFile.getDocumentOriFileName())
	                .document_new_file_name(documentFile.getDocumentNewFileName())
	                .document_folder_no(documentFile.getDocumentFolder().getDocumentFolderNo())
	                .document_box_type(documentFile.getDocumentFolder().getDocumentBoxType())
	                .document_file_size(documentFile.getDocumentFileSize())
	                .document_file_upload_date(documentFile.getDocumentFileUploadDate())
	                .document_file_update_date(documentFile.getDocumentFileUpdateDate())
	                .document_file_status(documentFile.getDocumentFileStatus())
	                .member_no(documentFile.getMember().getMemberNo())
	                .member_name(documentFile.getMember().getMemberName()) 
	                .department_no(documentFile.getMember().getDepartment().getDepartmentNo())
	                .department_name(documentFile.getMember().getDepartment().getDepartmentName())                
	                .position_no(documentFile.getMember().getPosition().getPositionNo()) 
	                .position_name(documentFile.getMember().getPosition().getPositionName())
	                .build();
	    }).collect(Collectors.toList());
	}

	// 폴더에 파일 가져오는 메소드 
	public List<DocumentFileDto> selectfileList(Long folderId){
		// 파일 상태 = 0
		Long fileStatus = 0L;
		List<DocumentFile> fileList = 
				documentFileRepository.findByDocumentFolderDocumentFolderNoAndDocumentFileStatus(folderId, fileStatus);		
		return changedToDocumentFile(fileList);
	}
	// 개인 폴더 모든 파일 용량 
	public double getPersonalFileSize(Long memberNo) {
		double formatTotalSize = 0;
		Long folderStatus = 0L;
		Long docBoxType = 0L;
		Long fileStatus = 0L;
		List<DocumentFolder> folderList = 
				documentFolderRepository.findByMemberMemberNoAndDocumentBoxTypeAndDocumentFolderStatus(memberNo, docBoxType, folderStatus);
		
		if(folderList != null && !folderList.isEmpty()) {
			List<Long> folderNoList = folderList.stream()
	                .map(DocumentFolder::getDocumentFolderNo)
	                .collect(Collectors.toList());

	        // 모든 파일 목록 가져오기
	        List<DocumentFile> allFileList = new ArrayList<>();
	        for (Long folderNo : folderNoList) {
	            List<DocumentFile> filesInFolder = 
	            		documentFileRepository.findByDocumentFolderDocumentFolderNoAndDocumentFileStatus(folderNo, fileStatus);
	            if (filesInFolder != null && !filesInFolder.isEmpty()) {
	            	allFileList.addAll(filesInFolder);
	            }
	        }

	        // 모든 파일 사이즈를 합산
	        double totalSize = 0;
	        if (!allFileList.isEmpty()) {
	            for (DocumentFile file : allFileList) {
	                String fileSizeStr = file.getDocumentFileSize();
	                if (fileSizeStr != null && !fileSizeStr.isEmpty()) {
	                    double fileSize = Double.parseDouble(fileSizeStr.replaceAll("[^0-9.]", ""));
	                    totalSize += fileSize;
	                }
	            }
	            // KB에서 GB로 변환
	            double totalSizeGB = totalSize / (1024*1024); 
	            formatTotalSize = Math.ceil(totalSizeGB * 100) / 100.0;
	        }
	    }
		
		return formatTotalSize;
	}
	
	// 부서 폴더 모든 파일 용량 
	public double getDeparmentFileSize(Long deptNo) {
		double formatTotalSize = 0;
		Long folderStatus = 0L;
		Long docBoxType = 1L;
		Long fileStatus = 0L;
		List<DocumentFolder> folderList = 
				documentFolderRepository.findByDepartmentDepartmentNoAndDocumentBoxTypeAndDocumentFolderStatus(deptNo, docBoxType, folderStatus);
		
		if(folderList != null && !folderList.isEmpty()) {
			List<Long> folderNoList = folderList.stream()
	                .map(DocumentFolder::getDocumentFolderNo)
	                .collect(Collectors.toList());

	        // 모든 파일 목록 가져오기
	        List<DocumentFile> allFileList = new ArrayList<>();
	        for (Long folderNo : folderNoList) {
	            List<DocumentFile> filesInFolder = 
	            		documentFileRepository.findByDocumentFolderDocumentFolderNoAndDocumentFileStatus(folderNo, fileStatus);
	            if (filesInFolder != null && !filesInFolder.isEmpty()) {
	            	allFileList.addAll(filesInFolder);
	            }
	        }

	        // 모든 파일 사이즈를 합산
	        double totalSize = 0;
	        if (!allFileList.isEmpty()) {
	            for (DocumentFile file : allFileList) {
	                String fileSizeStr = file.getDocumentFileSize();
	                if (fileSizeStr != null && !fileSizeStr.isEmpty()) {
	                    double fileSize = Double.parseDouble(fileSizeStr.replaceAll("[^0-9.]", ""));
	                    totalSize += fileSize;
	                }
	            }
	            // KB에서 GB로 변환
	            double totalSizeGB = totalSize / (1024*1024); 
	            formatTotalSize = Math.ceil(totalSizeGB * 100) / 100.0;
	        }
	    }
		
		return formatTotalSize;
	}
	
	// 사내 폴더 모든 파일 용량 
	public double getCompanyFileSize() {
		double formatTotalSize = 0;
		Long folderStatus = 0L;
		Long docBoxType = 2L;
		Long fileStatus = 0L;
		List<DocumentFolder> folderList = 
				documentFolderRepository.findByDocumentBoxTypeAndDocumentFolderStatus(docBoxType, folderStatus);
		
		if(folderList != null && !folderList.isEmpty()) {
			List<Long> folderNoList = folderList.stream()
	                .map(DocumentFolder::getDocumentFolderNo)
	                .collect(Collectors.toList());

	        // 모든 파일 목록 가져오기
	        List<DocumentFile> allFileList = new ArrayList<>();
	        for (Long folderNo : folderNoList) {
	            List<DocumentFile> filesInFolder = 
	            		documentFileRepository.findByDocumentFolderDocumentFolderNoAndDocumentFileStatus(folderNo, fileStatus);
	            if (filesInFolder != null && !filesInFolder.isEmpty()) {
	            	allFileList.addAll(filesInFolder);
	            }
	        }

	        // 모든 파일 사이즈를 합산
	        double totalSize = 0;
	        if (!allFileList.isEmpty()) {
	            for (DocumentFile file : allFileList) {
	                String fileSizeStr = file.getDocumentFileSize();
	                if (fileSizeStr != null && !fileSizeStr.isEmpty()) {
	                    double fileSize = Double.parseDouble(fileSizeStr.replaceAll("[^0-9.]", ""));
	                    totalSize += fileSize;
	                }
	            }
	            // KB에서 GB로 변환
	            double totalSizeGB = totalSize / (1024*1024); 
	            formatTotalSize = Math.ceil(totalSizeGB * 100) / 100.0;
	        }
	    }
		
		return formatTotalSize;
	}
	// 휴지통 파일 용량 
	public double getBinFileSize(Long memberNo) {
		double formatTotalSize = 0;
		Long fileStatus = 1L;
        // 모든 파일 목록 가져오기
        List<DocumentFile> files = new ArrayList<>();
        files = documentFileRepository.findByMemberMemberNoAndDocumentFileStatus(memberNo, fileStatus);

        // 모든 파일 사이즈를 합산
        double totalSize = 0;
        if (!files.isEmpty()) {
            for (DocumentFile file : files) {
                String fileSizeStr = file.getDocumentFileSize();
                if (fileSizeStr != null && !fileSizeStr.isEmpty()) {
                    double fileSize = Double.parseDouble(fileSizeStr.replaceAll("[^0-9.]", ""));
                    totalSize += fileSize;
                }
            }
            // KB에서 GB로 변환
            double totalSizeGB = totalSize / (1024*1024); 
            formatTotalSize = Math.ceil(totalSizeGB * 100) / 100.0;
        }
		return formatTotalSize;
	}
	
	// 휴지통 
	public List<DocumentFileDto> documentBinList(Long member_no){
		// 파일 상태 = 1
		Long document_file_status = 1L;
		// repository에 memberNo, file_Status를 넘겨줌 
		List<DocumentFile> documentFileList = 
				documentFileRepository.findByMemberMemberNoAndDocumentFileStatus(member_no, document_file_status);
		return changedToDocumentFile(documentFileList);
	}
	// 폴더에 파일 저장  
	public String fileUpload(MultipartFile file) {
		String newFileName = null;
		
		try {
			String oriFileName = file.getOriginalFilename();
			String fileExt 
				= oriFileName.substring(oriFileName.lastIndexOf("."),oriFileName.length());
			UUID uuid = UUID.randomUUID();
			String uniqueName = uuid.toString().replaceAll("-", "");
			newFileName = uniqueName + fileExt;
			
			// folderNo를 경로에 추가 
			File saveFile = new File(fileDir + newFileName);
	        if (!saveFile.exists()) {
	        	saveFile.mkdirs(); 
	        }
	        file.transferTo(saveFile);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return newFileName;
	}
	// DB에 파일 저장 
	public int saveFile(DocumentFile file) {
		int result = -1;
		try {
			documentFileRepository.save(file);
			result = 1;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	// 파일 다운 
	public ResponseEntity<Object> fileDownload(Long fileNo){
		try {
			DocumentFile documentFile = documentFileRepository.findByDocumentFileNo(fileNo);
			
			String newFileName = documentFile.getDocumentNewFileName();
			String oriFileName = URLEncoder.encode(documentFile.getDocumentOriFileName(),"UTF-8");
			String downDir = 
					fileDir + newFileName;
			
			Path filePath = Paths.get(downDir);
			Resource resource = new InputStreamResource(Files.newInputStream(filePath));
			
			HttpHeaders headers = new HttpHeaders();
			headers.setContentDisposition(ContentDisposition.builder("attachment").filename(oriFileName).build());
			
			return new ResponseEntity<Object>(resource, headers, HttpStatus.OK);
			
		}catch(Exception e) {
			e.printStackTrace();
			return new ResponseEntity<Object>(null,HttpStatus.CONFLICT);
		}
	}
	// 파일 미리보기 
	public ResponseEntity<Object> fileView(Long fileNo){
		try {
			DocumentFile documentFile = documentFileRepository.findByDocumentFileNo(fileNo);
			
			String newFileName = documentFile.getDocumentNewFileName();
			String oriFileName = URLEncoder.encode(documentFile.getDocumentOriFileName(),"UTF-8");
			String downDir = 
					fileDir + newFileName;
			
			Path filePath = Paths.get(downDir);
			Resource resource = new InputStreamResource(Files.newInputStream(filePath));
			
			HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.APPLICATION_PDF); 
	        headers.setContentDisposition(ContentDisposition.builder("inline")
	                .filename(oriFileName)
	                .build());
			
			return new ResponseEntity<Object>(resource, headers, HttpStatus.OK);
			
		}catch(Exception e) {
			e.printStackTrace();
			return new ResponseEntity<Object>(null,HttpStatus.CONFLICT);
		}
	}
	// 파일 영구 삭제 
	public int documentFilePermanentDelete(Long fileNo){
		int result = -1;	
		try {
			DocumentFile documentFile = documentFileRepository.findByDocumentFileNo(fileNo);
			String newFileName = documentFile.getDocumentNewFileName();	
			String resultDir = 
					fileDir + newFileName;
			if(resultDir != null && resultDir.isEmpty() == false) {
				File file = new File(resultDir);
				if(file.exists()) {
					file.delete();
					result = 1;
				}	
			}	
		}catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}	
}
