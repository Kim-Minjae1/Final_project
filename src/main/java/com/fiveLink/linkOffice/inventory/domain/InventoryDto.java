package com.fiveLink.linkOffice.inventory.domain;

import java.time.LocalDateTime;

import com.fiveLink.linkOffice.member.domain.Member;
import com.fiveLink.linkOffice.organization.domain.Department;

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
public class InventoryDto {
		
	private Long inventory_no;
	private String inventory_name;
	private Long inventory_category_no;
	private String inventory_category_name;
	private Integer inventory_price;
	private Integer inventory_quantity;
	private String inventory_location;
	private Long member_no;
	private String member_name;
	private Long department_no;
	private String department_name;
	private String inventory_purchase_date;
	private LocalDateTime inventory_create_date;
	
	public Inventory toEntity() {
		// Entity의 매개변수 생성자 사용
		// protected -> builder
		Department department = Department.builder()
		        .departmentNo(department_no)  
		        .build();

		Member member = Member.builder()
		        .memberNo(member_no)  
		        .build();
		
		InventoryCategory inventoryCategory = InventoryCategory.builder()
		        .inventoryCategoryNo(inventory_category_no)  
		        .build();
		
		
		 return Inventory.builder()
			        .inventoryNo(inventory_no)
			        .inventoryName(inventory_name)
			        .inventoryPrice(inventory_price)
			        .inventoryQuantity(inventory_quantity)
			        .inventoryLocation(inventory_location)
			        .inventoryPurchaseDate(inventory_purchase_date)
			        .inventoryCreateDate(inventory_create_date)
			        .department(department)  
			        .member(member) 
			        .inventoryCategory(inventoryCategory)
			        .build();			
	}
	
	// Entity로 받아온(DB) 정보를 사용할때
		// Entity를 DTO로 변경
		public InventoryDto toDto(Inventory inventory) {
			return InventoryDto.builder()
					.inventory_no(inventory.getInventoryNo())
					.inventory_name(inventory.getInventoryName())
					.inventory_price(inventory.getInventoryPrice())
					.inventory_quantity(inventory.getInventoryQuantity())
					.inventory_location(inventory.getInventoryLocation())
					.inventory_purchase_date(inventory.getInventoryPurchaseDate())
					.inventory_create_date(inventory.getInventoryCreateDate())
					.build();
		}
		
}
