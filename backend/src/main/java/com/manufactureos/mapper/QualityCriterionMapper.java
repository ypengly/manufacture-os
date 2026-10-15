package com.manufactureos.mapper;

import com.manufactureos.entity.QualityCriterion;
import com.manufactureos.dto.qualityCriterion.QualityCriterionRequest;
import com.manufactureos.dto.qualityCriterion.QualityCriterionResponse;
import org.springframework.stereotype.Component;

@Component
public class QualityCriterionMapper {

    public QualityCriterionResponse toResponse(QualityCriterion e) {
        QualityCriterionResponse r = new QualityCriterionResponse();
        r.setId(e.getId());
        r.setProductId(e.getProductId());
        r.setName(e.getName());
        r.setMinValue(e.getMinValue());
        r.setMaxValue(e.getMaxValue());
        r.setUnit(e.getUnit());
        r.setMandatory(e.getMandatory());
        return r;
    }

    public void applyToEntity(QualityCriterionRequest req, QualityCriterion e) {
        e.setProductId(req.getProductId());
        e.setName(req.getName());
        e.setMinValue(req.getMinValue());
        e.setMaxValue(req.getMaxValue());
        e.setUnit(req.getUnit());
        e.setMandatory(req.getMandatory());
    }
}
