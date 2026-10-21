package com.cricpulse.repository;

import com.cricpulse.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    List<Delivery> findByInningsIdOrderBySequenceOrderAsc(Long inningsId);
    List<Delivery> findByInningsIdOrderBySequenceOrderDesc(Long inningsId);
    Optional<Delivery> findTopByInningsIdOrderBySequenceOrderDesc(Long inningsId);
    List<Delivery> findTop12ByInningsIdOrderBySequenceOrderDesc(Long inningsId);
    List<Delivery> findByInningsIdAndOverNumberOrderByBallNumberInOverAsc(Long inningsId, Integer overNumber);
    long countByInningsId(Long inningsId);
    void deleteByInningsIdAndSequenceOrderGreaterThanEqual(Long inningsId, Integer sequenceOrder);
}
