/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spamfilter.server;

import com.spamfilter.model.EmailMessage;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class MailServer {
    private static final int PORT = 12345; // Cổng mặc định cho Mail Server
    
    // Danh sách từ khóa cấm (Spam keywords) theo yêu cầu đề tài
    private static final List<String> BLACKLIST_KEYWORDS = Arrays.asList(
        "casino", "trúng thưởng", "quảng cáo", "free money", "chuyển khoản gấp", "100% win"
    );
    
    // Danh sách lưu tất cả email đã nhận (dùng chung cho mọi Client)
    private static final List<EmailMessage> emailStorage = new CopyOnWriteArrayList<>();
    
    // Danh sách địa chỉ IP bị chặn (Blocked IPs)
    private static final List<String> BLACKLIST_IPS = Arrays.asList(
        "192.168.1.100", "203.162.4.19"
    );

    public static void main(String[] args) {
        System.out.println("[SERVER] Đang khởi động Mail Server tại cổng " + PORT + "...");
        // Sử dụng Pool đa luồng để xử lý nhiều kết nối Client đồng thời
        ExecutorService pool = Executors.newFixedThreadPool(10);
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("[SERVER] Mail Server đã sẵn sàng lắng nghe kết nối từ Client.");
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                // Chuyển giao kết nối cho một luồng xử lý riêng biệt
                pool.execute(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            System.err.println("[SERVER ERROR] Lỗi Server Socket: " + e.getMessage());
        }
    }

    // Lớp xử lý từng Client kết nối đến
    private static class ClientHandler implements Runnable {
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())
            ) {
                String clientIP = socket.getInetAddress().getHostAddress();
                System.out.println("[SERVER] Nhận kết nối từ IP: " + clientIP);

                // Đọc lệnh từ Client trước (String)
                String command = (String) ois.readObject();
                System.out.println("[SERVER] Lệnh nhận được: " + command);

                if ("SEND".equals(command)) {
                    // Nhận email
                    EmailMessage email = (EmailMessage) ois.readObject();

                    // ---- THUẬT TOÁN BỘ LỌC SPAM ----
                    boolean isSpam = false;
                    String spamReason = "";

                    if (BLACKLIST_IPS.contains(clientIP)) {
                        isSpam = true;
                        spamReason = "Địa chỉ IP bị chặn (" + clientIP + ")";
                    } else {
                        String contentToCheck = (email.getSubject() + " " + email.getBody()).toLowerCase();
                        for (String keyword : BLACKLIST_KEYWORDS) {
                            if (contentToCheck.contains(keyword.toLowerCase())) {
                                isSpam = true;
                                spamReason = "Phát hiện từ khóa cấm: '" + keyword + "'";
                                break;
                            }
                        }
                    }

                    if (isSpam) {
                        email.setStatus("SPAM");
                        System.out.println("[SPAM FILTER] Thư bị đưa vào SPAM! Lý do: " + spamReason);
                    } else {
                        email.setStatus("INBOX");
                        System.out.println("[SPAM FILTER] Thư hợp lệ → INBOX.");
                    }

                    // Lưu vào bộ nhớ + ghi log
                    emailStorage.add(email);
                    MailLogger.logEmail(email);

                    // Trả kết quả về Client
                    oos.writeObject(email);
                    oos.flush();

                } else if ("GET_INBOX".equals(command)) {
                    List<EmailMessage> inbox = emailStorage.stream()
                            .filter(e -> "INBOX".equals(e.getStatus()))
                            .collect(Collectors.toList());
                    oos.writeObject(inbox);
                    oos.flush();
                    System.out.println("[SERVER] Đã gửi " + inbox.size() + " thư INBOX");

                } else if ("GET_SPAM".equals(command)) {
                    List<EmailMessage> spam = emailStorage.stream()
                            .filter(e -> "SPAM".equals(e.getStatus()))
                            .collect(Collectors.toList());
                    oos.writeObject(spam);
                    oos.flush();
                    System.out.println("[SERVER] Đã gửi " + spam.size() + " thư SPAM");

                } else {
                    System.out.println("[SERVER] Lệnh không hợp lệ: " + command);
                }

            } catch (Exception e) {
                System.err.println("[SERVER ERROR] Lỗi xử lý Client: " + e.getMessage());
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    // bỏ qua
                }
            }
        }
    }
}