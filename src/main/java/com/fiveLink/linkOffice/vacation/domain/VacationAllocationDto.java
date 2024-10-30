package com.fiveLink.linkOffice.vacation.domain;

import lombok.*;

import java.time.LocalDate;
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class VacationAllocationDto {
    private Long vacation_allocation_no;
    private Long member_no;
    private LocalDate vacation_allocation_date;
    private int vacation_allocation_count;

    public VacationAllocation toEntity(){
        return VacationAllocation.builder()
                .vacationAllocationNo(vacation_allocation_no)
                .vacationAllocationDate(vacation_allocation_date)
                .vacationAllocationCount(vacation_allocation_count)
                .memberNo(member_no)
                .build();
    }
    public VacationAllocationDto toDto(VacationAllocation vacationAllocation){
        return VacationAllocationDto.builder()
                .vacation_allocation_no(vacationAllocation.getVacationAllocationNo())
                .vacation_allocation_date(vacationAllocation.getVacationAllocationDate())
                .vacation_allocation_count(vacationAllocation.getVacationAllocationCount())
                .member_no(vacationAllocation.getMemberNo())
                .build();

    }
}
