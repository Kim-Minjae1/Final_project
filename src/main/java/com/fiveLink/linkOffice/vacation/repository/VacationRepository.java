package com.fiveLink.linkOffice.vacation.repository;

import com.fiveLink.linkOffice.vacation.domain.Vacation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VacationRepository extends JpaRepository<Vacation, Long> {
    Vacation findByVacationNo(Long vacation_no);
}
