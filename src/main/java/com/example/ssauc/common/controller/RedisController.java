package com.example.ssauc.common.controller;

import com.example.ssauc.common.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RedisController {

    @Autowired
    private RedisService redisService;

    // 단순 key-value 저장
    @GetMapping("/save")
    public String saveData(@RequestParam String key, @RequestParam String value) {
        redisService.saveValue(key, value);
        return "Saved!";
    }

    // 단순 key-value 조회
    @GetMapping("/get")
    public Object getData(@RequestParam String key) {
        return redisService.getValue(key);
    }
}
