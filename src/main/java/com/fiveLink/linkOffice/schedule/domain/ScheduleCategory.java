package com.fiveLink.linkOffice.schedule.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "fl_schedule_category")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Builder
public class ScheduleCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_category_no")
    private Long scheduleCategoryNo;

    @Column(name = "schedule_category_name")
    private String scheduleCategoryName;

    @Column(name = "schedule_category_color")
    private String scheduleCategoryColor;

    @Column(name = "schedule_category_admin")
    private Long scheduleCategoryAdmin;

    @CreationTimestamp
    @Column(name = "schedule_category_create_date")
    private LocalDateTime scheduleCategoryCreateDate;

    @UpdateTimestamp
    @Column(name = "schedule_category_update_date")
    private LocalDateTime scheduleCategoryUpdateDate;

    @Column(name = "schedule_category_status", nullable = false, columnDefinition = "INT UNSIGNED DEFAULT 0")
    private Long scheduleCategoryStatus;
}