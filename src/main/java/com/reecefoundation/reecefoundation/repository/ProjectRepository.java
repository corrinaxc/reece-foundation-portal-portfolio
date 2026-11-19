package com.reecefoundation.reecefoundation.repository;

import com.reecefoundation.reecefoundation.model.Organisation;
import com.reecefoundation.reecefoundation.model.Owner;
import com.reecefoundation.reecefoundation.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByStatusOrderByIdDesc(String status);
    List<Project> findAllByOrderByIdDesc();
    List<Project> findByOwner(Owner owner);
}
