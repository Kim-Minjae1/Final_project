package com.fiveLink.linkOffice.vacation.domain;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class VacationOneUnderDto {

    private Long vacation_under_no;
    private int vacation_under_status;

    public VacationOneUnder toEntity() {
        return VacationOneUnder.builder()
                .vacationUnderNo(vacation_under_no)
                .vacationUnderStatus(vacation_under_status)
                .build();

    }

    public VacationOneUnderDto toDto(VacationOneUnder vacationOneUnder){
        return VacationOneUnderDto.builder()
                .vacation_under_no(vacationOneUnder.getVacationUnderNo())
                .vacation_under_status(vacationOneUnder.getVacationUnderStatus())
                .build();

    }
}
