package com.manufactureos.dto.qualityOps;

import com.manufactureos.entity.DefectStatus;
import jakarta.validation.constraints.NotNull;

public class DefectStatusUpdateRequest {

    @NotNull
    private DefectStatus status;

    private String correctiveAction;

    public DefectStatus getStatus() { return status; }
    public void setStatus(DefectStatus status) { this.status = status; }

    public String getCorrectiveAction() { return correctiveAction; }
    public void setCorrectiveAction(String correctiveAction) { this.correctiveAction = correctiveAction; }
}
