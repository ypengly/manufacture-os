package com.manufactureos.dto.inventoryTransaction;

import com.manufactureos.entity.TransactionType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;

public class InventoryTransactionRequest {

    private String transactionNumber;

    private Long inventoryId;

    private Long batchId;

    private Long materialId;

    private Long productId;

    private Long factoryId;

    private Long locationId;

    private TransactionType type;

    private BigDecimal quantity;

    private String referenceType;

    private Long referenceId;

    private BigDecimal previousQuantity;

    private BigDecimal newQuantity;

    private Long userId;

    private String notes;

    private Instant createdAt;

    public String getTransactionNumber() { return transactionNumber; }
    public void setTransactionNumber(String transactionNumber) { this.transactionNumber = transactionNumber; }
    public Long getInventoryId() { return inventoryId; }
    public void setInventoryId(Long inventoryId) { this.inventoryId = inventoryId; }
    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }
    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Long getFactoryId() { return factoryId; }
    public void setFactoryId(Long factoryId) { this.factoryId = factoryId; }
    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }
    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public String getReferenceType() { return referenceType; }
    public void setReferenceType(String referenceType) { this.referenceType = referenceType; }
    public Long getReferenceId() { return referenceId; }
    public void setReferenceId(Long referenceId) { this.referenceId = referenceId; }
    public BigDecimal getPreviousQuantity() { return previousQuantity; }
    public void setPreviousQuantity(BigDecimal previousQuantity) { this.previousQuantity = previousQuantity; }
    public BigDecimal getNewQuantity() { return newQuantity; }
    public void setNewQuantity(BigDecimal newQuantity) { this.newQuantity = newQuantity; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
