package com.fiveLink.linkOffice.organization.domain;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fiveLink.linkOffice.inventory.domain.Inventory;
import com.fiveLink.linkOffice.member.domain.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="fl_department")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Builder
public class Department {
	@Id
	@Column(name="department_no")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long departmentNo;
	
	@Column(name="department_name")
	private String departmentName;
	
	@Column(name="department_high")
	private Long departmentHigh;
	
	@Column(name="department_create_date")
	@CreationTimestamp
	private LocalDateTime departmentCreateDate;
	
	@Column(name="department_update_date") 
	@UpdateTimestamp
	private LocalDateTime departmentUpdateDate;
	
	@Column(name="department_status") 
	private Long departmentStatus ;
	  
	
	// member와 1대다 관계
	 @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
	 private List<Member> members;
	 
	 @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
	 private List<Inventory> inventory;
}
