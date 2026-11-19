package com.reecefoundation.reecefoundation.repository;

import com.reecefoundation.reecefoundation.model.ProjectBankDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectBankDetailRepository extends JpaRepository<ProjectBankDetail, Long> {
    boolean existsByProjectIdAndAccountNumberAndSortCode(Long projectId, String accountNumber, String sortCode);
    boolean existsByProjectIdAndAccountNumberAndSortCodeAndIdNot(Long projectId, String accountNumber, String sortCode, Long id);
    List<ProjectBankDetail> findByProjectId(Long projectId);
    Optional<ProjectBankDetail> findFirstByProjectIdAndAccountNumberAndSortCode(Long projectId, String accountNumber, String sortCode);
}
