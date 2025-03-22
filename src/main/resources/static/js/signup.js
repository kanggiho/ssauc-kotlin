/***********************************************
 * 전역 변수 및 CSRF (옵션)
 ***********************************************/
let firebaseToken = null;
const csrfTokenMeta = document.querySelector('meta[name="_csrf"]');
const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');
let csrfToken = csrfTokenMeta ? csrfTokenMeta.getAttribute("content") : "";
let csrfHeader = csrfHeaderMeta ? csrfHeaderMeta.getAttribute("content") : "";

/***********************************************
 * 요소 참조
 ***********************************************/
const signupForm = document.getElementById('signupForm');
const emailInput = document.getElementById("email");
const emailCheckBtn = document.getElementById("emailCheckBtn");
const nickInput = document.getElementById("userName");
const nickCheckBtn = document.getElementById("nickCheckBtn");
const passwordInput = document.getElementById("password");
const confirmPasswordInput = document.getElementById("confirmPassword");
const phoneInput = document.getElementById("phone");
const phoneValidateBtn = document.getElementById("phoneValidateBtn");
const smsCodeInput = document.getElementById("smsCode");
const verifySmsBtn = document.getElementById("verifySmsBtn");

const emailError = document.getElementById("emailError");
const nickError = document.getElementById("userNameError");
const passwordError = document.getElementById("passwordError");
const confirmPasswordError = document.getElementById("confirmPasswordError");
const phoneError = document.getElementById("phoneError");
const smsCodeError = document.getElementById("smsCodeError");
const passwordFeedback = document.getElementById("passwordFeedback");
const confirmFeedback = document.getElementById("confirmFeedback");
const cancelBtn = document.getElementById("cancelBtn");

// 모달 요소 참조
const successModal = document.getElementById("successModal");
const modalMessage = document.getElementById("modalMessage");
const modalConfirmBtn = document.getElementById("modalConfirmBtn");

// 이메일/닉네임 중복 확인 플래그
let emailVerified = false;
let nickVerified = false;

// 전역 reCAPTCHA 인스턴스 변수
let recaptchaVerifier = null;

/***********************************************
 * 0. 유틸리티 함수
 ***********************************************/
async function callApi(endpoint, method = 'GET', body = null) {
    const url = '/api/user' + endpoint;
    const options = {
        method,
        headers: {
            'Content-Type': 'application/json',
            ...(csrfHeader && csrfToken ? { [csrfHeader]: csrfToken } : {})
        },
        ...(body ? { body: JSON.stringify(body) } : {})
    };

    try {
        const response = await fetch(url, options);
        if (!response.ok) {
            const text = await response.text();
            throw new Error(text);
        }
        return await response.text();
    } catch (error) {
        console.error(`API 호출 실패 (${method} ${url}):`, error);
        throw error;
    }
}

function displayError(element, message, isSuccess = false) {
    element.textContent = message;
    element.style.color = isSuccess ? "green" : "red";
}

/***********************************************
 * 1. 동적 reCAPTCHA 컨테이너 생성 및 인스턴스 생성
 ***********************************************/
function createNewRecaptchaContainer() {
    const oldContainer = document.getElementById('recaptcha-container');
    if (oldContainer) {
        oldContainer.parentNode.removeChild(oldContainer);
    }
    const newContainer = document.createElement('div');
    newContainer.id = 'recaptcha-container';
    document.body.appendChild(newContainer);
    return newContainer;
}

function getRecaptchaVerifier() {
    createNewRecaptchaContainer();
    recaptchaVerifier = new firebase.auth.RecaptchaVerifier('recaptcha-container', {
        size: 'invisible',
        callback: (token) => {
            console.log("✅ [reCAPTCHA] 해결 완료, 토큰:", token);
        },
        'expired-callback': () => {
            console.log("⚠️ [reCAPTCHA] 토큰 만료됨");
            displayError(phoneError, "reCAPTCHA 토큰이 만료되었습니다. 다시 시도해주세요.");
        }
    });
    recaptchaVerifier.render()
        .then((widgetId) => {
            console.log("✅ [reCAPTCHA] 렌더링 완료, widgetId:", widgetId);
        })
        .catch((error) => {
            console.error("❌ [reCAPTCHA] 렌더링 실패:", error);
            displayError(phoneError, "reCAPTCHA 렌더링에 실패했습니다.");
        });
    return recaptchaVerifier;
}

