package com.fiveLink.linkOffice.chat.repository;

import com.fiveLink.linkOffice.chat.domain.ChatRead;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatReadRepository extends JpaRepository<ChatRead, Long> {
}
