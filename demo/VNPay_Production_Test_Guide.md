# VNPay Production Test Guide
*Test với ngân hàng thật - October 10, 2025*

## 🏦 **Test Với App Banking Thật**

### **Step 1: Sử dụng QR Code hiện tại**
QR code từ API `/vnpay/create` có thể được test trực tiếp với app ngân hàng thật!

### **Step 2: Download App Banking**
#### **VietcomBank (VCB Digibank)**
- Download: [Google Play](https://play.google.com/store/apps/details?id=com.VCB)
- Tính năng: **"Quét QR VNPay"**
- Không cần tài khoản để test sandbox

#### **Techcombank (TCB Mobile)**  
- Download: [Google Play](https://play.google.com/store/apps/details?id=com.techcombank.bb.app)
- Tính năng: **"Thanh toán QR"**
- Test mode có sẵn

#### **VietinBank (iPay Mobile)**
- Download: [Google Play](https://play.google.com/store/apps/details?id=com.vietinbank.ipay)
- Tính năng: **"VNPay QR"**

### **Step 3: Test Flow**
```
1. Mở app ngân hàng
2. Tìm "Quét QR" hoặc "VNPay"
3. Quét QR code từ API response
4. App sẽ hiển thị thông tin thanh toán
5. Chọn "Demo/Test Payment"
6. Hoàn tất test flow
```

## 📱 **VNPay Official App Test**

### **VNPay App Download**
- **Android**: https://play.google.com/store/apps/details?id=com.vnpay.vnpayapp
- **iOS**: https://apps.apple.com/vn/app/vnpay/id1434631317

### **VNPay App Test Steps**
1. **Download VNPay app** (miễn phí)
2. **Đăng ký tài khoản test** (không cần CCCD thật)
3. **Quét QR code** từ API
4. **Test payment flow** với tài khoản demo

## 🔄 **Real Bank Card Test (Sandbox)**

### **VNPay Sandbox Bank Cards** (Safe to use)
```
# NCB Bank Test Card (An toàn 100%)
Card Number: 9704198526191432198
Cardholder: NGUYEN VAN A
Issue Date: 07/15
OTP: 123456
CVV: 123

# VietcomBank Test Card
Card Number: 9704061619906205607
Cardholder: NGUYEN VAN A
Issue Date: 09/07
OTP: 123456

# Techcombank Test Card  
Card Number: 9704662370000103287
Cardholder: NGUYEN VAN A
Expiry: 03/07
OTP: 123456
```

**⚠️ LưU Ý**: Đây là test cards, không phải thẻ thật, an toàn 100%

## 🔐 **Production Environment Setup**

### **Để test với tiền thật (Cẩn thận!)**

#### **Step 1: Đăng ký VNPay Merchant**
1. Truy cập: https://vnpay.vn/dang-ky-doanh-nghiep
2. Cung cấp: Giấy phép kinh doanh, CCCD
3. Chờ duyệt: 2-5 ngày làm việc
4. Nhận: TMN_CODE và HASH_SECRET thật

#### **Step 2: Update Configuration**
```java
// Thay đổi VNPayConfig.java cho production
public static final String VNP_PAY_URL = "https://pay.vnpay.vn/vpcpay.html";
public static final String VNP_TMN_CODE = "YOUR_REAL_TMN_CODE"; // Từ VNPay
public static final String VNP_HASH_SECRET = "YOUR_REAL_SECRET"; // Từ VNPay
public static final String VNP_RETURN_URL = "https://yourdomain.com/vnpay/return";
```

#### **Step 3: Test với số tiền nhỏ**
```bash
# Test với 1,000 VND
curl -X POST "http://localhost:8080/vnpay/create" \
  -d "userID=1&servicePackID=1&amount=1000&orderInfo=Test 1k VND"
```

## 🎯 **Recommended Test Flow**

### **Phase 1: Sandbox Test (Hiện tại)**
✅ **QR Code generation** - DONE  
✅ **API integration** - DONE  
🔄 **App banking test** - Làm ngay

### **Phase 2: VNPay App Test**
1. Download VNPay app
2. Đăng ký tài khoản demo
3. Test QR payment flow
4. Verify return URL handling

### **Phase 3: Production Test (Tùy chọn)**
1. Đăng ký VNPay merchant
2. Update production config
3. Test với số tiền nhỏ (1,000 - 5,000 VND)
4. Full integration test

## 📞 **VNPay Support**

### **Technical Support**
- **Hotline**: 1900 555 577
- **Email**: support@vnpay.vn
- **Zalo**: @vnpayofficial

### **Developer Resources**
- **Documentation**: https://vnpay.vn/devcenter
- **Sandbox**: https://sandbox.vnpayment.vn
- **Test Guide**: https://vnpay.vn/devcenter/vi/web

## ⚡ **Quick Mobile App Test Steps**

### **VietcomBank App Test** (Khuyến nghị)
```
1. Download VCB Digibank
2. Mở app → "Chuyển tiền" → "Quét QR"
3. Quét QR code từ API response
4. Thấy màn hình VNPay payment
5. Chọn "Demo Payment" hoặc back về app
6. Check console log xem có receive callback không
```

### **VNPay App Test**
```
1. Download VNPay app
2. Đăng ký với số điện thoại
3. "Quét mã" → Quét QR từ API
4. Thấy payment details
5. Test payment flow
```

## 🔍 **Debugging Tips**

### **Check QR Code Content**
```bash
# Decode QR để xem URL
# Use online QR decoder: https://zxing.org/w/decode
# Paste QR image và xem URL có đúng format không
```

### **Monitor Return URL**
```bash
# Watch application logs
tail -f logs/application.log

# Check if return URL được call
curl "http://localhost:8080/vnpay/return?vnp_TxnRef=VNP20251010120311123&vnp_ResponseCode=00"
```

---

**🎉 Kết luận**: VNPay sandbox QR code có thể test với app ngân hàng thật mà không cần tiền! Chỉ cần download app và quét QR code thôi!