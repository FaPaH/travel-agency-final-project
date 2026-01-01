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

    @Override
    public VoucherDTO create(VoucherDTO voucherDTO) {
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

        return voucherMapper.toVoucherDTO(voucherRepository.save(voucher));
    }

    @Override
    public VoucherDTO update(String id, VoucherDTO voucherDTO) {
        if (!voucherRepository.existsById(UUID.fromString(id))) {
            throw new RuntimeException("Voucher not found");
        }

        Voucher voucher = voucherMapper.toVoucher(voucherDTO);

        return voucherMapper.toVoucherDTO(voucherRepository.save(voucher));
    }

    @Override
    public void delete(String voucherId) {
        if (!voucherRepository.existsById(UUID.fromString(voucherId))) {
            throw new IllegalStateException("Voucher not found");
        }

        voucherRepository.deleteById(UUID.fromString(voucherId));
    }

    @Override
    public VoucherDTO changeHotStatus(String id, VoucherDTO voucherDTO) {
        if (!voucherRepository.existsById(UUID.fromString(id))) {
            throw new RuntimeException("Voucher not found");
        }

        voucherDTO.setIsHot(!voucherDTO.getIsHot());

        return voucherMapper.toVoucherDTO(voucherRepository.save(voucherMapper.toVoucher(voucherDTO)));
    }

    @Override
    public PaginatedResponse<VoucherDTO> findAllByUserId(String userId, Pageable pageable) {
        Page<VoucherDTO> dtoPage = voucherRepository.findAllByUserId(UUID.fromString(userId), pageable).map(voucherMapper::toVoucherDTO);

        return PaginationMapper.toPaginatedResponse(dtoPage);
    }

    @Override
    public Page<VoucherDTO> findAllByTourType(TourType tourType, Pageable pageable) {
        return null;
    }

    @Override
    public Page<VoucherDTO> findAllByTransferType(String transferType, Pageable pageable) {
        return null;
    }

    @Override
    public Page<VoucherDTO> findAllByPrice(BigDecimal price, Pageable pageable) {
        return null;
    }

    @Override
    public Page<VoucherDTO> findAllByHotelType(HotelType hotelType, Pageable pageable) {
        return null;
    }

    @Override
    public PaginatedResponse<VoucherDTO> findWithFilers(VoucherFiler voucherFiler, Pageable pageable) {

        //boolean isFirstPage = pageable.getPageNumber() == 0;

        Specification<Voucher> spec = VoucherSpecifications.withFilters(voucherFiler);

        Page<VoucherDTO> dtoPage = voucherRepository.findAll(spec, pageable).map(voucherMapper::toVoucherDTO);

        return PaginationMapper.toPaginatedResponse(dtoPage);
    }

    @Override
    public Page<VoucherDTO> findAll(Pageable pageable) {
        return null;
    }
}
