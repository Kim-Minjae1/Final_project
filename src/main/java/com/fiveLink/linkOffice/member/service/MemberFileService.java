package com.fiveLink.linkOffice.member.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Base64;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fiveLink.linkOffice.member.domain.Member;
import com.fiveLink.linkOffice.member.repository.MemberRepository;

@Service
public class MemberFileService {
	
	private final MemberRepository memberRepository;
	
	@Autowired
	public MemberFileService(MemberRepository memberRepository) {
		this.memberRepository = memberRepository;
	}
	
    // 전자결재 이미지 파일경로
    private final String fileDigitalDir = "C:\\linkoffice\\upload\\member\\digital\\";
    
    // 이미지파일 이름 변환
    public String uploadDigital(String base64Image) throws IOException {
        // Base64 문자열에서 실제 이미지 데이터 추출
        String[] parts = base64Image.split(",");
        String data = parts[1];

        // 파일 확장자 및 파일 이름 생성
        String fileExt = ".png";
        UUID uuid = UUID.randomUUID();
        String uniqueName = uuid.toString().replaceAll("-", "");
        String newDigitalName = uniqueName + fileExt;
        File saveFile = new File(fileDigitalDir + newDigitalName);

        // 디렉토리가 존재하지 않으면 생성
        File dir = new File(fileDigitalDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Base64 데이터를 디코딩하여 파일로 저장
        byte[] imageBytes = Base64.getDecoder().decode(data);
        try (FileOutputStream fos = new FileOutputStream(saveFile)) {
            fos.write(imageBytes);
        }

        return newDigitalName;
    }
    
    // 전자결재 이미지 파일 삭제
    public int delete(Long memberNo) {
    	int result = -1;
    	try {
    		Member member = memberRepository.findByMemberNo(memberNo);
    		
    		String newFileDigital = member.getMemberNewDigitalImg();
    		String oriFileDigital = member.getMemberOriDigitalImg();
    		
    		String resultDir = fileDigitalDir + URLDecoder.decode(newFileDigital,"UTF-8");
    		
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
    
    // 프로필 이미지 
    
    String fileProfileDir = "C:\\linkoffice\\upload\\member\\profile\\";
    
    public String uploadProfile(MultipartFile file) {
    	String newProfileName = null;
    	
    	try {
    		String oriProfileName = file.getOriginalFilename();
    		String fileExt = oriProfileName.substring(oriProfileName.lastIndexOf("."),oriProfileName.length());
    		UUID uuid = UUID.randomUUID();
    		String uniqueName = uuid.toString().replaceAll("-", "");
    		newProfileName = uniqueName+fileExt;
    		File saveFile = new File(fileProfileDir+newProfileName);
    		if(!saveFile.exists()) {
    			saveFile.mkdirs();
    		}
    		file.transferTo(saveFile);
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
    	return newProfileName;
    }
    
    // 프로필 이미지 삭제 
    public int profileDelete(Long memberNo) {
    	int result = -1;
    	try {
    		Member member = memberRepository.findByMemberNo(memberNo);
    		
    		String newFileProfile = member.getMemberNewProfileImg();
    		String oriFileProfile = member.getMemberOriProfileImg();
    		
    		String resultDir = fileProfileDir + URLDecoder.decode(newFileProfile,"UTF-8");
    		
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