/***********************************************
 * 2. 이메일 중복 확인
 ***********************************************/
emailCheckBtn.addEventListener("click", async () => {
    const emailVal = emailInput.value.trim();
    emailError.textContent = "";
    emailVerified = false;
    if (!/^[A-Za-z0-9+_.-]+@(.+)$/.test(emailVal)) {
        emailError.textContent = "유효한 이메일 형식이 아닙니다.";
        return;
    }
    try {
        const res = await fetch(`/api/user/check-email?email=${encodeURIComponent(emailVal)}`);
        if (!res.ok) {
            throw new Error(await res.text());
        }
        const msg = await res.text(); // "사용 가능한 이메일입니다."
        emailError.textContent = msg;
        emailError.style.color = "green";
        emailVerified = true;
    } catch (err) {
        emailError.textContent = err.message;
        emailError.style.color = "red";
    }
});


/***********************************************
 * 3. 닉네임 중복 확인
 ***********************************************/
nickCheckBtn.addEventListener("click", async function () {
    const nick = nickInput.value.trim();
    if (nick.length < 2) {
        displayError(nickError, "닉네임은 최소 2글자 이상이어야 합니다.");
        nickVerified = false;
        return;
    }
    console.log("닉네임 중복 확인 요청, nick:", nick);
    try {
        const msg = await callApi(`/check-username?username=${encodeURIComponent(nick)}`);
        displayError(nickError, msg, true);
        nickVerified = true;
    } catch (err) {
        console.error("닉네임 중복 확인 에러:", err);
        displayError(nickError, err.message);
        nickVerified = false;
    }
});

/***********************************************
 * 4. 비밀번호 유효성 및 확인
 ***********************************************/
passwordInput.addEventListener("input", function () {
    const pwRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$/;
    if (pwRegex.test(passwordInput.value)) {
        passwordFeedback.textContent = "적합한 비밀번호입니다.";
        passwordFeedback.style.color = "green";
    } else {
        passwordFeedback.textContent = "비밀번호는 최소 8자, 영문, 숫자, 특수문자를 포함해야 합니다.";
        passwordFeedback.style.color = "red";
    }
});

confirmPasswordInput.addEventListener("input", function () {
    if (passwordInput.value === confirmPasswordInput.value) {
        confirmFeedback.textContent = "비밀번호가 일치합니다.";
        confirmFeedback.style.color = "green";
    } else {
        confirmFeedback.textContent = "비밀번호가 일치하지 않습니다.";
        confirmFeedback.style.color = "red";
    }
});

/***********************************************
 * 5. 휴대폰 번호 유효성 검사 (표시되는 값 그대로)
 ***********************************************/
phoneInput.addEventListener("blur", function () {
    console.log("휴대폰 번호 검사, phone:", phoneInput.value.trim());
    const finalRegex = /^\d{10,11}$/;
    if (!finalRegex.test(phoneInput.value.trim())) {
        displayError(phoneError, "유효한 핸드폰 번호를 입력하세요. (예: 01012345678)");
        phoneValidateBtn.disabled = true;
    } else {
        displayError(phoneError, "");
        phoneValidateBtn.disabled = false;
    }
});

/***********************************************
 * 6. 인증번호 요청 (Firebase SMS 전송)
 * 전송 시에만 입력된 휴대폰 번호를 +82 접두사 붙여 처리
 ***********************************************/
async function requestSmsCode() {
    let displayedPhone = phoneInput.value.trim();
    let formattedPhone = displayedPhone;
    if (displayedPhone.startsWith("010")) {
        formattedPhone = "+82" + displayedPhone.substring(1);
    }
    console.log("📌 [SMS] 요청 시작, formattedPhone:", formattedPhone);
    try {
        const verifier = getRecaptchaVerifier();
        const recaptchaToken = await verifier.verify();
        console.log("✅ [reCAPTCHA] 토큰 새로 발급 완료:", recaptchaToken);
        firebase.auth().signInWithPhoneNumber(formattedPhone, verifier)
            .then(function (confirmationResult) {
                window.confirmationResult = confirmationResult;
                console.log("📩 [SMS] 인증번호 전송 성공");
                displayError(phoneError, "인증번호가 전송되었습니다.", true);
                smsCodeInput.disabled = false;
                verifySmsBtn.disabled = false;
            })
            .catch(function (error) {
                console.error("🚨 [SMS] 전송 실패:", error);
                displayError(phoneError, "SMS 전송에 실패했습니다: " + error.message);
            });
    } catch (error) {
        console.error("❌ [reCAPTCHA] 토큰 생성 실패:", error);
        displayError(phoneError, "reCAPTCHA 인증에 실패했습니다: " + error.message);
    }
}

