package com.fiveLink.linkOffice.document.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fiveLink.linkOffice.document.domain.DocumentFolder;
import com.fiveLink.linkOffice.document.domain.DocumentFolderDto;
import com.fiveLink.linkOffice.document.repository.DocumentFolderRepository;

@Service
public class DocumentFolderService {
	
	private final DocumentFolderRepository documentFolderRepository;
	
	@Autowired
	public DocumentFolderService(DocumentFolderRepository documentFolderRepository) {
		this.documentFolderRepository = documentFolderRepository;
	}
	// 개인 문서함 
	public List<DocumentFolderDto> selectPersonalFolderList(Long member_no) {
		// 문서함 타입 = 0(개인)
		Long document_box_type = 0L;
		// 폴더 상태 = 0
		Long document_folder_status = 0L;
		// repository에 memberNo, documentBoxType, folderStatus를 넘겨줌 
		List<DocumentFolder> documentFolderList = documentFolderRepository.findByMemberMemberNoAndDocumentBoxTypeAndDocumentFolderStatus(
				member_no, document_box_type, document_folder_status);
		List<DocumentFolderDto> documentFolderDtoList = new ArrayList<DocumentFolderDto>();
		for(DocumentFolder d : documentFolderList) {
			DocumentFolderDto folderDto = d.toDto();
			documentFolderDtoList.add(folderDto);
		}
		return documentFolderDtoList;
	}
	// 개인 문서함 첫 폴더
	public int personalFirstFolder(DocumentFolder documentFolder) {
		int result = -1;
		try {
			documentFolderRepository.save(documentFolder);
			result = 1;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	// 개인 문서함 폴더 생성
	public int personalCreateFolder(DocumentFolder documentFolder) {
		int result = -1;
		try {
			documentFolderRepository.save(documentFolder);
			result = 1;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	// 부서 문서함 
	public List<DocumentFolderDto> selectDepartmentFolderList(Long department_no) {
		// 문서함 타입 = 1(부서)
		Long document_box_type = 1L;
		// 폴더 상태 = 0
		Long document_folder_status = 0L;
		// repository에 departmentNo, documentBoxType, folderStatus를 넘겨줌
		List<DocumentFolder> documentFolderList 
			= documentFolderRepository.findByDepartmentDepartmentNoAndDocumentBoxTypeAndDocumentFolderStatus(
					department_no, document_box_type, document_folder_status);
		List<DocumentFolderDto> documentFolderDtoList = new ArrayList<DocumentFolderDto>();
		for(DocumentFolder d : documentFolderList) {
			DocumentFolderDto folderDto = d.toDto();
			documentFolderDtoList.add(folderDto);
		}
		return documentFolderDtoList;
	}
	// 부서 문서함 첫 폴더
	public int departmentFirstFolder(DocumentFolder documentFolder) {
		int result = -1;
		try {
			documentFolderRepository.save(documentFolder);
			result = 1;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	// 부서 문서함 폴더 생성
	public int departmentCreateFolder(DocumentFolder documentFolder) {
		int result = -1;
		try {
			documentFolderRepository.save(documentFolder);
			result = 1;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	// 사내 문서함 
	public List<DocumentFolderDto> selectCompanyFolderList(){
		// 문서함 타입
		Long document_box_type = 2L;
		// 폴더 상태 = 0
		Long document_folder_status = 0L;
		// repository에 documentBoxType과 folderStatus를 넘겨줌 
		List<DocumentFolder> documentFolderList 
			= documentFolderRepository.findByDocumentBoxTypeAndDocumentFolderStatus(document_box_type, document_folder_status);
		List<DocumentFolderDto> documentFolderDtoList = new ArrayList<DocumentFolderDto>();
		for(DocumentFolder d : documentFolderList) {
			DocumentFolderDto folderDto = d.toDto();
			documentFolderDtoList.add(folderDto);
		}
		return documentFolderDtoList;
	}
	
	// 부서 문서함 폴더 생성
	public int companyCreateFolder(DocumentFolder documentFolder) {
		int result = -1;
		try {
			documentFolderRepository.save(documentFolder);
			result = 1;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	// 폴더 이름 변경
	public int changeFolderName(DocumentFolder newDocumentFolder) {
		int result = -1;
		try {
			documentFolderRepository.save(newDocumentFolder);
			result = 1;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	// 폴더 삭제 
	public int deleteFolder(DocumentFolder documentFolder) {
		int result = -1;
		try {
			documentFolderRepository.save(documentFolder);		
			result = 1;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}
}
