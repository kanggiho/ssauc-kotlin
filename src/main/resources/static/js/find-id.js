document.addEventListener("DOMContentLoaded", function() {
    // 요소 참조
    const userNameInput = document.getElementById("userName");
    const phoneInput = document.getElementById("phone");
    const codeInput = document.getElementById("code");
    const sendCodeBtn = document.getElementById("sendCodeBtn");
    const verifyCodeBtn = document.getElementById("verifyCodeBtn");
    const findIdBtn = document.getElementById("findIdBtn");
    const cancelBtn = document.getElementById("cancelBtn");
    const resultModal = document.getElementById("resultModal");
    const userEmailSpan = document.getElementById("userEmail");
    const closeModal = document.getElementById("closeModal");
    const modalConfirmBtn = document.getElementById("modalConfirmBtn");

    // 에러 표시용 span 요소
    const userNameError = document.getElementById("userNameError");
    const phoneError = document.getElementById("phoneError");
    const codeError = document.getElementById("codeError");

    // 전역 변수: Firebase ID 토큰과 최종 이메일
    let firebaseIdToken = null;
    let foundEmail = null;

    // 에러 메시지 설정 함수: isError true이면 빨간색, false이면 초록색
    function setError(element, message, isError = true) {
        console.log("setError:", element.id, message);
        element.textContent = message;
        element.style.color = isError ? "red" : "green";
    }

    // reCAPTCHA 인스턴스 생성 (invisible)
    const recaptchaVerifier = new firebase.auth.RecaptchaVerifier('recaptcha-container', {
        size: 'invisible',
        callback: function(response) {
            console.log("reCAPTCHA 해결됨:", response);
        },
        'expired-callback': function() {
            console.log("reCAPTCHA 만료됨");
            setError(phoneError, "reCAPTCHA가 만료되었습니다.");
        }
    });

    recaptchaVerifier.render().then(widgetId => {
        console.log("reCAPTCHA 위젯 렌더링 완료, widgetId:", widgetId);
    });

    /***********************************************
     * 1. 닉네임 실시간 검증 (2글자 이상)
     ***********************************************/
    userNameInput.addEventListener("blur", function() {
        const nick = userNameInput.value.trim();
        console.log("닉네임 입력:", nick);
        if (nick.length < 2) {
            setError(userNameError, "닉네임은 두 글자 이상이어야 합니다.");
        } else {
            setError(userNameError, "", false);
        }
    });

    /***********************************************
     * 2. 휴대폰 번호 유효성 검사 (010XXXXXXXX 형식)
     ***********************************************/
    phoneInput.addEventListener("blur", function() {
        const phone = phoneInput.value.trim();
        console.log("휴대폰 번호 입력:", phone);
        const phoneRegex = /^010\d{8}$/; // 010 + 8자리 숫자
        if (!phoneRegex.test(phone)) {
            setError(phoneError, "유효한 핸드폰 번호를 입력해주세요 ex)01012345678");
            sendCodeBtn.disabled = true;
        } else {
            setError(phoneError, "", false);
            sendCodeBtn.disabled = false;
        }
    });

    /***********************************************
     * 3. SMS 인증번호 전송 (Firebase)
     ***********************************************/
    sendCodeBtn.addEventListener("click", function() {
        setError(phoneError, "");
        setError(codeError, "");
        const phone = phoneInput.value.trim();
        console.log("SMS 전송 버튼 클릭 - 휴대폰 번호:", phone);
        if (!phone) {
            setError(phoneError, "휴대폰 번호를 입력하세요.");
            return;
        }
        const phoneRegex = /^010\d{8}$/;
        if (!phoneRegex.test(phone)) {
            setError(phoneError, "유효한 핸드폰 번호를 입력해주세요 ex)01012345678");
            return;
        }
        // Firebase에 전송할 번호: "010XXXXXXXX" -> "+8210XXXXXXXX"
        let firebasePhone = phone;
        if (phone.startsWith("010")) {
            firebasePhone = "+82" + phone.substring(1);
        }
        console.log("Firebase 전송할 번호:", firebasePhone);
        firebase.auth().signInWithPhoneNumber(firebasePhone, recaptchaVerifier)
            .then(confirmationResult => {
                window.confirmationResult = confirmationResult;
                console.log("SMS 인증번호 전송 성공:", confirmationResult);
                setError(phoneError, "인증번호가 전송되었습니다.", false);
            })
            .catch(error => {
                console.error("SMS 전송 에러:", error);
                setError(phoneError, error.message);
            });
    });

    /***********************************************
     * 4. SMS 인증번호 확인 (Firebase)
     ***********************************************/
    verifyCodeBtn.addEventListener("click", function() {
        setError(codeError, "");
        const code = codeInput.value.trim();
        console.log("SMS 인증번호 입력:", code);
        if (!code) {
            setError(codeError, "인증번호를 입력하세요.");
            return;
        }
        if (!window.confirmationResult) {
            setError(codeError, "먼저 인증번호 전송을 진행하세요.");
            return;
        }
        window.confirmationResult.confirm(code)
            .then(result => result.user.getIdToken())
            .then(idToken => {
                firebaseIdToken = idToken;
                console.log("인증번호 확인 성공, Firebase ID Token:", firebaseIdToken);
                setError(codeError, "인증번호 확인에 성공했습니다.", false);
                findIdBtn.disabled = false;
            })
            .catch(error => {
                console.error("인증번호 확인 실패:", error);
                setError(codeError, error.message);
            });
    });

    /***********************************************
     * 5. 아이디 찾기 버튼 클릭
     ***********************************************/
    findIdBtn.addEventListener("click", function() {
        console.log("아이디 찾기 버튼 클릭");
        // 인증 여부 확인
        if (!firebaseIdToken) {
            alert("먼저 인증번호 확인을 완료하세요.");
            return;
        }

        // 닉네임 검사
        const userName = userNameInput.value.trim();
        console.log("아이디 찾기 - 닉네임:", userName);
        if (!userName) {
            setError(userNameError, "닉네임을 입력하세요.");
            return;
        } else if (userName.length < 2) {
            setError(userNameError, "닉네임은 두 글자 이상이어야 합니다.");
            return;
        }

        // 휴대폰 번호 (로컬 형식: 010XXXXXXXX)
        const phone = phoneInput.value.trim();
        console.log("아이디 찾기 - 휴대폰 번호 (원본):", phone);
        if (!/^010\d{8}$/.test(phone)) {
            setError(phoneError, "유효한 휴대폰 번호를 입력해주세요.");
            return;
        }

        // 클라이언트에서는 변환 없이 로컬 형식 그대로 전송
        console.log("아이디 찾기 - 전송할 전화번호 (로컬 형식):", phone);

        const params = new URLSearchParams({
            idToken: firebaseIdToken,
            phone: phone,  // 변환하지 않고 로컬 번호 그대로 전송
            userName: userName
        });

        fetch("/api/find-id/verify-token", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: params
        })
            .then(response => {
                console.log("서버 응답 상태:", response.status);
                if (!response.ok) {
                    return response.text().then(text => { throw new Error(text); });
                }
                return response.text();
            })
            .then(email => {
                console.log("서버 응답 email:", email);
                if (!email || email.trim() === "" || email.trim().toLowerCase() === "null") {
                    alert("해당 닉네임과 휴대폰 번호로 가입된 아이디가 없습니다.");
                    resultModal.style.display = "none";
                } else {
                    foundEmail = email;
                    userEmailSpan.textContent = foundEmail;
                    resultModal.style.display = "flex";
                }
            })
            .catch(error => {
                console.error("아이디 찾기 실패:", error);
                alert("아이디 찾기 실패: " + error.message);
            });
    });

    /***********************************************
     * 6. 취소 버튼 → 로그인 페이지로 이동
     ***********************************************/
    cancelBtn.addEventListener("click", function() {
        console.log("취소 버튼 클릭 - 로그인 페이지로 이동");
        window.location.href = "/login";
    });

    /***********************************************
     * 7. 모달 닫기 (닫기 아이콘 & 확인 버튼)
     ***********************************************/
    closeModal.addEventListener("click", function() {
        console.log("모달 닫기 클릭 - 로그인 페이지로 이동");
        resultModal.style.display = "none";
        window.location.href = "/login";
    });
    modalConfirmBtn.addEventListener("click", function() {
        console.log("모달 확인 버튼 클릭 - 로그인 페이지로 이동");
        resultModal.style.display = "none";
        window.location.href = "/login";
    });

    // 모달 외부 클릭 시 닫기 (옵션)
    window.addEventListener("click", function(event) {
        if (event.target === resultModal) {
            console.log("모달 외부 클릭 - 모달 닫힘");
            resultModal.style.display = "none";
        }
    });
});
