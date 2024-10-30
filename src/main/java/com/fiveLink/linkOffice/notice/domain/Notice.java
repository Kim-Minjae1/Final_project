package com.fiveLink.linkOffice.notice.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.fiveLink.linkOffice.member.domain.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "Notice")
@Table(name="fl_notice")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Builder
public class Notice {
	@Id
	@Column(name="notice_no")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long noticeNo;
	
	@Column(name="notice_title")
	private String noticeTitle;
	
	@Column(name="notice_content")
	private String noticeContent;
	
	@ManyToOne
    @JoinColumn(name = "member_no")
    private Member member;
	
	@Column(name="notice_create_date")
	private String noticeCreateDate;
	
	@Column(name="notice_update_date")
	private String noticeUpdateDate;
	
	@Column(name="notice_importance")
	private Integer noticeImportance;
	
	@Column(name="notice_ori_img")
	private String noticeOriImg;
	
	@Column(name="notice_new_img")
	private String noticeNewImg;
}
