# 코드 분석 및 작성 의도 문서

## 1. 문서 목적

이 문서는 현재 StudyMate Android Java 코드가 어떤 의도로 작성되었는지, 각 파일이 어떤 역할을 맡는지, 그리고 실제 Firebase/AI 연동 단계에서 어떤 부분을 교체해야 하는지 설명한다.

현재 코드는 최종 완성 코드가 아니라 와이어프레임을 Android 앱 화면으로 빠르게 검증하기 위한 MVP 골격이다. 따라서 화면 이동, 입력 검증, 로딩 상태, 결과 표시 흐름을 먼저 구현하고, Firebase Authentication, Cloud Firestore, AI API 연동은 이후 서비스 계층으로 분리해 연결하는 구조를 전제로 한다.

## 2. 전체 구현 방향

### Android Java Activity 기반으로 시작한 이유

현재 프로젝트는 Android Studio에서 생성한 Android Native Java 프로젝트를 기준으로 한다. 화면 수가 많지만 기능 범위가 MVP 수준이기 때문에 Fragment, ViewModel, Repository까지 처음부터 도입하지 않고 Activity 단위로 화면을 나누었다.

이 방식의 장점은 다음과 같다.

- 와이어프레임의 `Splash`, `Login`, `Home`, `Quiz` 같은 화면 단위를 코드 파일과 1:1로 대응시킬 수 있다.
- 팀원이 각 화면을 나누어 작업하기 쉽다.
- Firebase와 AI API가 아직 연결되지 않은 상태에서도 화면 흐름을 먼저 확인할 수 있다.
- 발표용 시연 흐름을 빠르게 만들 수 있다.

단, 실제 데이터 저장과 API 연동이 들어가면 Activity가 직접 모든 로직을 처리하지 않도록 `service`, `model`, `util` 패키지를 추가해야 한다.

### XML Layout을 사용한 이유

UI는 `res/layout/*.xml` 파일로 작성했다. Java 기반 Android 프로젝트에서 가장 기본적인 화면 작성 방식이고, Android Studio 미리보기와 연결하기 쉽기 때문이다.

또한 와이어프레임의 어두운 배경, 카드, 입력창, 선택지, 버튼 스타일을 반복해서 사용하므로 `res/drawable`에 공통 배경 리소스를 분리했다. 이렇게 하면 버튼이나 카드 모양을 각 화면에서 직접 반복 작성하지 않아도 된다.

### 초기 더미 데이터 제거 현황

초기 구현 단계에서는 화면 흐름을 먼저 고정하기 위해 일부 화면에 더미 데이터와 TODO가 있었다. 현재 주요 사용자 흐름은 Firebase Auth, Firestore, AI Functions 호출, PDF 텍스트 추출, 이미지 OCR 흐름으로 연결되어 있다.

초기 더미 구현으로 먼저 확인한 항목은 다음과 같다.

- 입력값 검증과 버튼 활성화 상태를 먼저 확인한다.
- 로딩, 오류, 결과 화면 이동이 자연스럽게 이어지는지 확인한다.
- Firebase/AI 담당자가 작업하기 전에 필요한 화면 ID와 이동 경로를 확정한다.
- 발표 시 화면 전환을 먼저 시연할 수 있게 한다.

실제 화면에 표시되는 학습 데이터는 모델과 서비스 계층에서 가져오며, 발표용 더미 데이터는 앱 코드에 포함하지 않는다.

## 3. 공통 구조 분석

### BaseActivity.java

`BaseActivity`는 모든 화면에서 반복되는 공통 처리를 줄이기 위해 작성했다.

주요 역할은 다음과 같다.

| 기능 | 작성 이유 |
| --- | --- |
| 상태바/내비게이션바 색상 설정 | 모든 화면의 다크 테마가 동일하게 보이도록 공통 적용 |
| `goTo()` | 단순 화면 이동 코드를 줄이기 위함 |
| `goToAndClear()` | 로그인, 로그아웃, 홈 복귀처럼 이전 화면 스택을 제거해야 하는 흐름 처리 |
| `bindClick()` | `findViewById()` 후 클릭 리스너 연결 반복을 줄이기 위함 |
| `isLoggedIn()`, `setLoggedIn()` | Firebase Auth가 붙기 전 로그인 상태 분기용 임시 저장소 |
| `showShortToast()` | 간단한 사용자 피드백을 공통 함수로 사용 |

현재 로그인 상태는 `SharedPreferences`의 boolean 값으로만 관리한다. 이는 Splash 화면 분기를 확인하기 위한 임시 방식이며, 실제 인증 구현 후에는 Firebase Auth의 현재 사용자 상태를 기준으로 바꾸어야 한다.

### AndroidManifest.xml

