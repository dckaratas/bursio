package com.falcon.bursio.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "universities")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class University extends BaseEntity {

    @Column(nullable = false)
    private String name;

    private String city;

    @ElementCollection
    @CollectionTable(
            name = "university_email_domains",
            joinColumns = @JoinColumn(name = "university_id")
    )
    @Column(name = "domain")
    private List<String> emailDomains;

    @Column(nullable = false)
    private boolean active = true;
}
