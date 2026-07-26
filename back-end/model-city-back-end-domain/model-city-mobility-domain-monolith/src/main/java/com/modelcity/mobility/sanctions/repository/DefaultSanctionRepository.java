package com.modelcity.mobility.sanctions.repository;

import com.modelcity.mobility.sanctions.repository.model.Sanction;

/** Concrete Spring Data repository binding {@link SanctionRepository} to this topology's {@code Sanction}. */
public interface DefaultSanctionRepository extends SanctionRepository<Sanction> {
}
