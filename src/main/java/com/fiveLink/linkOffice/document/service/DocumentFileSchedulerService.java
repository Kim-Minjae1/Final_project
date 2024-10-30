package com.fiveLink.linkOffice.document.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fiveLink.linkOffice.document.domain.DocumentFile;
import com.fiveLink.linkOffice.document.repository.DocumentFileRepository;

@Component
public class DocumentFileSchedulerService {
	
	private final DocumentFileRepository documentFileRepository;
	private final DocumentFileService documentFileService;
	
	@Autowired
	public DocumentFileSchedulerService(DocumentFileRepository documentFileRepository,
			DocumentFileService documentFileService) {
		this.documentFileRepository = documentFileRepository;
		this.documentFileService = documentFileService;
	}
	// 매일 9시 10분에 실행됨 
	//@Scheduled(cron = "0 10 9 * * *")
	@Transactional
	public void checkDocumentBinFile() {
		Long fileStatus = 1L;
		// 휴지통에 든 파일 목록 조회 
		List<DocumentFile> files = documentFileRepository.findByDocumentFileStatus(fileStatus);
		LocalDate today = LocalDate.now();
		
		for(DocumentFile file : files) {
			LocalDate updateDate = file.getDocumentFileUpdateDate().toLocalDate();
			Long days = ChronoUnit.DAYS.between(updateDate, today);
			
			if(days > 30) {
				Long status = 2L;
				file.setDocumentFileStatus(status);
				documentFileRepository.save(file);
				documentFileService.documentFilePermanentDelete(file.getDocumentFileNo());
			}
		}		
	}
}
