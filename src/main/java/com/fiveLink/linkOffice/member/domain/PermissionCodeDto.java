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
public class PermissionCodeDto {

	private Long permission_code_no;
	private String permission_code_name;
	private LocalDateTime permission_code_create_date;
	private LocalDateTime permission_code_update_date;
	private Long permission_code_status;
	
	public PermissionCode toEntity() {
		return PermissionCode.builder()
					.permissionCodeNo(permission_code_no)
					.permissionCodeName(permission_code_name)
					.permissionCodeCreateDate(permission_code_create_date)
					.permissionCodeUpdateDate(permission_code_update_date)
					.permissionCodeStatus(permission_code_status)
					.build();
	}
	
	public PermissionCodeDto toDto(PermissionCode permissioncode) {
		return PermissionCodeDto.builder()
					.permission_code_no(permissioncode.getPermissionCodeNo())
					.permission_code_name(permissioncode.getPermissionCodeName())
					.permission_code_create_date(permissioncode.getPermissionCodeCreateDate())
					.permission_code_update_date(permissioncode.getPermissionCodeUpdateDate())
					.permission_code_status(permissioncode.getPermissionCodeStatus())
					.build();
	}
}