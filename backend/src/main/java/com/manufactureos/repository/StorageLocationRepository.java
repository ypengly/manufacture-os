package com.manufactureos.repository;

import com.manufactureos.entity.StorageLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface StorageLocationRepository extends JpaRepository<StorageLocation, Long>, JpaSpecificationExecutor<StorageLocation> {
}
