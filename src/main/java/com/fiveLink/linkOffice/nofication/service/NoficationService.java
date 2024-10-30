package com.fiveLink.linkOffice.nofication.service;

import com.fiveLink.linkOffice.mapper.NoficationMapper;
import com.fiveLink.linkOffice.nofication.domain.Nofication;
import com.fiveLink.linkOffice.nofication.domain.NoficationDto;
import com.fiveLink.linkOffice.nofication.respository.NoficationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NoficationService {
    private final NoficationRepository noficationRepository;
    private final NoficationMapper noficationMapper;

    @Autowired
    public NoficationService(NoficationRepository noficationRepository, NoficationMapper noficationMapper){
        this.noficationRepository = noficationRepository;
        this.noficationMapper = noficationMapper;
    }

    public int insertAlarm(NoficationDto noficationDto) {
        int result = -1;
        try{
            Nofication nofication = noficationDto.toEntity();
            noficationRepository.save(nofication);
            result = 1;
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }
    public long insertAlarmPk() {
        return noficationMapper.insertAlarmPk();
    }

    //현재 사용자의 안읽음 개수
    public int bellCount(Long memberNo) {
        int result = -1;
        try{
            result = noficationMapper.bellCount(memberNo);
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }
    //현재 사용자의 안읽음 알람 리스트
    public List<NoficationDto> selectUnreadList(Long memberNo) {
        return noficationMapper.selectUnreadList(memberNo);
    }
    //일괄 읽음 처리
    public boolean readNotification(Long memberNo, List<Long> notificationNos) {
        try {

            for (Long notificationNo : notificationNos) {
                Map<String, Object> params = new HashMap<>();
                params.put("notificationNo", notificationNo);
                params.put("memberNo", memberNo);
                System.out.println("alarm : "+notificationNo);
                noficationMapper.readNofication(params);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //타입별 읽음 처리
    public boolean readTypeNotification(Long memberNo, int functionType) {
        try {
                Map<String, Object> params = new HashMap<>();
                params.put("functionType", functionType);
                params.put("memberNo", memberNo);
                noficationMapper.readTypeNotification(params);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    //휴가 결재
    public boolean readTypePkNotification(Long memberNo, int functionType, Long noficationTypePk) {
        try {
        	System.out.println("알림"+memberNo);
        	System.out.println(functionType);
        	System.out.println(noficationTypePk);
            Map<String, Object> params = new HashMap<>();
            params.put("functionType", functionType);
            params.put("memberNo", memberNo);
            params.put("noficationTypePk", noficationTypePk);
            noficationMapper.readTypePkNotification(params);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


}
