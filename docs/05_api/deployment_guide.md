# Firebase Functions 배포 가이드

Firebase Functions AI 엔드포인트(`/summary`, `/quiz`)를 로컬 에뮬레이터 또는 Firebase 클라우드에 배포하는 절차를 설명한다.

---

## 사전 요구사항

- Node.js 22
- Firebase CLI (`npm install -g firebase-tools`)
- Firebase 프로젝트 및 `google-services.json` 발급 완료
- OpenAI API 키 발급 완료

---

## 1. OPENAI_API_KEY Secret 등록

Firebase Secret Manager에 키를 등록한다. 이 키는 `functions/index.js`에서 `defineSecret("OPENAI_API_KEY")`로 참조하며, 코드나 Git에 노출되지 않는다.

배포 전에 Google Cloud Console에서 다음 API가 활성화되어 있어야 한다.

- Cloud Functions API
- Cloud Run API
- Cloud Build API
- Secret Manager API

Firebase CLI 로그인 계정에는 해당 프로젝트의 Functions 조회/배포 권한이 필요하다.

```bash
# Firebase 프로젝트에 로그인
firebase login

# Secret 등록 (값을 직접 입력하거나 파이프로 전달)
firebase functions:secrets:set OPENAI_API_KEY
# 프롬프트에 API 키 값을 입력 후 Enter
```

등록 확인:

```bash
firebase functions:secrets:access OPENAI_API_KEY
```

---

## 2. Firebase Functions 배포

```bash
cd functions
npm install

# 배포
npm run deploy
# 또는
firebase deploy --only functions:api
```

`api` 함수는 `asia-northeast3` 리전에 배포되도록 코드에 명시되어 있다.
배포 완료 후 터미널에 출력되는 Functions URL을 복사한다.

```
✔  functions[api]: Deployed.
Function URL (api): https://api-<project-number>.asia-northeast3.run.app
```

---

## 3. Android ai.base.url 설정

Functions 배포 후 출력되는 `api` HTTPS 엔드포인트를 확인하고, 프로젝트 루트의 `local.properties`(Git에 포함되지 않음)에 추가한다.

```properties
ai.base.url=https://api-<project-number>.asia-northeast3.run.app
```

### 배포 직후 엔드포인트 검증

Android 설정 전에 반드시 다음 두 요청을 실행한다.

```bash
curl -i https://api-<project-number>.asia-northeast3.run.app/health
```

정상 응답:

```json
{"service":"studymate-api","status":"ok"}
```

인증 없이 AI 엔드포인트를 호출하면 반드시 `401`과 JSON 오류가 반환되어야 한다.

```bash
curl -i -X POST \
  https://api-<project-number>.asia-northeast3.run.app/summary \
  -H "Content-Type: application/json" \
  -d '{"text":"test"}'
```

정상 응답:

```json
{"error":"인증이 필요합니다."}
```

`200 Hello World!`가 반환되면 Firebase Functions 코드가 아닌 Cloud Run 샘플
리비전에 트래픽이 연결된 것이다. Cloud Run 콘솔에서 `api` 서비스의 최신 리비전과
트래픽을 확인한 뒤 `firebase deploy --only functions:api`를 다시 실행한다.

> **로컬 에뮬레이터 사용 시**
> ```bash
> cd functions && npm run serve
> ```
> `local.properties`에 에뮬레이터 주소를 입력한다. 실제 Android 기기에서 접근할 경우 `localhost` 대신 PC의 로컬 IP를 사용한다.
> ```properties
> ai.base.url=http://127.0.0.1:5001/<project-id>/us-central1/api
> ```

Android Studio에서 **Build > Clean Project → Rebuild Project** 후 실행해야 `BuildConfig.AI_BASE_URL`에 값이 반영된다.

> `ai.base.url`이 비어 있으면 앱은 AI 요청을 보내지 않고
> "AI 서버 주소가 설정되지 않았습니다" 오류를 표시한다. 발표 또는 실기기 QA 전에는
> 반드시 배포 URL 입력과 리빌드를 먼저 완료한다.

---

## 실제 Android 기기에서 Firebase 로그인 확인

이메일/비밀번호 로그인은 로컬 서버를 사용하지 않고 Firebase Auth에 직접 연결된다.
에뮬레이터에서는 되지만 다른 기기에서 실패하면 다음 항목을 확인한다.

