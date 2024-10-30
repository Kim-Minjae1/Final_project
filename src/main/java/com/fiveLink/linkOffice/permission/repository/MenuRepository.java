package com.fiveLink.linkOffice.permission.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fiveLink.linkOffice.member.domain.MenuPermission;
import com.fiveLink.linkOffice.permission.domain.Menu;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> { 
}
