package com.fiveLink.linkOffice.vacation.service;

import com.fiveLink.linkOffice.member.domain.Member;
import com.fiveLink.linkOffice.member.domain.MemberDto;
import com.fiveLink.linkOffice.member.repository.MemberRepository;
import com.fiveLink.linkOffice.member.service.MemberService;
import com.fiveLink.linkOffice.vacation.domain.Vacation;
import com.fiveLink.linkOffice.vacation.domain.VacationDto;
import com.fiveLink.linkOffice.vacation.repository.VacationCheckRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class AddVacationSchedulerService {

    private final MemberRepository memberRepository;
    private final VacationCheckRepository vacationCheckRepository;
    private final MemberService memberService;
    private final VacationService vacationService;

    @Autowired
    public AddVacationSchedulerService(MemberRepository memberRepository, VacationService vacationService, VacationCheckRepository vacationCheckRepository, MemberService memberService) {
        this.memberRepository = memberRepository;
        this.vacationCheckRepository = vacationCheckRepository;
        this.memberService = memberService;
        this.vacationService = vacationService;
    }
    @Scheduled(cron = "0 0 0 * * ?")  // 매일 자정에 실행
    //@Scheduled(cron = "*/5 * * * * *")
    @Transactional
    public void addVacationScheduler() {

        if (vacationService.countCheckOneYear() == 1) {
            List<MemberDto> underOneYearMembers = memberService.selectUnderYearMember(1);
            for (MemberDto dto : underOneYearMembers) {
                if (dto.getMember_vacation_date() == null || dto.getMember_vacation_date().isEmpty()) {
                    if (firstVacation(dto.getMember_hire_date())) {
                        double monthDif = (double) Period.between(LocalDate.parse(dto.getMember_hire_date()), LocalDate.now()).toTotalMonths();
                        // 입사 기준 날짜에 따라 3개월 차이가 나면 3개 입력되도록 구성
                        vacationService.incrementVacation(dto.getMember_no(), monthDif);
                    }
                } else {
                    if (monthVacation(dto.getMember_vacation_date())) {
                        vacationService.incrementVacation(dto.getMember_no(), 1.0);
                    }
                }
            }
        }

        if (vacationService.countCheckOneYear() == 1 || vacationService.countCheckOneYear() == 0) {
            List<MemberDto> overOneYearMembers = memberService.selectUnderYearMember(0);
            for (MemberDto dto : overOneYearMembers) {
                String vacationDate = vacationService.selectVacationDesignated(1); // 지정일 조회

                int vacationStandardStatus = vacationService.selectVacationStandardStatus();
                String referenceDate = (vacationDate != null) ? vacationDate : dto.getMember_hire_date(); // 입사일 기준

                if (resetDate(referenceDate, vacationStandardStatus)) {
                    int yearSinceJoin = Period.between(LocalDate.parse(dto.getMember_hire_date()), LocalDate.now()).getYears();
                    double vacationDay = getVacationDaysByYears(yearSinceJoin);

                    vacationService.resetVacation(dto.getMember_no(), vacationDay);
                }
            }
        }

    }


    // 입사일 기준 첫 번째 휴가 지급 시점 확인
    private boolean firstVacation(String joiningDate) {
        try {
            LocalDate joinDate = LocalDate.parse(joiningDate);
            return Period.between(joinDate, LocalDate.now()).toTotalMonths() >= 1;
        } catch (DateTimeParseException e) {
            // 날짜 파싱 실패 시 예외 처리
            return false;
        }
    }


    // 마지막 휴가 지급일 기준으로 한 달 경과 확인
    private boolean monthVacation(String lastVacationDateStr) {
        try {
            if (lastVacationDateStr == null || lastVacationDateStr.isEmpty()) {
                return false;
            }
            LocalDate lastVacationDate = LocalDate.parse(lastVacationDateStr);
            return Period.between(lastVacationDate, LocalDate.now()).toTotalMonths() >= 1;
        } catch (DateTimeParseException e) {
            // 날짜 파싱 실패 시 예외 처리
            return false;
        }
    }

    // 기준일에 맞춰서 휴가 리셋 시점 확인
    private boolean resetDate(String referenceDate, int status) {
        try {
            LocalDate refDate = LocalDate.parse(referenceDate);

            if (status == 1) {
                // 지정일 기준
                return LocalDate.now().isEqual(refDate) || LocalDate.now().isAfter(refDate);
            } else if (status == 0) {
                // 입사일 기준
                return LocalDate.now().isEqual(refDate) || LocalDate.now().isAfter(refDate);
            }

            return false;


        } catch (DateTimeParseException e) {
            return false;
        }
    }

    // 연차에 따른 휴가 개수 반환
    private int getVacationDaysByYears(int years) {
        // 휴가 일수 저장된 테이블에서 연차에 따른 휴가 개수 조회
        int count = vacationService.contVacationYear(years);

        return count;
    }

}