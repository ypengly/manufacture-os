package com.manufactureos.repository;

import com.manufactureos.entity.PurchaseOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface PurchaseOrderItemRepository extends JpaRepository<PurchaseOrderItem, Long>, JpaSpecificationExecutor<PurchaseOrderItem> {
    List<PurchaseOrderItem> findByPurchaseOrderId(Long purchaseOrderId);
}
