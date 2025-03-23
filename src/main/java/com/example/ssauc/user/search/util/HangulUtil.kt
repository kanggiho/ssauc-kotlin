package com.example.ssauc.user.search.util

object HangulUtil {
    // 초성 배열 (ㄱ, ㄲ, ㄴ, ㄷ, ㄸ, ㄹ, ㅁ, ㅂ, ㅃ, ㅅ, ㅆ, ㅇ, ㅈ, ㅉ, ㅊ, ㅋ, ㅌ, ㅍ, ㅎ)
    private val INITIALS = charArrayOf(
        'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ',
        'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ', 'ㅆ', 'ㅇ',
        'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    )

    // 한글 음절 범위: 가(AC00) ~ 힣(D7A3)
    private const val HANGUL_BASE = 0xAC00
    private const val HANGUL_LAST = 0xD7A3
    private const val NUM_JUNGSUNG = 21
    private const val NUM_JONGSUNG = 28

    // 입력 텍스트의 초성을 추출합니다.
    fun extractInitialConsonants(text: String): String {
        val sb = StringBuilder()
        for (ch in text.toCharArray()) {
            if (isHangul(ch)) {
                val code = ch.code - HANGUL_BASE
                val initialIndex = code / (NUM_JUNGSUNG * NUM_JONGSUNG)
                sb.append(INITIALS[initialIndex])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    // 한글 음절인지 체크합니다.
    fun isHangul(ch: Char): Boolean {
        return ch.code >= HANGUL_BASE && ch.code <= HANGUL_LAST
    }

    // 상품명에서 추출한 초성이 입력된 prefix를 포함하는지 여부 판단
    fun matchesInitialConsonants(text: String, prefix: String): Boolean {
        val initials = extractInitialConsonants(text)
        return initials.contains(prefix)
    }
}
