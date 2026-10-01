package com.kirana.service;

import com.kirana.dto.CategoryDto;
import com.kirana.dto.UnitDto;
import com.kirana.entity.Business;
import com.kirana.entity.Category;
import com.kirana.entity.Unit;
import com.kirana.exception.ResourceNotFoundException;
import com.kirana.repository.BusinessRepository;
import com.kirana.repository.CategoryRepository;
import com.kirana.repository.UnitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.Instant;
import java.util.List;
import java.util.Locale;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UnitRepository unitRepository;
    private final BusinessRepository businessRepository;

    public CategoryService(CategoryRepository categoryRepository, UnitRepository unitRepository, BusinessRepository businessRepository) {
        this.categoryRepository = categoryRepository;
        this.unitRepository = unitRepository;
        this.businessRepository = businessRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findByActiveTrueOrderByNameAsc().stream().map(this::mapToDto).toList();
    }

    @Transactional
    public CategoryDto createCategory(CategoryDto dto, Long businessId) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found with ID: " + businessId));

        Category cat = new Category();
        cat.setBusiness(business);
        cat.setName(dto.getName());
        cat.setSlug(toSlug(dto.getName()));
        cat.setDescription(dto.getDescription());
        cat.setActive(true);
        return mapToDto(categoryRepository.save(cat));
    }

    @Transactional
    public CategoryDto updateCategory(Long id, CategoryDto dto) {
        Category cat = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
        cat.setName(dto.getName());
        cat.setSlug(toSlug(dto.getName()));
        cat.setDescription(dto.getDescription());
        cat.setActive(dto.isActive());
        cat.setUpdatedAt(Instant.now());
        return mapToDto(categoryRepository.save(cat));
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category cat = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
        cat.setActive(false);
        cat.setUpdatedAt(Instant.now());
        categoryRepository.save(cat);
    }

    @Transactional(readOnly = true)
    public List<UnitDto> getAllUnits() {
        return unitRepository.findAll().stream()
                .map(u -> new UnitDto(u.getId(), u.getName(), u.getCode(), u.isAllowDecimals())).toList();
    }

    private CategoryDto mapToDto(Category c) {
        CategoryDto dto = new CategoryDto();
        dto.setId(c.getId());
        dto.setName(c.getName());
        dto.setSlug(c.getSlug());
        dto.setDescription(c.getDescription());
        dto.setActive(c.isActive());
        return dto;
    }

    private String toSlug(String input) {
        return Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("[^\\w\\s-]", "")
                .trim().toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", "-");
    }
}
