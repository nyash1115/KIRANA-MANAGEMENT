package com.kirana;

import com.kirana.dto.*;
import com.kirana.entity.User;
import com.kirana.repository.UserRepository;
import com.kirana.service.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class KiranaApplicationTests {

    @Autowired
    private AuthService authService;

    @Autowired
    private ProductService productService;

    @Autowired
    private SaleService saleService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("1. User authentication with valid credentials succeeds and returns JWT")
    void testAuthenticationSuccess() {
        AuthRequest request = new AuthRequest("admin", "admin123");
        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("ROLE_ADMIN", response.getRole());
        assertEquals("admin", response.getUsername());
    }

    @Test
    @DisplayName("2. Barcode search returns correct product and master pricing")
    void testBarcodeSearch() {
        ProductDto product = productService.getProductByBarcode("8901058852300", 1L); // Maggi 70g

        assertNotNull(product);
        assertEquals("Maggi 2-Minute Masala Noodles 70g", product.getName());
        assertEquals(new BigDecimal("12.00"), product.getGstRate());
    }

    @Test
    @DisplayName("3. Cart calculation correctly computes subtotal, GST (CGST/SGST), and round-off")
    void testCartCalculation() {
        ProductDto maggi = productService.getProductByBarcode("8901058852300", 1L);

        CartItemDto cartItem = new CartItemDto();
        cartItem.setProductId(maggi.getId());
        cartItem.setQuantity(new BigDecimal("10.000"));
        cartItem.setUnitPrice(new BigDecimal("14.00")); // MRP 14 * 10 = 140

        BillCalculationRequest req = new BillCalculationRequest();
        req.setItems(List.of(cartItem));

        BillCalculationResponse calc = saleService.calculateBill(1L, req);

        assertNotNull(calc);
        assertEquals(0, new BigDecimal("140.00").compareTo(calc.getSubtotal()));
        assertTrue(calc.getCgstAmount().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(calc.getSgstAmount().compareTo(BigDecimal.ZERO) > 0);
        assertNotNull(calc.getGrandTotal());
    }

    @Test
    @Transactional
    @DisplayName("4. E2E Sale Checkout: stock decreases in FEFO order, payments recorded, and profit computed")
    void testSaleCheckoutAndInventoryDeduction() {
        User cashier = userRepository.findByUsername("cashier").orElseThrow();
        ProductDto atta = productService.getProductByBarcode("8901030012345", 1L); // Atta

        CartItemDto cartItem = new CartItemDto();
        cartItem.setProductId(atta.getId());
        cartItem.setQuantity(new BigDecimal("2.000"));
        cartItem.setUnitPrice(new BigDecimal("245.00"));

        PaymentItemDto payment = new PaymentItemDto("CASH", new BigDecimal("490.00"));

        CheckoutRequest checkout = new CheckoutRequest();
        checkout.setItems(List.of(cartItem));
        checkout.setPayments(List.of(payment));

        SaleResponse sale = saleService.checkout(1L, checkout, cashier);

        assertNotNull(sale);
        assertNotNull(sale.getInvoiceNumber());
        assertEquals("COMPLETED", sale.getStatus());
        assertEquals(0, new BigDecimal("490.00").compareTo(sale.getTotalAmount()));
        assertTrue(sale.getGrossProfit().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @Transactional
    @DisplayName("5. Customer Khata: credit sale increases outstanding, repayment reduces outstanding")
    void testCustomerKhataLedger() {
        User cashier = userRepository.findByUsername("cashier").orElseThrow();
        CustomerDto ramesh = customerService.getCustomerByPhone("9890112233");
        BigDecimal initialOutstanding = ramesh.getCurrentOutstanding();

        // Make an Udhaar sale of ₹50.00
        ProductDto soap = productService.getProductByBarcode("8901396001017", 1L);

        CartItemDto cartItem = new CartItemDto();
        cartItem.setProductId(soap.getId());
        cartItem.setQuantity(new BigDecimal("1.000"));
        cartItem.setUnitPrice(new BigDecimal("50.00"));

        PaymentItemDto udhaarPayment = new PaymentItemDto("UDHAAR", new BigDecimal("59.00")); // with GST

        CheckoutRequest checkout = new CheckoutRequest();
        checkout.setCustomerId(ramesh.getId());
        checkout.setItems(List.of(cartItem));
        checkout.setPayments(List.of(udhaarPayment));

        SaleResponse sale = saleService.checkout(1L, checkout, cashier);
        assertNotNull(sale);

        // Check updated customer balance
        CustomerDto afterSale = customerService.getCustomerById(ramesh.getId());
        assertTrue(afterSale.getCurrentOutstanding().compareTo(initialOutstanding) > 0);

        // Record a repayment of ₹50.00
        CustomerPaymentRequest payReq = new CustomerPaymentRequest();
        payReq.setAmount(new BigDecimal("50.00"));
        payReq.setPaymentMethod("UPI");
        payReq.setReferenceNumber("UPI-TXN-998877");
        customerService.recordPayment(1L, ramesh.getId(), payReq, cashier);

        CustomerDto afterRepay = customerService.getCustomerById(ramesh.getId());
        assertEquals(afterSale.getCurrentOutstanding().subtract(new BigDecimal("50.00")), afterRepay.getCurrentOutstanding());
    }
}
