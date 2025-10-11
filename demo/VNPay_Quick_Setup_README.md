# VNPay Integration - Quick Setup Guide
*Đơn giản hơn PayOS rất nhiều! 🚀*

## Tại Sao Chọn VNPay?

### ✅ **Ưu Điểm Vượt Trội**
- **Setup chỉ 30 phút** (PayOS cần 2-3 giờ)
- **Sandbox miễn phí** - không cần đăng ký merchant thật
- **Test cards có sẵn** - không cần thẻ thật
- **Documentation tiếng Việt** - dễ hiểu
- **Chỉ 2 tables** database (PayOS cần 4 tables)
- **Không cần webhook** - dùng return URL đơn giản

## Quick Start (30 phút)

### 1. Database Setup (5 phút)
```sql
-- Chạy file SQL để tạo tables
USE YourDatabase;
-- Execute: create_vnpay_database_schema.sql
```

### 2. Test Payment (10 phút)
```bash
# Start application
mvn spring-boot:run

# Test create payment
curl -X POST "http://localhost:8080/vnpay/create" \
  -d "userID=1" \
  -d "servicePackID=1" \
  -d "amount=299000" \
  -d "orderInfo=Test Premium Package"

# Response sẽ có payment URL
{
  "success": true,
  "message": "VNPay payment URL created successfully",
  "data": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Version=2.1.0&..."
}
```

### 3. Test với Thẻ Giả (5 phút)
1. Click vào payment URL
2. Chọn NCB Bank
3. Nhập thẻ test: `9704198526191432198`
4. Tên chủ thẻ: `NGUYEN VAN A`
5. Ngày hết hạn: `07/15`
6. OTP: `123456`
7. Confirm payment

### 4. Verify Success (5 phút)
```bash
# Check payment status
curl "http://localhost:8080/vnpay/status/VNP20251010103000123"

# Response
{
  "success": true,
  "data": {
    "vnp_TxnRef": "VNP20251010103000123",
    "status": "SUCCESS",
    "vnp_ResponseCode": "00"
  }
}
```

## API Endpoints (Chỉ 4 cái!)

### 1. 🔥 **Create Payment** (Endpoint chính)
```
POST /vnpay/create
Parameters:
- userID: Integer (required)
- servicePackID: Integer (required) 
- amount: Double (required, VND)
- orderInfo: String (required)

Response: VNPay payment URL
```

### 2. 🔄 **Handle Return** (Tự động)
```
GET /vnpay/return
- VNPay sẽ redirect user về đây sau khi thanh toán
- Tự động update database và redirect to frontend
```

### 3. 📊 **Check Status**
```
GET /vnpay/status/{txnRef}
Response: Payment status và details
```

### 4. 🧪 **Test Connection**
```
GET /vnpay/test
Response: VNPay configuration info
```

## Test Cards (Sẵn Có)

### NCB Bank
```
Card: 9704198526191432198
Name: NGUYEN VAN A
Expiry: 07/15
OTP: 123456
```

### VietcomBank
```
Card: 9704061619906205607
Name: NGUYEN VAN A  
Expiry: 09/07
OTP: 123456
```

### Techcombank
```
Card: 9704662370000103287
Name: NGUYEN VAN A
Expiry: 03/07
OTP: 123456
```

## Response Codes

### Success ✅
- `00`: Giao dịch thành công
- `07`: Trừ tiền thành công (nghi ngờ gian lận)

### Common Errors ❌
- `09`: Thẻ chưa đăng ký dịch vụ
- `10`: Thẻ/Tài khoản không đúng
- `11`: Thẻ hết hạn
- `12`: Thẻ bị khóa
- `24`: User hủy giao dịch
- `75`: Ngân hàng bảo trì

## Database Schema (Siêu Đơn Giản)

