package com.fiveLink.linkOffice.permission.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fiveLink.linkOffice.member.domain.MenuPermission;

@Repository
public interface MenuPermissionRepository extends JpaRepository<MenuPermission, Long> {
    MenuPermission findByMenuNo(Long menuNo);
    
    @Query("SELECT mp.menuPermissionNo FROM MenuPermission mp WHERE mp.menuNo = :menuNo")
    Long findMenuPermissionNosByMenuNo(@Param("menuNo") Long menuNo); 
}