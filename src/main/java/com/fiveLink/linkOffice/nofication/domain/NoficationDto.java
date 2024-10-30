package com.fiveLink.linkOffice.nofication.domain;

import com.fiveLink.linkOffice.chat.domain.ChatRoom;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class NoficationDto {
    private Long nofication_no;
    private Long member_no;
    private int nofication_type;
    private LocalDateTime nofication_create_date;
    private String nofication_title;
    private String nofication_content;
    private int nofication_status;
    private Long nofication_receive_no;
    private Long nofication_type_pk;

    public Nofication toEntity() {
        return Nofication.builder()
                .noficationNo(nofication_no)
                .memberNo(member_no)
                .noficationType(nofication_type)
                .noficationCreateDate(nofication_create_date)
                .noficationTitle(nofication_title)
                .noficationContent(nofication_content)
                .noficationStatus(nofication_status)
                .noficationReceiveNo(nofication_receive_no)
                .noficationTypePk(nofication_type_pk)
                .build();
    }
    public NoficationDto toDto(Nofication nofication){
        return NoficationDto.builder()
                .nofication_no(nofication.getNoficationNo())
                .member_no(nofication.getMemberNo())
                .nofication_type(nofication.getNoficationType())
                .nofication_create_date(nofication.getNoficationCreateDate())
                .nofication_title(nofication.getNoficationTitle())
                .nofication_content(nofication.getNoficationContent())
                .nofication_status(nofication.getNoficationStatus())
                .nofication_receive_no(nofication.getNoficationReceiveNo())
                .nofication_type_pk(nofication.getNoficationTypePk())
                .build();
    }
}
