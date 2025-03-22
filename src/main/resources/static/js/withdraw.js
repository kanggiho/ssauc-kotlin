document.addEventListener("DOMContentLoaded", () => {
    const withdrawForm = document.getElementById("withdrawForm");
    const passwordInput = document.getElementById("password");
    const confirmPasswordInput = document.getElementById("confirmPassword");
    const passwordError = document.getElementById("passwordError");
    const confirmPasswordError = document.getElementById("confirmPasswordError");
    const withdrawErrorMsg = document.getElementById("withdrawErrorMsg");
    const cancelBtn = document.getElementById("cancelBtn");

    function checkPasswordsMatch() {
        const pw = passwordInput.value.trim();
        const pwConfirm = confirmPasswordInput.value.trim();
        if (pw !== pwConfirm) {
            confirmPasswordError.textContent = "비밀번호가 일치하지 않습니다.";
        } else {
            confirmPasswordError.textContent = "";
        }
    }

    passwordInput.addEventListener("input", () => {
        if (passwordInput.value.trim().length < 8) {
            passwordError.textContent = "비밀번호는 8자 이상이어야 합니다.";
        } else {
            passwordError.textContent = "";
        }
        checkPasswordsMatch();
    });
    confirmPasswordInput.addEventListener("input", () => {
        checkPasswordsMatch();
    });

    withdrawForm.addEventListener("submit", async (e) => {
        e.preventDefault();
        withdrawErrorMsg.textContent = "";

        const pw = passwordInput.value.trim();
        const pwConfirm = confirmPasswordInput.value.trim();

        if (!pw || !pwConfirm) {
            withdrawErrorMsg.textContent = "모든 필드를 입력해주세요.";
            return;
        }
        if (pw.length < 8) {
            withdrawErrorMsg.textContent = "비밀번호는 8자 이상이어야 합니다.";
            return;
        }
        if (pw !== pwConfirm) {
            withdrawErrorMsg.textContent = "비밀번호가 일치하지 않습니다.";
            return;
        }

        // 탈퇴 확인 (window.confirm 대신 모달 사용 가능)
        const isConfirmed = confirm("정말 회원 탈퇴하시겠습니까?");
        if (!isConfirmed) {
            return;
        }

        try {
            const res = await fetch("/mypage/withdraw", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ password: pw })
            });
            if (!res.ok) {
                const errorMsg = await res.text();
                throw new Error(errorMsg);
            }
            const msg = await res.text();
            alert(msg); // "회원 탈퇴가 완료되었습니다."
            window.location.href = "/login";
        } catch (err) {
            withdrawErrorMsg.textContent = err.message;
        }
    });

    // 취소 버튼
    cancelBtn.addEventListener("click", () => {
        window.location.href = "/mypage";
    });
});
