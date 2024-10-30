package com.fiveLink.linkOffice.vacation.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name="fl_vacation_allocation")
@NoArgsConstructor(access= AccessLevel.PROTECTED)
@AllArgsConstructor(access=AccessLevel.PROTECTED)
@Getter
@Builder
public class VacationAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="vacation_allocation_no")
    private Long vacationAllocationNo;

    @Column(name="vacation_allocation_date")
    private LocalDate vacationAllocationDate;

    @Column(name="vacation_allocation_count")
    private int vacationAllocationCount;

    @Column(name="member_no")
    private Long memberNo;

}
