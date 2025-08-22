package com.example.poemai.utils;

import android.util.Log;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import android.util.Base64;

public class JwtUtils {
    private static final String TAG = "JwtUtils";
    private static final long EXPIRATION_TIME = 604800000; // 7天
    private static final String SECRET_STRING = "your-secret-key-your-secret-key-1234-very-long-secret";
    
    public static String generateToken(String username) {
        Log.d(TAG, "Generated token for user: " + username);
        
        try {
            // 创建头部
            String header = Base64.encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(), Base64.URL_SAFE | Base64.NO_WRAP);
            
            // 创建载荷
            long now = System.currentTimeMillis();
            long exp = now + EXPIRATION_TIME;
            
            String payloadJson = "{\"sub\":\"" + username + "\",\"iat\":" + now + ",\"exp\":" + exp + "}";
            String payload = Base64.encodeToString(payloadJson.getBytes(), Base64.URL_SAFE | Base64.NO_WRAP);
            
            // 创建签名
            String signatureInput = header + "." + payload;
            String signature = createSignature(signatureInput);
            String completeSignature = Base64.encodeToString(signature.getBytes(), Base64.URL_SAFE | Base64.NO_WRAP);
            
            // 组合JWT
            String token = header + "." + payload + "." + completeSignature;
            
            Log.d(TAG, "Token generated: " + token.substring(0, Math.min(token.length(), 20)) + "...");
            return token;
        } catch (Exception e) {
            Log.e(TAG, "Error generating token", e);
            return null;
        }
    }
    
    public static String extractUsername(String token) {
        try {
            Log.d(TAG, "Extracting username from token: " + token.substring(0, Math.min(token.length(), 20)) + "...");
            
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                Log.e(TAG, "Invalid token format");
                return null;
            }
            
            // 解码头部和载荷
            String payload = new String(Base64.decode(parts[1], Base64.URL_SAFE), StandardCharsets.UTF_8);
            
            // 简单解析JSON获取用户名 (实际项目中应使用JSON库)
            int subStart = payload.indexOf("\"sub\":\"");
            if (subStart == -1) {
                Log.e(TAG, "Subject not found in payload");
                return null;
            }
            
            int subEnd = payload.indexOf("\"", subStart + 7);
            if (subEnd == -1) {
                Log.e(TAG, "Invalid subject format");
                return null;
            }
            
            String username = payload.substring(subStart + 7, subEnd);
            Log.d(TAG, "Extracted username: " + username);
            return username;
        } catch (Exception e) {
            Log.e(TAG, "Error extracting username from token", e);
            return null;
        }
    }
    
    public static boolean validateToken(String token) {
        try {
            Log.d(TAG, "Validating token: " + token.substring(0, Math.min(token.length(), 20)) + "...");
            
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                Log.e(TAG, "Invalid token format");
                return false;
            }
            
            // 验证签名
            String signatureInput = parts[0] + "." + parts[1];
            String expectedSignature = createSignature(signatureInput);
            String expectedSignatureEncoded = Base64.encodeToString(expectedSignature.getBytes(), Base64.URL_SAFE | Base64.NO_WRAP);
            
            if (!MessageDigest.isEqual(expectedSignatureEncoded.getBytes(), Base64.decode(parts[2], Base64.URL_SAFE))) {
                Log.e(TAG, "Invalid token signature");
                return false;
            }
            
            // 检查过期时间
            String payload = new String(Base64.decode(parts[1], Base64.URL_SAFE), StandardCharsets.UTF_8);
            int expStart = payload.indexOf("\"exp\":");
            if (expStart != -1) {
                int expEnd = payload.indexOf(",", expStart);
                if (expEnd == -1) {
                    expEnd = payload.indexOf("}", expStart);
                }
                
                if (expEnd != -1) {
                    String expStr = payload.substring(expStart + 6, expEnd).trim();
                    long exp = Long.parseLong(expStr);
                    long now = System.currentTimeMillis();
                    
                    if (now > exp) {
                        Log.e(TAG, "Token expired");
                        return false;
                    }
                }
            }
            
            Log.d(TAG, "Token validation result: true");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error validating token", e);
            return false;
        }
    }
    
    private static String createSignature(String input) {
        try {
            String dataToSign = input + "." + SECRET_STRING;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(dataToSign.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Error creating signature", e);
            return null;
        }
    }
}