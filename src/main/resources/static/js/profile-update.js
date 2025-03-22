document.addEventListener("DOMContentLoaded", () => {
    /********** 요소 참조 **********/
    const profileUpdateForm = document.getElementById("profileUpdateForm");
    const cancelBtn = document.getElementById("cancelBtn");
    const nickInput = document.getElementById("userName");
    const nickCheckBtn = document.getElementById("nickCheckBtn");
    const passwordInput = document.getElementById("password");
    const confirmPasswordInput = document.getElementById("confirmPassword");
    const phoneInput = document.getElementById("phone");
    const phoneValidateBtn = document.getElementById("phoneValidateBtn");
    const smsCodeInput = document.getElementById("smsCode");
    const verifySmsBtn = document.getElementById("verifySmsBtn");
    const zipcodeInput = document.getElementById("zipcode");
    const addressInput = document.getElementById("address");
    const addressDetailInput = document.getElementById("addressDetail");
    const attachFileBtn = document.getElementById("attachFileBtn");
    const uploadProfileImageBtn = document.getElementById("uploadProfileImageBtn");
    const profileImageInput = document.getElementById("profileImageInput");
    const profileImagePreview = document.getElementById("profileImagePreview");
    const profileImageHidden = document.getElementById("profileImage");

    // 에러/피드백 메시지 요소
    const nickError = document.getElementById("nickError");
    const passwordError = document.getElementById("passwordError");
    const confirmPasswordError = document.getElementById("confirmPasswordError");
    const phoneError = document.getElementById("phoneError");
    const smsCodeError = document.getElementById("smsCodeError");
    const addressError = document.getElementById("addressError");

    // 커스텀 Alert 모달 요소
    const customAlertModal = document.getElementById("customAlertModal");
    const customAlertMessage = document.getElementById("customAlertMessage");
    const customAlertConfirmBtn = document.getElementById("customAlertConfirmBtn");

    // 전역 변수
    let firebaseToken = null;
    let nickVerified = false;
    const currentNick = nickInput.value.trim(); // 현재 사용자의 기존 닉네임
    let uploadedProfileImageUrl = profileImageHidden.value; // 초기값: 기존 프로필 이미지 URL

    /********** 1. 닉네임 중복 확인 **********/
    nickCheckBtn.addEventListener("click", async () => {
        const newNick = nickInput.value.trim();
        // 현재 사용하는 닉네임이면 바로 통과
        if (newNick === currentNick) {
            nickError.textContent = "현재 사용 중인 닉네임은 사용 가능합니다.";
            nickError.style.color = "green";
            nickVerified = true;
            return;
        }
        if (newNick.length < 2) {
            nickError.textContent = "닉네임은 최소 2글자 이상이어야 합니다.";
            nickError.style.color = "red";
            nickVerified = false;
            return;
        }
        try {
            // 백엔드에 이메일 파라미터도 함께 전달하여 본인 계정이면 사용 가능하도록 함
            const res = await fetch(`/api/user/check-username?username=${encodeURIComponent(newNick)}&email=${encodeURIComponent(document.getElementById("email").value)}`);
            if (res.ok) {
                const msg = await res.text();
                nickError.textContent = msg;
                nickError.style.color = "green";
                nickVerified = true;
            } else {
                const errMsg = await res.text();
                nickError.textContent = errMsg;
                nickError.style.color = "red";
                nickVerified = false;
            }
        } catch (err) {
            nickError.textContent = "닉네임 중복 확인 오류: " + err.message;
            nickError.style.color = "red";
            nickVerified = false;
        }
    });

    /********** 2. 비밀번호 유효성 검사 및 확인 메시지 **********/
    passwordInput.addEventListener("input", () => {
        const pwRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&]).{8,}$/;
        if (passwordInput.value) {
            if (pwRegex.test(passwordInput.value)) {
                passwordError.textContent = "사용 가능한 비밀번호입니다.";
                passwordError.style.color = "green";
            } else {
                passwordError.textContent = "비밀번호는 최소 8자, 영문, 숫자, 특수문자를 포함해야 합니다.";
                passwordError.style.color = "red";
            }
        } else {
            passwordError.textContent = "";
        }
    });

    confirmPasswordInput.addEventListener("input", () => {
        if (passwordInput.value === confirmPasswordInput.value) {
            confirmPasswordError.textContent = "비밀번호가 일치합니다.";
            confirmPasswordError.style.color = "green";
        } else {
            confirmPasswordError.textContent = "비밀번호와 비밀번호 확인이 일치하지 않습니다.";
            confirmPasswordError.style.color = "red";
        }
    });

    /********** 3. 휴대폰 번호 유효성 검사 **********/
    phoneInput.addEventListener("blur", () => {
        const phoneVal = phoneInput.value.trim();
        const phoneRegex = /^\d{10,11}$/;
        if (!phoneRegex.test(phoneVal)) {
            phoneError.textContent = "유효한 핸드폰 번호를 입력하세요. (예: 01012345678)";
            phoneError.style.color = "red";
            phoneValidateBtn.disabled = true;
        } else {
            phoneError.textContent = "";
            phoneValidateBtn.disabled = false;
        }
    });

    /********** 4. 휴대폰 인증 (SMS 전송 및 확인) **********/
    function getRecaptchaVerifier() {
        const oldCaptcha = document.getElementById("recaptcha-container");
        if (oldCaptcha) {
            oldCaptcha.remove();
        }
        const newContainer = document.createElement("div");
        newContainer.id = "recaptcha-container";
        document.body.appendChild(newContainer);
        const verifier = new firebase.auth.RecaptchaVerifier("recaptcha-container", {
            size: "invisible",
            callback: (token) => {
                console.log("reCAPTCHA solved, token:", token);
            },
            "expired-callback": () => {
                phoneError.textContent = "reCAPTCHA 토큰이 만료되었습니다. 다시 시도해주세요.";
                phoneError.style.color = "red";
            }
        });
        verifier.render().then((widgetId) => {
            console.log("reCAPTCHA 렌더링 완료, widgetId:", widgetId);
        });
        return verifier;
    }

    phoneValidateBtn.addEventListener("click", async () => {
        const phoneVal = phoneInput.value.trim();
        let formattedPhone = phoneVal;
        if (phoneVal.startsWith("010")) {
            formattedPhone = "+82" + phoneVal.substring(1);
        }
        try {
            const verifier = getRecaptchaVerifier();
            await verifier.verify();
            firebase.auth().signInWithPhoneNumber(formattedPhone, verifier)
                .then((confirmationResult) => {
                    window.confirmationResult = confirmationResult;
                    phoneError.textContent = "인증번호가 전송되었습니다.";
                    phoneError.style.color = "green";
                    smsCodeInput.disabled = false;
                    verifySmsBtn.disabled = false;
                })
                .catch((error) => {
                    phoneError.textContent = "SMS 전송 실패: " + error.message;
                    phoneError.style.color = "red";
                });
        } catch (error) {
            phoneError.textContent = "reCAPTCHA 인증에 실패했습니다: " + error.message;
            phoneError.style.color = "red";
        }
    });

    verifySmsBtn.addEventListener("click", () => {
        const code = smsCodeInput.value.trim();
        if (!code) {
            smsCodeError.textContent = "인증번호를 입력하세요.";
            smsCodeError.style.color = "red";
            return;
        }
        window.confirmationResult.confirm(code)
            .then(() => {
                smsCodeError.textContent = "핸드폰 인증 완료";
                smsCodeError.style.color = "green";
                smsCodeInput.disabled = true;
                verifySmsBtn.disabled = true;
                firebase.auth().currentUser.getIdToken(true)
                    .then((token) => {
                        firebaseToken = token;
                        console.log("Firebase 토큰 획득:", token);
                    })
                    .catch((error) => {
                        smsCodeError.textContent = "토큰 획득 실패: " + error.message;
                        smsCodeError.style.color = "red";
                    });
            })
            .catch((error) => {
                smsCodeError.textContent = "인증번호가 올바르지 않습니다.";
                smsCodeError.style.color = "red";
            });
    });

    /********** 5. 주소 찾기 (Daum 우편번호 API) **********/
    const addAddressBtn = document.getElementById("addAddressBtn");
    addAddressBtn.addEventListener("click", () => {
        new daum.Postcode({
            oncomplete: function(data) {
                if (!data.jibunAddress || data.jibunAddress.trim() === "") {
                    addressError.textContent = "지번 주소가 제공되지 않습니다. 다시 선택해주세요.";
                    addressError.style.color = "red";
                    return;
                }
                zipcodeInput.value = data.zonecode;
                addressInput.value = data.jibunAddress;
                addressDetailInput.focus();
                // 새 주소 입력시 성공 메시지
                addressError.textContent = "유효한 주소입니다.";
                addressError.style.color = "green";
            }
        }).open();
    });

    /********** 6. 파일 첨부 및 프로필 이미지 업로드 및 미리보기 **********/
    attachFileBtn.addEventListener("click", () => {
        profileImageInput.click();
    });

    profileImageInput.addEventListener("change", (e) => {
        const file = e.target.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = function(evt) {
                profileImagePreview.src = evt.target.result;
            };
            reader.readAsDataURL(file);
        }
    });

    uploadProfileImageBtn.addEventListener("click", async () => {
        const file = profileImageInput.files[0];
        if (!file) {
            showCustomAlert("업로드할 이미지를 선택하세요.");
            return;
        }
        if (file.size > 3 * 1024 * 1024) {
            showCustomAlert("파일 크기는 3MB를 초과할 수 없습니다.");
            return;
        }
        if (!file.type.startsWith("image/")) {
            showCustomAlert("이미지 파일만 업로드 가능합니다.");
            return;
        }
        try {
            const formData = new FormData();
            formData.append("file", file);
            const res = await fetch("/mypage/uploadImage", {
                method: "POST",
                body: formData
            });
            if (!res.ok) {
                const errText = await res.text();
                throw new Error(errText);
            }
            const data = await res.json();
            uploadedProfileImageUrl = data.url;
            profileImageHidden.value = uploadedProfileImageUrl;
            // 업로드 성공 메시지를 메시지 컨테이너에 표시
            const msgContainer = document.getElementById("uploadMessageContainer");
            msgContainer.textContent = "이미지 업로드 성공!";
            msgContainer.style.color = "green";
        } catch (err) {
            const msgContainer = document.getElementById("uploadMessageContainer");
            msgContainer.textContent = "이미지 업로드 실패: " + err.message;
            msgContainer.style.color = "red";
        }
    });

    /********** 7. 프로필 업데이트 폼 제출 (휴대폰 인증 및 닉네임 중복 확인 필수) **********/
    profileUpdateForm.addEventListener("submit", async (e) => {
        e.preventDefault();

        // 필수 조건: 닉네임 중복 확인 완료 & 휴대폰 인증 완료 (smsCodeInput이 disabled)
        if (!nickVerified) {
            showCustomAlert("닉네임 중복 확인을 완료해야 합니다.");
            return;
        }
        if (!smsCodeInput.disabled) {
            showCustomAlert("휴대폰 인증을 완료해야 합니다.");
            return;
        }

        // 입력값 읽기
        const userName = nickInput.value.trim();
        const password = passwordInput.value.trim();
        const confirmPassword = confirmPasswordInput.value.trim();
        const phone = phoneInput.value.trim();
        const zipcode = zipcodeInput.value.trim();
        const address = addressInput.value.trim();
        const addressDetail = addressDetailInput.value.trim();
        const profileImage = profileImageHidden.value.trim();

        // 유효성 검사
        if (userName.length < 2) {
            showCustomAlert("닉네임은 최소 2글자 이상이어야 합니다.");
            return;
        }
        // 비밀번호가 입력된 경우에만 검사
        if (password) {
            const pwRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&]).{8,}$/;
            if (!pwRegex.test(password)) {
                showCustomAlert("비밀번호는 최소 8자, 영문, 숫자, 특수문자를 포함해야 합니다.");
                return;
            }
            if (password !== confirmPassword) {
                showCustomAlert("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
                return;
            }
        }
        const phoneRegex = /^\d{10,11}$/;
        if (!phoneRegex.test(phone)) {
            showCustomAlert("휴대폰 번호 형식이 올바르지 않습니다. (예: 01012345678)");
            return;
        }
        // 주소 변경 시, 모든 필드를 채워야 함 (세 칸 모두 입력되어야 함)
        if ((zipcode !== "" || address !== "" || addressDetail !== "") &&
            (zipcode === "" || address === "" || addressDetail === "")) {
            showCustomAlert("주소를 변경하려면 우편번호, 기본주소, 상세주소 모두 입력해야 합니다.");
            return;
        }

        // DTO 구성 (이메일은 수정하지 않음)
        const dto = {
            userName,
            password,
            confirmPassword,
            phone,
            zipcode,
            address,
            addressDetail,
            profileImage
        };

        try {
            const res = await fetch("/mypage/profile-update", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(dto)
            });
            if (!res.ok) {
                const errMsg = await res.text();
                throw new Error(errMsg);
            }
            const resultMsg = await res.text();
            showCustomAlert(resultMsg, () => {
                window.location.href = "/mypage";
            });
        } catch (err) {
            showCustomAlert("프로필 수정 실패: " + err.message);
        }
    });

    /********** 8. 취소 버튼 (마이페이지 이동) **********/
    if (cancelBtn) {
        cancelBtn.addEventListener("click", () => {
            window.location.href = "/mypage";
        });
    } else {
        console.error("취소 버튼을 찾을 수 없습니다.");
    }

    /********** 9. 커스텀 Alert 모달 함수 **********/
    function showCustomAlert(message, callback = null) {
        if (customAlertModal && customAlertMessage && customAlertConfirmBtn) {
            customAlertMessage.textContent = message;
            customAlertModal.style.display = "flex";
            customAlertConfirmBtn.onclick = () => {
                customAlertModal.style.display = "none";
                if (callback) callback();
            };
        } else {
            alert(message);
            if (callback) callback();
        }
    }
});