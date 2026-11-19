package com.reecefoundation.reecefoundation.repository;

import com.reecefoundation.reecefoundation.model.Action;
import com.reecefoundation.reecefoundation.model.Owner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActionRepository extends JpaRepository <Action, Long> {
    List<Action> findByOwner(Owner owner);
}
