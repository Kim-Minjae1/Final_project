package com.fiveLink.linkOffice.vacation.domain;
import lombok.*;

import java.time.LocalDateTime;
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
public class VacationDto  {
    private Long vacation_no;
    private int vacation_year;
    private int vacation_annual_leave;
    private LocalDateTime vacation_create_date;
    private Long member_no;

    @Builder.Default
    private Map<String, Object> vacationData = new HashMap<>();

    public Map<String, Object> getVacationData() {
        return vacationData;
    }

    public void setVacationData(Map<String, Object> vacationData) {
        this.vacationData = vacationData;
    }

    public Vacation toEntity(){
        return Vacation.builder()
                .vacationNo(vacation_no)
                .vacationYear(vacation_year)
                .vacationAnnualLeave(vacation_annual_leave)
                .vacationCreateDate(vacation_create_date)
                .memberNo(member_no)
                .build();

    }
    public List<Vacation> toEntities() {
        List<Vacation> vacations = new ArrayList<>();
        for (Map.Entry<String, Object> entry : vacationData.entrySet()) {
            try {
                String yearStr = entry.getKey();
                Object daysObj = entry.getValue();
                int year = Integer.parseInt(yearStr); // 연도를 정수로 변환
                int days = (daysObj instanceof String)
                        ? Integer.parseInt((String) daysObj) // daysObj가 String일 경우
                        : (Integer) daysObj; // daysObj가 Integer일 경우

                Vacation vacation = Vacation.builder()
                        .vacationNo(vacation_no)
                        .vacationYear(year)
                        .vacationAnnualLeave(days)
                        .vacationCreateDate(vacation_create_date)
                        .memberNo(member_no)
                        .build();
                vacations.add(vacation);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                // 로그를 남기거나 적절한 에러 처리를 수행할 수 있습니다.
            }
        }
        return vacations;
    }

    public VacationDto toDto(Vacation vacation){
        return VacationDto.builder()
                .vacation_no(vacation.getVacationNo())
                .vacation_year(vacation.getVacationYear())
                .vacation_annual_leave(vacation.getVacationAnnualLeave())
                .vacation_create_date(vacation.getVacationCreateDate())
                .member_no(vacation.getMemberNo())
                .build();

    }
}
