package com.fiveLink.linkOffice.chat.repository;

import com.fiveLink.linkOffice.chat.domain.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

}
