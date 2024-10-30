package com.fiveLink.linkOffice.vacation.repository;

import com.fiveLink.linkOffice.vacation.domain.VacationOneUnder;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VacationCheckRepository extends JpaRepository<VacationOneUnder, Long> {
}
