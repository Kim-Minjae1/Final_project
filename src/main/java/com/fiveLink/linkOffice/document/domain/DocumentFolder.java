package com.fiveLink.linkOffice.document.domain;

import java.time.LocalDateTime;

import com.fiveLink.linkOffice.member.domain.Member;
import com.fiveLink.linkOffice.organization.domain.Department;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "fl_document_folder")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class DocumentFolder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "document_folder_no")
    private Long documentFolderNo;

    @Column(name = "document_folder_name")
    private String documentFolderName;

    @Column(name = "document_folder_parent_no")
    private Long documentFolderParentNo;

    @Column(name = "document_folder_level")
    private Long documentFolderLevel;
    
    @ManyToOne
    @JoinColumn(name = "department_no")
    private Department department;

    @Column(name = "document_box_type")
    private Long documentBoxType;
    
    @ManyToOne
    @JoinColumn(name = "member_no")
    private Member member;

    @Column(name = "document_folder_create_date")
    private LocalDateTime documentFolderCreateDate;

    @Column(name = "document_folder_update_date")
    private LocalDateTime documentFolderUpdateDate;

    @Column(name = "document_folder_status")
    private Long documentFolderStatus;
    
    public DocumentFolderDto toDto() {
        return DocumentFolderDto.builder()
                .document_folder_no(documentFolderNo)
                .document_folder_name(documentFolderName)
                .document_folder_parent_no(documentFolderParentNo)
                .document_folder_level(documentFolderLevel)
                .department_no(department.getDepartmentNo())
                .document_box_type(documentBoxType)
                .member_no(member.getMemberNo())
                .document_folder_create_date(documentFolderCreateDate)
                .document_folder_update_date(documentFolderUpdateDate)
                .document_folder_status(documentFolderStatus)
                .build();
    }
}
