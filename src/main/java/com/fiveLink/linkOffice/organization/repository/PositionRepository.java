package com.fiveLink.linkOffice.organization.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fiveLink.linkOffice.organization.domain.Department;
import com.fiveLink.linkOffice.organization.domain.Position;

public interface PositionRepository extends JpaRepository<Position, Long> {
 
	List<Position> findAllByOrderByPositionLevelAsc();
	List<Position> findAllByOrderByPositionHighAsc();
    List<Position> findByPositionHighIsNotNull();
    
    boolean existsByPositionNameAndPositionStatus(String positionName, Long positionStatus);
    
    boolean existsByPositionNameAndPositionNoNotAndPositionStatus(String positionName, Long positionNo, Long positionStatus); 
  
    List<Position> findByPositionHigh(Long positionHigh);
    
    List<Position> findAllByPositionStatus(Long status);
    
    List<Position> findByPositionStatusAndPositionNoNotOrderByPositionLevelAsc(Long status, Long positionNo);
} 