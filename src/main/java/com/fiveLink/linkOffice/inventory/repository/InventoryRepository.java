package com.fiveLink.linkOffice.inventory.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fiveLink.linkOffice.inventory.domain.Inventory;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

	
    // 부서에 따른 카테고리 요약 정보 조회
    @Query("SELECT ic.inventoryCategoryNo, " + 
            "ic.inventoryCategoryName, " +
            "AVG(i.inventoryPrice) AS average_price, " +
            "SUM(i.inventoryQuantity) AS total_quantity, " +
            "MAX(i.inventoryPurchaseDate) AS latest_purchase_date, " +
            "MAX(i.inventoryLocation) AS latest_location " + 
            "FROM Inventory i " +
            "JOIN i.inventoryCategory ic " +
            "JOIN i.department d " +
            "WHERE d.departmentNo = :departmentNo " +
            "GROUP BY ic.inventoryCategoryNo, ic.inventoryCategoryName " +
            "ORDER BY ic.inventoryCategoryNo")
    List<Object[]> findCategorySummaryByDepartment(@Param("departmentNo") Long departmentNo);

    
    
    @Query("SELECT i FROM Inventory i " +
		       "WHERE i.inventoryCategory.inventoryCategoryNo = :inventoryCategoryNo " +
		       "AND i.department.departmentNo = :departmentNo " +
    	       "ORDER BY i.inventoryPurchaseDate DESC") 
		List<Inventory> findByCategoryAndDepartment(@Param("inventoryCategoryNo") Long inventoryCategoryNo, 
		                                            @Param("departmentNo") Long departmentNo);
    
    @Query("SELECT DISTINCT ic.inventoryCategoryName FROM InventoryCategory ic")
    List<String> findAllCategoryNames();
    
    
 // 수량을 제외한 동일한 비품이 있는지 확인 (가격과 구입 날짜 추가)
    @Query("SELECT i FROM Inventory i WHERE i.inventoryCategory.inventoryCategoryName = :categoryName " +
           "AND i.inventoryName = :name " +
           "AND i.inventoryLocation = :location " +
           "AND i.inventoryPrice = :price " +  
           "AND i.inventoryPurchaseDate = :purchaseDate " +  
           "AND i.department.departmentNo = :departmentNo")
    Inventory findByCategoryAndNameAndLocationAndPriceAndDate(
        @Param("categoryName") String categoryName,
        @Param("name") String name,
        @Param("location") String location,
        @Param("price") Integer price, 
        @Param("purchaseDate") String purchaseDate, 
        @Param("departmentNo") Long departmentNo);
    
    @Query("SELECT ic.inventoryCategoryNo FROM InventoryCategory ic WHERE ic.inventoryCategoryName = :categoryName")
    Long findCategoryNoByName(@Param("categoryName") String categoryName);
    
    @Query("SELECT m.memberNo FROM Member m WHERE m.memberName = :memberName")
    Long findMemberNoByName(@Param("memberName") String memberName);
    
    @Query("SELECT m.memberName FROM Member m WHERE m.memberNumber = :memberNumber")
    String findMemberNameByMemberNumber(@Param("memberNumber") String memberNumber);
    
    
    
}
