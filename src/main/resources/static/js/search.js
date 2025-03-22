document.addEventListener("DOMContentLoaded", function () {
    // DOM 요소 가져오기
    const searchInput = document.getElementById("search-input");
    const searchForm = document.getElementById("search-form");
    const recentSearchesUl = document.getElementById("recent-searches");
    const popularSearchesLeftUl = document.getElementById("popular-searches-left");
    const popularSearchesRightUl = document.getElementById("popular-searches-right");
    const closeBtn = document.getElementById("closeBtn");

    // 인기 검색어 영역 확인
    if (!popularSearchesLeftUl || !popularSearchesRightUl) {
        console.error("인기 검색어 영역(좌측, 우측)이 HTML에 정의되어 있지 않습니다.");
    }

    // 삭제된 키워드를 관리할 Set (삭제 후 re-fetch 시 필터링)
    const deletedKeywords = new Set();

    // 자동완성 목록 생성
    let autoCompleteList = document.createElement("ul");
    autoCompleteList.id = "autoCompleteList-search";
    Object.assign(autoCompleteList.style, {
        position: "absolute",
        border: "1px solid #ddd",
        backgroundColor: "#fff",
        zIndex: "9999",
        display: "none"
    });
    document.body.appendChild(autoCompleteList);

    // URL 파라미터에서 검색어 추출
    const urlParams = new URLSearchParams(window.location.search);
    const urlKeyword = urlParams.get("keyword");
    if (urlKeyword) {
        searchInput.value = urlKeyword;
    }

    // 닫기 버튼 클릭 시 메인 페이지로 이동
    if (closeBtn) {
        closeBtn.addEventListener("click", () => {
            window.location.href = "/";
        });
    }

    let searchLoggingInProgress = false;
    function saveSearchAndRedirect(keyword) {
        // 만약 새로 검색하면 해당 키워드는 삭제 목록에서 제거
        deletedKeywords.delete(keyword);
        if (searchLoggingInProgress) return;
        searchLoggingInProgress = true;
        const query = keyword.trim();
        if (!query) {
            alert("검색어를 입력하세요.");
            searchLoggingInProgress = false;
            return;
        }
        fetch("/api/save-search", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ keyword: query })
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error("검색어 저장 실패");
                }
                return response.json();
            })
            .then(data => {
                console.log("✅ 검색어 저장 완료:", query);
                window.location.href = `/plp?keyword=${encodeURIComponent(query)}`;
            })
            .catch(error => {
                console.error("❌ 검색어 저장 오류:", error);
                window.location.href = `/plp?keyword=${encodeURIComponent(query)}`;
            })
            .finally(() => {
                searchLoggingInProgress = false;
            });
    }

    if (searchForm) {
        searchForm.addEventListener("submit", function(e) {
            e.preventDefault();
            saveSearchAndRedirect(searchInput.value);
        });
    } else if (searchInput) {
        searchInput.addEventListener("keydown", function(e) {
            if (e.key === "Enter") {
                e.preventDefault();
                saveSearchAndRedirect(searchInput.value);
            }
        });
    }

    searchInput.addEventListener("input", function(e) {
        const prefix = e.target.value.trim();
        if (!prefix) {
            autoCompleteList.style.display = "none";
            return;
        }
        fetch(`/api/autocomplete?prefix=${encodeURIComponent(prefix)}`)
            .then(res => res.json())
            .then(suggestions => {
                renderAutoComplete(suggestions);
            })
            .catch(err => console.error("❌ 자동완성 오류:", err));
    });

    function renderAutoComplete(suggestions) {
        if (!suggestions || suggestions.length === 0) {
            autoCompleteList.style.display = "none";
            return;
        }
        const rect = searchInput.getBoundingClientRect();
        autoCompleteList.style.left = rect.left + "px";
        autoCompleteList.style.top = (rect.bottom + window.scrollY) + "px";
        autoCompleteList.style.width = rect.width + "px";
        autoCompleteList.innerHTML = "";
        suggestions.forEach(sugg => {
            const li = document.createElement("li");
            li.textContent = sugg;
            li.style.padding = "5px 10px";
            li.style.cursor = "pointer";
            li.addEventListener("click", () => {
                searchInput.value = sugg;
                autoCompleteList.style.display = "none";
                saveSearchAndRedirect(sugg);
            });
            autoCompleteList.appendChild(li);
        });
        autoCompleteList.style.display = "block";
    }

    searchInput.addEventListener("keypress", function (e) {
        if (e.key === "Enter") {
            e.preventDefault();
            saveSearchAndRedirect(searchInput.value);
        }
    });

    // 최근 검색어 API 호출
    fetch("/api/recent-searches", {
        method: "GET",
        headers: { "Content-Type": "application/json" }
    })
        .then(response => {
            if (!response.ok) {
                return response.json().then(err => { throw new Error(err.error || "서버 오류"); });
            }
            return response.json();
        })
        .then(data => {
            console.log("✅ 최근 검색어:", data.recentSearches);
            updateRecentSearches(data.recentSearches);
        })
        .catch(error => {
            console.error("❌ 최근 검색어 API 오류:", error);
            if (recentSearchesUl) {
                recentSearchesUl.innerHTML = `<li class="error">❌ 최근 검색어를 불러올 수 없습니다.</li>`;
            }
        });

    // 인기 검색어 API 호출
    fetch("/api/popular-searches", {
        method: "GET",
        headers: { "Content-Type": "application/json" }
    })
        .then(response => {
            if (!response.ok) {
                return response.json().then(err => { throw new Error(err.error || "서버 오류"); });
            }
            return response.json();
        })
        .then(data => {
            if (data.popularSearches) {
                console.log("✅ 인기 검색어:", data.popularSearches);
                updatePopularSearches(data.popularSearches);
            } else if (data.message) {
                console.log("인기 검색어 없음:", data.message);
                if (popularSearchesLeftUl) popularSearchesLeftUl.innerHTML = `<li class="empty">${data.message}</li>`;
                if (popularSearchesRightUl) popularSearchesRightUl.innerHTML = "";
            } else {
                console.log("예상치 못한 인기 검색어 응답:", data);
            }
        })
        .catch(error => {
            console.error("❌ 인기 검색어 API 오류:", error);
            if (popularSearchesLeftUl) popularSearchesLeftUl.innerHTML = `<li class="error">❌ 인기 검색어를 불러올 수 없습니다.</li>`;
            if (popularSearchesRightUl) popularSearchesRightUl.innerHTML = `<li class="error">❌ 인기 검색어를 불러올 수 없습니다.</li>`;
        });

    // 최근 검색어 업데이트 (캐러셀 형태)
    function updateRecentSearches(searches) {
        if (!recentSearchesUl) return;
        recentSearchesUl.innerHTML = "";
        if (!searches || searches.length === 0) {
            recentSearchesUl.innerHTML = `<li class="empty">최근 검색어가 없습니다.</li>`;
            return;
        }
        // deletedKeywords에 포함되지 않은 고유 키워드만 표시
        const uniqueSearches = Array.from(new Set(
            searches.filter(s => s && s.trim().length > 0 && !deletedKeywords.has(s))
        ));
        uniqueSearches.forEach(search => {
            const li = document.createElement("li");
            li.className = "recent-search-item";
            const button = document.createElement("button");
            button.className = "check-btn";
            const spanKeyword = document.createElement("span");
            spanKeyword.className = "keyword-part";
            spanKeyword.textContent = search;
            const deleteBtn = document.createElement("button");
            deleteBtn.className = "delete-btn";
            deleteBtn.textContent = "X";
            // X 버튼 클릭 시: UI에서 즉시 삭제하고, 서버에서 동일 키워드 모두 삭제
            deleteBtn.addEventListener("click", (e) => {
                e.stopPropagation();
                li.remove();
                deletedKeywords.add(search);
                deleteRecentSearch(search);
            });
            button.addEventListener("click", () => {
                saveSearchAndRedirect(search);
            });
            button.appendChild(spanKeyword);
            button.appendChild(deleteBtn);
            li.appendChild(button);
            recentSearchesUl.appendChild(li);
        });
    }

    // 인기 검색어 업데이트 (좌/우 두 컬럼)
    function updatePopularSearches(list) {
        if (!popularSearchesLeftUl || !popularSearchesRightUl) return;
        popularSearchesLeftUl.innerHTML = "";
        popularSearchesRightUl.innerHTML = "";
        if (!list || list.length === 0) {
            popularSearchesLeftUl.innerHTML = `<li class="empty">인기 검색어가 없습니다.</li>`;
            popularSearchesRightUl.innerHTML = "";
            return;
        }
        // 객체형이면 item.keyword, 단순 문자열이면 그대로 사용
        const processedList = list.map(item => (typeof item === "object" ? item.keyword : item));
        const firstHalf = processedList.slice(0, 5);
        const secondHalf = processedList.slice(5, 10);
        console.log("첫 번째 컬럼:", firstHalf);
        console.log("두 번째 컬럼:", secondHalf);
        firstHalf.forEach((keyword, index) => {
            const li = createPopularItem(keyword, index + 1);
            popularSearchesLeftUl.appendChild(li);
        });
        secondHalf.forEach((keyword, index) => {
            const li = createPopularItem(keyword, index + 6);
            popularSearchesRightUl.appendChild(li);
        });
    }

    function createPopularItem(keyword, rank) {
        const li = document.createElement("li");
        li.className = "popular-search-item";
        li.innerHTML = `
            <span class="rank">${rank}</span>
            <span class="keyword-span">${keyword}</span>
        `;
        li.addEventListener("click", () => {
            searchInput.value = keyword;
            saveSearchAndRedirect(keyword);
        });
        return li;
    }

    function deleteRecentSearch(searchKeyword) {
        fetch(`/api/recent-searches?keyword=${encodeURIComponent(searchKeyword)}`, {
            method: "DELETE",
            headers: { "Content-Type": "application/json" }
        })
            .then(response => {
                if (!response.ok) {
                    return response.json().then(err => { throw new Error(err.error || "검색어 삭제 실패"); });
                }
                return response.json();
            })
            .then(() => {
                // 삭제 후 최신 목록 갱신
                fetch("/api/recent-searches")
                    .then(resp => resp.json())
                    .then(data => {
                        console.log("✅ 최신 최근 검색어:", data.recentSearches);
                        updateRecentSearches(data.recentSearches);
                    })
                    .catch(err => console.error("❌ 최근 검색어 갱신 오류:", err));
            })
            .catch(error => console.error("❌ 최근 검색어 삭제 오류:", error));
    }
});
