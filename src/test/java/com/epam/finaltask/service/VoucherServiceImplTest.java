package com.epam.finaltask.service;

import com.epam.finaltask.dto.*;
import com.epam.finaltask.exception.AlreadyInUseException;
import com.epam.finaltask.exception.NotEnoughBalanceException;
import com.epam.finaltask.exception.ResourceNotFoundException;
import com.epam.finaltask.mapper.VoucherMapper;
import com.epam.finaltask.model.User;
import com.epam.finaltask.model.Voucher;
import com.epam.finaltask.model.VoucherStatus;
import com.epam.finaltask.repository.UserRepository;
import com.epam.finaltask.repository.VoucherRepository;
import com.epam.finaltask.service.impl.VoucherServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VoucherServiceImplTest {

    @Mock private VoucherRepository voucherRepository;
    @Mock private VoucherMapper voucherMapper;
    @Mock private UserRepository userRepository;
    @Mock private TokenStorageService<VoucherPaginatedResponse> voucherPageStorage;
    @Mock private TokenStorageService<UserDTO> userTokenStorageService;

    @InjectMocks private VoucherServiceImpl voucherService;

    @BeforeEach
    void setUp() {
        voucherService = new VoucherServiceImpl(
                voucherRepository,
                voucherMapper,
                userRepository,
                voucherPageStorage,
                userTokenStorageService
        );
    }

    // --- CRUD ---

    @Test
    @DisplayName("Create: Should save voucher and clear cache")
    void create_ShouldSave() {
        VoucherDTO dto = new VoucherDTO();
        when(voucherMapper.toVoucher(dto)).thenReturn(new Voucher());
        when(voucherRepository.save(any())).thenReturn(new Voucher());
        when(voucherMapper.toVoucherDTO(any())).thenReturn(dto);

        voucherService.create(dto);

        verify(voucherPageStorage).clearAll();
        verify(voucherRepository).save(any());
    }

    @Test
    @DisplayName("GetById: Should return DTO when exists")
    void getById_Exists_ShouldReturnDto() {
        UUID id = UUID.randomUUID();
        VoucherDTO dto = new VoucherDTO();
        dto.setId(id.toString());

        when(voucherRepository.existsById(id)).thenReturn(true);
        when(voucherRepository.getReferenceById(id)).thenReturn(new Voucher());
        when(voucherMapper.toVoucherDTO(any())).thenReturn(dto);

        VoucherDTO result = voucherService.getById(id.toString());
        assertThat(result.getId()).isEqualTo(id.toString());
    }

    @Test
    @DisplayName("GetById: Should throw exception when not found")
    void getById_NotFound_ShouldThrowException() {
        UUID id = UUID.randomUUID();
        when(voucherRepository.existsById(id)).thenReturn(false);
        assertThatThrownBy(() -> voucherService.getById(id.toString()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Update: Should update and clear cache")
    void update_Exists_ShouldUpdate() {
        UUID id = UUID.randomUUID();
        when(voucherRepository.existsById(id)).thenReturn(true);
        when(voucherMapper.toVoucher(any())).thenReturn(new Voucher());
        when(voucherRepository.save(any())).thenReturn(new Voucher());
        when(voucherMapper.toVoucherDTO(any())).thenReturn(new VoucherDTO());

        voucherService.update(id.toString(), new VoucherDTO());

        verify(voucherPageStorage).clearAll();
    }

    @Test
    @DisplayName("Update: Should throw exception when not found")
    void update_NotFound_ShouldThrowException() {
        UUID id = UUID.randomUUID();
        when(voucherRepository.existsById(id)).thenReturn(false);
        assertThatThrownBy(() -> voucherService.update(id.toString(), new VoucherDTO()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Delete: Should delete and clear cache")
    void delete_Exists_ShouldDelete() {
        UUID id = UUID.randomUUID();
        when(voucherRepository.existsById(id)).thenReturn(true);

        voucherService.delete(id.toString());

        verify(voucherRepository).deleteById(id);
        verify(voucherPageStorage).clearAll();
    }

    @Test
    @DisplayName("Delete: Should throw exception when not found")
    void delete_NotFound_ShouldThrowException() {
        UUID id = UUID.randomUUID();
        when(voucherRepository.existsById(id)).thenReturn(false);
        assertThatThrownBy(() -> voucherService.delete(id.toString()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- Order Logic ---

    @Test
    @DisplayName("Order: Should succeed when balance is sufficient")
    void order_SufficientBalance_ShouldOrder() {
        UUID vid = UUID.randomUUID();
        UUID uid = UUID.randomUUID();
        Voucher v = Voucher.builder().id(vid).price(BigDecimal.TEN).build();
        User u = User.builder().id(uid).balance(BigDecimal.valueOf(20)).build();

        when(voucherRepository.findById(vid)).thenReturn(Optional.of(v));
        when(userRepository.findById(uid)).thenReturn(Optional.of(u));
        when(voucherRepository.save(v)).thenReturn(v);
        when(voucherMapper.toVoucherDTO(v)).thenReturn(new VoucherDTO());

        voucherService.order(vid.toString(), uid.toString());

        assertThat(u.getBalance()).isEqualByComparingTo(BigDecimal.TEN);
        assertThat(v.getUser()).isEqualTo(u);
        assertThat(v.getStatus()).isEqualTo(VoucherStatus.REGISTERED);
        verify(userTokenStorageService).revoke(uid.toString());
        verify(voucherPageStorage).clearAll();
    }

    @Test
    @DisplayName("Order: Should throw exception when balance is insufficient")
    void order_InsufficientBalance_ShouldThrow() {
        UUID vid = UUID.randomUUID();
        UUID uid = UUID.randomUUID();
        Voucher v = Voucher.builder().id(vid).price(BigDecimal.valueOf(100)).build();
        User u = User.builder().id(uid).balance(BigDecimal.TEN).build();

        when(voucherRepository.findById(vid)).thenReturn(Optional.of(v));
        when(userRepository.findById(uid)).thenReturn(Optional.of(u));

        assertThatThrownBy(() -> voucherService.order(vid.toString(), uid.toString()))
                .isInstanceOf(NotEnoughBalanceException.class);
    }

    @Test
    @DisplayName("Order: Should throw exception when voucher already owned")
    void order_AlreadyOwned_ShouldThrow() {
        UUID vid = UUID.randomUUID();
        Voucher v = Voucher.builder().id(vid).user(new User()).build();
        when(voucherRepository.findById(vid)).thenReturn(Optional.of(v));
        when(userRepository.findById(any())).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> voucherService.order(vid.toString(), UUID.randomUUID().toString()))
                .isInstanceOf(AlreadyInUseException.class);
    }

    // --- Change Status (UPDATED) ---

    @Test
    @DisplayName("Change Status (CANCELED): Should refund user, revoke token and detach user")
    void changeStatus_ToCanceled_ShouldRefundAndRevokeCache() {
        // Arrange
        UUID vid = UUID.randomUUID();
        UUID uid = UUID.randomUUID();
        User user = User.builder().id(uid).balance(BigDecimal.ZERO).build();
        Voucher voucher = Voucher.builder()
                .id(vid)
                .price(BigDecimal.TEN)
                .user(user)
                .status(VoucherStatus.PAID)
                .build();

        when(voucherRepository.findById(vid)).thenReturn(Optional.of(voucher));
        when(voucherRepository.save(voucher)).thenReturn(voucher);
        when(voucherMapper.toVoucherDTO(voucher)).thenReturn(new VoucherDTO());

        VoucherStatusRequest request = new VoucherStatusRequest();
        request.setVoucherStatus("CANCELED");

        // Act
        voucherService.changeStatus(vid.toString(), request);

        // Assert
        assertThat(user.getBalance()).isEqualByComparingTo(BigDecimal.TEN);
        assertThat(voucher.getUser()).isNull();
        assertThat(voucher.getStatus()).isEqualTo(VoucherStatus.CANCELED);

        verify(userTokenStorageService).revoke(uid.toString());
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Change Status (CREATED): Should detach user")
    void changeStatus_ToCreated_ShouldDetachUser() {
        UUID vid = UUID.randomUUID();
        Voucher voucher = Voucher.builder().id(vid).user(new User()).build();

        when(voucherRepository.findById(vid)).thenReturn(Optional.of(voucher));
        when(voucherRepository.save(voucher)).thenReturn(voucher);
        when(voucherMapper.toVoucherDTO(voucher)).thenReturn(new VoucherDTO());

        VoucherStatusRequest request = new VoucherStatusRequest();
        request.setVoucherStatus("CREATED");

        voucherService.changeStatus(vid.toString(), request);

        assertThat(voucher.getUser()).isNull();
        assertThat(voucher.getStatus()).isEqualTo(VoucherStatus.CREATED);
    }

    @Test
    @DisplayName("Change Status (Generic): Should just update status")
    void changeStatus_ToRegistered_ShouldUpdate() {
        UUID vid = UUID.randomUUID();
        Voucher voucher = Voucher.builder().id(vid).build();

        when(voucherRepository.findById(vid)).thenReturn(Optional.of(voucher));
        when(voucherRepository.save(voucher)).thenReturn(voucher);
        when(voucherMapper.toVoucherDTO(voucher)).thenReturn(new VoucherDTO());

        VoucherStatusRequest request = new VoucherStatusRequest();
        request.setVoucherStatus("REGISTERED");

        voucherService.changeStatus(vid.toString(), request);

        assertThat(voucher.getStatus()).isEqualTo(VoucherStatus.REGISTERED);
    }

    @Test
    @DisplayName("Change Status: Should update isHot flag")
    void changeStatus_UpdateIsHot() {
        UUID vid = UUID.randomUUID();
        Voucher voucher = Voucher.builder().id(vid).isHot(false).build();

        when(voucherRepository.findById(vid)).thenReturn(Optional.of(voucher));
        when(voucherRepository.save(voucher)).thenReturn(voucher);
        when(voucherMapper.toVoucherDTO(voucher)).thenReturn(new VoucherDTO());

        VoucherStatusRequest request = new VoucherStatusRequest();
        request.setIsHot(true); // Status is null

        voucherService.changeStatus(vid.toString(), request);

        assertThat(voucher.getIsHot()).isTrue();
    }

    @Test
    @DisplayName("Change Status: Should throw DataIntegrityViolationException on invalid enum")
    void changeStatus_InvalidEnum_ShouldThrowException() {
        UUID vid = UUID.randomUUID();
        when(voucherRepository.findById(vid)).thenReturn(Optional.of(new Voucher()));

        VoucherStatusRequest request = new VoucherStatusRequest();
        request.setVoucherStatus("INVALID_STATUS");

        assertThatThrownBy(() -> voucherService.changeStatus(vid.toString(), request))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    // --- Filters ---

    @Test
    @DisplayName("FindWithFilters: Should use cache when filter is empty")
    void findWithFilters_Empty_UseCache() {
        Pageable p = PageRequest.of(0, 10);
        when(voucherPageStorage.get(anyString())).thenReturn(new VoucherPaginatedResponse());

        voucherService.findWithFilers(new VoucherFilerRequest(), p);

        verify(voucherRepository, never()).findAll(any(Specification.class), eq(p));
    }

    @Test
    @DisplayName("FindWithFilters: Should fetch DB when cache miss")
    void findWithFilters_Empty_CacheMiss() {
        Pageable p = PageRequest.of(0, 10);
        when(voucherPageStorage.get(anyString())).thenReturn(null);
        when(voucherRepository.findAll(any(Specification.class), eq(p))).thenReturn(Page.empty());

        voucherService.findWithFilers(new VoucherFilerRequest(), p);

        verify(voucherPageStorage).store(anyString(), any());
    }

    @Test
    @DisplayName("FindAllByUserId: Should fetch DB")
    void findAllByUserId_ShouldFetch() {
        PersonalVoucherFilterRequest req = new PersonalVoucherFilterRequest();
        req.setUserId(UUID.randomUUID());
        Pageable p = PageRequest.of(0, 10);

        when(voucherPageStorage.get(anyString())).thenReturn(null);
        when(voucherRepository.findAll(any(Specification.class), eq(p))).thenReturn(Page.empty());

        voucherService.findAllByUserId(req, p);

        verify(voucherPageStorage).store(anyString(), any());
    }

    @Test
    @DisplayName("Order: Should throw ResourceNotFoundException when Voucher not found")
    void order_VoucherNotFound_ShouldThrow() {
        String vid = UUID.randomUUID().toString();
        when(voucherRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> voucherService.order(vid, UUID.randomUUID().toString()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Order: Should throw ResourceNotFoundException when User not found")
    void order_UserNotFound_ShouldThrow() {
        UUID vid = UUID.randomUUID();
        when(voucherRepository.findById(vid)).thenReturn(Optional.of(new Voucher()));
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> voucherService.order(vid.toString(), UUID.randomUUID().toString()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Change Status: Should throw ResourceNotFoundException when Voucher not found")
    void changeStatus_VoucherNotFound_ShouldThrow() {
        when(voucherRepository.findById(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> voucherService.changeStatus(UUID.randomUUID().toString(), new VoucherStatusRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("FindWithFilters: Should not use cache when filter is NOT empty")
    void findWithFilters_NotEmpty_ShouldNotUseCache() {
        VoucherFilerRequest filter = new VoucherFilerRequest();
        filter.setMinPrice(BigDecimal.ONE);
        Pageable p = PageRequest.of(0, 10);
        when(voucherRepository.findAll(any(Specification.class), eq(p))).thenReturn(Page.empty());

        voucherService.findWithFilers(filter, p);

        verify(voucherPageStorage, never()).get(anyString());
        verify(voucherPageStorage, never()).store(anyString(), any());
    }

    @Test
    @DisplayName("isFilterEmpty: Should handle AdminVoucherFilterRequest branches")
    void isFilterEmpty_AdminFilter_ShouldCheckAllFields() {
        AdminVoucherFilterRequest adminFilter = new AdminVoucherFilterRequest();
        adminFilter.setTitle("Some Tour");
        Pageable p = PageRequest.of(0, 10);
        when(voucherRepository.findAll(any(Specification.class), eq(p))).thenReturn(Page.empty());

        voucherService.findWithFilers(adminFilter, p);

        verify(voucherPageStorage, never()).get(anyString());
    }

    @Test
    @DisplayName("isFilterEmpty: Should handle PersonalVoucherFilterRequest branches")
    void isFilterEmpty_PersonalFilter_ShouldCheckStatuses() {
        PersonalVoucherFilterRequest personalFilter = new PersonalVoucherFilterRequest();
        personalFilter.setStatuses(new ArrayList<>());
        Pageable p = PageRequest.of(0, 10);
        when(voucherRepository.findAll(any(Specification.class), eq(p))).thenReturn(Page.empty());

        voucherService.findAllByUserId(personalFilter, p);

        verify(voucherPageStorage, never()).get(anyString());
    }

    @Test
    @DisplayName("isFilterEmpty: Should return true for default request objects")
    void isFilterEmpty_VariousNullFields_ShouldReturnTrue() {
        VoucherFilerRequest filter = new VoucherFilerRequest();
        Pageable p = PageRequest.of(0, 10);
        when(voucherPageStorage.get(anyString())).thenReturn(new VoucherPaginatedResponse());

        voucherService.findWithFilers(filter, p);

        verify(voucherPageStorage).get(anyString());
    }

    @Test
    @DisplayName("FindWithFilters: Should return cached response when cache hit")
    void findWithFilters_CacheHit_ShouldReturnCachedResponse() {
        // Arrange
        VoucherFilerRequest filter = new VoucherFilerRequest();
        Pageable pageable = PageRequest.of(0, 10);
        VoucherPaginatedResponse expectedResponse = new VoucherPaginatedResponse();

        when(voucherPageStorage.get(anyString())).thenReturn(expectedResponse);

        // Act
        VoucherPaginatedResponse actualResponse = voucherService.findWithFilers(filter, pageable);

        // Assert
        assertThat(actualResponse).isSameAs(expectedResponse);
        verify(voucherRepository, never()).findAll(any(Specification.class), any(Pageable.class));
        verify(voucherPageStorage, never()).store(anyString(), any());
    }

    @Test
    @DisplayName("Change Status: Canceled branch should skip refund if user is null")
    void changeStatus_CanceledWithoutUser_ShouldNotRefund() {
        UUID vid = UUID.randomUUID();
        Voucher voucher = Voucher.builder()
                .id(vid)
                .user(null)
                .status(VoucherStatus.REGISTERED)
                .build();

        when(voucherRepository.findById(vid)).thenReturn(Optional.of(voucher));
        when(voucherRepository.save(voucher)).thenReturn(voucher);
        when(voucherMapper.toVoucherDTO(voucher)).thenReturn(new VoucherDTO());

        VoucherStatusRequest request = new VoucherStatusRequest();
        request.setVoucherStatus("CANCELED");

        voucherService.changeStatus(vid.toString(), request);

        verify(userRepository, never()).save(any());
        verify(userTokenStorageService, never()).revoke(anyString());
        assertThat(voucher.getStatus()).isEqualTo(VoucherStatus.CANCELED);
    }

    @Test
    @DisplayName("isFilterEmpty: Should return true for PersonalVoucherFilterRequest with null statuses")
    void isFilterEmpty_PersonalFilter_NullStatuses_ReturnsTrue() {
        // Arrange
        PersonalVoucherFilterRequest filter = new PersonalVoucherFilterRequest();
        filter.setUserId(UUID.randomUUID());
        filter.setStatuses(null);

        Pageable p = PageRequest.of(0, 10);
        when(voucherPageStorage.get(anyString())).thenReturn(new VoucherPaginatedResponse());

        // Act
        voucherService.findAllByUserId(filter, p);

        // Assert
        verify(voucherPageStorage).get(anyString());
    }

    @Test
    @DisplayName("isFilterEmpty: Should return false for PersonalVoucherFilterRequest with non-null statuses")
    void isFilterEmpty_PersonalFilter_WithStatuses_ReturnsFalse() {
        // Arrange
        PersonalVoucherFilterRequest filter = new PersonalVoucherFilterRequest();
        filter.setStatuses(new ArrayList<>());

        Pageable p = PageRequest.of(0, 10);
        when(voucherRepository.findAll(any(Specification.class), eq(p))).thenReturn(Page.empty());

        // Act
        voucherService.findAllByUserId(filter, p);

        // Assert
        verify(voucherPageStorage, never()).get(anyString());
    }

    @Test
    @DisplayName("isFilterEmpty: Should return true for AdminVoucherFilterRequest with all null target fields")
    void isFilterEmpty_AdminFilter_AllNull_ReturnsTrue() {
        // Arrange
        AdminVoucherFilterRequest filter = new AdminVoucherFilterRequest();

        Pageable p = PageRequest.of(0, 10);
        when(voucherPageStorage.get(anyString())).thenReturn(new VoucherPaginatedResponse());

        // Act
        voucherService.findWithFilers(filter, p);

        // Assert
        verify(voucherPageStorage).get(anyString());
    }

    @Test
    @DisplayName("isFilterEmpty: Should return false when AdminVoucherFilterRequest has any field set")
    void isFilterEmpty_AdminFilter_OneFieldNotProperty_ReturnsFalse() {
        AdminVoucherFilterRequest filter = new AdminVoucherFilterRequest();
        filter.setIsHot(true);

        Pageable p = PageRequest.of(0, 10);
        when(voucherRepository.findAll(any(Specification.class), eq(p))).thenReturn(Page.empty());

        // Act
        voucherService.findWithFilers(filter, p);

        // Assert
        verify(voucherPageStorage, never()).get(anyString());
    }

    @Test
    @DisplayName("isFilterEmpty: Should return false when base VoucherFilerRequest field is set")
    void isFilterEmpty_BaseFilter_SortFieldNotNull_ReturnsFalse() {
        // Arrange
        VoucherFilerRequest filter = new VoucherFilerRequest();
        filter.setSortField("price");

        Pageable p = PageRequest.of(0, 10);
        when(voucherRepository.findAll(any(Specification.class), eq(p))).thenReturn(Page.empty());

        // Act
        voucherService.findWithFilers(filter, p);

        // Assert
        verify(voucherPageStorage, never()).get(anyString());
    }
}