`SplashActivity`를 launcher activity로 등록해 앱 실행 시 스플래시 화면이 먼저 보이게 했다. 나머지 Activity도 명시적으로 등록해 각 화면 이동이 가능하도록 했다.

현재 Manifest에는 네트워크 권한과 Firebase 관련 설정이 들어가 있지 않다. AI API 또는 Firestore 연동 단계에서는 `android.permission.INTERNET` 권한과 Firebase 설정 파일 적용 여부를 반드시 확인해야 한다.

## 4. 화면별 코드 분석

### SplashActivity

Splash 화면은 앱 실행 후 900ms 동안 로고 화면을 보여준 뒤 로그인 상태에 따라 `HomeActivity` 또는 `LoginActivity`로 이동한다.

이렇게 작성한 이유는 다음과 같다.

- 와이어프레임에 있는 스플래시 화면을 실제 앱 첫 화면으로 확인하기 위함
- 로그인 여부에 따라 앱 진입 지점이 달라지는 흐름을 미리 구성하기 위함
- Firebase Auth 연동 전에도 홈/로그인 분기를 테스트하기 위함

개선이 필요한 부분은 다음과 같다.

- Android 12 이상에서는 시스템 SplashScreen API 사용을 검토한다.
- `Handler.postDelayed()`는 Activity 종료 시 취소할 수 있도록 관리한다.
- 로그인 상태 확인은 `SharedPreferences`가 아니라 Firebase Auth 기준으로 변경한다.

### LoginActivity

Login 화면은 이메일과 비밀번호가 비어 있는지만 확인한 뒤 홈으로 이동한다.

현재 방식의 작성 의도는 다음과 같다.

- 로그인 화면 UI와 오류 문구 표시를 먼저 검증하기 위함
- 로그인 성공 시 백스택을 지우고 홈으로 이동하는 흐름을 확정하기 위함
- 회원가입 링크가 정상적으로 연결되는지 확인하기 위함

현재 코드는 실제 인증이 아니므로 보안 기능으로 보면 안 된다. `AuthService.signIn(email, password)`가 구현되면 다음 동작으로 교체해야 한다.

1. 이메일 형식 검증
2. Firebase Auth 로그인 요청
3. 실패 사유별 오류 메시지 표시
4. 성공 시 사용자 UID 기준으로 홈 데이터 조회

### SignUpActivity

회원가입 화면은 이메일 입력 여부, 비밀번호 6자 이상, 비밀번호 확인 일치를 검사한다.

이렇게 작성한 이유는 다음과 같다.

- 회원가입에서 최소한 필요한 입력 검증 흐름을 먼저 구현하기 위함
- 실패 메시지가 화면에서 어떻게 보이는지 확인하기 위함
- 가입 완료 후 로그인 화면으로 돌아가는 흐름을 확정하기 위함

실제 구현 시에는 `AuthService.signUp(email, password)`로 교체하고, 가입 성공 시 Firestore에 사용자 기본 문서를 생성해야 한다.

### HomeActivity

홈 화면은 학습 시작, 오답노트, 마이페이지로 이동하는 중심 화면이다.

현재 화면에는 Firestore에서 조회한 총 학습 수, 퀴즈 풀이 수, 평균 정답률, 최근 학습 기록이 표시된다. 최근 학습 기록 카드를 누르면 저장된 요약 결과 화면으로 이동한다.

이 화면은 다음 데이터에 의존한다.

- 현재 사용자 정보
- 최근 학습 기록 목록
- 총 학습 기록 수
- 총 퀴즈 풀이 수
- 평균 정답률
- 빈 상태 화면

### StudyInputActivity

학습 입력 화면은 제목과 학습 내용을 입력받고, 학습 내용이 30자 이상인지 검사한다. 버튼을 누르면 로딩 박스를 보여주고 버튼을 비활성화한 뒤 요약 결과 화면으로 이동한다.

이렇게 작성한 이유는 다음과 같다.

- AI 요청 전 입력 검증 기준을 먼저 적용하기 위함
- AI 생성 중 중복 클릭을 막는 UI 상태를 확인하기 위함
- 네트워크 지연처럼 보이는 로딩 상태를 화면에서 검증하기 위함
- 다음 화면인 요약 결과 화면으로 이어지는 흐름을 먼저 연결하기 위함

현재는 입력값을 `SummaryResultActivity`로 전달하지 않는다. 실제 구현 시에는 입력 내용을 `AiService.generateSummary()`에 전달하고, 응답 결과를 `StudyNoteModel`로 저장한 뒤 결과 화면에 표시해야 한다.

### SummaryResultActivity

요약 결과 화면은 AI 요약문, 핵심 키워드, 퀴즈 생성 버튼을 보여준다.

현재 Java 코드는 버튼 이동만 담당하고, 실제 표시 데이터는 XML에 고정되어 있다. 이 구조는 결과 화면 레이아웃과 CTA 위치를 먼저 확인하기 위해 만든 것이다.

