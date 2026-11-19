package com.reecefoundation.reecefoundation.repository;

import com.reecefoundation.reecefoundation.model.Owner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OwnerRepository extends JpaRepository<Owner, Long> {
}
