package com.chenpeiyu.service.impl;

import com.chenpeiyu.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.util.Date;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {
    /**
     * 注入邮件工具类
     */
    @Autowired
    private JavaMailSenderImpl javaMailSender;

    @Value("${spring.mail.username}")
    private String sendMailer;

    @Override
    public void sendVerificationCode(String to, String code) {
        try {
            // true 代表支持复杂的类型
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(javaMailSender.createMimeMessage(), true);
            //邮件发信人
            mimeMessageHelper.setFrom(sendMailer);
            //邮件收信人  1或多个
            mimeMessageHelper.setTo(to.split(","));
            //邮件主题
            mimeMessageHelper.setSubject("Riggie Takeout Verification Code");
            //邮件内容
            mimeMessageHelper.setText("[Riggie Takeout] " + code + " is your Riggie Takeout Activation Code (RAC). It expires in 5 minutes. Do not share it with anyone.");
            //邮件发送时间
            mimeMessageHelper.setSentDate(new Date());

            //发送邮件
            javaMailSender.send(mimeMessageHelper.getMimeMessage());
            log.info("发送邮件成功：" + sendMailer + "->" + to);

        } catch (MessagingException e) {
            e.printStackTrace();
            log.info("发送邮件失败：" + e.getMessage());
        }
    }
}
