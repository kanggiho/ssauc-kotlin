document.addEventListener("DOMContentLoaded", () => {
    // 요소 참조
    const emailInput = document.getElementById("email");
    const phoneInput = document.getElementById("phone");
    const codeInput = document.getElementById("code");
    const sendCodeBtn = document.getElementById("sendCodeBtn");
    const verifyCodeBtn = document.getElementById("verifyCodeBtn");
    const openModalBtn = document.getElementById("openModalBtn");
    const cancelBtn = document.getElementById("cancelBtn");

    const emailError = document.getElementById("emailError");
    const phoneError = document.getElementById("phoneError");
    const codeError = document.getElementById("codeError");

    // 모달 관련 요소
    const pwModal = document.getElementById("pwModal");
    const closeModal = document.getElementById("closeModal");
    const newPasswordInput = document.getElementById("newPassword");
    const confirmPasswordInput = document.getElementById("confirmPassword");
    const newPwError = document.getElementById("newPwError");
    const confirmPwError = document.getElementById("confirmPwError");
    const changePwBtn = document.getElementById("changePwBtn");

    // 전역 변수: Firebase ID 토큰 저장
    let firebaseIdToken = null;

    // 헬퍼 함수: 에러 메시지 설정 (isError true이면 빨간색, false이면 초록색)
    const setError = (element, message, isError = true) => {
        element.textContent = message;
        element.style.color = isError ? "red" : "green";
    };

    // 정규식 검사 함수
    const isValidEmail = (email) =>
        /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(email);
    const isPasswordComplex = (pw) =>
        /^(?=.*[A-Za-z])(?=.*\d)(?=.*[!@#$%^&*])[A-Za-z\d!@#$%^&*]{8,}$/.test(pw);

    // 이메일 실시간 검사
    emailInput.addEventListener("input", () => {
        const email = emailInput.value.trim();
        if (!isValidEmail(email)) {
            displayError(emailError, "유효한 이메일 형식이 아닙니다.");
        } else {
            displayError(emailError, "", false);
        }
    });

    // 새 비밀번호 실시간 복잡도 검사
    newPasswordInput.addEventListener("input", () => {
        const newPw = newPasswordInput.value.trim();
        if (!isPasswordComplex(newPw)) {
            displayError(newPwError, "비밀번호는 최소 8자, 영문, 숫자, 특수문자를 포함해야 합니다.");
        } else {
            displayError(newPwError, "적합한 비밀번호입니다.", false);
        }
    });

    // 비밀번호 확인 실시간 검사
    confirmPasswordInput.addEventListener("input", () => {
        const newPw = newPasswordInput.value.trim();
        const confirmPw = confirmPasswordInput.value.trim();
        if (newPw !== confirmPw) {
            displayError(confirmPwError, "비밀번호가 일치하지 않습니다.");
        } else {
            displayError(confirmPwError, "비밀번호가 일치합니다.", false);
        }
    });

    // reCAPTCHA 인스턴스 생성 (invisible)
    const recaptchaContainer = document.getElementById("recaptcha-container");
    if (!recaptchaContainer) {
        console.error("recaptcha-container 요소가 없습니다.");
        return;
    }
    const recaptchaVerifier = new firebase.auth.RecaptchaVerifier(recaptchaContainer, {
        size: 'invisible',
        callback: (response) => {
            console.log("reCAPTCHA 해결됨:", response);
        },
        'expired-callback': () => {
            console.log("reCAPTCHA 만료됨");
            setError(phoneError, "reCAPTCHA가 만료되었습니다.");
        }
    });
    recaptchaVerifier.render().then(widgetId => {
        console.log("reCAPTCHA 위젯 렌더링 완료, widgetId:", widgetId);
    });

    // 공통 POST 요청 함수
    const sendPostRequest = (url, params) => {
        return fetch(url, {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: params,
        }).then(res => {
            if (!res.ok) {
                return res.text().then(text => { throw new Error(text); });
            }
            return res.text();
        });
    };

    // 1) SMS 인증번호 전송 (Firebase)
    sendCodeBtn.addEventListener("click", () => {
        // 이전 에러 지우기
        emailError.textContent = "";
        phoneError.textContent = "";
        codeError.textContent = "";
        let phone = phoneInput.value.trim();
        if (!phone) {
            phoneError.textContent = "휴대폰 번호를 입력하세요.";
            return;
        }
        // 만약 로컬 형식("01035438227")이면 국제 형식으로 변환: "+821035438227"
        if (phone.startsWith("010")) {
            phone = "+82" + phone.substring(1);
        }
        console.log("전송 전 휴대폰 (국제 형식):", phone);
        if (!/^\+82\d{9,10}$/.test(phone)) {
            phoneError.textContent = "유효한 핸드폰 번호를 입력해주세요 ex)01012345678";
            return;
        }
        firebase.auth().signInWithPhoneNumber(phone, recaptchaVerifier)
            .then(confirmationResult => {
                window.confirmationResult = confirmationResult;
                alert("인증번호가 전송되었습니다.");
            })
            .catch(error => {
                console.error("SMS 전송 에러:", error);
                phoneError.textContent = error.message;
            });
    });

    // 2) SMS 인증번호 확인 및 Firebase ID 토큰 획득
    verifyCodeBtn.addEventListener("click", () => {
        codeError.textContent = "";
        const code = codeInput.value.trim();
        if (!code) {
            codeError.textContent = "인증번호를 입력하세요.";
            return;
        }
        if (!window.confirmationResult) {
            codeError.textContent = "먼저 인증번호 전송을 진행하세요.";
            return;
        }
        window.confirmationResult.confirm(code)
            .then(result => result.user.getIdToken())
            .then(idToken => {
                firebaseIdToken = idToken;
                alert("인증번호 확인에 성공했습니다.");
                openModalBtn.disabled = false;
            })
            .catch(error => {
                console.error("인증번호 확인 실패:", error);
                codeError.textContent = error.message;
            });
    });

    // 3) 서버로 Firebase ID 토큰 전달하여 검증 (비밀번호 변경 전)
    openModalBtn.addEventListener("click", () => {
        // 이전 에러 지우기
        emailError.textContent = "";
        phoneError.textContent = "";
        const email = emailInput.value.trim();
        const phone = phoneInput.value.trim(); // DB에는 로컬 형식으로 저장됨, 예: "01035438227"
        if (!isValidEmail(email)) {
            emailError.textContent = "유효한 이메일 형식이 아닙니다.";
            return;
        }
        if (!/^(010\d{8})$/.test(phone)) {
            phoneError.textContent = "유효한 핸드폰 번호를 입력해주세요 ex)01012345678";
            return;
        }
        if (!firebaseIdToken) {
            alert("먼저 SMS 인증을 완료하세요.");
            return;
        }
        const params = new URLSearchParams({ idToken: firebaseIdToken, email, phone });
        sendPostRequest("/api/reset-password/verify-token", params)
            .then(msg => {
                alert(msg);
                if (pwModal) {
                    pwModal.style.display = "flex";
                } else {
                    console.error("pwModal 요소를 찾을 수 없습니다.");
                }
            })
            .catch(err => {
                alert("토큰 검증 실패: " + err.message);
            });
    });

    // 4) 모달 닫기 및 취소 시 로그인 페이지 이동
    const closeModalAndRedirect = () => {
        if (pwModal) {
            pwModal.style.display = "none";
        }
        window.location.href = "/login";
    };
    if (closeModal) {
        closeModal.addEventListener("click", closeModalAndRedirect);
    } else {
        console.error("closeModal 요소를 찾을 수 없습니다.");
    }
    cancelBtn.addEventListener("click", () => {
        window.location.href = "/login";
    });
    window.addEventListener("click", (e) => {
        if (pwModal && e.target === pwModal) {
            closeModalAndRedirect();
        }
    });

    // 5) 새 비밀번호 변경 처리
    changePwBtn.addEventListener("click", () => {
        // 이전 모달 에러 지우기
        newPwError.textContent = "";
        confirmPwError.textContent = "";
        const email = emailInput.value.trim();
        const newPw = newPasswordInput.value.trim();
        const confirmPw = confirmPasswordInput.value.trim();
        if (!newPw) {
            newPwError.textContent = "새 비밀번호를 입력하세요.";
            return;
        }
        if (!confirmPw) {
            confirmPwError.textContent = "비밀번호 확인을 입력하세요.";
            return;
        }
        if (newPw === confirmPw && newPw) {
            // 여기서 같은 비밀번호로 변경하지 못하도록 서버에서 체크하도록 하고,
            // 클라이언트에서는 새 비밀번호가 일치하는지와 복잡도 검사만 진행
            if (!isPasswordComplex(newPw)) {
                newPwError.textContent = "비밀번호는 최소 8자, 영문, 숫자, 특수문자를 포함해야 합니다.";
                return;
            } else {
                // POST 요청 전송
                const params = new URLSearchParams({ email, newPassword: newPw });
                sendPostRequest("/api/reset-password/new-password", params)
                    .then(msg => {
                        alert(msg);
                        closeModalAndRedirect();
                    })
                    .catch(err => {
                        confirmPwError.textContent = err.message;
                    });
            }
        } else {
            confirmPwError.textContent = "비밀번호가 일치하지 않습니다.";
        }
    });
});
