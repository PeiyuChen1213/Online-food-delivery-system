package com.chenpeiyu.service;

public interface EmailService {
    public void sendVerificationCode(String to,String code);
}
