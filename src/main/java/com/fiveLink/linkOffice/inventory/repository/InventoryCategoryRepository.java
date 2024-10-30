package com.fiveLink.linkOffice.inventory.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fiveLink.linkOffice.inventory.domain.InventoryCategory;

public interface InventoryCategoryRepository extends JpaRepository<InventoryCategory, Long> {
	
	 
    @Query("SELECT c FROM InventoryCategory c WHERE REPLACE(c.inventoryCategoryName, ' ', '') = REPLACE(:categoryName, ' ', '')")
    Optional<InventoryCategory> findByInventoryCategoryName(@Param("categoryName") String categoryName);
    
    
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END FROM InventoryCategory c WHERE REPLACE(c.inventoryCategoryName, ' ', '') = REPLACE(:categoryName, ' ', '')")
    boolean existsByInventoryCategoryName(@Param("categoryName") String categoryName);
}
