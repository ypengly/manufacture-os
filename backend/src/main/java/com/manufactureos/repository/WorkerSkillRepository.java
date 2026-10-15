package com.manufactureos.repository;

import com.manufactureos.entity.WorkerSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkerSkillRepository extends JpaRepository<WorkerSkill, Long> {
    List<WorkerSkill> findByWorkerId(Long workerId);
    boolean existsByWorkerIdAndSkillIgnoreCase(Long workerId, String skill);
}
