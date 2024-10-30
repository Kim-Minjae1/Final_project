package com.fiveLink.linkOffice.organization.domain;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fiveLink.linkOffice.member.domain.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "fl_position")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Builder
public class Position {
    
    @Id
    @Column(name = "position_no")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long positionNo;

    @Column(name = "position_name")
    private String positionName;

    @Column(name = "position_high")
    private Long positionHigh;

    @Column(name = "position_create_date")
    @CreationTimestamp
    private LocalDateTime positionCreateDate;

    @Column(name = "position_update_date")
    @UpdateTimestamp
    private LocalDateTime positionUpdateDate;

    @Column(name = "position_status")
    private Long positionStatus;
    
    @Column(name = "position_level")
    private Long positionLevel;
    
    // member와 1대다 관계
    @OneToMany(mappedBy = "position", fetch = FetchType.LAZY)
    private List<Member> members;
    
}
