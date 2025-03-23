package com.example.ssauc.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * 전역 예외 처리: API 호출 시 발생하는 예외를 JSON 형태로 반환
 * 필요하지 않다면 사용하지 않아도 됨
 */
@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): ResponseEntity<Map<String, String?>> {
        val error: MutableMap<String, String?> = HashMap()
        error["error"] = ex.message
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error)
    }
}
