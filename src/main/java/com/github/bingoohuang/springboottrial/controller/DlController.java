package com.github.bingoohuang.springboottrial.controller;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.file.Files;


/*
http://127.0.0.1:8080/dl?filename=我是黄进兵.docx&utf8=true
http://127.0.0.1:8080/dl?filename=我是黄进兵.doc&utf8=true
http://127.0.0.1:8080/dl?filename=我是黄进兵.pdf&utf8=true
http://127.0.0.1:8080/dl?filename=我是黄进兵.pdf&utf8=true
http://127.0.0.1:8080/dl?filename=我是黄进兵.txt&utf8=false
http://127.0.0.1:8080/dl?filename=他是黄进兵.txt&utf8=true
*/
@RestController
public class DlController {
    @GetMapping("/dl") @SneakyThrows
    public ResponseEntity<byte[]> downloadByDocumentId(@Param("filename") String filename, @Param("utf8") boolean utf8) {
        val cpr = new ClassPathResource("dl/" + filename);
        @Cleanup InputStream is = cpr.getInputStream();
        byte[] bytes = cpr.exists() ? ByteStreams.toByteArray(is) : "我是黄进兵".getBytes(Charsets.UTF_8);

        val headers = new HttpHeaders();
        String f = filename;
        if (utf8) {
//            headers.add("Content-Type", MimeTypes.getFileMimeType(filename) + "; charset=UTF-8");
            f = URLEncoder.encode(filename, "UTF-8");
        } else {
//            headers.add("Content-Type", MimeTypes.getFileMimeType(filename));
        }

        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        // https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Headers/Content-Disposition
        // https://cn.charlee.li/use-utf8-in-download-filename.html
        // 其实按照RFC2231的定义， 多语言编码的Content-Disposition应该这么定义：
        // Content-Disposition: attachment; filename*="utf8''%E4%B8%AD%E6%96%87%20%E6%96%87%E4%BB%B6%E5%90%8D.txt"

        // 注意：跨域CORS时，需要配置： Access-Control-Expose-Headers: Content-Disposition
        // https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Headers/Access-Control-Expose-Headers

        headers.add("Content-Disposition", "attachment;filename=" + f + ";filename*=UTF-8''" + f);
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }
}
