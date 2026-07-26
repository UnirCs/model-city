package com.modelcity.core.otp.repository;

import com.modelcity.core.otp.repository.model.OperationAuthorization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OperationAuthorizationRepository extends JpaRepository<OperationAuthorization, UUID> {
}

