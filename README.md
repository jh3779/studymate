# StudyMate

AI 기반 학습 요약 및 퀴즈 생성 모바일 앱 설계 문서 저장소입니다.

## 프로젝트 소개

StudyMate는 사용자가 입력한 학습 내용을 AI가 자동으로 요약하고, 객관식 퀴즈를 생성하여 복습할 수 있도록 돕는 모바일 학습 도우미 앱입니다.

## 주요 기능

- 이메일 기반 회원가입 및 로그인
- 학습 제목, 과목명, 학습 내용 입력
- AI 기반 핵심 요약 생성
- AI 기반 객관식 4지선다 퀴즈 생성
- 퀴즈 풀이 및 결과 확인
- 틀린 문제 오답노트 저장
- 날짜별 학습 기록 조회

## 기술 스택

| 구분 | 기술 |
| --- | --- |
| Frontend | Android Native Java |
| Authentication | Firebase Authentication |
| Database | Cloud Firestore |
| AI API | OpenAI API 또는 Gemini API |
| Storage | Firebase Storage, 추후 확장용 |

## 문서 구조

```text
docs/
 ├─ 00_project_overview.md
 ├─ 01_planning/
 ├─ 02_requirements/
 ├─ 03_design/
 ├─ 04_database/
 ├─ 05_api/
 ├─ 06_development/
 └─ 07_presentation/
```

## 실행 예정 환경

- Android Studio
- Java 11
- Android Emulator 또는 실제 Android 기기
- Firebase 프로젝트 1개
- 개발용 AI API Key, 앱 내부 하드코딩 금지

## 개발 일정 요약

| 기간 | 주요 작업 |
| --- | --- |
| 1주차 | 주제 확정, 기능 목록, 화면 흐름, DB 설계, Firebase 생성 |
| 2주차 | Android Java 프로젝트 생성, Firebase 연동, 로그인/회원가입 구현 |
| 3주차 | 홈, 학습 입력, Firestore 저장 및 목록 조회 구현 |
| 4주차 | AI 요약 기능, 요약 결과 화면, 요약 저장 구현 |
| 5주차 | AI 퀴즈 생성, JSON 파싱, 퀴즈 화면 구현 |
| 6주차 | 답안 선택, 점수 계산, 결과 저장, 오답 추출 구현 |
| 7주차 | 오답노트, 다시 풀기, 마이페이지, 로그아웃 구현 |
| 8주차 | 전체 테스트, 오류 수정, UI 정리, 발표 및 최종 보고서 작성 |

## 주요 문서 바로가기

- [프로젝트 개요](docs/00_project_overview.md)
- [MVP 범위](docs/02_requirements/mvp_scope.md)
- [화면 흐름](docs/03_design/screen_flow.md)
- [Firestore 설계](docs/04_database/firestore_schema.md)
- [AI 프롬프트 명세](docs/05_api/ai_prompt_spec.md)
- [개발 일정](docs/06_development/development_schedule.md)
- [팀 작업 분담](docs/06_development/team_task_distribution.md)
- [우지훈 작업 우선도](docs/06_development/woo_jihun_priority_tasks.md)
- [발표 구성](docs/07_presentation/presentation_outline.md)
