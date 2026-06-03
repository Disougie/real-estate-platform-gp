package com.disougie.intial_contract;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.disougie.app_user.AppUser;
import com.disougie.app_user.AppUserRepository;
import com.disougie.notification.NotificationService;
import com.disougie.property.PropertyRepository;
import com.disougie.property.entity.Property;
import com.disougie.property.entity.PropertyStatus;
import com.disougie.property.entity.PropertyType;
import com.disougie.security.JwtService;

import jakarta.validation.ConstraintViolationException;

@ExtendWith(MockitoExtension.class)
public class InitialContractServiceTest {

    @Mock
    private InitialContractRepository initialContractRepository;
    @Mock
    private InitialContractResponseMapper initialContractResponseMapper;
    @Mock
    private PropertyRepository propertyRepository;
    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private InitialContractService initialContractService;

    private MockedStatic<JwtService> mockedJwtService;

    private AppUser owner;
    private AppUser seeker;
    private AppUser otherUser;
    private Property property;
    private InitialContract contract;

    @BeforeEach
    void setUp() {
        owner = AppUser.builder().id(1L).email("owner@test.com").build();
        seeker = AppUser.builder().id(2L).email("seeker@test.com").build();
        otherUser = AppUser.builder().id(3L).email("other@test.com").build();

        property = Property.builder()
                .id("prop-1")
                .ownerId(1L)
                .type(PropertyType.RENT)
                .price(1000.0)
                .status(PropertyStatus.AVAILABLE)
                .build();

        contract = new InitialContract();
        contract.setId(10L);
        contract.setProperty_id("prop-1");
        contract.setOwner(owner);
        contract.setSeeker(seeker);
        contract.setStatus(InitialContractStatus.PENDING_APPROVAL);

        mockedJwtService = mockStatic(JwtService.class);
    }

    @AfterEach
    void tearDown() {
        mockedJwtService.close();
    }

    @Test
    @DisplayName("Should return my initial contracts")
    void getMyInitialContracts_ShouldReturnList() {
        // Given
        mockedJwtService.when(JwtService::getCurrentUser).thenReturn(seeker);
        when(initialContractRepository.findByUser(seeker)).thenReturn(List.of(contract));
        initialContractResponse expectedResponse = new initialContractResponse(10L, null, null, null, null, null, null, null);
        when(initialContractResponseMapper.apply(contract)).thenReturn(expectedResponse);

        // When
        List<initialContractResponse> result = initialContractService.getMyInitialContracts();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(initialContractRepository).findByUser(seeker);
    }

    @Test
    @DisplayName("Should return initial contract by ID if authorized")
    void getInitialContract_ShouldReturnContractForAuthorizedUser() {
        // Given
        mockedJwtService.when(JwtService::getCurrentUser).thenReturn(owner);
        when(initialContractRepository.findById(10L)).thenReturn(Optional.of(contract));
        initialContractResponse expectedResponse = new initialContractResponse(10L, null, null, null, null, null, null, null);
        when(initialContractResponseMapper.apply(contract)).thenReturn(expectedResponse);

        // When
        initialContractResponse result = initialContractService.getInitialContract(10L);

        // Then
        assertNotNull(result);
        assertEquals(10L, result.id());
    }

    @Test
    @DisplayName("Should throw AccessDeniedException if not owner or seeker")
    void getInitialContract_ShouldThrowExceptionForUnauthorizedUser() {
        // Given
        mockedJwtService.when(JwtService::getCurrentUser).thenReturn(otherUser);
        when(initialContractRepository.findById(10L)).thenReturn(Optional.of(contract));

        // When & Then
        assertThrows(AccessDeniedException.class, () -> initialContractService.getInitialContract(10L));
    }

    @Test
    @DisplayName("Should create initial contract successfully")
    void createInitialContract_ShouldSaveContractAndSendNotification() {
        // Given
        InitialContractCreationRequest request = new InitialContractCreationRequest("prop-1", 12);
        mockedJwtService.when(JwtService::getCurrentUser).thenReturn(seeker);
        when(propertyRepository.findById("prop-1")).thenReturn(Optional.of(property));
        when(appUserRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(initialContractRepository.save(any(InitialContract.class))).thenAnswer(invocation -> {
            InitialContract saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        // When
        InitialContractCreationResponse result = initialContractService.createInitialContract(request);

        // Then
        assertNotNull(result);
        assertEquals(10L, result.id());
        verify(initialContractRepository).save(any(InitialContract.class));
        verify(notificationService).sendNotification(eq(owner), anyString());
    }

    @Test
    @DisplayName("Should throw ConstraintViolationException if owner tries to reserve own property")
    void createInitialContract_ShouldThrowExceptionIfOwnerReservesOwnProperty() {
        // Given
        InitialContractCreationRequest request = new InitialContractCreationRequest("prop-1", 12);
        mockedJwtService.when(JwtService::getCurrentUser).thenReturn(owner);
        when(propertyRepository.findById("prop-1")).thenReturn(Optional.of(property));
        when(appUserRepository.findById(1L)).thenReturn(Optional.of(owner));

        // When & Then
        ConstraintViolationException exception = assertThrows(ConstraintViolationException.class, 
                () -> initialContractService.createInitialContract(request));
        assertTrue(exception.getMessage().contains("can not reserve your own property"));
    }

    @Test
    @DisplayName("Should accept contract successfully")
    void acceptContract_ShouldChangeStatusToPendingProcessing() {
        // Given
        mockedJwtService.when(JwtService::getCurrentUser).thenReturn(owner);
        when(initialContractRepository.findById(10L)).thenReturn(Optional.of(contract));
        when(initialContractRepository.findByPropertyId("prop-1")).thenReturn(List.of(contract));
        when(propertyRepository.findById("prop-1")).thenReturn(Optional.of(property));

        // When
        initialContractService.acceptContract(10L);

        // Then
        assertEquals(InitialContractStatus.PENDING_PROCESSING, contract.getStatus());
        assertEquals(PropertyStatus.PENDING_PROCESSING, property.getStatus());
        verify(initialContractRepository).save(contract);
        verify(propertyRepository).save(property);
    }

    @Test
    @DisplayName("Should reject contract successfully")
    void rejectContract_ShouldChangeStatusToReject() {
        // Given
        mockedJwtService.when(JwtService::getCurrentUser).thenReturn(owner);
        when(initialContractRepository.findById(10L)).thenReturn(Optional.of(contract));
        when(propertyRepository.findById("prop-1")).thenReturn(Optional.of(property));

        // When
        initialContractService.rejectContract(10L);

        // Then
        assertEquals(InitialContractStatus.REJECT, contract.getStatus());
        assertEquals(PropertyStatus.AVAILABLE, property.getStatus());
        verify(initialContractRepository).save(contract);
        verify(propertyRepository).save(property);
    }

    @Test
    @DisplayName("Should throw exception if trying to accept an already rejected contract")
    void acceptContract_ShouldThrowExceptionIfAlreadyRejected() {
        // Given
        contract.setStatus(InitialContractStatus.REJECT);
        when(initialContractRepository.findById(10L)).thenReturn(Optional.of(contract));

        // When & Then
        ConstraintViolationException exception = assertThrows(ConstraintViolationException.class, 
                () -> initialContractService.acceptContract(10L));
        assertTrue(exception.getMessage().contains("already rejected"));
        verify(propertyRepository, never()).save(any(Property.class));
    }
}
