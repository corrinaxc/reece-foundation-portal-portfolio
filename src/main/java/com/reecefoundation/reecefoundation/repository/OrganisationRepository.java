package com.reecefoundation.reecefoundation.repository;

import com.reecefoundation.reecefoundation.model.Organisation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrganisationRepository extends JpaRepository<Organisation, Long> {
    List<Organisation> findAllByOrderByOrganisationNameAsc();
    boolean existsByOrganisationName(String organisationName);
    boolean existsByRegNumber(String regNumber);
}
