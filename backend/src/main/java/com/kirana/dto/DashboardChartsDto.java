package com.kirana.dto;

import java.math.BigDecimal;
import java.util.List;

public class DashboardChartsDto {

    private List<SalesTrendPoint> salesTrend;
    private List<TopProductPoint> topSellingProducts;
    private List<CategorySalesPoint> categorySales;

    public static class SalesTrendPoint {
        private String date;
        private long bills;
        private BigDecimal sales;
        private BigDecimal profit;

        public SalesTrendPoint() {}
        public SalesTrendPoint(String date, long bills, BigDecimal sales, BigDecimal profit) {
            this.date = date;
            this.bills = bills;
            this.sales = sales;
            this.profit = profit;
        }

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }

        public long getBills() { return bills; }
        public void setBills(long bills) { this.bills = bills; }

        public BigDecimal getSales() { return sales; }
        public void setSales(BigDecimal sales) { this.sales = sales; }

        public BigDecimal getProfit() { return profit; }
        public void setProfit(BigDecimal profit) { this.profit = profit; }
    }

    public static class TopProductPoint {
        private String productName;
        private BigDecimal quantity;
        private BigDecimal revenue;

        public TopProductPoint() {}
        public TopProductPoint(String productName, BigDecimal quantity, BigDecimal revenue) {
            this.productName = productName;
            this.quantity = quantity;
            this.revenue = revenue;
        }

        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }

        public BigDecimal getQuantity() { return quantity; }
        public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

        public BigDecimal getRevenue() { return revenue; }
        public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }
    }

    public static class CategorySalesPoint {
        private String categoryName;
        private BigDecimal revenue;

        public CategorySalesPoint() {}
        public CategorySalesPoint(String categoryName, BigDecimal revenue) {
            this.categoryName = categoryName;
            this.revenue = revenue;
        }

        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

        public BigDecimal getRevenue() { return revenue; }
        public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }
    }

    public DashboardChartsDto() {}

    public List<SalesTrendPoint> getSalesTrend() { return salesTrend; }
    public void setSalesTrend(List<SalesTrendPoint> salesTrend) { this.salesTrend = salesTrend; }

    public List<TopProductPoint> getTopSellingProducts() { return topSellingProducts; }
    public void setTopSellingProducts(List<TopProductPoint> topSellingProducts) { this.topSellingProducts = topSellingProducts; }

    public List<CategorySalesPoint> getCategorySales() { return categorySales; }
    public void setCategorySales(List<CategorySalesPoint> categorySales) { this.categorySales = categorySales; }
}
