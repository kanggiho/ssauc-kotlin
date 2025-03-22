document.addEventListener("DOMContentLoaded", function() {
    const loginForm = document.getElementById("login-form");
    const emailErrorElement = document.getElementById("email-error");
    const passwordErrorElement = document.getElementById("password-error");

    // 페이지 로드시 URL 쿼리 파라미터에 error=true가 있으면
    // 서버 로그인 실패 메시지를 비밀번호 필드 아래에 표시
    const params = new URLSearchParams(window.location.search);
    if (params.get("error") === "true") {
        passwordErrorElement.textContent = "아이디 또는 비밀번호를 확인해주세요";
    }

    // 폼 제출 시 클라이언트 유효성 검사
    loginForm.addEventListener("submit", function(event) {
        // 기존 에러 메시지 초기화
        emailErrorElement.textContent = "";
        passwordErrorElement.textContent = "";

        const email = document.getElementById("email").value.trim();
        const password = document.getElementById("password").value.trim();

        // 아이디(이메일) 빈 값 체크
        if (email === "") {
            emailErrorElement.textContent = "아이디를 입력해주세요";
            event.preventDefault(); // form 제출 막음
            return;
        }

        // 비밀번호 빈 값 체크
        if (password === "") {
            passwordErrorElement.textContent = "비밀번호를 입력해주세요";
            event.preventDefault(); // form 제출 막음
            return;
        }

        // 둘 다 입력되었다면 서버로 POST
    });

    // (선택) 사용자가 입력 시작 시 에러 메시지 초기화
    document.getElementById("email").addEventListener("input", function() {
        emailErrorElement.textContent = "";
    });
    document.getElementById("password").addEventListener("input", function() {
        passwordErrorElement.textContent = "";
    });
});
