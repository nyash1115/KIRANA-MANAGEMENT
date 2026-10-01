package com.kirana.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "units")
public class Unit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(name = "allow_decimals", nullable = false)
    private boolean allowDecimals = false;

    public Unit() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public boolean isAllowDecimals() { return allowDecimals; }
    public void setAllowDecimals(boolean allowDecimals) { this.allowDecimals = allowDecimals; }
}
