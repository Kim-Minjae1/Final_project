package com.fiveLink.linkOffice.inventory.domain;

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
public class InventoryCategoryDto {
	private Long inventory_category_no;
	private String inventory_category_name;
	
	public InventoryCategory toEntity() {
	    return InventoryCategory.builder()
	            .inventoryCategoryNo(inventory_category_no)
	            .inventoryCategoryName(inventory_category_name)
	            .build();
	}
	
	public InventoryCategoryDto toDto(InventoryCategory inventoryCategory) {
		return InventoryCategoryDto.builder()
				.inventory_category_no(inventoryCategory.getInventoryCategoryNo())
				.inventory_category_name(inventoryCategory.getInventoryCategoryName())
				.build();
				
	}
}
