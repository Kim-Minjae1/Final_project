package com.fiveLink.linkOffice.chat.repository;

import com.fiveLink.linkOffice.chat.domain.ChatMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMemberRepository extends JpaRepository<ChatMember, Long> {
}
