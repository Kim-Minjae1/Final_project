package com.fiveLink.linkOffice.permission.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fiveLink.linkOffice.member.domain.MemberPermission;
import com.fiveLink.linkOffice.member.domain.MenuPermission;
import com.fiveLink.linkOffice.member.repository.MemberRepository;
import com.fiveLink.linkOffice.member.repository.PermissionCodeRepository;
import com.fiveLink.linkOffice.member.service.MemberPermissionService;
import com.fiveLink.linkOffice.permission.domain.Menu;
import com.fiveLink.linkOffice.permission.domain.MenuDto;
import com.fiveLink.linkOffice.permission.repository.MemberPermissionRepository;
import com.fiveLink.linkOffice.permission.repository.MenuPermissionRepository;
import com.fiveLink.linkOffice.permission.repository.MenuRepository;

@Service
public class PermissionService {

    private final MenuRepository menuRepository;
    private final PermissionCodeRepository permissionCodeRepository;
    private final MemberPermissionRepository memberPermissionRepository;
    private final MenuPermissionRepository menuPermissionRepository;
    private final MemberPermissionService memberPermissionService;
    private final MemberRepository memberRepository;

    @Autowired
    public PermissionService(MenuRepository menuRepository, PermissionCodeRepository permissionCodeRepository, 
                             MemberPermissionRepository memberPermissionRepository, MenuPermissionRepository menuPermissionRepository, MemberPermissionService memberPermissionService, MemberRepository memberRepository) {
        this.menuRepository = menuRepository;
        this.permissionCodeRepository = permissionCodeRepository;
        this.memberPermissionRepository = memberPermissionRepository;
        this.menuPermissionRepository = menuPermissionRepository;
        this.memberPermissionService = memberPermissionService;
        this.memberRepository = memberRepository;
    }

    public List<MenuDto> getPermissionList() {
        List<Menu> menus = menuRepository.findAll();
        return menus.stream()
            .filter(menu -> List.of(2L, 3L, 4L, 5L, 6L, 7L, 8L).contains(menu.getMenuNo()))
            .map(MenuDto::toDto)
            .collect(Collectors.toList());
    }

    public List<Object[]> findMembersByMenuNo(Long menuNo) { 
        return permissionCodeRepository.findMembersByMenuNoWithDetails(menuNo);
    } 

    // menu_no에 따른 menu_permission_no 조회
    public Long findMenuPermissionNosByMenuNo(Long menuNo) {
        // menu_no가 2일 경우 menu_permission_no를 3으로 설정
        if (menuNo == 2) {
            return 3L;
        }
         
        MenuPermission menuPermission = menuPermissionRepository.findByMenuNo(menuNo);
        
        return menuPermission.getMenuPermissionNo();
    }

    // menu_permission_no에 따른 사원 목록 조회
    public List<Object[]> findMembersByMenuPermissionNos(List<Long> menuPermissionNos) {
        return memberPermissionRepository.findMembersByMenuPermissionNos(menuPermissionNos);
    }
    
    @Transactional
    public void saveSelectedMembers(String menuNo, List<String> memberNos) {
        Long menuNoLong = Long.parseLong(menuNo);
        Long menuPermissionNo = (menuNoLong == 2) ? 3 : getMenuPermissionNo(menuNoLong);

        // 새 권한 부여
        for (String memberNo : memberNos) {
            try {
                Long memberNoLong = Long.parseLong(memberNo);
                MemberPermission memberPermission = MemberPermission.builder()
                    .memberNo(memberNoLong)
                    .menuPermissionNo(menuPermissionNo)
                    .memberPermissionCreateDate(LocalDateTime.now())
                    .memberPermissionUpdateDate(LocalDateTime.now())
                    .build();
                    
                memberPermissionRepository.save(memberPermission); 
                  
            } catch (Exception e) {
                System.err.println("권한자 등록 오류: " + e.getMessage());
            }
        } 
    }


    private Long getMenuPermissionNo(Long menuNo) {
        MenuPermission menuPermission = menuPermissionRepository.findByMenuNo(menuNo);
        return (menuPermission != null) ? menuPermission.getMenuPermissionNo() : null;
    }
 
    public List<Long> getAssignedMembersByMenuNo(Long menuNo) {
    	Long menuPermissionNo = (menuNo == 2) ? 3 : getMenuPermissionNo(menuNo); 
        return memberPermissionRepository.findMemberNosByMenuPermissionNo(menuPermissionNo);
    }
    
    // 삭제
    @Transactional
    public Long deleteSelectedMembers(List<Long> memberNos, Long menuNo) { 
 
        Long menuPermissionNo = findMenuPermissionNosByMenuNo(menuNo); 
 
        List<MemberPermission> memberPermissions = memberPermissionRepository.findByMemberNoInAndMenuPermissionNo(memberNos, menuPermissionNo);
        
        for (MemberPermission memberPermission : memberPermissions) {
            memberPermission.setMemberPermissionStatus(1L);
        }  
        memberPermissionRepository.saveAll(memberPermissions); 

        return menuPermissionNo;
    }

    // member_permission_status가 0인 값 존재 확인
    public boolean hasInactivePermissions(Long memberNo) {
        return memberPermissionRepository.existsByMemberNoAndMemberPermissionStatus(memberNo, 0);
    }

    // member_additional 상태 업데이트
    @Transactional
    public void updateMemberAdditionalStatus(Long memberNo) {
        // member_permission_status가 0인 레코드가 있으면 1, 없으면 0으로 업데이트
        Long newStatus = hasInactivePermissions(memberNo) ? 1L : 0L;
        memberRepository.updateMemberAdditionalStatus(memberNo, newStatus);
    }
}