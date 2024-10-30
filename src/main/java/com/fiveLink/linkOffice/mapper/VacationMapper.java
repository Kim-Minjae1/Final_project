package com.fiveLink.linkOffice.mapper;

import com.fiveLink.linkOffice.member.domain.MemberDto;
import com.fiveLink.linkOffice.vacation.domain.VacationDto;
import com.fiveLink.linkOffice.vacation.domain.VacationStandardDto;
import com.fiveLink.linkOffice.vacation.domain.VacationTypeDto;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

@Mapper
public interface VacationMapper {
    List<VacationDto> selectVacationList();

    int countVacation();

    int countVacationType();

    List<VacationTypeDto> selectVacationTypeList();

    int countCheckOneYear();

    int countStandard();

    List<VacationStandardDto> selectVacationStandard();

    List<MemberDto> selectUnderYearMember(int num);

    int contVacationYear(int year);

    String selectVacationDesignated(int num);

    int selectVacationStandardStatus();

    int checkType(String name);

    int checkTypeName( Map<String, Object> map);

    double userVacationCount(Long memberNo);

    String memberHireDate(Long memberNo);
}
