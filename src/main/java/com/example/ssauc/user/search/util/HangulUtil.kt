package com.example.ssauc.user.search.util;

public class HangulUtil {
    // 초성 배열 (ㄱ, ㄲ, ㄴ, ㄷ, ㄸ, ㄹ, ㅁ, ㅂ, ㅃ, ㅅ, ㅆ, ㅇ, ㅈ, ㅉ, ㅊ, ㅋ, ㅌ, ㅍ, ㅎ)
    private static final char[] INITIALS = {
            'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ',
            'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ', 'ㅆ', 'ㅇ',
            'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    };

    // 한글 음절 범위: 가(AC00) ~ 힣(D7A3)
    private static final int HANGUL_BASE = 0xAC00;
    private static final int HANGUL_LAST = 0xD7A3;
    private static final int NUM_JUNGSUNG = 21;
    private static final int NUM_JONGSUNG = 28;

    // 입력 텍스트의 초성을 추출합니다.
    public static String extractInitialConsonants(String text) {
        StringBuilder sb = new StringBuilder();
        for (char ch : text.toCharArray()) {
            if (isHangul(ch)) {
                int code = ch - HANGUL_BASE;
                int initialIndex = code / (NUM_JUNGSUNG * NUM_JONGSUNG);
                sb.append(INITIALS[initialIndex]);
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    // 한글 음절인지 체크합니다.
    public static boolean isHangul(char ch) {
        return ch >= HANGUL_BASE && ch <= HANGUL_LAST;
    }

    // 상품명에서 추출한 초성이 입력된 prefix를 포함하는지 여부 판단
    public static boolean matchesInitialConsonants(String text, String prefix) {
        String initials = extractInitialConsonants(text);
        return initials.contains(prefix);
    }
}
