package com.reecefoundation.reecefoundation.repository;

import com.reecefoundation.reecefoundation.model.SubClassification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface SubClassificationRepository extends JpaRepository<SubClassification, Long> {
    List<SubClassification> findAllByIdInAndClassificationIdIn(Collection<Long> ids, Collection<Long> classificationIds);
}
