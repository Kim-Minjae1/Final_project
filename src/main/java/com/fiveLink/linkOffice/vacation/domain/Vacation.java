package com.fiveLink.linkOffice.vacation.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name="fl_vacation")
@NoArgsConstructor(access= AccessLevel.PROTECTED)
@AllArgsConstructor(access=AccessLevel.PROTECTED)
@Getter
@Builder
public class Vacation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="vacation_no")
    private Long vacationNo;

    @Column(name="vacation_year")
    private int vacationYear;

    @Column(name="vacation_annual_leave")
    private int vacationAnnualLeave;

    @Column(name="vacation_create_date")
    @CreationTimestamp
    private LocalDateTime vacationCreateDate;

    @Column(name="member_no")
    private Long memberNo;


}