1. Firebase Console > Authentication > Sign-in method에서 `Email/Password`가 활성화되어 있는지 확인한다.
2. Firebase Console > Project settings > Your apps의 Android 패키지명이
   `com.example.studymate`인지 확인한다.
3. 앱의 `app/google-services.json`이 같은 Firebase 프로젝트
   (`studymate-f03e7`)에서 내려받은 최신 파일인지 확인한다.
4. Google Cloud Console에서 API 키에 Android 앱 제한을 설정했다면 패키지명과
   현재 APK를 서명한 인증서의 SHA-1을 모두 등록한다.
5. 다른 PC에서 debug APK를 빌드하면 debug keystore가 달라지므로 그 PC의 SHA-1도
   등록하거나 동일한 서명 키로 빌드한다.

현재 debug 인증서의 SHA 값은 다음 명령으로 확인할 수 있다.

```powershell
.\gradlew.bat signingReport
```

실패 원문과 Firebase 오류 코드는 Android Studio Logcat에서 `AuthService`로
필터링해 확인한다.

### 팀 공용 debug 서명

팀원이 동일한 SHA 인증서로 debug APK를 만들려면 공용 `team-debug.keystore`를
프로젝트 루트의 `key/` 폴더에 둔다. 기존 루트 위치도 호환되지만 `key/` 폴더를 권장한다.
키 파일과 `keystore.properties`는 Git에 커밋하지 않고
팀 내부의 안전한 채널로 공유한다.

새 PC에서는 다음 순서로 설정한다.

1. 전달받은 `team-debug.keystore`를 `app` 폴더와 동일위치에 둔다.
2. 전달받은 `keystore.properties`를 `app` 폴더와 동일위치에 둔다.

```properties
storePassword=<팀 공용 키 저장소 비밀번호>
keyAlias=androiddebugkey
keyPassword=<팀 공용 키 비밀번호>
```

3. Android Studio에서 평소처럼 Run 또는 Build한다.
4. `.\gradlew.bat signingReport`에서 팀 공용 SHA-1이 출력되는지 확인한다.

두 파일이 모두 없으면 해당 PC의 기본 debug keystore를 사용한다. 한 파일만
있거나 필수 설정값이 누락되면 다른 SHA로 빌드되는 실수를 막기 위해 빌드가
실패한다.
운영 release keystore는 공용 debug 키와 반드시 분리한다.

---

## 4. 엔드포인트 보호 정책

`functions/index.js`에 적용된 보호 정책:

| 정책 | 내용 |
|---|---|
| Firebase Auth 검증 | 모든 요청에 `Authorization: Bearer <Firebase ID Token>` 헤더 필수. 앱은 `AiService`에서 자동으로 첨부. |
| 이메일 인증 검증 | ID Token의 `email_verified`가 `true`인 사용자만 AI 엔드포인트 호출 가능. |
| 입력 길이 제한 | `text` 필드 최대 5,000자. 초과 시 400 응답. |
| 사용자별 호출 제한 | `/summary`, `/quiz` 합산 10분당 12회. 초과 시 `429`와 `Retry-After` 반환. |
| 인스턴스 제한 | `maxInstances: 3`, `concurrency: 20`으로 트래픽 급증 비용을 제한. |
| Secret Manager | `OPENAI_API_KEY`는 코드에 하드코딩하지 않고 Secret Manager로 관리. |

호출 제한 문서는 `ai_rate_limits/{uid}`에 저장된다. Admin SDK만 접근하며
클라이언트는 Firestore Rules의 기본 거부 정책으로 읽거나 수정할 수 없다.

---

## 5. 최종 확인 체크리스트

- [ ] `OPENAI_API_KEY` Secret이 Firebase 프로젝트에 등록되어 있는가
- [ ] `firebase deploy --only functions:api` 성공 여부 확인
- [ ] 배포 로그에서 런타임이 `Node.js 22`인지 확인
- [ ] `local.properties`에 `ai.base.url` 값이 입력되어 있는가
- [ ] Android Studio에서 Rebuild 후 앱 실행 → 학습 내용 입력 → 요약/퀴즈 생성 정상 동작 확인
