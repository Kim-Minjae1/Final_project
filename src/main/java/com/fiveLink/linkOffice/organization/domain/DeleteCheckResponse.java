package com.fiveLink.linkOffice.organization.domain;

import java.time.LocalDateTime;
import java.util.List;

import com.fiveLink.linkOffice.member.domain.Member;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor 
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Builder
public class DeleteCheckResponse {
    private boolean canDelete;
    private boolean hasEmployees;
    private boolean hasSubDepartments;
    private String message;
 
}
