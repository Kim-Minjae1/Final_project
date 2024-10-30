package com.fiveLink.linkOffice.meeting.service;

import java.io.File;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MeetingFileService {
	// 회의실 이미지 파일 경로
	private final String fileDir = "C:\\linkoffice\\upload\\meeting\\";
	
	// 업로드
	public String upload(MultipartFile file) {
		
		String newFileName = null;
		
		try { 
			String oriFileName = file.getOriginalFilename();
		 
			String fileExt 
				= oriFileName.substring(oriFileName.lastIndexOf("."),oriFileName.length());
			 
			UUID uuid = UUID.randomUUID();
			 
			String uniqueName = uuid.toString().replaceAll("-", "");
			 
			newFileName = uniqueName+fileExt;
			 
			File saveFile = new File(fileDir+newFileName);
			 
			if(!saveFile.exists()) {
				saveFile.mkdirs();
			}
			 
			file.transferTo(saveFile);
		}catch(Exception e) {
			e.printStackTrace();
		}
		return newFileName;
	}
}
