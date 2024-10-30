package com.fiveLink.linkOffice.organization.domain;

import java.time.LocalDateTime;
import java.util.List;

import com.fiveLink.linkOffice.member.domain.MemberDto;

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
public class PositionDto {

    private Long position_no;
    private String position_name;
    private Long position_high;
    private String position_high_name;  
    private LocalDateTime position_create_date;
    private LocalDateTime position_update_date;
    private Long position_status;
    private Long position_level;
 
    private List<MemberDto> members;
    private Long positionId;
    
    public Position toEntity() {
    	return Position.builder()
			.positionNo(position_no)
			.positionName(position_name)
			.positionHigh(position_high)
			.positionCreateDate(position_create_date)
			.positionUpdateDate(position_update_date)
			.positionStatus(position_status)
			.positionLevel(position_level)
			.build();
    }
    
    public static PositionDto toDto(Position position) {
        return PositionDto.builder()
                .position_no(position.getPositionNo())
                .position_name(position.getPositionName())
                .position_high(position.getPositionHigh())
                .position_create_date(position.getPositionCreateDate())
                .position_update_date(position.getPositionUpdateDate())
                .position_status(position.getPositionStatus())
                .position_level(position.getPositionLevel())
                .build();
    }
 
}
