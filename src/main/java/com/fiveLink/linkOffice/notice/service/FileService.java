package com.fiveLink.linkOffice.notice.service;

import java.io.File;
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

import com.fiveLink.linkOffice.notice.domain.Notice;
import com.fiveLink.linkOffice.notice.repository.NoticeRepository;

@Service
public class FileService {
    
	private final String fileDir = "C:\\linkoffice\\upload\\notice\\";


    private final NoticeRepository NoticeRepository;
    private final NoticeService NoticeService;
    
    @Autowired
    public FileService(NoticeRepository NoticeRepository, NoticeService noticeService) {
        this.NoticeRepository = NoticeRepository;
        this.NoticeService = noticeService;
    }

    public ResponseEntity<Object> download(Long notice_no) {
        try {
            Notice n = NoticeRepository.findBynoticeNo(notice_no);
            
            String newFileName = n.getNoticeNewImg();
            String oriFileName = URLEncoder.encode(n.getNoticeOriImg(), "UTF-8");
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

    public String upload(MultipartFile file) {
        String newFileName = null;

        try {
            if (file.isEmpty()) {
                return null; 
            }

            String oriFileName = file.getOriginalFilename();
            int dotIndex = oriFileName.lastIndexOf(".");
            String fileExt = "";
            if (dotIndex != -1) {
                fileExt = oriFileName.substring(dotIndex);
            }
            UUID uuid = UUID.randomUUID();
            String uniqueName = uuid.toString().replaceAll("-", "");
            newFileName = uniqueName + fileExt;

            File saveDir = new File(fileDir);
            if (!saveDir.exists()) {
                saveDir.mkdirs();
            }

            File saveFile = new File(saveDir, newFileName);
            file.transferTo(saveFile);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return newFileName;
    }

    public int delete(Long notice_no) {
        int result = -1;
        try {
            Notice notice = NoticeRepository.findBynoticeNo(notice_no);
            if (notice == null) {
                return result; 
            }

            
            String oriFileName = notice.getNoticeOriImg();
            if (oriFileName != null && !oriFileName.isEmpty()) {
                File oriFile = new File(fileDir + oriFileName);
                if (oriFile.exists()) {
                    oriFile.delete();  
                }
            }

           
            notice.setNoticeOriImg(null);
            notice.setNoticeNewImg(null);
            NoticeRepository.save(notice); 

            result = 1; 

        } catch (Exception e) {
            e.printStackTrace();
            result = -1; 
        }
        return result;
    }

}
