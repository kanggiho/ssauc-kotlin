package com.example.ssauc.common.controller

import com.example.ssauc.common.service.RedisService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class RedisController {
    @Autowired
    private val redisService: RedisService? = null

    // 단순 key-value 저장
    @GetMapping("/save")
    fun saveData(@RequestParam key: String?, @RequestParam value: String?): String {
        redisService!!.saveValue(key, value)
        return "Saved!"
    }

    // 단순 key-value 조회
    @GetMapping("/get")
    fun getData(@RequestParam key: String?): Any {
        return redisService!!.getValue(key)
    }
}
