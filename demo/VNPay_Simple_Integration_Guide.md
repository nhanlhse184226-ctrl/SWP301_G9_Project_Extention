# VNPay Integration Guide - Đơn Giản Hơn PayOS
*Created: October 10, 2025*

## Tại Sao VNPay Đơn Giản Hơn PayOS?

### ✅ **VNPay Advantages**
- **Sandbox miễn phí**: Không cần đăng ký merchant thật
- **Documentation rõ ràng**: Tiếng Việt, có sample code
- **Ít field required**: Chỉ cần 5-6 parameters cơ bản
- **No webhook needed**: Dùng return URL đơn giản
- **Test cards sẵn có**: Không cần card thật để test

### ❌ **PayOS Complexity**
- Cần đăng ký merchant account
- Webhook signature phức tạp
- Nhiều field required hơn
- Documentation tiếng Anh
- Cần verify business info

## VNPay Database Schema (Đơn Giản)

### 2 Tables Chính Thôi:

1. **`vnpay_payments`** - Track payments
2. **`user_subscriptions`** - Track subscriptions

### So Sánh với PayOS:
- **PayOS**: 4 tables (payments, logs, webhooks, subscriptions)
- **VNPay**: 2 tables (payments, subscriptions)
- **Complexity**: VNPay đơn giản hơn 50%

## VNPay Test Configuration

### Sandbox Info (Miễn Phí)
```properties
# VNPay Sandbox Config
vnpay.api.url=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
vnpay.return.url=http://localhost:8080/vnpay/return
vnpay.tmn.code=DEMOV210  # Demo terminal code
vnpay.hash.secret=RAOEXHYVSDDIIENYWSLDIIZTANXUXZFJ  # Demo secret key
vnpay.version=2.1.0
vnpay.command=pay
vnpay.currency.code=VND
```

### Test Cards (Sẵn Có)
```
# NCB Bank Test Card
Card Number: 9704198526191432198
Cardholder: NGUYEN VAN A
Issue Date: 07/15
OTP: 123456

# VietcomBank Test Card  
Card Number: 9704061619906205607
Cardholder: NGUYEN VAN A
Issue Date: 09/07
OTP: 123456
```

## VNPay Integration Flow (Đơn Giản)

### 1. Payment Creation (3 Steps)
```java
// Step 1: Create payment record
String txnRef = vnpayService.createPayment(userID, servicePackID, amount);

// Step 2: Build VNPay URL
String vnpayUrl = vnpayService.buildPaymentUrl(txnRef, amount, orderInfo);

// Step 3: Redirect user to VNPay
return "redirect:" + vnpayUrl;
```

### 2. Payment Return (2 Steps)
```java
// Step 1: Verify VNPay response
boolean isValid = vnpayService.verifyPayment(request);

// Step 2: Update payment status
if (isValid) {
    vnpayService.updatePaymentSuccess(txnRef, responseData);
}
```

## Sample VNPay URLs

### Payment URL Structure
```
https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?
vnp_Version=2.1.0
&vnp_Command=pay
&vnp_TmnCode=DEMOV210
&vnp_Amount=29900000        // 299,000 VND * 100
&vnp_CreateDate=20251010103000
&vnp_CurrCode=VND
&vnp_IpAddr=192.168.1.1
&vnp_Locale=vn
&vnp_OrderInfo=Thanh%20toan%20goi%20Premium
&vnp_OrderType=billpayment
&vnp_ReturnUrl=http%3A%2F%2Flocalhost%3A8080%2Fvnpay%2Freturn
&vnp_TxnRef=VNP20251010103000123
&vnp_SecureHash=abc123...
```

### Return URL Response
```
http://localhost:8080/vnpay/return?
vnp_Amount=29900000
&vnp_BankCode=NCB
&vnp_BankTranNo=VNP14567890123
&vnp_CardType=ATM
&vnp_OrderInfo=Thanh%20toan%20goi%20Premium
&vnp_PayDate=20251010103500
&vnp_ResponseCode=00        // 00 = Success
&vnp_TmnCode=DEMOV210
&vnp_TransactionNo=14567890123
&vnp_TransactionStatus=00   // 00 = Success
&vnp_TxnRef=VNP20251010103000123
&vnp_SecureHash=def456...
```