실제 구현 시에는 다음 데이터가 필요하다.

- 학습 제목
- 과목명
- AI 요약문
- 핵심 키워드 목록
- Firestore에 저장된 noteId

퀴즈 생성 버튼을 누르면 noteId와 요약 또는 원문을 기준으로 AI 퀴즈 생성 요청을 보내야 한다.

### QuizActivity

퀴즈 화면은 3개의 고정 문제, 4지선다 보기, 정답 배열을 Java 배열로 관리한다. 사용자가 보기를 선택하면 다음 버튼이 활성화되고, 마지막 문제 후 정답 수를 결과 화면으로 전달한다.

이렇게 작성한 이유는 다음과 같다.

- 객관식 선택 UI와 선택 상태 표시를 먼저 검증하기 위함
- 다음 문제 이동과 진행률 표시를 빠르게 구현하기 위함
- AI 퀴즈 데이터가 없어도 점수 계산 흐름을 확인하기 위함
- 선택된 보기에는 `contentDescription`을 추가해 기본 접근성 상태를 확인하기 위함

실제 구현에서는 Java 배열 대신 `QuizModel` 목록을 사용해야 한다. 또한 화면 회전 시 `currentIndex`, `selectedIndex`, `correctCount`가 사라지지 않도록 `onSaveInstanceState()` 또는 ViewModel 도입을 검토해야 한다.

### QuizResultActivity

퀴즈 결과 화면은 `QuizActivity`에서 전달받은 `correctCount`, `totalCount`를 기준으로 정답률을 계산해 표시한다.

이렇게 작성한 이유는 다음과 같다.

- 퀴즈 풀이 화면과 결과 화면의 책임을 분리하기 위함
- 정답률 계산 결과가 UI에 어떻게 표현되는지 확인하기 위함
- 결과 화면에서 홈 또는 오답노트로 이동하는 흐름을 확정하기 위함

현재는 모든 문제를 맞혀도 오답 상세 보기 버튼이 남아 있다. 실제 구현 시에는 오답이 없으면 버튼을 숨기거나 비활성화하고, 오답이 있을 때만 WrongAnswer 화면으로 이동해야 한다.

### WrongAnswerActivity

오답노트 화면은 틀린 문제의 질문, 내가 선택한 답, 정답, 해설을 보여주는 구조로 작성했다.

현재는 고정 오답 하나를 보여주지만, 작성 의도는 다음과 같다.

- 오답 상세 카드의 정보 구조를 먼저 검증하기 위함
- 다시 풀기 버튼과 하단 탭 이동을 연결하기 위함
- 추후 `WrongAnswerModel` 목록을 표시할 화면 구조를 미리 확보하기 위함

실제 구현 시에는 퀴즈 결과 저장 후 오답만 추출해 Firestore에 저장하고, 사용자별 오답 목록을 조회해 표시해야 한다.

### MyPageActivity

마이페이지는 사용자 이메일, 학습 기록 수, 퀴즈 풀이 수, 앱 버전, 로그아웃 버튼을 보여준다.

현재 통계와 이메일은 고정 데이터이며, 로그아웃은 `SharedPreferences` 값을 false로 바꾸고 로그인 화면으로 이동한다.

이렇게 작성한 이유는 다음과 같다.

- 사용자 계정 화면의 레이아웃을 먼저 확정하기 위함
- 로그아웃 후 이전 화면으로 돌아가지 못하게 백스택 제거 흐름을 검증하기 위함
- 홈/오답노트/마이페이지 하단 탭 이동을 확인하기 위함

실제 구현 시에는 Firebase Auth 로그아웃을 호출하고, Firestore에서 사용자 통계를 조회해야 한다.

## 5. 리소스 구조 분석

### layout

`activity_*.xml` 파일은 화면별 UI를 1개씩 가진다. 와이어프레임의 화면 이름과 파일 이름을 맞추어 작업자가 화면을 찾기 쉽도록 했다.

| 화면 | layout 파일 |
| --- | --- |
| Splash | `activity_splash.xml` |
| Login | `activity_login.xml` |
| SignUp | `activity_signup.xml` |
| Home | `activity_home.xml` |
| StudyInput | `activity_study_input.xml` |
| SummaryResult | `activity_summary_result.xml` |
| Quiz | `activity_quiz.xml` |
| QuizResult | `activity_quiz_result.xml` |
| WrongAnswer | `activity_wrong_answer.xml` |
| MyPage | `activity_my_page.xml` |

### drawable

버튼, 카드, 입력창, 선택지 배경을 drawable로 분리했다. 작성 이유는 다음과 같다.

