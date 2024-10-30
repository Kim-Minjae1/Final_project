package com.fiveLink.linkOffice.document.domain;

import java.time.LocalDateTime;

import com.fiveLink.linkOffice.member.domain.Member;
import com.fiveLink.linkOffice.organization.domain.Department;

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
public class DocumentFolderDto {
    private Long document_folder_no;         
    private String document_folder_name;  
    private Long document_folder_parent_no;      
    private Long document_folder_level;   
    private Long department_no;
    private Long document_box_type;     
    private Long member_no;
    private LocalDateTime document_folder_create_date; 
    private LocalDateTime document_folder_update_date; 
    private Long document_folder_status;       

    // DTO를 Entity로 변경
    public DocumentFolder toEntity(Member member, Department department,
    		DocumentFolder documentFolder) {   	
        return DocumentFolder.builder()
                .documentFolderNo(document_folder_no)
                .documentFolderName(document_folder_name)
                .documentFolderParentNo(document_folder_parent_no)
                .documentFolderLevel(document_folder_level)
                .department(department)
                .documentBoxType(document_box_type)
                .member(member)
                .documentFolderCreateDate(document_folder_create_date)
                .documentFolderUpdateDate(document_folder_update_date)
                .documentFolderStatus(document_folder_status)
                .build();
    }

}
