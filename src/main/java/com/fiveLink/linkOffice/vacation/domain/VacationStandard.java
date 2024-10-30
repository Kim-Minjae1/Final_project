package com.fiveLink.linkOffice.vacation.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="fl_vacation_standard")
@NoArgsConstructor(access= AccessLevel.PROTECTED)
@AllArgsConstructor(access=AccessLevel.PROTECTED)
@Getter
@Builder
public class VacationStandard {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="vacation_standard_no")
    private Long vacationStandardNo;

    @Column(name="vacation_standard_status")
    private int vacationStandardStatus;

    @Column(name="vacation_standard_date")
    private String vacationStandardDate;

}
