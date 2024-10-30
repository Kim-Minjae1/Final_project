package com.fiveLink.linkOffice.member.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fiveLink.linkOffice.mapper.VacationMapper;
import com.fiveLink.linkOffice.member.repository.MemberRepository;
import com.fiveLink.linkOffice.permission.repository.MemberPermissionRepository;

import jakarta.transaction.Transactional;

@Service
public class MemberPermissionService {
	 
    private MemberRepository memberRepository; 
    private MemberPermissionRepository memberPermissionRepository;
    
    @Autowired
	public MemberPermissionService(MemberRepository memberRepository, MemberPermissionRepository memberPermissionRepository) {
		this.memberRepository = memberRepository;
		this.memberPermissionRepository = memberPermissionRepository; 
	} 
}