- 같은 모양의 UI를 여러 XML에서 반복하지 않기 위함
- 와이어프레임의 다크 테마 톤을 일관되게 유지하기 위함
- 선택지 기본 상태와 선택 상태를 코드에서 쉽게 바꿀 수 있게 하기 위함

### values

`colors.xml`, `dimens.xml`, `themes.xml`은 색상, 여백, 테마를 공통으로 관리하기 위한 파일이다. 앞으로 `strings.xml`에 화면 문구를 분리하면 다국어, 접근성 라벨, QA 수정 대응이 쉬워진다.

## 6. 현재 코드의 의도적인 단순화 지점

| 단순화된 부분 | 현재 방식 | 이렇게 둔 이유 | 실제 구현 시 교체 방향 |
| --- | --- | --- | --- |
| 인증 | `SharedPreferences` boolean | Splash/Login 흐름 검증 | Firebase Auth |
| 회원가입 | 입력 검증 후 `finish()` | 화면 검증 | Firebase Auth + 사용자 문서 생성 |
| 홈 데이터 | XML 고정값 | 와이어프레임 검증 | Firestore 조회 |
| AI 요약 | `Handler` 지연 후 이동 | 로딩 상태 검증 | `AiService.generateSummary()` |
| 요약 결과 | XML 고정값 | 결과 화면 레이아웃 검증 | Intent extra 또는 Firestore noteId 조회 |
| 퀴즈 | Java 배열 | 풀이 흐름 검증 | `QuizModel` 목록 |
| 결과 저장 | Intent extra만 사용 | 점수 표시 검증 | `QuizResultModel` 저장 |
| 오답노트 | 고정 오답 카드 | UI 구조 검증 | `WrongAnswerModel` 목록 조회 |
| 마이페이지 | 고정 통계 | 화면 구조 검증 | 사용자별 통계 조회 |

## 7. 실제 구현으로 넘어가기 전 개선해야 할 부분

### 우선순위 1: 데이터 흐름 연결

학습 입력, 요약 결과, 퀴즈, 결과, 오답노트는 모델과 서비스 계층을 통해 연결되어 있다. 이후에는 Intent extra key 상수화, 화면 회전 대응, 실패 상태 세분화가 개선 대상이다.

### 우선순위 2: 서비스 계층 추가

Activity가 직접 인증, 저장, AI 요청을 처리하지 않도록 다음 클래스를 추가한다.

- `AuthService`
- `FirestoreService`
- `AiService`
- `QuizService`
- `Validators`
- `AppConstants`

### 우선순위 3: 접근성 및 화면 대응

긴 AI 생성 문장과 큰 글자 설정에 대비해 Quiz 화면과 Summary 화면은 스크롤, 줄바꿈, 동적 높이를 보장해야 한다. 버튼과 선택지는 화면 낭독기가 이해할 수 있도록 선택 상태와 역할을 더 명확히 제공해야 한다.

### 우선순위 4: Android 생명주기 대응

`Handler.postDelayed()` 사용 화면은 Activity 종료 시 콜백을 취소해야 한다. Quiz 진행 상태는 화면 회전이나 프로세스 재생성에도 유지되도록 저장해야 한다.

### 우선순위 5: 문자열과 리소스 정리

현재는 XML과 Java에 문구가 직접 들어가 있다. 추후 유지보수를 위해 `strings.xml`로 옮기고, lint 경고를 줄여야 한다.

## 8. 다음 개발자가 참고할 기준

현재 코드를 수정할 때는 다음 기준을 따른다.

- 화면 이동과 UI 상태는 Activity에서 처리한다.
- 인증, Firestore, AI 요청, 퀴즈 계산은 service 계층으로 옮긴다.
- 화면에 표시되는 데이터는 더미 XML 값이 아니라 model 객체에서 가져온다.
- Intent extra key와 Firestore 필드명은 상수로 관리한다.
- 발표용 더미 데이터가 필요한 경우 실제 구현 코드와 구분되는 별도 mock 클래스로 분리한다.
- API key는 Android 코드에 직접 넣지 않는다.
- AI 요청 실패, JSON 파싱 실패, 네트워크 오류, 빈 데이터 상태를 화면에 반드시 표시한다.

## 9. 결론

현재 코드는 완성된 기능 구현이라기보다 StudyMate의 화면 구조와 사용자 흐름을 Android Java 프로젝트 위에 올려놓은 초기 골격이다. 이렇게 작성한 이유는 Firebase와 AI API가 완성되기 전에도 화면, 이동, 입력 검증, 결과 표시, 오답노트, 마이페이지 흐름을 먼저 확인하기 위해서다.

이후 작업의 핵심은 남은 생명주기 대응, 문자열 리소스 정리, 실제 기기 기반 QA를 진행하는 것이다. 현재 Activity들은 화면 표시와 사용자 이벤트 처리를 담당하고, 인증/저장/AI 요청은 서비스 계층에서 관리한다.
