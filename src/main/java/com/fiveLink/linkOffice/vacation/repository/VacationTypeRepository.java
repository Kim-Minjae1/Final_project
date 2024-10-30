package com.fiveLink.linkOffice.vacation.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fiveLink.linkOffice.vacation.domain.VacationType;
import com.fiveLink.linkOffice.vacation.domain.VacationTypeDto;

public interface VacationTypeRepository extends JpaRepository<VacationType, Long> {
    Optional<VacationTypeDto> findByVacationTypeNo(Long vacationTypeNo);
    
    VacationType findByvacationTypeNo(Long vacationTypeNo);
    
    @Query("SELECT v.vacationTypeCalculate FROM VacationType v WHERE v.vacationTypeNo = :vacationTypeNo")
	Double findVacationTypeCalculateByVacationNo(@Param("vacationTypeNo") Long vacationTypeNo);
}
