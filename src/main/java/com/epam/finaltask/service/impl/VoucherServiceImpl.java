package com.epam.finaltask.service.impl;

import com.epam.finaltask.dto.*;
import com.epam.finaltask.exception.AlreadyInUseException;
import com.epam.finaltask.exception.NotEnoughBalanceException;
import com.epam.finaltask.mapper.PaginationMapper;
import com.epam.finaltask.mapper.VoucherMapper;
import com.epam.finaltask.model.*;
import com.epam.finaltask.repository.UserRepository;
import com.epam.finaltask.repository.VoucherRepository;
import com.epam.finaltask.repository.specification.VoucherSpecifications;
import com.epam.finaltask.service.TokenStorageService;
import com.epam.finaltask.service.VoucherService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;
    private final VoucherMapper voucherMapper;
    private final UserRepository userRepository;
    private final TokenStorageService<VoucherPaginatedResponse> voucherPageStorage;
    private final TokenStorageService<UserDTO> userTokenStorageService;

    @Override
    public VoucherDTO create(VoucherDTO voucherDTO) {
        voucherPageStorage.clearAll();

        return voucherMapper.toVoucherDTO(voucherRepository.save(voucherMapper.toVoucher(voucherDTO)));
    }

    @Override
    public VoucherDTO order(String id, String userId) {
        Voucher voucher = voucherRepository.findById(UUID.fromString(id)).orElseThrow(
                () -> new EntityNotFoundException("Voucher not found")
        );

        User user = userRepository.findById(UUID.fromString(userId)).orElseThrow(
                () -> new EntityNotFoundException("User not found")
        );

        if (voucher.getUser() != null) {
            throw new AlreadyInUseException("Voucher already taken");
        }

        log.info("Processing payment operation from user {} with voucher {}", userId, voucher);

        BigDecimal newBalance = user.getBalance().subtract(voucher.getPrice());

        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new NotEnoughBalanceException("Not enough balance");
        }

        user.setBalance(newBalance);
        userRepository.save(user);
        userTokenStorageService.revoke(user.getId().toString());

        voucher.setUser(user);
        voucher.setStatus(VoucherStatus.REGISTERED);

        log.info("Successful payment operation, new user {} balance: {}", userId, user.getBalance());

        voucherPageStorage.clearAll();

        return voucherMapper.toVoucherDTO(voucherRepository.save(voucher));
    }

    @Override
    public VoucherDTO update(String id, VoucherDTO voucherDTO) {
        if (!voucherRepository.existsById(UUID.fromString(id))) {
            throw new EntityNotFoundException("Voucher not found");
        }

        Voucher voucher = voucherMapper.toVoucher(voucherDTO);

        voucherPageStorage.clearAll();

        return voucherMapper.toVoucherDTO(voucherRepository.save(voucher));
    }

    @Override
    public VoucherDTO getById(String id) {
        if (!voucherRepository.existsById(UUID.fromString(id))) {
            throw new EntityNotFoundException("Voucher not found");
        }

        return voucherMapper.toVoucherDTO(voucherRepository.getReferenceById(UUID.fromString(id)));
    }

    @Override
    public void delete(String voucherId) {
        if (!voucherRepository.existsById(UUID.fromString(voucherId))) {
            throw new EntityNotFoundException("Voucher not found");
        }

        voucherPageStorage.clearAll();

        voucherRepository.deleteById(UUID.fromString(voucherId));
    }

    @Override
    public VoucherDTO changeStatus(String id, VoucherStatusRequest statusRequest) {
        try {
            Voucher voucher = voucherRepository.findById(UUID.fromString(id)).orElseThrow(
                    () -> new EntityNotFoundException("Voucher not found")
            );

            User user = voucher.getUser();

            if (statusRequest.getVoucherStatus() != null) {
                switch (VoucherStatus.valueOf(statusRequest.getVoucherStatus())) {
                    case CREATED:
                        voucher.setUser(null);
                        voucherRepository.save(voucher);

                        break;
//                    case PAID:
//                        BigDecimal newBalance = user.getBalance().subtract(voucher.getPrice());
//
//                        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
//                            throw new NotEnoughBalanceException("Not enough balance");
//                        }
//
//                        user.setBalance(newBalance);
//                        userRepository.save(user);
//
//                        break;
                    case CANCELED:
                        user.setBalance(user.getBalance().add(voucher.getPrice()));
                        userRepository.save(user);

                        break;
                }
                voucher.setStatus(VoucherStatus.valueOf(statusRequest.getVoucherStatus()));
            }

            if (statusRequest.getIsHot() != null) {
                voucher.setIsHot(statusRequest.getIsHot());
            }

            voucherPageStorage.clearAll();

            return voucherMapper.toVoucherDTO(voucherRepository.save(voucher));
        } catch (IllegalArgumentException e) {
            throw new DataIntegrityViolationException("Status is not valid");
        }
    }

    @Override
    public VoucherPaginatedResponse findAllByUserId(PersonalVoucherFilterRequest filterRequest, Pageable pageable) {
        boolean isDefaultRequest = isFilterEmpty(filterRequest);

        String cacheKey = String.format("user_vouchers_id%s_p%d",
                filterRequest.getUserId() ,pageable.getPageNumber());

        if (isDefaultRequest) {
            VoucherPaginatedResponse cached = voucherPageStorage.get(cacheKey);
            if (cached != null) {
                return cached;
            }
        }

        Specification<Voucher> spec = VoucherSpecifications.withFilters(filterRequest);

        Page<VoucherDTO> dtoPage = voucherRepository.findAll(spec, pageable).map(voucherMapper::toVoucherDTO);
        VoucherPaginatedResponse paginatedResponse = PaginationMapper.toVoucherResponse(dtoPage);

        if (isDefaultRequest) {
            voucherPageStorage.store(cacheKey, paginatedResponse);
        }

        return paginatedResponse;
    }
    @Override
    public VoucherPaginatedResponse findWithFilers(VoucherFilerRequest voucherFilerRequest, Pageable pageable) {
        boolean isDefaultRequest = isFilterEmpty(voucherFilerRequest);

        String cacheKey = String.format("vouchers_p%d_s%d",
                pageable.getPageNumber(), pageable.getPageSize());

        if (isDefaultRequest) {
            VoucherPaginatedResponse cached = voucherPageStorage.get(cacheKey);
            if (cached != null) {
                return cached;
            }
        }

        Specification<Voucher> spec = VoucherSpecifications.withFilters(voucherFilerRequest);

        Page<VoucherDTO> dtoPage = voucherRepository.findAll(spec, pageable).map(voucherMapper::toVoucherDTO);
        VoucherPaginatedResponse paginatedResponse = PaginationMapper.toVoucherResponse(dtoPage);

        if (isDefaultRequest) {
            voucherPageStorage.store(cacheKey, paginatedResponse);
        }

        return paginatedResponse;
    }

    private boolean isFilterEmpty(VoucherFilerRequest filter) {
        boolean isEmpty = true;

        if (filter instanceof PersonalVoucherFilterRequest personalFilter) {
            isEmpty = personalFilter.getStatuses() == null;
        }

        if (filter instanceof AdminVoucherFilterRequest adminFilter) {
            isEmpty = adminFilter.getStatuses() == null &&
                        adminFilter.getIsHot() == null &&
                        adminFilter.getVoucherId() == null &&
                        adminFilter.getTitle() == null;
        }

        return filter.getTours() == null &&
                filter.getMinPrice() == null &&
                filter.getMaxPrice() == null &&
                filter.getHotels() == null &&
                filter.getTransfers() == null &&
                isEmpty;
    }
}
