package com.spamfilter.client;

import com.spamfilter.model.EmailMessage;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class TestSendMany {
    public static void main(String[] args) {
        String serverIP = "10.49.25.229";   // ← Đổi thành IP ZeroTier máy Server
        int soLuong = 50;                  // Số email muốn gửi

        for (int i = 1; i <= soLuong; i++) {
            try (Socket socket = new Socket(serverIP, 12345);
                 ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {

                String subject = "Test email số " + i;
                String body = "Đây là nội dung test thứ " + i;

                // Cứ 5 email thì nhét từ khóa spam để test bộ lọc
                if (i % 5 == 0) {
                    subject = "casino trúng thưởng " + i;
                    body = "free money quảng cáo";
                }

                EmailMessage email = new EmailMessage(
                        "10.49.25.50",               // IP giả
                        "test" + i + "@gmail.com",
                        "nguoinhan@gmail.com",
                        subject,
                        body
                );

                // Protocol mới
                oos.writeObject("SEND");
                oos.flush();
                oos.writeObject(email);
                oos.flush();

                EmailMessage result = (EmailMessage) ois.readObject();
                System.out.println("Gửi #" + i + " → Status: " + result.getStatus());

            } catch (Exception e) {
                System.err.println("Lỗi ở email #" + i + ": " + e.getMessage());
            }
        }
        System.out.println("=== Đã gửi xong " + soLuong + " email ===");
    }
}