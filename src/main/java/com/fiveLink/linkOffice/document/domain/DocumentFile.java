package com.fiveLink.linkOffice.document.domain;

import java.time.LocalDateTime;

import com.fiveLink.linkOffice.member.domain.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
@Table(name = "fl_document_file")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class DocumentFile {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "document_file_no")
    private Long documentFileNo;

    @Column(name = "document_ori_file_name")
    private String documentOriFileName;

    @Column(name = "document_new_file_name")
    private String documentNewFileName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_folder_no")
    private DocumentFolder documentFolder;

    @ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="member_no")
	private Member member;
    
    @Column(name = "document_file_size")
    private String documentFileSize;

    @Column(name = "document_file_upload_date")
    private LocalDateTime documentFileUploadDate;

    @Column(name = "document_file_update_date")
    private LocalDateTime documentFileUpdateDate;
    
    @Column(name = "document_file_status")
    private Long documentFileStatus;
    
    // Entity를 DTO로 변경
    public DocumentFileDto toDto() {
        return DocumentFileDto.builder()
                .document_file_no(documentFileNo)
                .document_ori_file_name(documentOriFileName)
                .document_new_file_name(documentNewFileName)
                .document_folder_no(documentFolder.getDocumentFolderNo())
                .member_no(member.getMemberNo())
                .document_file_size(documentFileSize)
                .document_file_upload_date(documentFileUploadDate)
                .document_file_update_date(documentFileUpdateDate)
                .document_file_status(documentFileStatus)
                .build();
    }
}
