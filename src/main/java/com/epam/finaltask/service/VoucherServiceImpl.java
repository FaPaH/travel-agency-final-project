package com.epam.finaltask.service;

import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.mapper.PaginationMapper;
import com.epam.finaltask.mapper.VoucherMapper;
import com.epam.finaltask.model.*;
import com.epam.finaltask.repository.UserRepository;
import com.epam.finaltask.repository.VoucherRepository;
import com.epam.finaltask.repository.specification.VoucherSpecifications;
import lombok.RequiredArgsConstructor;
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

    //TODO: Implement cashing for hot vouchers and most accessed vouchers
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

        if (!userRepository.existsById(UUID.fromString(userId))) {
            throw new RuntimeException("User not found");
        } else if (!voucherRepository.existsById(UUID.fromString(id))) {
            throw new RuntimeException("Voucher not found");
        }

        Voucher voucher = voucherRepository.findById(UUID.fromString(id)).get();

        if (voucher.getUser() != null) {
            throw new RuntimeException("Voucher already taken");
        }

        voucher.setUser(userRepository.findById(UUID.fromString(userId)).get());

        voucherPageStorage.clearAll();

        return voucherMapper.toVoucherDTO(voucherRepository.save(voucher));
    }

    @Override
    public VoucherDTO update(String id, VoucherDTO voucherDTO) {
        if (!voucherRepository.existsById(UUID.fromString(id))) {
            throw new RuntimeException("Voucher not found");
        }

        Voucher voucher = voucherMapper.toVoucher(voucherDTO);

        voucherPageStorage.clearAll();

        return voucherMapper.toVoucherDTO(voucherRepository.save(voucher));
    }

    @Override
    public void delete(String voucherId) {
        if (!voucherRepository.existsById(UUID.fromString(voucherId))) {
            throw new IllegalStateException("Voucher not found");
        }

        voucherPageStorage.clearAll();

        voucherRepository.deleteById(UUID.fromString(voucherId));
    }

    @Override
    public VoucherDTO changeHotStatus(String id, VoucherDTO voucherDTO) {
        if (!voucherRepository.existsById(UUID.fromString(id))) {
            throw new RuntimeException("Voucher not found");
        }

        voucherDTO.setIsHot(!voucherDTO.getIsHot());

        voucherPageStorage.clearAll();

        return voucherMapper.toVoucherDTO(voucherRepository.save(voucherMapper.toVoucher(voucherDTO)));
    }

    @Override
    public PaginatedResponse<VoucherDTO> findAllByUserId(String userId, Pageable pageable) {

        String cacheKey = String.format("user_vouchers_id%s_p%d",
                userId ,pageable.getPageNumber());

        VoucherPaginatedResponse cached = voucherPageStorage.get(cacheKey);

        if (cached != null) {
            return cached;
        }

        Page<VoucherDTO> dtoPage = voucherRepository.findAllByUserId(UUID.fromString(userId), pageable).map(voucherMapper::toVoucherDTO);
        PaginatedResponse<VoucherDTO> paginatedResponse = PaginationMapper.toPaginatedResponse(dtoPage);

        voucherPageStorage.store(cacheKey, (VoucherPaginatedResponse) paginatedResponse);

        return paginatedResponse;
    }

    @Override
    public PaginatedResponse<VoucherDTO> findWithFilers(VoucherFiler voucherFiler, Pageable pageable) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN") || a.getAuthority().equals("MANAGER"));

        boolean isDefaultRequest = !isAdmin && isFilterEmpty(voucherFiler);

        String cacheKey = String.format("vouchers_p%d_s%d",
                pageable.getPageNumber(), pageable.getPageSize());

        if (isDefaultRequest) {
            VoucherPaginatedResponse cached = voucherPageStorage.get(cacheKey);
            if (cached != null) {
                return cached;
            }
        }

        Specification<Voucher> spec = VoucherSpecifications.withFilters(voucherFiler);

        Page<VoucherDTO> dtoPage = voucherRepository.findAll(spec, pageable).map(voucherMapper::toVoucherDTO);
        PaginatedResponse<VoucherDTO> paginatedResponse = PaginationMapper.toPaginatedResponse(dtoPage);

        if (isDefaultRequest) {
            voucherPageStorage.store(cacheKey, (VoucherPaginatedResponse) paginatedResponse);
        }

        return paginatedResponse;
    }

    private boolean isFilterEmpty(VoucherFiler filter) {
        return filter.getTours() == null &&
                filter.getMinPrice() == null &&
                filter.getMaxPrice() == null &&
                filter.getHotels() == null &&
                filter.getTransfers() == null;
    }
}
