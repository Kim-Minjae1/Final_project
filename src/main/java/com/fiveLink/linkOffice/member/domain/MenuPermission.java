package com.fiveLink.linkOffice.member.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="fl_menu_permission")
@NoArgsConstructor(access=AccessLevel.PROTECTED)
@AllArgsConstructor(access=AccessLevel.PROTECTED)
@Getter
@Builder
public class MenuPermission {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="menu_permission_no")
	private Long menuPermissionNo;
	
	@Column(name="menu_no")
	private Long menuNo;
	
	@Column(name="permission_code_no")
	private Long permissionCodeNo;
	
	@Column(name="menu_permission_create_date")
	private LocalDateTime menuPermissionCreateDate;
	
	@Column(name="menu_permission_update_date")
	private LocalDateTime menuPermissionUpdateDate;
	
	@Column(name="menu_permission_status", insertable = false, updatable = true)
	private Long menuPermissionStatus;
	
}