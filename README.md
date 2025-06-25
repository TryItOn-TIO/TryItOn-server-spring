# TryItOn-server-spring
[TryItOn] 서버(백엔드) 스프링 부트 레포지토리입니다.  

---

## 🎯 Commit Convention

| 태그       | 설명                                           |
| ---------- | ---------------------------------------------- |
| `feat`     | 새로운 기능 추가                               |
| `fix`      | 버그 수정                                      |
| `docs`     | 문서 수정 (README 등)                          |
| `style`    | 코드 포맷팅, 세미콜론 누락 등 (기능 변경 없음) |
| `refactor` | 코드 리팩토링 (기능 변경 없음)                 |
| `test`     | 테스트 코드 추가 및 리팩토링                   |
| `chore`    | 빌드 설정, 패키지 매니저 등 기타 변경          |

💡 **예시**

```bash
git commit -m "feat: 로그인 페이지 UI 구현"
git commit -m "fix: 상품 상세 페이지 오류 수정"
```

---

## 🧪 필요한 개발 도구  

### `IntelliJ` 사용 시 `Lombok` 플러그인 설치

```declarative
Settings -> Plugins -> Marketplace에서 lombok 검색 -> install
```

---

## 🚀 프로젝트 실행 방법

### 1. 레포지토리 클론

```bash
git clone https://github.com/TryItOn-TIO/TryItOn-server-spring.git
cd TryItOn-server-spring
```

### 2. 패키지 설치 (Build 및 의존성 다운로드)

```bash
./gradlew build
```

### 3. 개발 서버 실행 후 브라우저에 접속

`CoreApplication.java` 실행 후 아래 주소에서 실행 확인 :  

```declarative
localhost:8080
```

**Whitelabel Error Page**가 뜨면 됩니다.
