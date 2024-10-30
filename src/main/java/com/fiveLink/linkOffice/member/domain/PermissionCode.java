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
@Table(name="fl_permission_code")
@NoArgsConstructor(access=AccessLevel.PROTECTED)
@AllArgsConstructor(access=AccessLevel.PROTECTED)
@Getter
@Builder
public class PermissionCode {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="permission_code_no")
	private Long permissionCodeNo;
	
	@Column(name="permission_code_name")
	private String permissionCodeName;
	
	@Column(name="permission_code_create_date")
	private LocalDateTime permissionCodeCreateDate;
	
	@Column(name="permission_code_update_date")
	private LocalDateTime permissionCodeUpdateDate;
	
	@Column(name="permission_code_status")
	private Long permissionCodeStatus;
	
	
}