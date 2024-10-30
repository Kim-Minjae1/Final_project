package com.fiveLink.linkOffice.inventory.domain;


import java.util.List;

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

@Entity(name = "InventoryCategory")
@Table(name="fl_inventory_category")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
public class InventoryCategory {
	@Id
	@Column(name="inventory_category_no")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long inventoryCategoryNo;
	
	@Column(name="inventory_category_name")
	private String inventoryCategoryName;
	
	@OneToMany(mappedBy = "inventoryCategory", fetch = FetchType.LAZY)
    private List<Inventory> inventory;
}
