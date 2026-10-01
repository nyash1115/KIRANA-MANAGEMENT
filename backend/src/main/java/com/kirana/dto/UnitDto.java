package com.kirana.dto;

public class UnitDto {
    private Long id;
    private String name;
    private String code;
    private boolean allowDecimals;

    public UnitDto() {}

    public UnitDto(Long id, String name, String code, boolean allowDecimals) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.allowDecimals = allowDecimals;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public boolean isAllowDecimals() { return allowDecimals; }
    public void setAllowDecimals(boolean allowDecimals) { this.allowDecimals = allowDecimals; }
}
