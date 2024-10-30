package com.fiveLink.linkOffice.mapper;

import com.fiveLink.linkOffice.nofication.domain.NoficationDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface NoficationMapper {

    int bellCount(Long memberNo);

    List<NoficationDto> selectUnreadList(Long memberNo);

    void readNofication(Map<String, Object> params);

    void readTypeNotification(Map<String, Object> params);

    Long insertAlarmPk();

    Long getVacationApprovalPk();

    //휴가 결재
    void readTypePkNotification(Map<String, Object> params);
    
    Long getApprovalPk();
    

}
