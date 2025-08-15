package com.xxx.mailspringbootdemo.pojo;

import lombok.*;

@Data
public class EmailPojo {

    /**
     * 收件人邮箱
     **/
    private String receiver;

    /**
     * 一对多群发收件人邮箱
     **/
    private String[] receivers;

    /**
     * 邮件标题
     **/
    private String subject;

    /**
     * 邮件正文
     **/
    private String content;
    private String jpgBase64;
    private String pdfBase64;
    private String attachmentName;
    /**
     * 发件人姓名
     **/
    private String fromName;

    /**
     * 收件人姓名
     **/
    private String receiverName;

}