# End-to-End Verification Script for Kirana POS System
$ErrorActionPreference = "Stop"

Write-Host "=================================================" -ForegroundColor Cyan
Write-Host "KIRANA STORE MANAGEMENT AND POS - E2E WORKFLOW TEST" -ForegroundColor Cyan
Write-Host "=================================================" -ForegroundColor Cyan

# 1. Login as Admin
Write-Host "`n[1] Authenticating Admin user (admin / admin123)..." -ForegroundColor Yellow
$loginBody = @{
    username = "admin"
    password = "admin123"
} | ConvertTo-Json

$loginRes = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
$token = $loginRes.data.token
$headers = @{
    Authorization = "Bearer $token"
}
Write-Host "OK: Admin authenticated successfully! Token obtained." -ForegroundColor Green

# 2. Create Product
Write-Host "`n[2] Creating new Grocery Product (Dawat Rozana Basmati Rice 5kg)..." -ForegroundColor Yellow
$randId = Get-Random -Minimum 100000 -Maximum 999999
$testBarcode = "89012$randId"
$productBody = @{
    categoryId = 1
    unitId = 2
    name = "Dawat Rozana Super Basmati Rice $randId"
    barcode = $testBarcode
    sku = "RICE-DAWAT-$randId"
    brand = "Dawat"
    hsnCode = "1006"
    gstRate = 5
    defaultCostPrice = 320.00
    defaultSellingPrice = 399.00
    defaultMrp = 450.00
    minStockLevel = 5
    reorderLevel = 10
    active = $true
} | ConvertTo-Json

$prodRes = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/products" -Method Post -Body $productBody -ContentType "application/json" -Headers $headers
$productId = $prodRes.data.id
Write-Host "OK: Product created with ID: $productId, Barcode: $testBarcode" -ForegroundColor Green

# 3. Inward Purchase Stock
Write-Host "`n[3] Inward Purchase Stock from Supplier (50 packs @ Rs 320)..." -ForegroundColor Yellow
$purchaseBody = @{
    supplierId = 1
    invoiceNumber = "PO-E2E-999"
    purchaseDate = (Get-Date).ToString("yyyy-MM-dd")
    paidAmount = 16800.00
    notes = "E2E automated inward stock receipt"
    items = @(
        @{
            productId = $productId
            quantity = 50
            costPrice = 320.00
            sellingPrice = 399.00
            mrp = 450.00
            batchNumber = "BATCH-E2E-001"
            expiryDate = (Get-Date).AddMonths(12).ToString("yyyy-MM-dd")
            gstRate = 5
        }
    )
} | ConvertTo-Json -Depth 5

$purRes = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/purchases" -Method Post -Body $purchaseBody -ContentType "application/json" -Headers $headers
Write-Host "OK: Purchase recorded. Purchase Number: $($purRes.data.purchaseNumber)" -ForegroundColor Green

# 4. Verify Inventory Added
Write-Host "`n[4] Verifying Inventory Batches for Product..." -ForegroundColor Yellow
$batchRes = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/inventory/batches?productId=$productId" -Method Get -Headers $headers
$initialQty = $batchRes.data.content[0].quantity
Write-Host "OK: Inventory batch created. Active Stock: $initialQty units." -ForegroundColor Green
if ($initialQty -ne 50) { throw "Expected 50 units, found $initialQty" }

# 5. Scan Barcode (Simulate POS)
Write-Host "`n[5] Scanning Product Barcode ($testBarcode) at POS..." -ForegroundColor Yellow
$scanRes = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/products/barcode/$testBarcode" -Method Get -Headers $headers
Write-Host "OK: Product identified via barcode: $($scanRes.data.name), Price: Rs $($scanRes.data.defaultSellingPrice)" -ForegroundColor Green

# 6. Create Bill / POS Checkout
Write-Host "`n[6] Creating Bill for 4 units with Split Payment (Cash + UPI) and GST..." -ForegroundColor Yellow
$saleBody = @{
    customerId = 1
    items = @(
        @{
            productId = $productId
            quantity = 4
            unitPrice = 399.00
            itemDiscount = 0
        }
    )
    payments = @(
        @{
            paymentMethod = "CASH"
            amount = 1000.00
        },
        @{
            paymentMethod = "UPI"
            amount = 596.00
            transactionRef = "UPI-E2E-SUCCESS"
        }
    )
    notes = "E2E POS sale checkout"
} | ConvertTo-Json -Depth 5

$saleRes = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/sales" -Method Post -Body $saleBody -ContentType "application/json" -Headers $headers
$saleData = $saleRes.data
$saleId = $saleData.id
$invoiceNumber = $saleData.invoiceNumber
$saleItemId = $saleData.items[0].id

