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
import lombok.Setter;

@Entity
@Table(name="fl_member_permission")
@NoArgsConstructor(access=AccessLevel.PROTECTED)
@AllArgsConstructor(access=AccessLevel.PROTECTED)
@Getter
@Setter
@Builder
public class MemberPermission {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="member_permission_no")
	private Long memberPermissionNo;
	
	@Column(name="member_no")
	private Long memberNo;
	
	@Column(name="menu_permission_no")
	private Long menuPermissionNo;
	
	@Column(name="member_permission_create_date")
	private LocalDateTime memberPermissionCreateDate;
	
	@Column(name="member_permission_update_date")
	private LocalDateTime memberPermissionUpdateDate;
	
	@Column(name="member_permission_status", insertable = false, updatable = true)
	private Long memberPermissionStatus;
}