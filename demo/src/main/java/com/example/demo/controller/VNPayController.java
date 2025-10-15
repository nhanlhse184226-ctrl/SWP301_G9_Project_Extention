package com.example.demo.controller;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dao.PaymentDAO;
import com.example.demo.dao.VNPayPaymentDAO;
import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.VNPayPaymentDTO;
import com.example.demo.dto.VNPayPaymentResponseDTO;
import com.example.demo.service.VNPayService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

/**
 * VNPay Payment Controller - Siêu đơn giản!
 * Chỉ cần 2 endpoint: create payment và handle return
 */
@RestController
@RequestMapping("/vnpay")
@Tag(name = "VNPay Payment", description = "VNPay payment integration - Simple & Easy")
@CrossOrigin(origins = "*")
public class VNPayController {
    
    @Autowired
    private VNPayService vnpayService;
    
    private final PaymentDAO paymentDAO = new PaymentDAO();
    
    /**
     * Tạo VNPay payment với QR code - API mới cho frontend
     */
    @PostMapping("/create")
    @Operation(summary = "Create VNPay Payment with QR Code", 
               description = "Tạo VNPay payment và trả về QR code để hiển thị trên frontend")
    public ApiResponse<VNPayPaymentResponseDTO> createPayment(
            @Parameter(description = "User ID") @RequestParam Integer userID,
            @Parameter(description = "Pack ID") @RequestParam Integer packID,
            @Parameter(description = "Amount in VND") @RequestParam Double amount,
            @Parameter(description = "Order description") @RequestParam String orderInfo,
            @Parameter(description = "Total credits/lượt") @RequestParam(required = false) Integer total,
            HttpServletRequest request) {
        
        try {
            // Validate userID exists in database
            if (!paymentDAO.isUserExists(userID)) {
                return ApiResponse.error("UserID " + userID + " không tồn tại trong hệ thống");
            }
            
            // Get client IP
            String ipAddress = getClientIpAddress(request);
            
            // NOTE: stationID và pinID không được lưu vào database nữa (legacy parameters for backward compatibility)
            // Tạo payment với QR code
            VNPayPaymentResponseDTO paymentResponse = vnpayService.createPaymentWithQR(
                userID, packID, amount, orderInfo, ipAddress, total);
            
            return ApiResponse.success("VNPay payment with QR code created successfully", paymentResponse);
            
        } catch (UnsupportedEncodingException e) {
            return ApiResponse.error("Failed to create payment: " + e.getMessage());
        } catch (SQLException e) {
            return ApiResponse.error("Database error: " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("Unexpected error: " + e.getMessage());
        }
    }
    
    /**
     * Tạo VNPay payment URL truyền thống - Cho mobile/redirect
     */
    @PostMapping("/create-url")
    @Operation(summary = "Create VNPay Payment URL", 
               description = "Tạo VNPay payment URL truyền thống để redirect user")
    public ApiResponse<String> createPaymentUrl(
            @Parameter(description = "User ID") @RequestParam Integer userID,
            @Parameter(description = "Pack ID") @RequestParam Integer packID,
            @Parameter(description = "Amount in VND") @RequestParam Double amount,
            @Parameter(description = "Order description") @RequestParam String orderInfo,
            @Parameter(description = "Total credits/lượt") @RequestParam(required = false) Integer total,
            HttpServletRequest request) {
        
        try {
            // Validate userID exists in database
            if (!paymentDAO.isUserExists(userID)) {
                return ApiResponse.error("UserID " + userID + " không tồn tại trong hệ thống");
            }
            
            // NOTE: stationID và pinID không được lưu vào database nữa (legacy parameters for backward compatibility)
            // Tạo payment record với total parameter
            String txnRef = vnpayService.createPayment(userID, packID, amount, orderInfo, total);
            
            // Get client IP
            String ipAddress = getClientIpAddress(request);
            
            // Build VNPay payment URL
            String paymentUrl = vnpayService.buildPaymentUrl(txnRef, amount, orderInfo, ipAddress);
            
            return ApiResponse.success("VNPay payment URL created successfully", paymentUrl);
            
        } catch (UnsupportedEncodingException e) {
            return ApiResponse.error("Failed to create payment URL: " + e.getMessage());
        } catch (SQLException e) {
            return ApiResponse.error("Database error: " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("Unexpected error: " + e.getMessage());
        }
    }
    
    /**
     * Handle VNPay Return URL - Trả về HTML page cho FE redirect
     */
    @GetMapping("/return")
    public ResponseEntity<String> handleVNPayReturn(HttpServletRequest request) {
        
        try {
            System.out.println("=== VNPay Return URL Handler START ===");
            
            // Sử dụng VNPayService verifyPayment method thay vì tự tính signature
            boolean isValidSignature = vnpayService.verifyPayment(request);
            
            String txnRef = request.getParameter("vnp_TxnRef");
            String responseCode = request.getParameter("vnp_ResponseCode");
            String transactionStatus = request.getParameter("vnp_TransactionStatus");
            String vnp_SecureHash = request.getParameter("vnp_SecureHash");
            
            System.out.println("=== VNPay Return URL Processing ===");
            System.out.println("Signature valid: " + isValidSignature);
            System.out.println("TxnRef: " + txnRef + ", ResponseCode: " + responseCode + ", TransactionStatus: " + transactionStatus);
            
            String orderInfo = request.getParameter("vnp_OrderInfo");
            String amount = request.getParameter("vnp_Amount");
            String transactionNo = request.getParameter("vnp_TransactionNo");
            String payDate = request.getParameter("vnp_PayDate");
            String bankCode = request.getParameter("vnp_BankCode");
            
            // Update payment status in database if signature is valid
            if (isValidSignature) {
                System.out.println("✅ Signature verification SUCCESS - proceeding with status update");
                
                Map<String, String> vnpParams = new HashMap<>();
                vnpParams.put("vnp_TxnRef", txnRef);
                vnpParams.put("vnp_ResponseCode", responseCode);
                vnpParams.put("vnp_TransactionNo", transactionNo);
                vnpParams.put("vnp_TransactionStatus", transactionStatus);
                vnpParams.put("vnp_PayDate", payDate);
                vnpParams.put("vnp_BankCode", bankCode);
                
                // Process payment response and update status
                System.out.println("Calling processPaymentResponse...");
                boolean updateResult = vnpayService.processPaymentResponse(vnpParams);
                System.out.println("processPaymentResponse result: " + updateResult);
                
                // Handle VNPay callback for subscription updates
                System.out.println("Calling handleVnPayCallback...");
                String callbackResult = vnpayService.handleVnPayCallback(vnpParams);
                System.out.println("handleVnPayCallback result: " + callbackResult);
            } else {
                System.out.println("❌ Signature verification FAILED - no status update");
            }
            
            // Tạo HTML response
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html><html><head><title>Kết quả thanh toán</title>");
            html.append("<meta charset='UTF-8'>");
            html.append("<style>body{font-family:Arial;text-align:center;padding:50px;}</style>");
            html.append("</head><body>");
            
            if (isValidSignature) {
                if ("00".equals(responseCode) && "00".equals(transactionStatus)) {
                    // Thanh toán thành công
                    html.append("<h1 style='color:green;'>✅ Thanh toán thành công!</h1>");
                    html.append("<p>Mã giao dịch: ").append(txnRef).append("</p>");
                    html.append("<p>Thông tin: ").append(orderInfo).append("</p>");
                    html.append("<p>Số tiền: ").append(Long.parseLong(amount) / 100).append(" VND</p>");
                    
                    // JavaScript redirect về FE (thay YOUR_FE_URL bằng URL thực tế)
                    html.append("<script>");
                    html.append("setTimeout(function(){");
                    html.append("window.location.href = 'https://your-frontend.com/payment/success?txnRef=").append(txnRef).append("';");
                    html.append("}, 3000);"); // Redirect sau 3 giây
                    html.append("</script>");
                    html.append("<p>Đang chuyển hướng về ứng dụng...</p>");
                    
                } else {
                    // Thanh toán thất bại
                    html.append("<h1 style='color:red;'>❌ Thanh toán thất bại!</h1>");
                    html.append("<p>Mã lỗi: ").append(responseCode).append("</p>");
                    html.append("<p>Mã giao dịch: ").append(txnRef).append("</p>");
                    
                    html.append("<script>");
                    html.append("setTimeout(function(){");
                    html.append("window.location.href = 'https://your-frontend.com/payment/failure?txnRef=").append(txnRef).append("';");
                    html.append("}, 3000);");
                    html.append("</script>");
                    html.append("<p>Đang chuyển hướng về ứng dụng...</p>");
                }
            } else {
                // Chữ ký không hợp lệ
                html.append("<h1 style='color:red;'>❌ Lỗi xác thực!</h1>");
                html.append("<p>Chữ ký không hợp lệ</p>");
            }
            
            html.append("</body></html>");
            
            return ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.TEXT_HTML)
                    .body(html.toString());
            
        } catch (Exception e) {
            String errorHtml = "<!DOCTYPE html><html><body><h1>Lỗi xử lý</h1><p>" + e.getMessage() + "</p></body></html>";
            return ResponseEntity.internalServerError()
                    .contentType(org.springframework.http.MediaType.TEXT_HTML)
                    .body(errorHtml);
        }
    }
    
    /**
     * VNPay IPN Handler - Theo code mẫu chính thức VNPay
     * URL này để VNPay gọi về thông báo kết quả thanh toán (server-to-server)
     */
    @GetMapping("/ipn")
    @Operation(summary = "VNPay IPN Handler", 
               description = "Endpoint để VNPay gọi về thông báo kết quả thanh toán")
    public ResponseEntity<Map<String, String>> handleVNPayIPN(HttpServletRequest request) {
        
        Map<String, String> responseMap = new HashMap<>();
        
        try {
            // Begin process return from VNPAY - theo code mẫu IPN chính thức
            // ENCODE parameters khi put vào fields (lấy raw name/value rồi encode)
            Map<String, String> fields = new HashMap<>();
            for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {
                String rawName = params.nextElement();
                String rawValue = request.getParameter(rawName);
                if (rawValue != null && rawValue.length() > 0) {
                    String fieldName = java.net.URLEncoder.encode(rawName, java.nio.charset.StandardCharsets.US_ASCII.toString());
                    String fieldValue = java.net.URLEncoder.encode(rawValue, java.nio.charset.StandardCharsets.US_ASCII.toString());
                    fields.put(fieldName, fieldValue);
                }
            }

            // Lấy vnp_SecureHash từ request gốc (KHÔNG ENCODE) - Theo code mẫu VNPay IPN
            String vnp_SecureHash = request.getParameter("vnp_SecureHash");

            // Remove encoded secure hash keys (fields map chứa encoded keys)
            String encSecureHashTypeIpn = java.net.URLEncoder.encode("vnp_SecureHashType", java.nio.charset.StandardCharsets.US_ASCII.toString());
            String encSecureHashIpn = java.net.URLEncoder.encode("vnp_SecureHash", java.nio.charset.StandardCharsets.US_ASCII.toString());
            if (fields.containsKey(encSecureHashTypeIpn)) {
                fields.remove(encSecureHashTypeIpn);
            }
            if (fields.containsKey(encSecureHashIpn)) {
                fields.remove(encSecureHashIpn);
            }
            
            // Check checksum - hashAllFields sẽ xử lý fields đã encode
            String signValue = vnpayService.hashAllFields(fields);
            if (signValue.equals(vnp_SecureHash)) {
                // Checksum hợp lệ
                
                String txnRef = request.getParameter("vnp_TxnRef");
                String responseCode = request.getParameter("vnp_ResponseCode");
                String transactionNo = request.getParameter("vnp_TransactionNo");
                String transactionStatus = request.getParameter("vnp_TransactionStatus");
                String payDate = request.getParameter("vnp_PayDate");
                String bankCode = request.getParameter("vnp_BankCode");
                
                // Check if payment exists in database
                try {
                    VNPayPaymentDTO existingPayment = vnpayService.getPaymentByTxnRef(txnRef);
                    boolean checkOrderId = (existingPayment != null);
                    boolean checkAmount = true; // Can add amount validation here
                    boolean checkOrderStatus = (existingPayment != null && existingPayment.getStatus() == 0); // PENDING
                    
                    if(checkOrderId) {
                        if(checkAmount) {
                            if (checkOrderStatus) {
                                // Update payment status in database
                                Map<String, String> vnpParams = new HashMap<>();
                                vnpParams.put("vnp_TxnRef", txnRef);
                                vnpParams.put("vnp_ResponseCode", responseCode);
                                vnpParams.put("vnp_TransactionNo", transactionNo);
                                vnpParams.put("vnp_TransactionStatus", transactionStatus);
                                vnpParams.put("vnp_PayDate", payDate);
                                vnpParams.put("vnp_BankCode", bankCode);
                                
                                boolean updateSuccess = vnpayService.processPaymentResponse(vnpParams);
                                
                                // Handle VNPay callback for subscription updates
                                String callbackResult = vnpayService.handleVnPayCallback(vnpParams);
                                
                                if (updateSuccess) {
                                    responseMap.put("RspCode", "00");
                                    responseMap.put("Message", "Confirm Success");
                                } else {
                                    responseMap.put("RspCode", "99");
                                    responseMap.put("Message", "Failed to update payment status");
                                }
                            } else {
                                responseMap.put("RspCode", "02");
                                responseMap.put("Message", "Order already confirmed");
                            }
                        } else {
                            responseMap.put("RspCode", "04");
                            responseMap.put("Message", "Invalid Amount");
                        }
                    } else {
                        responseMap.put("RspCode", "01");
                        responseMap.put("Message", "Order not Found");
                    }
                } catch (Exception dbEx) {
                    responseMap.put("RspCode", "99");
                    responseMap.put("Message", "Database error: " + dbEx.getMessage());
                }
            } else {
                responseMap.put("RspCode", "97");
                responseMap.put("Message", "Invalid Checksum");
            }
            
        } catch(Exception e) {
            responseMap.put("RspCode", "99");
            responseMap.put("Message", "Unknown error");
        }
        
        return ResponseEntity.ok(responseMap);
    }
    
    /**
     * Check payment status - API để frontend check status
     */
    @GetMapping("/status/{txnRef}")
    @Operation(summary = "Check Payment Status", 
               description = "Kiểm tra trạng thái thanh toán bằng transaction reference")
    public ApiResponse<?> checkPaymentStatus(
            @Parameter(description = "VNPay Transaction Reference") @PathVariable String txnRef) {
        
        try {
            var payment = vnpayService.getPaymentByTxnRef(txnRef);
            
            if (payment != null) {
                return ApiResponse.success("Payment status retrieved", payment);
            } else {
                return ApiResponse.error("Payment not found with txnRef: " + txnRef);
            }
            
        } catch (Exception e) {
            return ApiResponse.error("Failed to check payment status: " + e.getMessage());
        }
    }
    
    /**
     * Test endpoint để generate hash cho test return URL
     */
    @GetMapping("/test-hash")
    public ResponseEntity<String> testHash() {
        // Test parameters
        Map<String, String> testParams = new HashMap<>();
        testParams.put("vnp_Amount", "100000");
        testParams.put("vnp_BankCode", "NCB");
        testParams.put("vnp_OrderInfo", "Test payment");
        testParams.put("vnp_PayDate", "20251010123000");
        testParams.put("vnp_ResponseCode", "00");
        testParams.put("vnp_TmnCode", "8I1PNFUT");
        testParams.put("vnp_TransactionNo", "14226112");
        testParams.put("vnp_TransactionStatus", "00");
        testParams.put("vnp_TxnRef", "VNP123456");
        
        // Encode như trong return handler
        Map<String, String> encodedFields = new HashMap<>();
        for (Map.Entry<String, String> entry : testParams.entrySet()) {
            try {
                String encodedKey = java.net.URLEncoder.encode(entry.getKey(), java.nio.charset.StandardCharsets.US_ASCII.toString());
                String encodedValue = java.net.URLEncoder.encode(entry.getValue(), java.nio.charset.StandardCharsets.US_ASCII.toString());
                encodedFields.put(encodedKey, encodedValue);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("Encoding error: " + e.getMessage());
            }
        }
        
        String hash = vnpayService.hashAllFields(encodedFields);
        
        String testUrl = "http://localhost:8080/vnpay/return?" +
            "vnp_TmnCode=8I1PNFUT&vnp_Amount=100000&vnp_BankCode=NCB&vnp_OrderInfo=Test+payment&vnp_PayDate=20251010123000&vnp_ResponseCode=00&vnp_TransactionNo=14226112&vnp_TransactionStatus=00&vnp_TxnRef=VNP123456&vnp_SecureHash=" + hash;
        
        return ResponseEntity.ok("Test URL: " + testUrl + "\nHash: " + hash);
    }
    
    // Helper method để get client IP
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    /**
     * Lấy lịch sử số lần đổi pin theo userID
     */
    @GetMapping("/pin-history/{userID}")
    @Operation(summary = "Get Pin Change History", 
               description = "Lấy lịch sử số lần đổi pin theo userID - trả về stationID, pinID, createdAt, bankCode, status")
    public ResponseEntity<ApiResponse<Object>> getPinChangeHistory(@PathVariable Integer userID) {
        try {
            VNPayPaymentDAO dao = new VNPayPaymentDAO();
            List<VNPayPaymentDTO> history = dao.getPinChangeHistory(userID);
            
            if (history != null && !history.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.success("Pin change history retrieved successfully", history));
            } else {
                return ResponseEntity.ok(ApiResponse.success("No pin change history found", history));
            }
            
        } catch (Exception e) {
            System.out.println("Error at VNPayController - getPinChangeHistory: " + e.toString());
            return ResponseEntity.internalServerError().body(ApiResponse.error("System error occurred"));
        }
    }

    /**
     * Lấy lịch sử thanh toán theo userID
     */
    @GetMapping("/payment-history/{userID}")
    @Operation(summary = "Get Payment History", 
               description = "Lấy lịch sử thanh toán theo userID - trả về packID, txnRef, orderInfo, amount, bankCode, status, createdAt")
    public ResponseEntity<ApiResponse<Object>> getPaymentHistory(@PathVariable Integer userID) {
        try {
            VNPayPaymentDAO dao = new VNPayPaymentDAO();
            List<VNPayPaymentDTO> history = dao.getPaymentHistory(userID);
            
            if (history != null && !history.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.success("Payment history retrieved successfully", history));
            } else {
                return ResponseEntity.ok(ApiResponse.success("No payment history found", history));
            }
            
        } catch (Exception e) {
            System.out.println("Error at VNPayController - getPaymentHistory: " + e.toString());
            return ResponseEntity.internalServerError().body(ApiResponse.error("System error occurred"));
        }
    }
}