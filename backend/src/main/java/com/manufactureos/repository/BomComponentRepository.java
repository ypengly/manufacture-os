package com.manufactureos.repository;

import com.manufactureos.entity.BomComponent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BomComponentRepository extends JpaRepository<BomComponent, Long> {
    List<BomComponent> findByBomId(Long bomId);
}
