package com.fiveLink.linkOffice.vacation.service;

import com.fiveLink.linkOffice.member.repository.MemberRepository;
import com.fiveLink.linkOffice.vacation.repository.VacationCheckRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Service
public class VacationSchedulerService {
    private final MemberRepository memberRepository;
    private final VacationCheckRepository vacationCheckRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    public VacationSchedulerService(MemberRepository memberRepository, VacationCheckRepository vacationCheckRepository) {
        this.memberRepository = memberRepository;
        this.vacationCheckRepository = vacationCheckRepository;
    }

    @Scheduled(cron = "0 0 0 * * ?")
    //@Scheduled(cron = "*/5 * * * * *")
    @Transactional
    public void updateVacationStatus() {
        LocalDate now = LocalDate.now();

            memberRepository.findAll().forEach(member -> {
                String hire= member.getMemberHireDate();
                LocalDate hireDate = LocalDate.parse(hire, DATE_FORMATTER);
                long monthsBetween = ChronoUnit.MONTHS.between(hireDate, now);

                if (monthsBetween < 12) {
                    member.setMemberOneUnder(1); // 상태를 1로 변경
                    memberRepository.save(member);
                } else {
                    member.setMemberOneUnder(0); // 1년 이상인 경우 상태를 0으로 변경
                    memberRepository.save(member);
                }
            });

    }
}