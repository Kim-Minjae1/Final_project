package com.fiveLink.linkOffice.meeting.domain;

import java.time.LocalDateTime;

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
public class MeetingDto {
    private Long meeting_no;
    private String meeting_name;
    private Long meeting_max;
    private String meeting_available_start;
    private String meeting_available_end;
    private String meeting_ori_image;
    private String meeting_new_image;
    private String meeting_comment;
    private LocalDateTime meeting_create_date;
    private LocalDateTime meeting_update_date;
    private Long meeting_status;

    public Meeting toEntity() {
        return Meeting.builder()
            .meetingNo(meeting_no)
            .meetingName(meeting_name)
            .meetingMax(meeting_max)
            .meetingAvailableStart(meeting_available_start)
            .meetingAvailableEnd(meeting_available_end)
            .meetingOriImage(meeting_ori_image)
            .meetingNewImage(meeting_new_image)
            .meetingComment(meeting_comment)
            .meetingCreateDate(meeting_create_date)
            .meetingUpdateDate(meeting_update_date)
            .meetingStatus(meeting_status)
            .build();
    }

    public static MeetingDto toDto(Meeting meeting) {
        return MeetingDto.builder()
            .meeting_no(meeting.getMeetingNo())
            .meeting_name(meeting.getMeetingName())
            .meeting_max(meeting.getMeetingMax())
            .meeting_available_start(meeting.getMeetingAvailableStart())
            .meeting_available_end(meeting.getMeetingAvailableEnd())
            .meeting_ori_image(meeting.getMeetingOriImage())
            .meeting_new_image(meeting.getMeetingNewImage())
            .meeting_comment(meeting.getMeetingComment())
            .meeting_create_date(meeting.getMeetingCreateDate())
            .meeting_update_date(meeting.getMeetingUpdateDate())
            .meeting_status(meeting.getMeetingStatus())
            .build();
    }
}