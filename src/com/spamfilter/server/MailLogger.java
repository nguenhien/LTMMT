/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spamfilter.server;

/**
 *
 * @author phamq
 */
import com.spamfilter.model.EmailMessage;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MailLogger {
    private static final String LOG_FILE = "server_mail_history.log";
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static synchronized void logEmail(EmailMessage email) {
        String time = dtf.format(LocalDateTime.now());

        String logContent = String.format("[%s] IP: %s | From: %s | To: %s | Subject: %s | Status: %s%n",
                time, 
                email.getSenderIP(), 
                email.getSenderEmail(), 
                email.getRecipient(), 
                email.getSubject(), 
                email.getStatus());

        // In ra console của Server
        System.out.print(logContent);

        // Ghi vào file log lưu trữ
        try (PrintWriter pw = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            pw.write(logContent);
        } catch (IOException e) {
            System.err.println("Lỗi ghi file log: " + e.getMessage());
        }
    }
}
