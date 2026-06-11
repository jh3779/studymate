# Firebase Functions 배포 가이드

Firebase Functions AI 엔드포인트(`/summary`, `/quiz`)를 로컬 에뮬레이터 또는 Firebase 클라우드에 배포하는 절차를 설명한다.

---

## 사전 요구사항

- Node.js 20 이상
- Firebase CLI (`npm install -g firebase-tools`)
- Firebase 프로젝트 및 `google-services.json` 발급 완료
- OpenAI API 키 발급 완료

---

## 1. OPENAI_API_KEY Secret 등록

Firebase Secret Manager에 키를 등록한다. 이 키는 `functions/index.js`에서 `defineSecret("OPENAI_API_KEY")`로 참조하며, 코드나 Git에 노출되지 않는다.

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
firebase deploy --only functions
```

배포 완료 후 터미널에 출력되는 Functions URL을 복사한다.

```
✔  functions[api]: Deployed.
Function URL (api): https://api-<hash>-uc.a.run.app
```

---

## 3. Android ai.base.url 설정

프로젝트 루트의 `local.properties`(Git에 포함되지 않음)에 위 URL을 추가한다.

```properties
ai.base.url=https://api-<hash>-uc.a.run.app
```

> **로컬 에뮬레이터 사용 시**
> ```bash
> cd functions && npm run serve
> ```
> `local.properties`에 에뮬레이터 주소를 입력한다. 실제 Android 기기에서 접근할 경우 `localhost` 대신 PC의 로컬 IP를 사용한다.
> ```properties
> ai.base.url=http://127.0.0.1:5001/<project-id>/us-central1/api
> ```

Android Studio에서 **Build > Clean Project → Rebuild Project** 후 실행해야 `BuildConfig.AI_BASE_URL`에 값이 반영된다.

---

## 4. 엔드포인트 보호 정책

`functions/index.js`에 적용된 보호 정책:

| 정책 | 내용 |
|---|---|
| Firebase Auth 검증 | 모든 요청에 `Authorization: Bearer <Firebase ID Token>` 헤더 필수. 앱은 `AiService`에서 자동으로 첨부. |
| 이메일 인증 검증 | ID Token의 `email_verified`가 `true`인 사용자만 AI 엔드포인트 호출 가능. |
| 입력 길이 제한 | `text` 필드 최대 5,000자. 초과 시 400 응답. |
| Secret Manager | `OPENAI_API_KEY`는 코드에 하드코딩하지 않고 Secret Manager로 관리. |

---

## 5. 최종 확인 체크리스트

- [ ] `OPENAI_API_KEY` Secret이 Firebase 프로젝트에 등록되어 있는가
- [ ] `firebase deploy --only functions` 성공 여부 확인
- [ ] `local.properties`에 `ai.base.url` 값이 입력되어 있는가
- [ ] Android Studio에서 Rebuild 후 앱 실행 → 학습 내용 입력 → 요약/퀴즈 생성 정상 동작 확인
