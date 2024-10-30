package com.fiveLink.linkOffice.vacation.service;


import com.fiveLink.linkOffice.member.domain.Member;
import com.fiveLink.linkOffice.member.repository.MemberRepository;
import com.fiveLink.linkOffice.vacation.domain.*;
import com.fiveLink.linkOffice.mapper.VacationMapper;
import com.fiveLink.linkOffice.vacation.repository.VacationCheckRepository;
import com.fiveLink.linkOffice.vacation.repository.VacationRepository;
import com.fiveLink.linkOffice.vacation.repository.VacationStandardRepository;
import com.fiveLink.linkOffice.vacation.repository.VacationTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class VacationService {

    private final VacationRepository vacationRepository;
    private final VacationTypeRepository vacationTypeRepository;
    private final VacationMapper vacationMapper;
    private final VacationCheckRepository vacationCheckRepository;
    private final VacationStandardRepository vacationStandardRepository;
    private final MemberRepository memberRepository;

    @Autowired
    public VacationService(VacationRepository vacationRepository, VacationMapper vacationMapper, VacationTypeRepository vacationTypeRepository, VacationCheckRepository vacationCheckRepository ,VacationStandardRepository vacationStandardRepository, MemberRepository memberRepository){
        this.vacationRepository = vacationRepository;
        this.vacationMapper = vacationMapper;
        this.vacationTypeRepository = vacationTypeRepository;
        this.vacationCheckRepository = vacationCheckRepository;
        this.vacationStandardRepository =vacationStandardRepository;
        this.memberRepository = memberRepository;
    }
//휴가 연차 생성
    public int addVacation(VacationDto dto) {
        int result = -1;
        try {
            Vacation vacation = dto.toEntity();
            vacationRepository.save(vacation);

            result = 1;

        } catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }


    public List<VacationDto> selectVacationList(){
        return vacationMapper.selectVacationList();
    }

    public int countVacation() {
        return vacationMapper.countVacation();


    }
// 휴가 종류 생성

    public int addTypeVacation(VacationTypeDto dto) {
        int result = -1;
        try {
            VacationType vacationTypes = dto.toEntity();
            vacationTypeRepository.save(vacationTypes);

            result = 1;

        } catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }
    public int countVacationType() {
        return vacationMapper.countVacationType();


    }
    public List<VacationTypeDto> selectVacationTypeList(){
        return vacationMapper.selectVacationTypeList();
    }

    //1년미만
    public int checkOneYear(VacationOneUnderDto dto) {
        int result = -1;
        try {
            VacationOneUnder vacationOneUnder = dto.toEntity();
            vacationCheckRepository.save(vacationOneUnder);

            result = 1;

        } catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }
    public int countCheckOneYear() {
        return vacationMapper.countCheckOneYear();


    }

    //휴가 지급 기준
    public int checkStandard(VacationStandardDto dto) {
        int result = -1;
        try {
            VacationStandard vacationStandard = dto.toEntity();
            vacationStandardRepository.save(vacationStandard);
            result = 1;

        } catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }
    public int countStandard() {
        return vacationMapper.countStandard();


    }
    public List<VacationStandardDto> selectVacationStandard(){
        return vacationMapper.selectVacationStandard();
    }


    //1년미만 재직자 한달에 한번 휴가 지급
    public void incrementVacation(Long memberNo, double num){
            Member member = memberRepository.findById(memberNo)
                    .orElseThrow(() -> new IllegalArgumentException("직원을 찾을 수 없습니다"));
            // 휴가 일수 증가
            member.setMemberVacationCount(member.getMemberVacationCount() + num);

            // 현재 날짜로 휴가 지급일 업데이트
            member.setMemberVacationDate(String.valueOf(LocalDate.now()));

            // 변경 사항 저장
            memberRepository.save(member);

    }

    //1년이상 재직자 입사일 기준 경과 시, 휴가 지급
    public void resetVacation(Long memberNo, double num){

            Member member = memberRepository.findById(memberNo)
                    .orElseThrow(() -> new IllegalArgumentException("직원을 찾을 수 없습니다"));

            // 기존 휴가 수 초기화
            member.setMemberVacationCount(num);

            // 현재 날짜로 휴가 지급일 업데이트
            member.setMemberVacationDate(String.valueOf(LocalDate.now()));

            // 변경 사항 저장
            memberRepository.save(member);

    }
    // 1년이상 재직자 입사일 기준 지급
    public int contVacationYear(int years){
        int count =vacationMapper.contVacationYear(years);
        return count;
    }

    //1년이상 재직자 지정일 기준 지급
    public String selectVacationDesignated(int num){
        return vacationMapper.selectVacationDesignated(num);
    }


    public int selectVacationStandardStatus() {
        return vacationMapper.selectVacationStandardStatus();
    }

    public int checkType(String name){
        return vacationMapper.checkType(name);
    }

    //휴가 종류 수정 시 체크
    public int checkTypeName(Map<String, Object> map) {
        return vacationMapper.checkTypeName(map);
    }

    //사용자 별 휴가 개수
    public double userVacationCount(Long memberNo) {
        return vacationMapper.userVacationCount(memberNo);
    }

    //사용자별 연차
    public String memberHireDate(Long memberNo){
        return vacationMapper.memberHireDate(memberNo);
    }
    
    // 휴가 번호로 해당 휴가 갯수 조회
    public Double vacationType(Long vacationNo) {
    	try {
    		vacationTypeRepository.findVacationTypeCalculateByVacationNo(vacationNo);
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
    	return vacationTypeRepository.findVacationTypeCalculateByVacationNo(vacationNo);
    }

}