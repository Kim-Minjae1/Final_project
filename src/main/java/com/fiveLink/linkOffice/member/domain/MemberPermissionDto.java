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
public class MemberPermissionDto {
	
	private Long member_permission_no;
	private Long member_no;
	private Long menu_permission_no;
	private LocalDateTime member_permission_create_date;
	private LocalDateTime member_permission_update_date;
	private Long member_permission_status;
	
	public MemberPermission toEntity() {
		return MemberPermission.builder()
					.memberPermissionNo(member_permission_no)
					.memberNo(member_no)
					.menuPermissionNo(menu_permission_no)
					.memberPermissionCreateDate(member_permission_create_date)
					.memberPermissionUpdateDate(member_permission_update_date)
					.memberPermissionStatus(member_permission_status)
					.build();
	}
	
	public MemberPermissionDto toDto(MemberPermission memberpermission) {
		return MemberPermissionDto.builder()
					.member_permission_no(memberpermission.getMemberPermissionNo())
					.member_no(memberpermission.getMemberNo())
					.menu_permission_no(memberpermission.getMenuPermissionNo())
					.member_permission_create_date(memberpermission.getMemberPermissionCreateDate())
					.member_permission_update_date(memberpermission.getMemberPermissionUpdateDate())
					.member_permission_status(memberpermission.getMemberPermissionStatus())
					.build();
	}
}