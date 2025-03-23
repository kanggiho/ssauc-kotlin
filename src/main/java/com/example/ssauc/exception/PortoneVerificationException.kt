package com.example.ssauc.exception

class PortoneVerificationException : RuntimeException {
    constructor(message: String?) : super(message)

    // (선택사항) 원인(Throwable)을 함께 전달하는 생성자
    constructor(message: String?, cause: Throwable?) : super(message, cause)
}
