package com.example.demo.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * QR Code Service - Tạo VNPay QR code đúng format
 */
@Service
public class VNPayQRCodeService {
    
    private static final int QR_CODE_WIDTH = 300;
    private static final int QR_CODE_HEIGHT = 300;
    
    /**
     * Tạo VNPay QR Code - URL based (tương thích 100% với VNPay sandbox)
     */
    public String generateVNPayQRCode(String vnp_TxnRef, Long vnp_Amount, String vnp_OrderInfo) {
        try {
            System.out.println("Generating URL-based VNPay QR code for transaction: " + vnp_TxnRef);
            
            // Thay vì tạo EMVCo format phức tạp, tạo QR chứa URL thanh toán VNPay
            // Điều này đảm bảo 100% tương thích với VNPay sandbox
            return null; // Return null để VNPayService tạo payment URL QR
            
        } catch (Exception e) {
            System.err.println("Error generating VNPay QR code: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Build VNPay QR data cho sandbox
     */
    private String buildVNPayQRData(String txnRef, Long amount, String orderInfo) {
        // VNPay Sandbox QR Format - Simplified for testing
        StringBuilder qrData = new StringBuilder();
        
        // Payload Format Indicator (Tag 00)
        qrData.append("00").append("02").append("01");
        
        // Point of Initiation Method (Tag 01) - Static QR
        qrData.append("01").append("02").append("12");
        
        // VNPay Merchant Account Information (Tag 38)
        String vnpayData = buildVNPayMerchantData();
        qrData.append("38").append(String.format("%02d", vnpayData.length())).append(vnpayData);
        
        // Transaction Currency (Tag 53) - VND
        qrData.append("53").append("03").append("704");
        
        // Transaction Amount (Tag 54) - Convert to VND
        String amountStr = String.valueOf(amount / 100);
        qrData.append("54").append(String.format("%02d", amountStr.length())).append(amountStr);
        
        // Country Code (Tag 58) - Vietnam
        qrData.append("58").append("02").append("VN");
        
        // Merchant Name (Tag 59)
        String merchantName = "VNPAY DEMO";
        qrData.append("59").append(String.format("%02d", merchantName.length())).append(merchantName);
        
        // Merchant City (Tag 60)
        String merchantCity = "Hanoi";
        qrData.append("60").append(String.format("%02d", merchantCity.length())).append(merchantCity);
        
        // Additional Data Field (Tag 62)
        String additionalData = buildAdditionalData(txnRef, orderInfo);
        qrData.append("62").append(String.format("%02d", additionalData.length())).append(additionalData);
        
        // CRC (Tag 63) - Will be calculated
        qrData.append("6304");
        String crc = calculateCRC16(qrData.toString());
        qrData.append(crc);
        
        return qrData.toString();
    }
    
    /**
     * Build VNPay merchant specific data cho sandbox
     */
    private String buildVNPayMerchantData() {
        StringBuilder merchantData = new StringBuilder();
        
        // Globally Unique Identifier (Tag 00) - VNPay GUID
        String guid = "A000000775"; 
        merchantData.append("00").append(String.format("%02d", guid.length())).append(guid);
        
        // Payment Network Specific (Tag 01) - VNPay Service Code
        String vnpayId = "VNPAYQR"; 
        merchantData.append("01").append(String.format("%02d", vnpayId.length())).append(vnpayId);
        
        // Merchant Account Information (Tag 02) - Terminal Code
        String merchantInfo = "DEMOV210"; // TMN Code từ config
        merchantData.append("02").append(String.format("%02d", merchantInfo.length())).append(merchantInfo);
        
        return merchantData.toString();
    }
    
    /**
     * Build additional data field
     */
    private String buildAdditionalData(String txnRef, String orderInfo) {
        StringBuilder additionalData = new StringBuilder();
        
        // Bill Number (Tag 01)
        additionalData.append("01").append(String.format("%02d", txnRef.length())).append(txnRef);
        
        // Reference Label (Tag 05) 
        String refLabel = orderInfo.length() > 25 ? orderInfo.substring(0, 25) : orderInfo;
        additionalData.append("05").append(String.format("%02d", refLabel.length())).append(refLabel);
        
        // Terminal Label (Tag 07)
        String terminalLabel = "VNPAY";
        additionalData.append("07").append(String.format("%02d", terminalLabel.length())).append(terminalLabel);
        
        return additionalData.toString();
    }
    
    /**
     * Calculate CRC16 for QR code
     */
    private String calculateCRC16(String data) {
        int crc = 0xFFFF;
        byte[] bytes = data.getBytes();
        
        for (byte b : bytes) {
            crc ^= (b & 0xFF) << 8;
            for (int i = 0; i < 8; i++) {
                if ((crc & 0x8000) != 0) {
                    crc = (crc << 1) ^ 0x1021;
                } else {
                    crc <<= 1;
                }
                crc &= 0xFFFF;
            }
        }
        
        return String.format("%04X", crc);
    }
    
    /**
     * Generate QR code image from data
     */
    private String generateQRCodeBase64(String qrData) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(qrData, BarcodeFormat.QR_CODE, QR_CODE_WIDTH, QR_CODE_HEIGHT);
        
        BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(qrImage, "PNG", baos);
        byte[] imageBytes = baos.toByteArray();
        
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
    }
    
    /**
     * Tạo URL QR code cho trường hợp fallback
     */
    public String generatePaymentUrlQRCode(String paymentUrl) {
        try {
            return generateQRCodeBase64(paymentUrl);
        } catch (Exception e) {
            System.err.println("Error generating URL QR code: " + e.getMessage());
            return null;
        }
    }
}