package com.xxx.mailspringbootdemo.util;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;

import javax.annotation.Resource;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

@Slf4j
@Component
public class EmailUtil {

    @Resource
    private JavaMailSender mailSender;
    @Value("${spring.mail.username}")
    private String from;

    // 防止附件名称过长163邮箱乱码并且进行全局定义
    static {
        System.setProperty("mail.mime.splitlongparameters", "false");
    }

    public boolean sendSimpleEmail(String to, String subject, String htmlBody) {
        return sendEmail(to, subject, htmlBody, null, null);
    }

    public boolean sendEmail(String to, String subject, String htmlBody, String attachmentName, String attachmentBase64) {
        log.info("发送邮件入参：{},{},{},{}", to, subject, htmlBody, attachmentName);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(from);
            helper.setTo(to.split(","));
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            if (attachmentBase64 != null) {
                helper.addAttachment(MimeUtility.encodeWord(attachmentName, "UTF-8", "B"),
                        new ByteArrayResource(Base64Utils.decodeFromString(attachmentBase64)));
            }
            mailSender.send(message);
            log.info("发送邮件成功");
            return true;
        } catch (Exception e) {
            log.error("发送邮件异常，{}", ExceptionUtils.getStackTrace(e));
        }
        return false;
    }
}
