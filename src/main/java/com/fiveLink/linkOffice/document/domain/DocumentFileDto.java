package com.fiveLink.linkOffice.document.domain;

import java.time.LocalDateTime;

import com.fiveLink.linkOffice.member.domain.Member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class DocumentFileDto {
	private Long document_file_no;         
    private String document_ori_file_name;  
    private String document_new_file_name;  
    private Long document_folder_no;  
    private Long document_box_type;
    private Long member_no;
    private String member_name;
    private String document_file_size;
    private LocalDateTime document_file_upload_date; 
    private LocalDateTime document_file_update_date; 
    private Long document_file_status; 
    private Long department_no;
    private String department_name;
    private Long position_no;
    private String position_name;
    
    // DTO를 Entity로 변경
    public DocumentFile toEntity(DocumentFile documentFile, Member member,
    		DocumentFolder documentFolder) {
        return DocumentFile.builder()
                .documentFileNo(document_file_no)
                .documentOriFileName(document_ori_file_name)
                .documentNewFileName(document_new_file_name)
                .documentFolder(documentFolder)
                .member(member)
                .documentFileSize(document_file_size)
                .documentFileUploadDate(document_file_upload_date)
                .documentFileUpdateDate(document_file_update_date)
                .documentFileStatus(document_file_status)
                .build();
    }

}
