package com.disougie.intial_contract;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import com.disougie.app_user.AppUser;
import com.disougie.app_user.AppUserRole;

@DataJpaTest
public class InitialContractRepositoryTest {

    @Autowired
    private InitialContractRepository initialContractRepository;

    @Autowired
    private TestEntityManager entityManager;

    private AppUser owner;
    private AppUser seeker;
    private AppUser lawyer;
    private InitialContract contract1;
    private InitialContract contract2;

    @BeforeEach
    void setUp() {
        owner = AppUser.builder().name("Owner").email("owner@test.com").password("pass").role(AppUserRole.USER).build();
        seeker = AppUser.builder().name("Seeker").email("seeker@test.com").password("pass").role(AppUserRole.USER).build();
        lawyer = AppUser.builder().name("Lawyer").email("lawyer@test.com").password("pass").role(AppUserRole.LAWYER).build();
        
        entityManager.persistAndFlush(owner);
        entityManager.persistAndFlush(seeker);
        entityManager.persistAndFlush(lawyer);

        contract1 = new InitialContract();
        contract1.setProperty_id("prop-1");
        contract1.setOwner(owner);
        contract1.setSeeker(seeker);
        contract1.setStatus(InitialContractStatus.PENDING_PROCESSING);
        contract1.setCreated_at(LocalDateTime.now().minusDays(2));
        entityManager.persistAndFlush(contract1);

        contract2 = new InitialContract();
        contract2.setProperty_id("prop-2");
        contract2.setOwner(owner);
        contract2.setSeeker(seeker);
        contract2.setLawyer(lawyer);
        contract2.setStatus(InitialContractStatus.UNDER_PROCESS);
        contract2.setCreated_at(LocalDateTime.now().minusDays(1));
        entityManager.persistAndFlush(contract2);
    }

    @AfterEach
    void tearDown() {
        initialContractRepository.deleteAll();
        entityManager.clear();
    }

    @Test
    @DisplayName("Should find contracts by owner")
    void findByOwner_ShouldReturnContracts() {
        List<InitialContract> results = initialContractRepository.findByOwner(owner);
        assertNotNull(results);
        assertEquals(2, results.size());
    }

    @Test
    @DisplayName("Should find contracts by user (owner or seeker) ordered by date descending")
    void findByUser_ShouldReturnContractsOrdered() {
        List<InitialContract> results = initialContractRepository.findByUser(seeker);
        assertFalse(results.isEmpty());
        assertEquals(2, results.size());
        // contract2 was created later (minusDays(1) vs minusDays(2)), so it should be first
        assertEquals(contract2.getId(), results.get(0).getId());
        assertEquals(contract1.getId(), results.get(1).getId());
    }

    @Test
    @DisplayName("Should find contracts by property ID")
    void findByPropertyId_ShouldReturnContract() {
        List<InitialContract> results = initialContractRepository.findByPropertyId("prop-1");
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals("prop-1", results.get(0).getProperty_id());
    }

    @Test
    @DisplayName("Should find contracts by status ordered by date descending")
    void findByStatus_ShouldReturnContracts() {
        List<InitialContract> results = initialContractRepository.findByStatus(InitialContractStatus.PENDING_PROCESSING);
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals(InitialContractStatus.PENDING_PROCESSING, results.get(0).getStatus());
    }

    @Test
    @DisplayName("Should find contracts by assigned lawyer ordered by date descending")
    void findByLawyer_ShouldReturnContracts() {
        List<InitialContract> results = initialContractRepository.findByLawyer(lawyer);
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals(lawyer.getId(), results.get(0).getLawyer().getId());
    }
}
