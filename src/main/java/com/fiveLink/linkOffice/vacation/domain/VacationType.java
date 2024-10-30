package com.fiveLink.linkOffice.vacation.domain;

import java.util.List;

import com.fiveLink.linkOffice.vacationapproval.domain.VacationApproval;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="fl_vacation_type")
@NoArgsConstructor(access= AccessLevel.PROTECTED)
@AllArgsConstructor(access=AccessLevel.PROTECTED)
@Getter
@Builder
public class VacationType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="vacation_type_no")
    private Long vacationTypeNo;

    @Column(name="vacation_type_name")
    private String vacationTypeName;

    @Column(name="vacation_type_calculate")
    private double vacationTypeCalculate;

    @Column(name="vacation_type_status")
    private int vacationTypeStatus;
    
    @OneToMany(mappedBy = "vacationType", fetch = FetchType.LAZY)
	private List<VacationApproval> vacationDocument;
}
