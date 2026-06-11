# Firestore DB 설계서

## 1. 문서 목적

이 문서는 StudyMate에서 사용할 Cloud Firestore 컬렉션, 문서 필드, 데이터 타입을 정의한다.

## 2. 핵심 내용

사용 컬렉션은 다음 5개이다.

- users
- study_notes
- quizzes
- quiz_results
- wrong_answers

## 3. 상세 설명

### users 컬렉션

```text
users
 └─ userId
     ├─ email: string
     ├─ nickname: string
     └─ createdAt: timestamp
```

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| email | string | 예 | 사용자 이메일 |
| nickname | string | 아니오 | 사용자 표시 이름 |
| createdAt | timestamp | 예 | 계정 생성일 |

### study_notes 컬렉션

```text
study_notes
 └─ noteId
     ├─ userId: string
     ├─ title: string
     ├─ subject: string
     ├─ originalText: string
     ├─ summary: array<string>
     ├─ keywords: array<string>
     └─ createdAt: timestamp
```

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| userId | string | 예 | 작성자 사용자 ID |
| title | string | 예 | 학습 제목 |
| subject | string | 아니오 | 과목명 |
| originalText | string | 예 | 사용자가 입력한 원문 |
| summary | array<string> | 예 | AI 요약 문장 목록 |
| keywords | array<string> | 예 | 핵심 키워드 목록 |
| createdAt | timestamp | 예 | 생성일 |

### quizzes 컬렉션

```text
quizzes
 └─ quizId
     ├─ noteId: string
     ├─ userId: string
     ├─ question: string
     ├─ options: array<string>
     ├─ answerIndex: number
     ├─ explanation: string
     └─ createdAt: timestamp
```

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| noteId | string | 예 | 연결된 학습 기록 ID |
| userId | string | 예 | 사용자 ID |
| question | string | 예 | 문제 내용 |
| options | array<string> | 예 | 보기 4개 |
| answerIndex | number | 예 | 정답 인덱스, 0~3 |
| explanation | string | 예 | 정답 해설 |
| createdAt | timestamp | 예 | 생성일 |

### quiz_results 컬렉션

```text
quiz_results
 └─ resultId
     ├─ userId: string
     ├─ noteId: string
     ├─ totalCount: number
     ├─ correctCount: number
     ├─ score: number
     └─ createdAt: timestamp
```

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| userId | string | 예 | 사용자 ID |
| noteId | string | 예 | 연결된 학습 기록 ID |
| totalCount | number | 예 | 전체 문제 수 |
| correctCount | number | 예 | 맞힌 문제 수 |
| score | number | 예 | 정답률 또는 점수 |
| createdAt | timestamp | 예 | 결과 저장일 |

### wrong_answers 컬렉션

```text
wrong_answers
 └─ wrongId
     ├─ userId: string
     ├─ quizId: string
     ├─ noteId: string
     ├─ selectedIndex: number
     ├─ correctIndex: number
     ├─ question: string
     ├─ options: array<string>
     ├─ explanation: string
     └─ createdAt: timestamp
```

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| userId | string | 예 | 사용자 ID |
| quizId | string | 예 | 원본 퀴즈 ID |
| noteId | string | 예 | 연결된 학습 기록 ID |
| selectedIndex | number | 예 | 사용자가 선택한 보기 인덱스 |
| correctIndex | number | 예 | 정답 인덱스 |
| question | string | 예 | 문제 내용 스냅샷 |
| options | array<string> | 예 | 보기 스냅샷 |
| explanation | string | 예 | 해설 스냅샷 |
| createdAt | timestamp | 예 | 오답 저장일 |

## 4. 개발 시 참고사항

- 모든 사용자 데이터 컬렉션에는 `userId`를 포함한다.
- 학습 기록 목록은 `study_notes`에서 `userId` 기준 조회 후 `createdAt` 내림차순으로 표시한다.
- 퀴즈는 `noteId` 기준으로 조회한다.
- 오답노트는 원본 퀴즈가 변경되더라도 표시 가능하도록 문제, 보기, 해설을 스냅샷으로 저장한다.
- Firestore 보안 규칙에서 로그인한 사용자 본인의 문서만 읽고 쓸 수 있도록 제한해야 한다.
- `users` 최초 생성 이후의 사용자 데이터 접근은 이메일 인증 완료(`email_verified == true`) 사용자만 허용한다.

## 5. 확인 체크리스트

- [ ] 모든 컬렉션에 필요한 필드가 정의되어 있는가?
- [ ] 사용자별 데이터 분리를 위한 userId가 포함되어 있는가?
- [ ] 퀴즈와 학습 기록의 연결 기준 noteId가 정의되어 있는가?
- [ ] 오답노트가 원본 퀴즈 스냅샷을 보관하는가?
- [ ] createdAt 필드가 정렬 기준으로 사용 가능한가?
