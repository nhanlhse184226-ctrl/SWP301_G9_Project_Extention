import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class VNPayHashTest {
    
    public static String hmacSHA512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            mac.init(secretKeySpec);
            byte[] result = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder sb = new StringBuilder();
            for (byte b : result) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static void main(String[] args) throws Exception {
        // VNPay credentials from email
        String vnp_HashSecret = "PV6VFWLWR39A6AE4FL755AU5TU9IG0T1";
        
        // Test với exact same params từ log
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Amount", "21300");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_CreateDate", "20251010153229");
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_ExpireDate", "20251010154729");
        vnp_Params.put("vnp_IpAddr", "127.0.0.1");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_OrderInfo", "231");
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_ReturnUrl", "http://localhost:8080/vnpay/return");
        vnp_Params.put("vnp_TmnCode", "5NZLUNMQ");
        vnp_Params.put("vnp_TxnRef", "VNP20251010153229069");
        vnp_Params.put("vnp_Version", "2.1.0");
        
        // Build hash data theo code mẫu VNPay
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                // Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                
                // Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        
        String vnp_SecureHash = hmacSHA512(vnp_HashSecret, hashData.toString());
        
        System.out.println("=== VNPay Hash Test ===");
        System.out.println("Hash Data: " + hashData.toString());
        System.out.println("Secure Hash: " + vnp_SecureHash);
        System.out.println("Payment URL: https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?" + query.toString() + "&vnp_SecureHash=" + vnp_SecureHash);
    }
}
