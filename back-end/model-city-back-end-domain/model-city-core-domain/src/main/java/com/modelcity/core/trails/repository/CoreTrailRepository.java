package com.modelcity.core.trails.repository;

import com.modelcity.core.trails.repository.model.CoreTrail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface CoreTrailRepository extends JpaRepository<CoreTrail, UUID>, JpaSpecificationExecutor<CoreTrail> {
}
