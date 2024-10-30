package com.fiveLink.linkOffice.organization.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fiveLink.linkOffice.member.repository.MemberRepository;
import com.fiveLink.linkOffice.member.service.MemberService;
import com.fiveLink.linkOffice.organization.domain.Position;
import com.fiveLink.linkOffice.organization.domain.PositionDto;
import com.fiveLink.linkOffice.organization.repository.PositionRepository;

import jakarta.transaction.Transactional;

@Service
public class PositionService {
	private static final Logger LOGGER = LoggerFactory.getLogger(PositionService.class); 

    private PositionRepository positionRepository;
    private MemberRepository memberRepository;
    private final MemberService memberService; 
    
    @Autowired
    public PositionService(PositionRepository positionRepository, MemberRepository memberRepository, MemberService memberService){
        this.positionRepository = positionRepository;
        this.memberRepository = memberRepository;
        this.memberService = memberService; 
    }
    
    public List<PositionDto> getAllPositions() {
        List<Position> positions = positionRepository.findAll();
        return positions.stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }
    
    private PositionDto mapToDto(Position position) {
        return PositionDto.builder()
            .position_no(position.getPositionNo())
            .position_name(position.getPositionName())
            .position_high(position.getPositionHigh())
            .position_create_date(position.getPositionCreateDate())
            .position_update_date(position.getPositionUpdateDate())
            .position_status(position.getPositionStatus())
            .position_high_name(getClosestHighPositionName(position.getPositionHigh()))
            .build();
    }
     
    private String getHighPositionName(Long highPositionId) {
        if (highPositionId == null || highPositionId == 0) {
            return "-";
        }
        return positionRepository.findById(highPositionId)
            .map(Position::getPositionName)
            .orElse("미지정");
    } 
     
    public List<PositionDto> getAllPositionsForSelect() {
        List<Position> positions = positionRepository.findAllByOrderByPositionLevelAsc();
        return positions.stream()
            .filter(position -> position.getPositionStatus() == 0)  
            .map(this::mapToDto)
            .collect(Collectors.toList());
    } 

    public List<PositionDto> getAllPositionsForSelect(Long excludePositionId) { 
        List<Position> positions = positionRepository.findAllByOrderByPositionLevelAsc(); 
        return positions.stream()
            .filter(position -> position.getPositionStatus() == 0 && !position.getPositionNo().equals(excludePositionId))
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }

    
    public boolean isPositionNameDuplicate(String positionName) {
        return positionRepository.existsByPositionNameAndPositionStatus(positionName, 0L);
    }

    public boolean isPositionNameDuplicate(String positionName, Long excludedPositionId) { 
        return positionRepository.existsByPositionNameAndPositionNoNotAndPositionStatus(positionName, excludedPositionId, 0L);
    }
    
    // 등록
    @Transactional
    public void addPosition(String positionName, Long positionHigh) {
        Position newPosition = Position.builder()
                .positionName(positionName)
                .positionHigh(positionHigh)
                .positionStatus(0L)
                .positionLevel(0L)  
                .build();
        positionRepository.save(newPosition);

        if (positionHigh != null) { 
            Long newPositionNo = newPosition.getPositionNo();
 
            List<Position> positionsToUpdate = positionRepository.findByPositionHigh(positionHigh);
 
            for (Position position : positionsToUpdate) { 
                position.setPositionHigh(newPositionNo);
                positionRepository.save(position);
            }
        }

        newPosition.setPositionHigh(positionHigh);
        positionRepository.save(newPosition);

        updatePositionLevels();   
    }


    // 직위 레벨 변경
    private void updatePositionLevels() {
        List<Position> rootPositions = positionRepository.findByPositionHigh(0L);

        for (Position rootPosition : rootPositions) {
            setPositionLevel(rootPosition, 1); 
        }
    }

    private void setPositionLevel(Position position, int level) { 
        position.setPositionLevel((long) level);
        positionRepository.save(position);
 
        List<Position> childPositions = positionRepository.findByPositionHigh(position.getPositionNo());
 
        for (Position childPosition : childPositions) {
            setPositionLevel(childPosition, level + 1);  
        }
    }
    
    // 특정 직위의 정보 반환
    public Optional<PositionDto> getPositionById(Long id) {
        return positionRepository.findById(id).map(position -> {
        	PositionDto dto = mapToDto(position); 
            return dto;
        });
    }
     
    
    // 직위 소속 사원 수
    public long getMemberCountByPositionNo(Long positionNo) {
        return memberRepository.countByPositionNo(positionNo);
    }

