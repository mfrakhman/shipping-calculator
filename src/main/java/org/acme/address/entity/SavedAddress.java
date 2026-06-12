package org.acme.address.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "saved_addresses")
public class SavedAddress extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "user_id", nullable = false)
    public UUID userId;

    @Column(nullable = false)
    public String label;

    @Column(name = "province_id", nullable = false)
    public String provinceId;

    @Column(name = "province_label", nullable = false)
    public String provinceLabel;

    @Column(name = "city_id", nullable = false)
    public String cityId;

    @Column(name = "city_label", nullable = false)
    public String cityLabel;

    @Column(name = "district_id", nullable = false)
    public String districtId;

    @Column(name = "district_label", nullable = false)
    public String districtLabel;

    @Column(name = "subdistrict_id", nullable = false)
    public Integer subdistrictId;

    @Column(name = "subdistrict_label", nullable = false)
    public String subdistrictLabel;

    @Column
    public String street;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    public LocalDateTime createdAt;

    public static List<SavedAddress> findByUser(UUID userId) {
        return list("userId = ?1 order by createdAt desc", userId);
    }
}
