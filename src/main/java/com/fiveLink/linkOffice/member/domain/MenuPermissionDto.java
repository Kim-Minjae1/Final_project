package com.fiveLink.linkOffice.member.domain;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class MenuPermissionDto {

	private Long menu_permission_no;
	private Long menu_no;
	private Long permission_code_no;
	private LocalDateTime menu_permission_create_date;
	private LocalDateTime menu_permission_update_date;
	private Long menu_permission_status;
	
	public MenuPermission toEntity() {
		return MenuPermission.builder()
					.menuPermissionNo(menu_permission_no)
					.menuNo(menu_no)
					.permissionCodeNo(permission_code_no)
					.menuPermissionCreateDate(menu_permission_create_date)
					.menuPermissionUpdateDate(menu_permission_update_date)
					.menuPermissionStatus(menu_permission_status)
					.build();
	}
	 
	public MenuPermissionDto toDto(MenuPermission menupermission) {
	    return MenuPermissionDto.builder()
	        .menu_permission_no(menupermission.getMenuPermissionNo())
	        .menu_no(menupermission.getMenuNo())   
	        .permission_code_no(menupermission.getPermissionCodeNo())
	        .menu_permission_create_date(menupermission.getMenuPermissionCreateDate())
	        .menu_permission_update_date(menupermission.getMenuPermissionUpdateDate())
	        .menu_permission_status(menupermission.getMenuPermissionStatus())
	        .build();
	}

	
}