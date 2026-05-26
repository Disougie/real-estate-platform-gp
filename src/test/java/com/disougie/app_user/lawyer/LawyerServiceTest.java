package com.disougie.app_user.lawyer;

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
import com.disougie.intial_contract.InitialContract;
import com.disougie.intial_contract.InitialContractRepository;
import com.disougie.intial_contract.InitialContractResponseMapper;
import com.disougie.intial_contract.InitialContractStatus;
import com.disougie.intial_contract.initialContractResponse;
import com.disougie.notification.NotificationService;
import com.disougie.property.PropertyRepository;
import com.disougie.property.entity.Property;
import com.disougie.property.entity.PropertyStatus;
import com.disougie.security.JwtService;

import jakarta.validation.ConstraintViolationException;

@ExtendWith(MockitoExtension.class)
public class LawyerServiceTest {

    @Mock
    private InitialContractRepository initialContractRepository;
    @Mock
    private InitialContractResponseMapper initialContractResponseMapper;
    @Mock
    private PropertyRepository propertyRepository;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private LawyerService lawyerService;

    private MockedStatic<JwtService> mockedJwtService;

    private AppUser mockLawyer;
    private AppUser mockOwner;
    private AppUser mockSeeker;
    private InitialContract mockContract;
    private Property mockProperty;

    @BeforeEach
    void setUp() {
        mockLawyer = AppUser.builder().id(1L).email("lawyer@test.com").build();
        mockOwner = AppUser.builder().id(2L).email("owner@test.com").build();
        mockSeeker = AppUser.builder().id(3L).email("seeker@test.com").build();
        
        mockContract = new InitialContract();
        mockContract.setId(10L);
        mockContract.setProperty_id("prop-1");
        mockContract.setOwner(mockOwner);
        mockContract.setSeeker(mockSeeker);
        mockContract.setStatus(InitialContractStatus.PENDING_PROCESSING);
        
        mockProperty = Property.builder()
                .id("prop-1")
                .status(PropertyStatus.PENDING_PROCESSING)
                .build();

        mockedJwtService = mockStatic(JwtService.class);
    }

    @AfterEach
    void tearDown() {
        mockedJwtService.close();
    }

    @Test
    @DisplayName("Should get pending contracts")
    void getPendingContracts_ShouldReturnList() {
        // Given
        when(initialContractRepository.findByStatus(InitialContractStatus.PENDING_PROCESSING))
                .thenReturn(List.of(mockContract));
        
        initialContractResponse expectedResponse = new initialContractResponse(10L, null, null, null, null, null, null, null);
        when(initialContractResponseMapper.apply(mockContract)).thenReturn(expectedResponse);

        // When
        List<initialContractResponse> result = lawyerService.getPendingContracts();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(initialContractRepository).findByStatus(InitialContractStatus.PENDING_PROCESSING);
    }

    @Test
    @DisplayName("Should assign contract to lawyer and update status to UNDER_PROCESS")
    void workingOnContract_ShouldAssignLawyerAndSendNotification() {
        // Given
        when(initialContractRepository.findById(10L)).thenReturn(Optional.of(mockContract));
        mockedJwtService.when(JwtService::getCurrentUser).thenReturn(mockLawyer);

        // When
        lawyerService.workingOnContract(10L);

        // Then
        assertEquals(mockLawyer, mockContract.getLawyer());
        assertEquals(InitialContractStatus.UNDER_PROCESS, mockContract.getStatus());
        verify(initialContractRepository).save(mockContract);
        verify(notificationService).sendNotification(eq(mockOwner), anyString());
        verify(notificationService).sendNotification(eq(mockSeeker), anyString());
    }

    @Test
    @DisplayName("Should throw ConstraintViolationException if contract is already assigned to a lawyer")
    void workingOnContract_ShouldThrowExceptionIfAlreadyAssigned() {
        // Given
        mockContract.setLawyer(AppUser.builder().id(99L).build());
        when(initialContractRepository.findById(10L)).thenReturn(Optional.of(mockContract));

        // When & Then
        ConstraintViolationException exception = assertThrows(ConstraintViolationException.class, 
                () -> lawyerService.workingOnContract(10L));
        assertTrue(exception.getMessage().contains("للاسف تم قبول هذا الحجز"));
        verify(initialContractRepository, never()).save(any(InitialContract.class));
    }

    @Test
    @DisplayName("Should complete contract if called by assigned lawyer")
    void completeContract_ShouldCompleteAndSendNotification() {
        // Given
        mockContract.setLawyer(mockLawyer);
        when(initialContractRepository.findById(10L)).thenReturn(Optional.of(mockContract));
        when(propertyRepository.findById("prop-1")).thenReturn(Optional.of(mockProperty));
        mockedJwtService.when(JwtService::getCurrentUser).thenReturn(mockLawyer);

        // When
        lawyerService.completeContract(10L);

        // Then
        assertEquals(InitialContractStatus.COMPLETED, mockContract.getStatus());
        assertEquals(PropertyStatus.COMPLETED, mockProperty.getStatus());
        verify(initialContractRepository).save(mockContract);
        verify(propertyRepository).save(mockProperty);
        verify(notificationService).sendNotification(eq(mockOwner), anyString());
        verify(notificationService).sendNotification(eq(mockSeeker), anyString());
    }

    @Test
    @DisplayName("Should throw AccessDeniedException if completion is attempted by unassigned lawyer")
    void completeContract_ShouldThrowAccessDeniedExceptionForUnassignedLawyer() {
        // Given
        mockContract.setLawyer(AppUser.builder().id(99L).build());
        when(initialContractRepository.findById(10L)).thenReturn(Optional.of(mockContract));
        when(propertyRepository.findById("prop-1")).thenReturn(Optional.of(mockProperty));
        mockedJwtService.when(JwtService::getCurrentUser).thenReturn(mockLawyer);

        // When & Then
        assertThrows(AccessDeniedException.class, () -> lawyerService.completeContract(10L));
        verify(initialContractRepository, never()).save(any(InitialContract.class));
    }
}
