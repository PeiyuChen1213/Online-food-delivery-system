package com.chenpeiyu.controller;

import com.chenpeiyu.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * 上传和下载的控制类
 */

@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {

    //设置一个路径
    @Value("${reggit.path}")
    private String basePath;

    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) {
        //file是一个临时文件，需要转存到指定的位置，否则本次请求之后，文件会被删除
        //先查看一下文件的情况
        log.info(file.toString());

        //获取原始文件名
        String originalFilename = file.getOriginalFilename();
        //根据原始的文件名去截取后缀
        //从点开是截取
        String suffix = originalFilename.substring(originalFilename.lastIndexOf('.'));

        //使用UUID开始重新生成文件名，防止文件名称造成重复造成文件覆盖

        String fileName = UUID.randomUUID().toString() + suffix;

        //创建一个文件夹对象
        File dir = new File(basePath);

        //判断当前的文件夹是否是存在的
        if (!dir.exists()) {
            //说明不存在 则需要创建一个文件夹
            dir.mkdirs();
        }

        //将文件转存到指定的位置当中去

        try {
            file.transferTo(new File(basePath + fileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return R.success(fileName);
    }


    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) {

        try {
            //将上传保存到本地的文件转化为输入流
            FileInputStream fileInputStream = new FileInputStream(new File(basePath + name));

            //输出流
            ServletOutputStream outputStream = response.getOutputStream();


            //设置浏览器的响应格式
            response.setContentType("image/jpeg");

            //获取浏览器的输出流之后，使用输出流，将文件写回浏览器
            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len = fileInputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len);
                //刷新一下
                outputStream.flush();
            }

            // 关闭资源
            outputStream.close();
            fileInputStream.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
