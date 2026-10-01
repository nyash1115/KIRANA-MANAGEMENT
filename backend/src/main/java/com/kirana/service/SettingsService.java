package com.kirana.service;

import com.kirana.dto.StoreSettingsDto;
import com.kirana.entity.Store;
import com.kirana.entity.StoreSettings;
import com.kirana.exception.ResourceNotFoundException;
import com.kirana.repository.StoreRepository;
import com.kirana.repository.StoreSettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class SettingsService {

    private final StoreSettingsRepository storeSettingsRepository;
    private final StoreRepository storeRepository;

    public SettingsService(StoreSettingsRepository storeSettingsRepository, StoreRepository storeRepository) {
        this.storeSettingsRepository = storeSettingsRepository;
        this.storeRepository = storeRepository;
    }

    @Transactional(readOnly = true)
    public StoreSettingsDto getStoreSettings(Long storeId) {
        StoreSettings settings = storeSettingsRepository.findByStoreId(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Settings not found for store ID: " + storeId));
        return mapToDto(settings);
    }

    @Transactional
    public StoreSettingsDto updateStoreSettings(Long storeId, StoreSettingsDto dto) {
        StoreSettings settings = storeSettingsRepository.findByStoreId(storeId)
                .orElseGet(() -> {
                    Store store = storeRepository.findById(storeId)
                            .orElseThrow(() -> new ResourceNotFoundException("Store not found with ID: " + storeId));
                    StoreSettings s = new StoreSettings();
                    s.setStore(store);
                    return s;
                });

        settings.setTaxInclusivePricing(dto.isTaxInclusivePricing());
        settings.setEnableIgst(dto.isEnableIgst());
        settings.setAllowNegativeStock(dto.isAllowNegativeStock());
        if (dto.getDefaultGstRate() != null) settings.setDefaultGstRate(dto.getDefaultGstRate());
        if (dto.getLowStockThresholdDefault() != null) settings.setLowStockThresholdDefault(dto.getLowStockThresholdDefault());
        if (dto.getExpiryAlertDaysDefault() > 0) settings.setExpiryAlertDaysDefault(dto.getExpiryAlertDaysDefault());
        if (dto.getInvoiceFooterMessage() != null) settings.setInvoiceFooterMessage(dto.getInvoiceFooterMessage());
        if (dto.getInvoiceTerms() != null) settings.setInvoiceTerms(dto.getInvoiceTerms());
        if (dto.getThermalPaperWidthMm() > 0) settings.setThermalPaperWidthMm(dto.getThermalPaperWidthMm());
        if (dto.getCurrencySymbol() != null) settings.setCurrencySymbol(dto.getCurrencySymbol());
        if (dto.getCurrencyCode() != null) settings.setCurrencyCode(dto.getCurrencyCode());
        settings.setUpdatedAt(Instant.now());

        // Also update store header info if provided
        Store store = settings.getStore();
        if (dto.getStoreName() != null) store.setName(dto.getStoreName());
        if (dto.getStoreAddress() != null) store.setAddress(dto.getStoreAddress());
        if (dto.getStorePhone() != null) store.setPhone(dto.getStorePhone());
        if (dto.getStoreGstin() != null) store.setGstin(dto.getStoreGstin());
        storeRepository.save(store);

        return mapToDto(storeSettingsRepository.save(settings));
    }

    private StoreSettingsDto mapToDto(StoreSettings s) {
        StoreSettingsDto dto = new StoreSettingsDto();
        dto.setId(s.getId());
        dto.setStoreId(s.getStore().getId());
        dto.setStoreName(s.getStore().getName());
        dto.setStoreAddress(s.getStore().getAddress());
        dto.setStorePhone(s.getStore().getPhone());
        dto.setStoreGstin(s.getStore().getGstin());
        dto.setTaxInclusivePricing(s.isTaxInclusivePricing());
        dto.setEnableIgst(s.isEnableIgst());
        dto.setAllowNegativeStock(s.isAllowNegativeStock());
        dto.setDefaultGstRate(s.getDefaultGstRate());
        dto.setLowStockThresholdDefault(s.getLowStockThresholdDefault());
        dto.setExpiryAlertDaysDefault(s.getExpiryAlertDaysDefault());
        dto.setInvoiceFooterMessage(s.getInvoiceFooterMessage());
        dto.setInvoiceTerms(s.getInvoiceTerms());
        dto.setThermalPaperWidthMm(s.getThermalPaperWidthMm());
        dto.setCurrencySymbol(s.getCurrencySymbol());
        dto.setCurrencyCode(s.getCurrencyCode());
        return dto;
    }
}
