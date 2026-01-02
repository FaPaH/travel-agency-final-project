package com.epam.finaltask.service;

public interface MailService {

    void sendTextMail(String to, String subject, String text);

    void sendHtmlMail(String to, String subject, String htmlBody);
}