### Table 1: `vnpay_payments`
```sql
-- Main payment tracking
- paymentID (PK)
- userID, servicePackID (FK)
- vnp_TxnRef (unique)
- vnp_Amount (VND * 100)
- vnp_ResponseCode, vnp_TransactionStatus
- status (PENDING/SUCCESS/FAILED)
```

### Table 2: `user_subscriptions`
```sql
-- Active subscriptions
- subscriptionID (PK)
- userID, servicePackID, paymentID (FK)
- startDate, endDate
- isActive
```

**Chỉ 2 tables vs PayOS 4 tables!** 🎯

## Frontend Integration

### React Example
```javascript
// Create payment
const createPayment = async () => {
  const response = await fetch('/vnpay/create', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: 'userID=1&servicePackID=1&amount=299000&orderInfo=Premium Package'
  });
  
  const result = await response.json();
  if (result.success) {
    // Redirect to VNPay
    window.location.href = result.data;
  }
};

// Check payment status
const checkPayment = async (txnRef) => {
  const response = await fetch(`/vnpay/status/${txnRef}`);
  const result = await response.json();
  return result.data.status;
};
```

### Success/Failure Pages
```javascript
// pages/payment/success.js
const PaymentSuccess = () => {
  const txnRef = new URLSearchParams(window.location.search).get('txnRef');
  return <div>Payment {txnRef} successful! 🎉</div>;
};

// pages/payment/failure.js  
const PaymentFailure = () => {
  const error = new URLSearchParams(window.location.search).get('error');
  return <div>Payment failed. Error: {error} ❌</div>;
};
```

## Production Setup

### 1. Get Real VNPay Account
- Đăng ký tại: https://vnpay.vn
- Cung cấp giấy tờ doanh nghiệp
- Nhận TMN_CODE và HASH_SECRET thật

### 2. Update Configuration
```java
// Production config
public static final String VNP_PAY_URL = "https://pay.vnpay.vn/vpcpay.html";
public static final String VNP_TMN_CODE = "YOUR_REAL_TMN_CODE";
public static final String VNP_HASH_SECRET = "YOUR_REAL_SECRET";
```

### 3. SSL Certificate
- VNPay yêu cầu HTTPS cho production
- Update return URL thành HTTPS

## Troubleshooting

### Common Issues
1. **Payment URL không work**: Check TMN_CODE và SECRET
2. **Signature verification failed**: Check hash algorithm
3. **Return URL không được call**: Check URL format
4. **Test card bị reject**: Dùng đúng test cards list

### Debug Tips
```bash
# Check application logs
tail -f logs/application.log

# Test VNPay connection
curl http://localhost:8080/vnpay/test

# Check database
SELECT * FROM vnpay_payments ORDER BY createdAt DESC LIMIT 5;
```

## Performance Tips

### Database Optimization
```sql
-- Add indexes for better performance
CREATE INDEX idx_vnpay_payments_user ON vnpay_payments(userID);
CREATE INDEX idx_vnpay_payments_status ON vnpay_payments(status);
CREATE INDEX idx_subscriptions_active ON user_subscriptions(isActive);
```

### Caching Strategy
- Cache active subscriptions
- Cache payment status for 5 minutes
- Use Redis for session management

---

## Summary: VNPay vs PayOS

| Feature | VNPay | PayOS |
|---------|--------|--------|
| **Setup Time** | 30 minutes ⚡ | 2-3 hours 🐌 |
| **Database** | 2 tables 💚 | 4 tables 😰 |
| **Test Environment** | Free sandbox 🆓 | Need real merchant 💳 |
| **Documentation** | Vietnamese 🇻🇳 | English 🇺🇸 |
| **Learning Curve** | Easy 😊 | Medium 😐 |
| **Webhook** | Optional ✨ | Required 🔧 |

**Kết luận**: VNPay thắng áp đảo cho project học tập! 🏆

**Next Steps**: 
1. Run database script ✅
2. Test với sandbox ✅  
3. Integrate với frontend ✅
4. Deploy và enjoy! 🚀