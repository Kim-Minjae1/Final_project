package com.fiveLink.linkOffice.vacation.controller;

import com.fiveLink.linkOffice.member.domain.MemberDto;
import com.fiveLink.linkOffice.vacation.domain.*;
import com.fiveLink.linkOffice.vacation.service.VacationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/vacation")
public class VacationFunctionController {

    private final VacationService vacationService;

    @Autowired
    public VacationFunctionController(VacationService vacationService){
        this.vacationService = vacationService;
    }


    // 휴가 종류 추가
    @PostMapping("/addVacationAction")
    @ResponseBody
    public Map<String, String> addVacation(@RequestBody Map<String, Object> params) {
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("res_code", "404");
        resultMap.put("res_msg", "휴가 생성 중 오류가 발생했습니다.");

        try {
            Long memberNo = Long.parseLong((String) params.get("memberNo"));
            // 연차 입력 데이터 처리
            Map<String, Object> vacationData = (Map<String, Object>) params.get("vacationData");
            VacationDto dto = new VacationDto();
            dto.setMember_no(memberNo);
            dto.setVacationData(vacationData);

            int count = Integer.parseInt(String.valueOf(params.get("countVacation")));

            if(count > 0) {
                List<String> vacationPk = (List<String>) params.get("vacationPkData");
                for(int i = 0; i< vacationPk.size(); i++){

                    String pk = vacationPk.get(i);
                    dto.setVacation_no(Long.parseLong(pk));

                    List<Vacation> vacations = dto.toEntities();

                    dto.setVacation_no(vacations.get(i).getVacationNo());
                    dto.setVacation_annual_leave(vacations.get(i).getVacationAnnualLeave());
                    dto.setVacation_year(vacations.get(i).getVacationYear());

                    if (vacationService.addVacation(dto) > 0) {
                        resultMap.put("res_code", "200");
                        resultMap.put("res_msg", "휴가 개수가 지정되었습니다.");
                    }
                }

            }
            List<Vacation> vacations = dto.toEntities();

            for (Vacation vacation : vacations) {
                dto.setVacation_year(vacation.getVacationYear());
                dto.setVacation_annual_leave(vacation.getVacationAnnualLeave());

                if (vacationService.addVacation(dto) > 0) {
                    resultMap.put("res_code", "200");
                    resultMap.put("res_msg", "휴가 개수가 지정되었습니다.");
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("res_code", "404");
            resultMap.put("res_msg", "처리 중 오류가 발생했습니다.");
        }

        return resultMap;
    }

    @PostMapping("/addTypeVacation")
    @ResponseBody
    public Map<String, String> addTypeVacation(@RequestBody Map<String, Object> payload) {
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("res_code", "404");
        resultMap.put("res_msg", "휴가 생성 중 오류가 발생했습니다.");

        try {
            String vacationType = (String) payload.get("vacationType");
            double vacationValue = Double.parseDouble((String)payload.get("vacationValue")) ;
            VacationTypeDto dto = new VacationTypeDto();
            dto.setVacation_type_name(vacationType);
            dto.setVacation_type_calculate(vacationValue);

                if(vacationService.addTypeVacation(dto)>0) {
                    resultMap.put("res_code", "200");
                    resultMap.put("res_msg", "휴가가 종류가 생성되었습니다.");
                }


        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("res_code", "404");
            resultMap.put("res_msg", "처리 중 오류가 발생했습니다.");
        }


        return resultMap;
    }

    @PostMapping("/checkVacationTypeExists")
    @ResponseBody
    public Map<String, Object> checkTypeVacation(@RequestBody Map<String, Object> payload) {
        Map<String, Object> resultMap = new HashMap<>();

        try {
            String vacationType = (String) payload.get("vacationType");

            boolean exists = vacationService.checkType(vacationType) > 0;
            if (exists) {
                resultMap.put("exists", true);
                resultMap.put("res_code", "200");
                resultMap.put("res_msg", "이미 존재하는 휴가 종류입니다.");
            } else {

                resultMap.put("exists", false);
                resultMap.put("res_code", "200");
                resultMap.put("res_msg", "새로운 휴가 종류를 생성할 수 있습니다.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("res_code", "404");
            resultMap.put("res_msg", "처리 중 오류가 발생했습니다.");
        }

        return resultMap;
    }

    // 휴가 종류 수정
    @PostMapping("/updateVacation")
    @ResponseBody
    public Map<String, String> updateVacation(@RequestBody Map<String, Object> payload) {
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("res_code", "404");
        resultMap.put("res_msg", "휴가 생성 중 오류가 발생했습니다.");


        try {
            Long vacationTypeNo = Long.parseLong((String) payload.get("vacationTypeNo"));
            String vacationTypeName = (String)payload.get("vacationTypeName");
            double vacationTypeCalculate = Double.parseDouble((String)payload.get("vacationTypeCalculate"));

            VacationTypeDto dto = new VacationTypeDto();

            dto.setVacation_type_no(vacationTypeNo);
            dto.setVacation_type_name(vacationTypeName);
            dto.setVacation_type_calculate(vacationTypeCalculate);
            Map<String, Object> params = new HashMap<>();
            params.put("num", dto.getVacation_type_no());
            params.put("name",  dto.getVacation_type_name());
            if (vacationService.checkTypeName(params) > 0) {
                if (vacationService.addTypeVacation(dto) > 0) {
                    resultMap.put("res_code", "200");
                    resultMap.put("res_msg", "종류 수정이 완료되었습니다.");
                }
            }



        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("res_code", "404");
            resultMap.put("res_msg", "처리 중 오류가 발생했습니다.");

        }


        return resultMap;

    }

    // 휴가 종류 삭제
    @PostMapping("/deleteVacation")
    @ResponseBody
    public Map<String, String> deleteVacation(@RequestBody Map<String, Object> payload) {
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("res_code", "404");
        resultMap.put("res_msg", "휴가 생성 중 오류가 발생했습니다.");


        try {
            Long vacationTypeNo = Long.parseLong((String) payload.get("vacationTypeNo"));
            String vacationTypeName = (String)payload.get("vacationTypeName");
            double VacationTypeCal = Double.parseDouble((String)payload.get("vacationTypeCal"));

            VacationTypeDto dto = new VacationTypeDto();

            dto.setVacation_type_no(vacationTypeNo);
            dto.setVacation_type_name(vacationTypeName);
            dto.setVacation_type_calculate(VacationTypeCal);
            dto.setVacation_type_status(1);

            if(vacationService.addTypeVacation(dto)>0) {
                resultMap.put("res_code", "200");
                resultMap.put("res_msg", "삭제되었습니다.");
            }


        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("res_code", "404");
            resultMap.put("res_msg", "처리 중 오류가 발생했습니다.");
        }


        return resultMap;

    }

    @PostMapping("/checkOneYear")
    @ResponseBody
    public Map<String, String> checkOneYear(@RequestBody Map<String, Object> payload) {
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("res_code", "404");
        resultMap.put("res_msg", "휴가 생성 중 오류가 발생했습니다.");


        try {
            Boolean isChecked = (Boolean) payload.get("isChecked");
            VacationOneUnderDto dto = new VacationOneUnderDto();
            if (Boolean.TRUE.equals(isChecked)) {
                dto.setVacation_under_status(1);
            } else {
                dto.setVacation_under_status(0);
            }
            if(vacationService.checkOneYear(dto)>0) {
                resultMap.put("res_code", "200");
                resultMap.put("res_msg", "기준 설정되었습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("res_code", "404");
            resultMap.put("res_msg", "처리 중 오류가 발생했습니다.");
        }


        return resultMap;

    }

    //휴가 기준 설정
    @PostMapping("/checkStandard")
    @ResponseBody
    public Map<String, String> checkStandard(@RequestBody Map<String, Object> payload) {
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("res_code", "404");
        resultMap.put("res_msg", "휴가 생성 중 오류가 발생했습니다.");

        try {
            String type = (String) payload.get("type");
            VacationStandardDto dto = new VacationStandardDto();

            System.out.println("타입:"+type);
            if ("designated".equals(type)) {
                String designatedDate = (String) payload.get("designatedDate"); //지정날짜
                dto.setVacation_standard_status(1);
                dto.setVacation_standard_date(designatedDate);

            } else if ("joined".equals(type)) {
                dto.setVacation_standard_status(0);

            }

            if (vacationService.checkStandard(dto) > 0) {
                resultMap.put("res_code", "200");
                resultMap.put("res_msg", "휴가 기준이 설정되었습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("res_code", "404");
            resultMap.put("res_msg", "처리 중 오류가 발생했습니다.");
        }

        return resultMap;
    }





}