package com.manufactureos.repository;

import com.manufactureos.entity.Workstation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface WorkstationRepository extends JpaRepository<Workstation, Long>, JpaSpecificationExecutor<Workstation> {
}