phoneValidateBtn.addEventListener("click", function () {
    requestSmsCode();
});

/***********************************************
 * 7. 인증번호 검증
 ***********************************************/
verifySmsBtn.addEventListener("click", function () {
    const code = smsCodeInput.value.trim();
    if (!code) {
        displayError(smsCodeError, "인증번호를 입력하세요.");
        return;
    }
    console.log("📌 [SMS] 인증번호 확인 요청, 입력값:", code);
    window.confirmationResult.confirm(code)
        .then(function (result) {
            displayError(smsCodeError, "핸드폰 인증 완료", true);
            smsCodeInput.disabled = true;
            verifySmsBtn.disabled = true;
            firebase.auth().currentUser.getIdToken(true)
                .then(function (token) {
                    firebaseToken = token;
                    console.log("🔑 [Firebase] 토큰 획득 성공:", firebaseToken);
                })
                .catch(function (error) {
                    console.error("🚨 [Firebase] 토큰 획득 실패:", error);
                    displayError(smsCodeError, "토큰 획득에 실패했습니다: " + error.message);
                });
        })
        .catch(function (error) {
            console.error("❌ [SMS] 인증 실패:", error);
            displayError(smsCodeError, "인증번호가 올바르지 않습니다.");
        });
});

/***********************************************
 * 8. 회원가입 폼 제출 (주소 입력 필드 포함)
 ***********************************************/
signupForm.addEventListener("submit", async function (e) {
    e.preventDefault();

    // 주소 입력값 가져오기 및 검증
    const zipcode = document.getElementById("zipcode").value.trim();
    const address = document.getElementById("address").value.trim();
    const addressDetail = document.getElementById("addressDetail").value.trim();
    if (zipcode === "" || address === "" || addressDetail === "") {
        displayError(document.getElementById("addressError"), "우편번호, 주소, 상세주소 모두 입력해주세요.");
        return;
    }

    // 나머지 필드 값 가져오기
    const email = emailInput.value.trim();
    const userName = nickInput.value.trim();
    const password = passwordInput.value.trim();
    const confirmPassword = confirmPasswordInput.value.trim();
    let phone = phoneInput.value.trim();
    const smsCode = smsCodeInput.value.trim();

    let hasError = false;

    if (!email) {
        displayError(emailError, '이메일을 입력해주세요.');
        hasError = true;
    } else if (!/^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(email)) {
        displayError(emailError, '유효한 이메일 주소를 입력하세요.');
        hasError = true;
    } else {
        displayError(emailError, '');
    }

    if (!userName) {
        displayError(nickError, '닉네임을 입력해주세요.');
        hasError = true;
    } else {
        displayError(nickError, '');
    }

    if (!password) {
        displayError(passwordError, '비밀번호를 입력해주세요.');
        hasError = true;
    } else if (!/^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$/.test(password)) {
        displayError(passwordError, '비밀번호는 최소 8자, 영문, 숫자, 특수문자를 포함해야 합니다.');
        hasError = true;
    } else {
        displayError(passwordError, '');
    }

    if (!confirmPassword) {
        displayError(confirmPasswordError, '비밀번호 확인을 입력해주세요.');
        hasError = true;
    } else if (password !== confirmPassword) {
        displayError(confirmPasswordError, '비밀번호가 일치하지 않습니다.');
        hasError = true;
    } else {
        displayError(confirmPasswordError, '');
    }

    if (!phone) {
        displayError(phoneError, '휴대폰 번호를 입력해주세요.');
        hasError = true;
    } else if (!/^\d{10,11}$/.test(phone)) {
        displayError(phoneError, '유효한 핸드폰 번호를 입력하세요. (예: 01012345678)');
        hasError = true;
    } else {
        displayError(phoneError, '');
    }

    if (!smsCode) {
        displayError(smsCodeError, '인증번호를 입력해주세요.');
        hasError = true;
    } else {
        displayError(smsCodeError, '');
    }

    if (!emailVerified) {
        displayError(emailError, '이메일 중복 확인이 필요합니다.');
        hasError = true;
    }
    if (!nickVerified) {
        displayError(nickError, '닉네임 중복 확인이 필요합니다.');
        hasError = true;
    }
    if (!smsCodeInput.disabled) {
        displayError(smsCodeError, '핸드폰 인증을 완료해주세요.');
        hasError = true;
    }
    if (!firebaseToken) {
        displayError(smsCodeError, 'Firebase 토큰이 확인되지 않았습니다. 휴대폰 인증을 다시 시도해주세요.');
        hasError = true;
    }

    if (hasError) return;

    const userData = {
        email,
        userName,
        password,
        confirmPassword,
        phone,
        smsCode,
        firebaseToken,
        zipcode,       // 주소: 우편번호
        address,       // 주소: 기본주소
        addressDetail  // 주소: 상세주소
    };

    console.log("회원가입 데이터:", userData);
    try {
        const result = await callApi('/register', 'POST', userData);
        showSuccessModal(result);
    } catch (error) {
        displayError(emailError, error.message);
    }
});

