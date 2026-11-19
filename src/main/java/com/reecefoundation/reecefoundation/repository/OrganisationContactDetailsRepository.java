package com.reecefoundation.reecefoundation.repository;

import com.reecefoundation.reecefoundation.model.OrganisationContactDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrganisationContactDetailsRepository extends JpaRepository <OrganisationContactDetails, Long> {
    List<OrganisationContactDetails> findByOrganisationId(Long id);
}
