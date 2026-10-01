package com.kirana.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.Instant;

public class CustomerDto {
    private Long id;

    @NotBlank(message = "Customer name is required")
    private String name;

    @NotBlank(message = "Customer phone number is required")
    private String phone;

    private String email;
    private String address;
    private String gstin;
    private BigDecimal creditLimit;
    private BigDecimal currentOutstanding;
    private String notes;
    private boolean active = true;
    private Instant createdAt;

    public CustomerDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getGstin() { return gstin; }
    public void setGstin(String gstin) { this.gstin = gstin; }

    public BigDecimal getCreditLimit() { return creditLimit; }
    public void setCreditLimit(BigDecimal creditLimit) { this.creditLimit = creditLimit; }

    public BigDecimal getCurrentOutstanding() { return currentOutstanding; }
    public void setCurrentOutstanding(BigDecimal currentOutstanding) { this.currentOutstanding = currentOutstanding; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