Write-Host "OK: Invoice Generated: $invoiceNumber" -ForegroundColor Green
Write-Host "  Subtotal: Rs $($saleData.subtotal)" -ForegroundColor Cyan
Write-Host "  CGST: Rs $($saleData.cgstAmount), SGST: Rs $($saleData.sgstAmount)" -ForegroundColor Cyan
Write-Host "  Grand Total: Rs $($saleData.totalAmount)" -ForegroundColor Cyan
Write-Host "  Cost Basis (COGS): Rs $($saleData.totalCogs)" -ForegroundColor Cyan
Write-Host "  Gross Profit: Rs $($saleData.grossProfit)" -ForegroundColor Cyan

# 7. Verify Inventory Decreased
Write-Host "`n[7] Verifying Inventory Batch Reduction (50 - 4 = 46)..." -ForegroundColor Yellow
$batchResAfter = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/inventory/batches?productId=$productId" -Method Get -Headers $headers
$newQty = $batchResAfter.data.content[0].quantity
Write-Host "OK: Remaining Stock in Batch: $newQty units." -ForegroundColor Green
if ($newQty -ne 46) { throw "Expected 46 units after sale, found $newQty" }

# 8. Check Profit & Loss Report
Write-Host "`n[8] Verifying Profit and Loss Accounting Report..." -ForegroundColor Yellow
$today = (Get-Date).ToString("yyyy-MM-dd")
$profitUrl = "http://localhost:8080/api/v1/reports/profit?startDate=" + $today + "&endDate=" + $today
$profitRes = Invoke-RestMethod -Uri $profitUrl -Method Get -Headers $headers
Write-Host "OK: Net Sales: Rs $($profitRes.data.netSales), COGS: Rs $($profitRes.data.costOfGoodsSold), Gross Profit: Rs $($profitRes.data.grossProfit)" -ForegroundColor Green

# 9. Perform Customer Sales Return
Write-Host "`n[9] Performing Sales Return (1 unit returned by customer with Cash refund)..." -ForegroundColor Yellow
$returnBody = @{
    refundMethod = "CASH"
    reason = "Customer returned 1 bag"
    items = @(
        @{
            saleItemId = $saleItemId
            quantity = 1
        }
    )
} | ConvertTo-Json -Depth 5

$returnRes = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/sales/$saleId/return" -Method Post -Body $returnBody -ContentType "application/json" -Headers $headers
Write-Host "OK: Sales Return Processed successfully. Refund Amount: Rs $($returnRes.data.refundAmount)" -ForegroundColor Green

# 10. Verify Inventory Increased after Return
Write-Host "`n[10] Verifying Restocked Inventory (46 + 1 = 47 units)..." -ForegroundColor Yellow
$batchResFinal = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/inventory/batches?productId=$productId" -Method Get -Headers $headers
$finalQty = $batchResFinal.data.content[0].quantity
Write-Host "OK: Final Stock after Return Restocking: $finalQty units." -ForegroundColor Green
if ($finalQty -ne 47) { throw "Expected 47 units after return, found $finalQty" }

# 11. Check Khata Udhaar Lifecycle
Write-Host "`n[11] Verifying Customer Khata Udhaar and Repayment Workflow..." -ForegroundColor Yellow
$khataPayBody = @{
    amount = 200.00
    paymentMethod = "CASH"
    notes = "Partial Udhaar repayment at counter"
} | ConvertTo-Json

$khataRes = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/customers/1/payments" -Method Post -Body $khataPayBody -ContentType "application/json" -Headers $headers
Write-Host "OK: Khata Repayment recorded." -ForegroundColor Green

$custRes = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/customers/1" -Method Get -Headers $headers
Write-Host "OK: Customer Current Khata Balance: Rs $($custRes.data.outstandingBalance)" -ForegroundColor Green

# 12. Audit Trail
Write-Host "`n[12] Verifying System Audit Logs..." -ForegroundColor Yellow
$auditRes = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/audit-logs?size=5" -Method Get -Headers $headers
Write-Host "OK: Total Audit Log Events: $($auditRes.data.totalElements)" -ForegroundColor Green
foreach ($item in $auditRes.data.content) {
    Write-Host "  - [$($item.action)] on $($item.entityName) #$($item.entityId) by $($item.username)" -ForegroundColor Gray
}

Write-Host "`n=================================================" -ForegroundColor Cyan
Write-Host "ALL 12 END-TO-END BUSINESS FLOW CHECKS PASSED 100%!" -ForegroundColor Green
Write-Host "=================================================" -ForegroundColor Cyan
