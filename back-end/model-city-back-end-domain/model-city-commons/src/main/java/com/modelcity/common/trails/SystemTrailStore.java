package com.modelcity.common.trails;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Persistence port for system trails. Each vertical declares its own sub-interface so that, in the
 * monolith (single context with all verticals), the four adapters resolve to distinct beans.
 */
public interface SystemTrailStore {

    SystemTrailView save(NewSystemTrail event);

    Page<? extends SystemTrailView> search(SystemTrailQuery query, Pageable pageable);
}
