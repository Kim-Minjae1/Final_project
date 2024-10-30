package com.fiveLink.linkOffice.mapper;

import com.fiveLink.linkOffice.chat.domain.ChatMember;
import com.fiveLink.linkOffice.chat.domain.ChatMemberDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface ChatMapper {
    //채팅방 목록 가져오기
    List<ChatMemberDto> selectChatList(Long memberNo);
    //채팅 내용 가져오기
    List<Map<String, Object>> getChatMessages(Map<String, Object> params);
    //채팅방 갯수 번호
    int countRoomNo();
    //채팅방 목록 디비 입력 이름+부서
    String searchPosition(Long memberNo);
    //채팅방 이름 가져오기
    String selectChatRoomName(Map<String, Object> params);
    //채팅방 이름 수정
    int updateChatRoom(Map<String, Object> params);

    //채팅방 타입 가져오기
    int chatRoomType(Long chatRooNo);
    //같은 채팅방에 있는 사용자 번호
    List<Long> chatRoomMemberNo(Long chatRoomNo);
    //수정 채팅방 이름 가져오기
    String selectMemberChatRoomName(Long chatRoomNo);
    //채팅방 멤버 가져오기
    List<Long> getMemberInfo(Long chatRoomNo);
    //개인 채팅방 정보 가져오기
    List<ChatMemberDto> getMembersByChatRoomNo(Long chatRoomNo);
    // 채팅방 나가기
    int chatRoomOut(Map<String, Object> params);
    // 채팅방 고정
    int chatRoomPin(Map<String, Object> params);
    // 채팅방 고정 여부
    int selectChatPin(Map<String, Object> params);
    //참여자 수
    int countParicipant(Long chatRoomNo);
    // 채팅방 별 안읽은 메시지 읽기
    List<Long> markMessagesAsReadForChatRoom(Map<String, Object> params);
    //채팅방 별 안읽은 메시지 개수
    List<Map<String, Object>> getUnreadCounts(Long memberNo);

    List<Long> findChatRoomsByMember(Long selectedMembers);

    List<Long> findMembersByChatRoom(Long chatRoomNo);

    //한 채팅방 참여자 번호
    List<Long> findChatRoomMembers(Map<String, Object> params);

    //한 채팅방 참여자 안읽은 개수
    int chatRoomUnread(Map<String, Object> params);
    //채팅 메시지
    String getChatMessageText(Long chatRoom);

    //멤버 이름
    String getMemberName(Long memberNo);
    //본인을 제외한 채팅방 목록 사용자의 정보
    List<Long> selectAnotherUser(Map<String, Object> params);
    //본인을 제외한 채팅방 목록 사용자의 사진
    String getProfileImageByMemberNo(Long memberNo);

    //상단 채팅방 고정 개수
    int getPinnedChatRoomCount(Long memberNo);
}
