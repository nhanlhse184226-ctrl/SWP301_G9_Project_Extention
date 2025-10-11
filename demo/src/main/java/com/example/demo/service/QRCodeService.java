package com.example.demo.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

/**
 * QR Code Service - Tạo QR code cho VNPay payment
 */
@Service
public class QRCodeService {
    
    private static final int QR_CODE_WIDTH = 300;
    private static final int QR_CODE_HEIGHT = 300;
    
    /**
     * Tạo QR code từ text và trả về base64 string
     */
    public String generateQRCodeBase64(String text) throws WriterException, IOException {
        // Tạo QR code matrix
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, QR_CODE_WIDTH, QR_CODE_HEIGHT);
        
        // Convert matrix thành BufferedImage
        BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
        
        // Convert BufferedImage thành base64 string
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(qrImage, "PNG", baos);
        byte[] imageBytes = baos.toByteArray();
        
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
    }
    
    /**
     * Tạo QR code cho VNPay payment URL
     */
    public String generateVNPayQRCode(String paymentUrl) {
        try {
            return generateQRCodeBase64(paymentUrl);
        } catch (WriterException | IOException e) {
            System.err.println("Error generating QR code: " + e.getMessage());
            return null;
        }
    }
}