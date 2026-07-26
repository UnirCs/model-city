package com.modelcity.mobility.sanctions.store;

import com.modelcity.mobility.sanctions.controller.model.SanctionRequestDto;
import com.modelcity.mobility.sanctions.repository.SanctionRepository;
import com.modelcity.mobility.sanctions.repository.model.Sanction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultSanctionStoreTest {

    @Mock
    SanctionRepository<Sanction> sanctionRepository;

    DefaultSanctionStore store;

    @BeforeEach
    void setUp() {
        store = new DefaultSanctionStore(sanctionRepository);
    }

    @Test
    void create_normalizesLicensePlateToUpperCaseAndTrims() {
        when(sanctionRepository.save(any(Sanction.class))).thenAnswer(inv -> inv.getArgument(0));
        SanctionRequestDto request = new SanctionRequestDto("  1234abc  ", 40.0, -3.0, "imgdata");

        Sanction result = store.create("agent-sub", request);

        assertThat(result.getLicensePlate()).isEqualTo("1234ABC");
        assertThat(result.getAgentSub()).isEqualTo("agent-sub");
        assertThat(result.getImageBase64()).isEqualTo("imgdata");
        assertThat(result.getCreatedAt()).isNotNull();
    }

    @Test
    void findById_delegatesToRepository() {
        store.findById(1L);
        verify(sanctionRepository).findById(1L);
    }

    @Test
    @SuppressWarnings("unchecked")
    void search_delegatesWithSpecification() {
        Page<Sanction> page = new PageImpl<>(List.of());
        Pageable pageable = PageRequest.of(0, 20);
        doReturn(page).when(sanctionRepository).findAll(any(Specification.class), eq(pageable));

        Page<Sanction> result = store.search("1234ABC", null, null, pageable);

        assertThat(result).isNotNull();
        verify(sanctionRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @SuppressWarnings("unchecked")
    void findByPlatesIn_delegatesWithSpecification() {
        Page<Sanction> page = new PageImpl<>(List.of());
        Pageable pageable = PageRequest.of(0, 20);
        doReturn(page).when(sanctionRepository).findAll(any(Specification.class), eq(pageable));

        Page<Sanction> result = store.findByPlatesIn(List.of("1234ABC"), pageable);

        assertThat(result).isNotNull();
        verify(sanctionRepository).findAll(any(Specification.class), eq(pageable));
    }
}