    // 삭제
    @Transactional
    public boolean deletePosition(Long positionId) { 
        Optional<Position> positionOpt = positionRepository.findById(positionId);

        if (positionOpt.isPresent()) {
            Position position = positionOpt.get();
 
            long memberCount = getMemberCountByPositionNo(positionId);

            // 사원이 있는 경우 삭제 불가능 
            if (memberCount > 0) {
                return false;
            }
 
            position.setPositionStatus(1L);
            positionRepository.save(position);

            return true;
        }
 
        return false;
    }
    
    // 가장 가까운 상위 직위 
    private String getClosestHighPositionName(Long highPositionId) { 
        if (highPositionId == null || highPositionId == 0) {
            return "-";
        }
        
        Optional<Position> highPosition = positionRepository.findById(highPositionId);
         
        while (highPosition.isPresent() && highPosition.get().getPositionStatus() == 1L) {
            highPositionId = highPosition.get().getPositionHigh(); 
            if (highPositionId == null || highPositionId == 0) {
                return "-";  
            }
            highPosition = positionRepository.findById(highPositionId);
        }
         
        return highPosition.map(Position::getPositionName).orElse("-");
    }
    
    // 수정
    @Transactional
    public boolean updatePosition(PositionDto positionDto) {
        Optional<Position> positionOpt = positionRepository.findById(positionDto.getPosition_no());
        
        if (positionOpt.isPresent()) {
            Position position = positionOpt.get();
            Long oldPositionHigh = position.getPositionHigh();
            Long newPositionHigh = positionDto.getPosition_high();
             
            position.setPositionName(positionDto.getPosition_name());
            position.setPositionHigh(newPositionHigh);
            positionRepository.save(position);
             
            List<Position> formerSubordinates = positionRepository.findByPositionHigh(position.getPositionNo());
            for (Position subordinate : formerSubordinates) {
                subordinate.setPositionHigh(oldPositionHigh);
                positionRepository.save(subordinate);
            }
             
            List<Position> newSubordinates = positionRepository.findByPositionHigh(newPositionHigh);
            for (Position subordinate : newSubordinates) {
                if (!subordinate.getPositionNo().equals(position.getPositionNo())) {
                    subordinate.setPositionHigh(position.getPositionNo());
                    positionRepository.save(subordinate);
                }
            }
             
            updateAllPositionLevels();
            
            return true;
        }
        return false;
    }

    private void updateAllPositionLevels() {
        List<Position> allPositions = positionRepository.findAll();
        Map<Long, Integer> levelMap = new HashMap<>();
 
        for (Position position : allPositions) {
            if (position.getPositionHigh() == 0) {
                setPositionLevelRecursively(position, 1, levelMap);
            }
        }
    }

    private void setPositionLevelRecursively(Position position, int level, Map<Long, Integer> levelMap) {
        position.setPositionLevel((long) level);
        levelMap.put(position.getPositionNo(), level);
        positionRepository.save(position);

        List<Position> subordinates = positionRepository.findByPositionHigh(position.getPositionNo());
        for (Position subordinate : subordinates) {
            setPositionLevelRecursively(subordinate, level + 1, levelMap);
        }
    }
    
    public List<PositionDto> getActivePositions(Long id) {
        return positionRepository.findByPositionStatusAndPositionNoNotOrderByPositionLevelAsc(0L, id)
                .stream()
                .map(this::updatemapToDto)
                .collect(Collectors.toList());
    }
    
    public Long findHighestActiveParent(Long positionHigh) {
        if (positionHigh == null || positionHigh == 0) {
            return 0L;
        }
        
        Optional<Position> parentPosition = positionRepository.findById(positionHigh);
        while (parentPosition.isPresent() && parentPosition.get().getPositionStatus() == 1) {
            Long nextParentId = parentPosition.get().getPositionHigh();
            if (nextParentId == null || nextParentId == 0) {
                return 0L;
            }
            parentPosition = positionRepository.findById(nextParentId);
        }
        
        return parentPosition.map(Position::getPositionNo).orElse(0L);
    }
    
    private PositionDto updatemapToDto(Position position) {
        return PositionDto.builder()
                .position_no(position.getPositionNo())
                .position_name(position.getPositionName())
                .position_high(findHighestActiveParent(position.getPositionHigh()))
                .position_level(position.getPositionLevel())
                .build();
    }
}