/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spamfilter.model;

import java.io.Serializable;

public class EmailMessage implements Serializable {
    private String senderIP;
    private String senderEmail;
    private String recipient;
    private String subject;
    private String body;
    private String status; // Trạng thái: "INBOX" hoặc "SPAM"

    public EmailMessage(String senderIP, String senderEmail, String recipient, String subject, String body) {
        this.senderIP = senderIP;
        this.senderEmail = senderEmail;
        this.recipient = recipient;
        this.subject = subject;
        this.body = body;
        this.status = "INBOX"; // Mặc định là hộp thư đến
    }

    // Getters and Setters
    public String getSenderIP() { return senderIP; }
    public String getSenderEmail() { return senderEmail; }
    public String getRecipient() { return recipient; }
    public String getSubject() { return subject; }
    public String getBody() { return body; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