## VNPay Response Codes (Đơn Giản)

### Success Codes
- `00`: Giao dịch thành công
- `07`: Trừ tiền thành công (Giao dịch bị nghi ngờ)

### Error Codes (Common)
- `09`: Thẻ/Tài khoản chưa đăng ký dịch vụ
- `10`: Thẻ/Tài khoản không đúng
- `11`: Thẻ/Tài khoản hết hạn
- `12`: Thẻ/Tài khoản bị khóa
- `24`: Hủy giao dịch
- `75`: Ngân hàng bảo trì

## Database Queries (Đơn Giản)

### User Payment History
```sql
SELECT 
    p.vnp_TxnRef,
    p.vnp_OrderInfo,
    p.vnp_Amount/100.0 as amount_vnd,
    p.status,
    p.vnp_BankCode,
    p.createdAt
FROM vnpay_payments p
WHERE p.userID = ?
ORDER BY p.createdAt DESC;
```

### Check Active Subscription
```sql
SELECT COUNT(*) as hasActiveSubscription
FROM user_subscriptions 
WHERE userID = ? 
  AND isActive = 1 
  AND endDate > GETDATE();
```

### Payment Analytics
```sql
SELECT 
    DATE_FORMAT(createdAt, '%Y-%m') as month,
    COUNT(*) as total_payments,
    SUM(CASE WHEN status = 'SUCCESS' THEN vnp_Amount/100.0 ELSE 0 END) as revenue
FROM vnpay_payments
WHERE createdAt >= DATEADD(MONTH, -6, GETDATE())
GROUP BY DATE_FORMAT(createdAt, '%Y-%m')
ORDER BY month;
```

## Implementation Steps

### 1. Database Setup (5 phút)
```sql
-- Chạy file create_vnpay_database_schema.sql
USE YourDatabase;
-- Copy paste và run script
```

### 2. Java Configuration (10 phút)
```java
// VNPayConfig.java
@Configuration
public class VNPayConfig {
    public static final String VNP_TMN_CODE = "DEMOV210";
    public static final String VNP_HASH_SECRET = "RAOEXHYVSDDIIENYWSLDIIZTANXUXZFJ";
    public static final String VNP_PAY_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    public static final String VNP_RETURN_URL = "http://localhost:8080/vnpay/return";
}
```

### 3. Service Implementation (20 phút)
```java
// VNPayService.java - Đơn giản, chỉ cần 3 methods:
// 1. createPayment()
// 2. buildPaymentUrl() 
// 3. verifyAndUpdatePayment()
```

### 4. Controller (10 phút)
```java
// VNPayController.java - Chỉ cần 2 endpoints:
// 1. POST /vnpay/create - Tạo payment
// 2. GET /vnpay/return - Xử lý return
```

## Testing Process

### 1. Local Testing
```bash
# Start application
mvn spring-boot:run

# Test payment creation
curl -X POST localhost:8080/vnpay/create \
  -d "userID=1&servicePackID=1&amount=299000"

# Will return VNPay payment URL
```

### 2. Manual Testing
1. Click payment URL
2. Choose NCB Bank
3. Enter test card: `9704198526191432198`
4. Enter OTP: `123456`
5. Confirm payment
6. Check return URL for success

### 3. Database Verification
```sql
-- Check payment was created
SELECT * FROM vnpay_payments WHERE status = 'SUCCESS';

-- Check subscription was created
SELECT * FROM user_subscriptions WHERE isActive = 1;
```

## Advantages Summary

| Feature | VNPay | PayOS |
|---------|-------|-------|
| **Setup Time** | 30 minutes | 2-3 hours |
| **Database Tables** | 2 tables | 4 tables |
| **Test Cards** | Free, ready-to-use | Need real cards |
| **Documentation** | Vietnamese | English |
| **Webhook Required** | No (use return URL) | Yes (complex) |
| **Merchant Account** | Demo account | Real business |
| **Learning Curve** | Easy | Medium |

**Kết luận**: VNPay đơn giản hơn PayOS rất nhiều, đặc biệt phù hợp cho project học tập và prototype! 🚀