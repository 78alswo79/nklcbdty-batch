package com.nklcbdty.batch.nklcbdty.batch.user.vo;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user_interest")
public class UserInterestVo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id")
    private String userId;
    private String itemType;
    private String itemValue;
    @CreationTimestamp // 엔티티가 처음 저장될 때 현재 시간이 자동 설정됩니다.
    private LocalDateTime insertDts;
    @UpdateTimestamp // 엔티티가 업데이트될 때마다 현재 시간이 자동 설정됩니다.
    private LocalDateTime updateDts;
}
