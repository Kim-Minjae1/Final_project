package com.fiveLink.linkOffice.inventory.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fiveLink.linkOffice.inventory.domain.InventoryCategoryDto;
import com.fiveLink.linkOffice.inventory.domain.InventoryDto;
import com.fiveLink.linkOffice.inventory.service.InventoryService;
import com.fiveLink.linkOffice.member.service.MemberService;
import com.fiveLink.linkOffice.organization.service.DepartmentService;

@Controller
public class InventoryApiController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(InventoryViewController.class);

    private final InventoryService inventoryService;
    private final DepartmentService departmentService;
    private final MemberService memberService;
    
    @Autowired
    public InventoryApiController(MemberService memberService, DepartmentService departmentService, InventoryService inventoryService) {
    	this.memberService = memberService;
        this.inventoryService = inventoryService;
        this.departmentService = departmentService;
    }
    
    // 비품 등록/수정 처리
    @ResponseBody
    @PostMapping("/submit-inventory")
    public Map<String, String> createOrUpdateInventory(
            @RequestParam("department") Long departmentNo,
            @RequestParam("date") String date,
            @RequestParam("category") String categoryName,
            @RequestParam("inventoryName") String inventoryName,
            @RequestParam("inventoryLocation") String inventoryLocation,
            @RequestParam("inventoryPrice") Integer inventoryPrice, 
            @RequestParam("inventoryManager") String memberName,
            @RequestParam("inventoryQuantity") Integer inventoryQuantity 
    ) {
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("res_code", "404");
        resultMap.put("res_msg", "비품 처리 중 오류가 발생했습니다.");


        // DTO 생성 및 값 설정
        InventoryDto dto = InventoryDto.builder()
            .department_no(departmentNo)
            .inventory_purchase_date(date)
            .inventory_category_name(categoryName)
            .inventory_name(inventoryName)
            .inventory_location(inventoryLocation)
            .inventory_price(inventoryPrice)
            .member_name(memberName)
            .inventory_quantity(inventoryQuantity)
            .build();

        try {
            boolean isUpdated = inventoryService.createOrUpdateInventory(dto);
            if (isUpdated) {
                resultMap.put("res_code", "200");
                resultMap.put("res_msg", "비품이 등록되었습니다.");
            } else {
                resultMap.put("res_code", "200");
                resultMap.put("res_msg", "새로운 비품이 등록되었습니다.");
            }
        } catch (Exception e) {
            resultMap.put("res_code", "500");
            resultMap.put("res_msg", "비품 처리 중 오류가 발생했습니다.");
        }

        return resultMap;
    }

    // 카테고리 등록 처리
    @PostMapping("/inventory/register-category")
    @ResponseBody
    public Map<String, String> registerCategory(@RequestBody InventoryCategoryDto inventoryCategoryDto) {
        Map<String, String> responseMap = new HashMap<>();

        // 입력된 카테고리명 처리
        String originalCategoryName = inventoryCategoryDto.getInventory_category_name();
        String normalizedCategoryName = originalCategoryName.replaceAll("\\s+", "");

        // 카테고리 이름이 비어 있거나 중복된 경우 처리
        if (normalizedCategoryName.isEmpty()) {
            responseMap.put("res_code", "400");
            responseMap.put("res_msg", "카테고리 이름을 입력해주세요.");
        } else if (inventoryService.isCategoryNameDuplicate(normalizedCategoryName)) {
            responseMap.put("res_code", "400");
            responseMap.put("res_msg", "카테고리 이름이 이미 존재합니다.");
        } else {
            inventoryCategoryDto.setInventory_category_name(normalizedCategoryName);
            inventoryService.registerCategory(inventoryCategoryDto);
            responseMap.put("res_code", "200");
            responseMap.put("res_msg", "카테고리가 등록되었습니다.");
        }

        return responseMap;
    }

    // 비품 수정 처리
    @PostMapping("/inventory/update")
    @ResponseBody
    public Map<String, String> updateInventory(@RequestBody InventoryDto dto) {
        Map<String, String> resultMap = new HashMap<>();
        try {
            if (dto.getInventory_no() == null) {
                throw new RuntimeException("수정할 비품 번호가 없습니다.");
            }
            inventoryService.updateInventory(dto);
            resultMap.put("res_code", "200");
            resultMap.put("res_msg", "비품이 수정되었습니다.");
        } catch (Exception e) {
            resultMap.put("res_code", "500");
            resultMap.put("res_msg", "수정 중 오류가 발생했습니다. " + e.getMessage());
        }
        return resultMap;
    }

    // 비품 삭제 처리
    @PostMapping("/inventory/delete/{no}")
    @ResponseBody
    public Map<String, String> deleteInventory(@PathVariable("no") Long no) {
        Map<String, String> resultMap = new HashMap<>();
        try {
            inventoryService.deleteInventory(no);
            resultMap.put("res_code", "200");
            resultMap.put("res_msg", "비품이 삭제되었습니다.");
        } catch (Exception e) {
            resultMap.put("res_code", "500");
            resultMap.put("res_msg", "삭제 중 오류가 발생했습니다.");
        }
        return resultMap;
    }
}
