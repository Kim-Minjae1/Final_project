package com.fiveLink.linkOffice.vacation.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="fl_vacation_under")
@NoArgsConstructor(access= AccessLevel.PROTECTED)
@AllArgsConstructor(access=AccessLevel.PROTECTED)
@Getter
@Builder
public class VacationOneUnder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="vacation_under_no")
    private Long vacationUnderNo;

    @Column(name="vacation_under_status")
    private int vacationUnderStatus;


}
