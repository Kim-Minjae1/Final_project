package com.fiveLink.linkOffice.notice.domain;

import java.time.LocalDateTime;

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
public class NoticeDto {

	private Long notice_no;
	private String notice_title;
	private String notice_content;
	private Long member_no;
	private String member_name;
	private String position_name;
	private String notice_create_date;
	private String notice_update_date;
	private Integer notice_importance;
	private String notice_ori_img;
	private String notice_new_img;
	
	private int search_type = 1;
	private String search_text;
	
	public Notice toEntity() {
		 return Notice.builder()
			        .noticeNo(notice_no)
			        .noticeTitle(notice_title)
			        .noticeContent(notice_content)
			        .noticeCreateDate(notice_create_date)
			        .noticeUpdateDate(notice_update_date)
			        .noticeImportance(notice_importance)
			        .noticeOriImg(notice_ori_img)
			        .noticeNewImg(notice_new_img)
			        .build();			
	}
	
	
			public NoticeDto toDto(Notice notice) {
				return NoticeDto.builder()
						.notice_no(notice.getNoticeNo())
						.notice_title(notice.getNoticeTitle())
						.notice_content(notice.getNoticeContent())
						.notice_create_date(notice.getNoticeCreateDate())
						.notice_update_date(notice.getNoticeUpdateDate())
						.notice_importance(notice.getNoticeImportance())
						.notice_ori_img(notice.getNoticeOriImg())
						.notice_new_img(notice.getNoticeNewImg())
						.build();
			}
}
