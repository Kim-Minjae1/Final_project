package com.fiveLink.linkOffice.security.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.fiveLink.linkOffice.member.domain.Member;
import com.fiveLink.linkOffice.member.domain.MemberDto;
import com.fiveLink.linkOffice.member.repository.MemberRepository;
import com.fiveLink.linkOffice.member.repository.PermissionCodeRepository;
import com.fiveLink.linkOffice.security.exception.CustomUserResignedException;
import com.fiveLink.linkOffice.security.vo.SecurityUser;

@Service
public class SecurityService implements UserDetailsService {
    
    private final MemberRepository memberRepository;
    private final PermissionCodeRepository permissionCodeRepository;
    
    @Autowired
    public SecurityService(MemberRepository memberRepository, PermissionCodeRepository permissionCodeRepository) {
        this.memberRepository = memberRepository;
        this.permissionCodeRepository = permissionCodeRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        
        Member member = memberRepository.findByMemberNumber(username);
        
        if (member == null) {
            throw new CustomUserResignedException("사용자를 찾을 수 없습니다: ");
            
        }

        if (member.getMemberStatus() == 1) {
            throw new CustomUserResignedException("사용자가 퇴사하였습니다: ");
        }

        MemberDto dto = new MemberDto().toDto(member);

        List<GrantedAuthority> authorities = new ArrayList<>();
        List<String> additionalPermissions = permissionCodeRepository.findPermissionsByMemberNo(member.getMemberNo());
        for (String permission : additionalPermissions) {
            authorities.add(new SimpleGrantedAuthority(permission));
        }
        
        dto.setAuthorities(authorities);

        return new SecurityUser(dto);
    }
}
