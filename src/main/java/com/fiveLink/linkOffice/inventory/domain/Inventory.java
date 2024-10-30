package com.fiveLink.linkOffice.inventory.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.fiveLink.linkOffice.member.domain.Member;
import com.fiveLink.linkOffice.organization.domain.Department;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "Inventory")
@Table(name="fl_inventory")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Builder
public class Inventory {
	@Id
	@Column(name="inventory_no")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long inventoryNo;
	
	@Column(name="inventory_name")
	private String inventoryName;
	
	@ManyToOne
	@JoinColumn(name = "inventory_category_no")
	private InventoryCategory inventoryCategory;
	
	@Column(name="inventory_price")
	private Integer inventoryPrice;
	
	@Column(name="inventory_quantity")
	private Integer inventoryQuantity;
	
	@Column(name="inventory_location")
	private String inventoryLocation;
	
	@ManyToOne
    @JoinColumn(name = "member_no")
    private Member member;
	
	@ManyToOne
    @JoinColumn(name = "department_no")
    private Department department;
	
	@Column(name="inventory_purchase_date")
	private String inventoryPurchaseDate;
	
	@Column(name="inventory_create_date")
	@CreationTimestamp
	private LocalDateTime inventoryCreateDate;
}
