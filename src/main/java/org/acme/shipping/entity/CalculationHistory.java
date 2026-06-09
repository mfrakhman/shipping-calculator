package org.acme.shipping.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Entity
@Table(name = "calculation_history")
public class CalculationHistory extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "user_id", nullable = false)
    public UUID userId;

    @Column(name = "origin_id", nullable = false)
    public Integer originId;

    @Column(name = "origin_label", nullable = false)
    public String originLabel;

    @Column(name = "destination_id", nullable = false)
    public Integer destinationId;

    @Column(name = "destination_label", nullable = false)
    public String destinationLabel;

    @Column(nullable = false)
    public Integer weight;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    public String result;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    public LocalDateTime createdAt;

    public static Optional<CalculationHistory> findCached(UUID userId, int originId, int destinationId, int weight) {
        return find("userId = ?1 and originId = ?2 and destinationId = ?3 and weight = ?4",
                userId, originId, destinationId, weight).firstResultOptional();
    }

    public static List<CalculationHistory> findByUser(UUID userId) {
        return list("userId = ?1 order by createdAt desc", userId);
    }

    public static List<CalculationHistory> findAllSorted() {
        return getEntityManager()
                .createQuery("from CalculationHistory order by createdAt desc", CalculationHistory.class)
                .getResultList();
    }

    public static long countTodayByUser(UUID userId) {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        return count("userId = ?1 and createdAt >= ?2", userId, startOfToday);
    }
}
