package com.fiveLink.linkOffice.organization.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fiveLink.linkOffice.member.domain.MemberDto;
import com.fiveLink.linkOffice.organization.repository.DepartmentRepository;

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
public class DepartmentDto {
    private Long department_no;
    private String department_name; 
    private Long department_high;
    private String department_high_name;  
    private LocalDateTime department_create_date;
    private LocalDateTime department_update_date;
    private Long department_status;
    private List<DepartmentDto> subDepartments = new ArrayList<>();
    
    private Long departmentId;
    private String departmentName;
    private Long departmentHigh;
    private Long memberCount; 
    private Long subMemberCount;
    
    private List<MemberDto> members;
    
    public Department toEntity() { 
        return Department.builder()
            .departmentNo(department_no)
            .departmentName(department_name)
            .departmentHigh(department_high)
            .departmentCreateDate(department_create_date)
            .departmentUpdateDate(department_update_date)
            .departmentStatus(department_status)
            .build();
    }
    
    public DepartmentDto toDto(Department department) {
        return DepartmentDto.builder()
            .department_no(department.getDepartmentNo())
            .department_name(department.getDepartmentName())
            .department_high(department.getDepartmentHigh())
            .department_create_date(department.getDepartmentCreateDate())
            .department_update_date(department.getDepartmentUpdateDate())
            .department_status(department.getDepartmentStatus())
            .build();
    }
     
}
