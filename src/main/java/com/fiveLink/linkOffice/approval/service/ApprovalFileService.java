package com.fiveLink.linkOffice.approval.service;

import java.io.File;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fiveLink.linkOffice.approval.domain.Approval;
import com.fiveLink.linkOffice.approval.repository.ApprovalFileRepository;
import com.fiveLink.linkOffice.approval.repository.ApprovalRepository;
import com.fiveLink.linkOffice.notice.domain.Notice;

@Service
public class ApprovalFileService {

	
	private final ApprovalRepository approvalRepository;
	private final ApprovalFileRepository approvalFileRepository;
	
	@Autowired
	public ApprovalFileService(ApprovalRepository approvalRepository, ApprovalFileRepository approvalFileRepository) {
		this.approvalRepository = approvalRepository;
		this.approvalFileRepository = approvalFileRepository;
	}
	
	private final String fileDir = "C:\\linkoffice\\upload\\approval\\approval\\";
	
    public String upload(MultipartFile file) {
    	String newFile = null;
    	
    	try {
    		String oriFile = file.getOriginalFilename();
    		String fileExt = oriFile.substring(oriFile.lastIndexOf("."),oriFile.length());
    		UUID uuid = UUID.randomUUID();
    		String uniqueName = uuid.toString().replaceAll("-", "");
    		newFile = uniqueName+fileExt;
    		File saveFile = new File(fileDir+newFile);
    		if(!saveFile.exists()) {
    			saveFile.mkdirs();
    		}
    		file.transferTo(saveFile);
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
    	return newFile;
    }
    
        public int delete(Long aapNo) {
        	int result = -1;
        	
        	try {
        		Approval dto = approvalRepository.findByApprovalNo(aapNo);
        		
        		String newFile = dto.getApprovalFile().getApprovalFileNewName();
        		
        		String resultDir = fileDir + URLDecoder.decode(newFile,"UTF-8");
        		
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
        
        public boolean existsFileForVacationApproval(Long aapNo) {
            Approval approval = approvalRepository.findByApprovalNo(aapNo);
            if (approval == null) {
                return false;
            }
            return approvalFileRepository.existsByApproval(approval);
        }
        
        public ResponseEntity<Object> download(Long aapNo) {
        	
            try {
            	Approval approval = approvalRepository.findByApprovalNo(aapNo);
                
                String newFileName = approval.getApprovalFile().getApprovalFileNewName();
                String oriFileName = URLEncoder.encode(approval.getApprovalFile().getApprovalFileOriName(), "UTF-8");
                String downDir = fileDir + newFileName;
                
                Path filePath = Paths.get(downDir);
                Resource resource = new InputStreamResource(Files.newInputStream(filePath));
                
                HttpHeaders headers = new HttpHeaders();
                headers.setContentDisposition(ContentDisposition.builder("attachment").filename(oriFileName).build());
                
                return new ResponseEntity<>(resource, headers, HttpStatus.OK);
                
            } catch (Exception e) {
                e.printStackTrace();
                return new ResponseEntity<>(null, HttpStatus.CONFLICT);
            }
        }
}
