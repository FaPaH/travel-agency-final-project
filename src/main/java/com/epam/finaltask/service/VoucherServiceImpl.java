package com.epam.finaltask.service;

import com.epam.finaltask.dto.PersonalVoucherFilterRequest;
import com.epam.finaltask.dto.VoucherFilerRequest;
import com.epam.finaltask.dto.VoucherStatusRequest;
import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.exception.AlreadyInUseException;
import com.epam.finaltask.exception.NotEnoughBalanceException;
import com.epam.finaltask.mapper.PaginationMapper;
import com.epam.finaltask.mapper.VoucherMapper;
import com.epam.finaltask.model.*;
import com.epam.finaltask.repository.UserRepository;
import com.epam.finaltask.repository.VoucherRepository;
import com.epam.finaltask.repository.specification.VoucherSpecifications;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;
    private final VoucherMapper voucherMapper;
    private final UserRepository userRepository;
    private final TokenStorageService<VoucherPaginatedResponse> voucherPageStorage;

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

        if (user.getBalance().subtract(voucher.getPrice()).compareTo(BigDecimal.ZERO) < 0) {
            throw new NotEnoughBalanceException("Not enough balance");
        }

        user.setBalance(user.getBalance().subtract(voucher.getPrice()));

        voucher.setUser(user);
        voucher.setStatus(VoucherStatus.REGISTERED);
        userRepository.save(user);

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

            if (statusRequest.getVoucherStatus() != null) {
                voucher.setStatus(VoucherStatus.valueOf(statusRequest.getVoucherStatus()));
            } else if (statusRequest.getIsHot() != null) {
                voucher.setIsHot(statusRequest.getIsHot());
            } else {
                throw new DataIntegrityViolationException("Requested statuses is not set");
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

//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        boolean isAdmin = auth != null && auth.getAuthorities().stream()
//                .anyMatch(a -> a.getAuthority().equals("ADMIN") || a.getAuthority().equals("MANAGER"));

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

        return filter.getTours() == null &&
                filter.getMinPrice() == null &&
                filter.getMaxPrice() == null &&
                filter.getHotels() == null &&
                filter.getTransfers() == null &&
                isEmpty;
    }
}
