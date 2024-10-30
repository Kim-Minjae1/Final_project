package com.fiveLink.linkOffice.document.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fiveLink.linkOffice.document.domain.DocumentFolder;

@Repository
public interface DocumentFolderRepository extends JpaRepository<DocumentFolder, Long>{
	
	// 개인 폴더 리스트 
	List<DocumentFolder> findByMemberMemberNoAndDocumentBoxTypeAndDocumentFolderStatus(
	        Long memberNo, Long documentBoxType, Long documentFolderStatus);
	
	// 부서 폴더 리스트 
	List<DocumentFolder> findByDepartmentDepartmentNoAndDocumentBoxTypeAndDocumentFolderStatus(
			Long departmentNo, Long documentBoxType, Long documentFolderStatus);
	
	// 사내 폴더 리스트
	List<DocumentFolder> findByDocumentBoxTypeAndDocumentFolderStatus(Long documentBoxType, Long documentFolderStatus);

	// folderNo로 폴더 찾기
	DocumentFolder findByDocumentFolderNo(Long documentFolderNo);
	
	// 폴더이름과 folderParentNo로 폴더 찾기
	DocumentFolder findByDocumentFolderNameAndDocumentFolderParentNo(String documentFolderName, Long documentFolderParentNo);
	
	// parentNo로 폴더 찾기
	List<DocumentFolder> findByDocumentFolderParentNo(Long documentFolderParentNo);

	// 개인 문서함 최상위 폴더 찾기 
	DocumentFolder findByMemberMemberNoAndDocumentBoxTypeAndDocumentFolderParentNoAndDocumentFolderStatus(Long memberNo, Long documentBoxType, 
			Long documentFolderParentNo, Long documentFolderStatus);
	
	// 부서 문서함 최상위 폴더 찾기 
	DocumentFolder findByDepartmentDepartmentNoAndDocumentBoxTypeAndDocumentFolderParentNoAndDocumentFolderStatus(Long departmentNo, Long documentBoxType,
			Long documentFolderParentNo, Long documentFolderStatus);
	
	// 사내 문서함 최상위 폴더 찾기
	DocumentFolder findByDocumentBoxTypeAndDocumentFolderParentNoAndDocumentFolderStatus(Long documentBoxType, Long documentParentNo, Long DocumentFolderStatus);

}