/***********************************************
 * 9. 취소 버튼 -> 로그인 페이지로
 ***********************************************/
if (cancelBtn) {
    cancelBtn.addEventListener('click', function () {
        window.location.href = '/login';
    });
}

/***********************************************
 * 10. 페이지 로드시 reCAPTCHA 생성
 ***********************************************/
document.addEventListener('DOMContentLoaded', function() {
    getRecaptchaVerifier();
});

/***********************************************
 * 11. 주소 입력 처리 (Daum Postcode API 활용)
 ***********************************************/
document.addEventListener("DOMContentLoaded", function () {
    document.getElementById("addAdressBtn").addEventListener("click", function () {
        new daum.Postcode({
            oncomplete: function (data) {
                if (!data.jibunAddress || data.jibunAddress.trim() === "") {
                    displayError(document.getElementById("zipcode"), "지번 주소가 제공되지 않습니다. 다시 선택해주세요.");
                    return;
                }
                var addr = data.jibunAddress;
                document.getElementById("zipcode").value = data.zonecode;
                document.getElementById("address").value = addr;
                document.getElementById("addressDetail").focus();
            }
        }).open();
    });
    // 주소 입력 후 폼 제출 시, 주소 필드 비활성화 처리 (중복 실행 주의)
    signupForm.addEventListener("submit", function (e) {
        var zipcode = document.getElementById("zipcode").value.trim();
        var address = document.getElementById("address").value.trim();
        var addressDetail = document.getElementById("addressDetail").value.trim();
        if (zipcode === "" || address === "" || addressDetail === "") {
            displayError(document.getElementById("addressError"), "우편번호, 주소, 상세주소 모두 입력해주세요.");
            e.preventDefault();
            return;
        }
        document.getElementById("zipcode").setAttribute("disabled", "true");
        document.getElementById("address").setAttribute("disabled", "true");
        document.getElementById("addressDetail").setAttribute("disabled", "true");
        document.getElementById("zipcode").classList.add("disabled-input");
        document.getElementById("address").classList.add("disabled-input");
        document.getElementById("addressDetail").classList.add("disabled-input");
        document.getElementById("addAdressBtn").disabled = true;
    });
});

/***********************************************
 * 회원가입 성공 시 모달 표시 (모달이 없으면 alert 대체)
 ***********************************************/
function showSuccessModal(message) {
    const successModal = document.getElementById("successModal");
    const modalMessage = document.getElementById("modalMessage");
    const modalConfirmBtn = document.getElementById("modalConfirmBtn");

    if (successModal && modalMessage && modalConfirmBtn) {
        console.log("✅ 모달 표시: ", message);
        modalMessage.textContent = message;
        successModal.style.display = "flex";

        // 기존 이벤트 리스너 제거 후 추가 (중복 실행 방지)
        modalConfirmBtn.onclick = () => {
            successModal.style.display = "none";
            window.location.href = "/login";
        };
    } else {
        // 모달 요소를 찾을 수 없으면 alert 대체
        console.warn("⚠️ 모달 요소를 찾을 수 없어 alert 대체");
        alert(message);
        window.location.href = "/login";
    }
}

