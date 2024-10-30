package com.fiveLink.linkOffice.vacation.domain;

import lombok.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class VacationTypeDto {
    private Long vacation_type_no;
    private String vacation_type_name;
    private double vacation_type_calculate;
    private int vacation_type_status;
    @Builder.Default
    private Map<String, String> vacationTypesData = new HashMap<>();

    // getter 및 setter 메서드
    public Map<String, String> getVacationTypesData() {
        return vacationTypesData;
    }

    public void setVacationTypesData(Map<String, String> vacationTypesData) {
        this.vacationTypesData = vacationTypesData;
    }

    public VacationType toEntity(){
        return VacationType.builder()
                .vacationTypeNo(vacation_type_no)
                .vacationTypeName(vacation_type_name)
                .vacationTypeCalculate(vacation_type_calculate)
                .vacationTypeStatus(vacation_type_status)
                .build();

    }


    public VacationTypeDto toDto(VacationType vacationType){
        return VacationTypeDto.builder()
                .vacation_type_no(vacationType.getVacationTypeNo())
                .vacation_type_name(vacationType.getVacationTypeName())
                .vacation_type_calculate(vacationType.getVacationTypeCalculate())
                .vacation_type_status(vacationType.getVacationTypeStatus())
                .build();

    }

}
