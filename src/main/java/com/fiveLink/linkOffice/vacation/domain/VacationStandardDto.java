package com.fiveLink.linkOffice.vacation.domain;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class VacationStandardDto {
    private Long vacation_standard_no;
    private int vacation_standard_status;
    private String vacation_standard_date;

    public VacationStandard toEntity() {
        return VacationStandard.builder()
                .vacationStandardNo(vacation_standard_no)
                .vacationStandardStatus(vacation_standard_status)
                .vacationStandardDate(vacation_standard_date)
                .build();

    }

    public VacationStandardDto toDto(VacationStandard vacationStandard){
        return VacationStandardDto.builder()
                .vacation_standard_no(vacationStandard.getVacationStandardNo())
                .vacation_standard_status(vacationStandard.getVacationStandardStatus())
                .vacation_standard_date(vacationStandard.getVacationStandardDate())
                .build();

    }
}
