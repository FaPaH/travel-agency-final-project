package com.epam.finaltask.service;

import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.model.HotelType;
import com.epam.finaltask.model.TourType;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VoucherServiceImpl implements VoucherService {

    @Override
    public VoucherDTO create(VoucherDTO voucherDTO) {
        return null;
    }

    @Override
    public VoucherDTO order(String id, String userId) {
        return null;
    }

    @Override
    public VoucherDTO update(String id, VoucherDTO voucherDTO) {
        return null;
    }

    @Override
    public void delete(String voucherId) {

    }

    @Override
    public VoucherDTO changeHotStatus(String id, VoucherDTO voucherDTO) {
        return null;
    }

    @Override
    public List<VoucherDTO> findAllByUserId(String userId) {
        return List.of();
    }

    @Override
    public List<VoucherDTO> findAllByTourType(TourType tourType) {
        return List.of();
    }

    @Override
    public List<VoucherDTO> findAllByTransferType(String transferType) {
        return List.of();
    }

    @Override
    public List<VoucherDTO> findAllByPrice(Double price) {
        return List.of();
    }

    @Override
    public List<VoucherDTO> findAllByHotelType(HotelType hotelType) {
        return List.of();
    }

    @Override
    public List<VoucherDTO> findAll() {
        return List.of();
    }
}
