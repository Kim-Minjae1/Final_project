package com.fiveLink.linkOffice.security.vo;

import org.springframework.security.core.userdetails.User;

import com.fiveLink.linkOffice.member.domain.MemberDto;

import lombok.Getter;

@Getter
public class SecurityUser extends User{
	
	private static final long serialVersionUID = 1L;
	
	private MemberDto dto;
	
	public SecurityUser(MemberDto dto) {
			super(dto.getMember_number(), dto.getMember_pw(), dto.getAuthorities());
			this.dto = dto;
	}
}
