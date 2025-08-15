package com.xxx.mailspringbootdemo.controller;

import com.xxx.mailspringbootdemo.util.EmailUtil;
import org.apache.commons.io.FileUtils;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;

/**
 * 发送邮件接口
 */
@RestController
public class SendMailController {
    @Resource
    private EmailUtil emailUtil;



    @PostMapping("sendAttachmentsEmail")
    public String sendAttachmentsEmail() throws IOException {
        byte[] bytes = FileUtils.readFileToByteArray(new File("test.pdf"));
        String subject = "好啊好啊_客户信息一览表_客户信息保险合同缴费明细_20230115&&abcdeijnbguhhjbhk";
        if (emailUtil.sendEmail("zhengliuwei666@gmail.com,liuwei_zheng@163.com", subject, "你好！<br>附件请查收！",
                subject + ".pdf", Base64Utils.encodeToString(bytes))) {
            return "成功";
        }
        return "失败";
    }
}