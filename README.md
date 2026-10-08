웹서비스설계 실습1 - 백엔드 프레임워크 실습 과제
1. 개발 환경
항목	내용
언어	Java 21
프레임워크	Spring Boot 4.1.1 (Spring Web MVC)
빌드 도구	Gradle
IDE	Spring Tools for Eclipse
테스트 도구	Postman
데이터 저장	메모리(HashMap) — 서버를 재시작하면 데이터가 초기화됩니다
2. 프로젝트 구조
src/main/java/kr/ac/jbnu/sjw/webservice
├── WebserviceApplication.java        # 애플리케이션 시작점
├── api
│   ├── v3/ArticleController3.java    # 기사 API (컨트롤러)
│   ├── dto/ArticleDto.java           # 저장되는 기사 데이터
│   ├── request/                      # 요청 본문 (ArticleCreateRequest, ArticleUpdateRequest)
│   ├── response/ApiResponse.java     # 표준 응답 형식
│   └── exception/GlobalExceptionHandler.java  # 전역 예외 처리
└── config/RequestLoggingFilter.java  # 미들웨어 (요청 로그 + 점검 모드)
3. API 목록 (요구사항 A)
기본 주소: /api/v3/articles

메서드	URL	설명	응답 코드
GET	/api/v3/articles	기사 목록 조회 · 제목 검색 · 페이지 처리	200
GET	/api/v3/articles/{id}	기사 단건 조회	200, 404
POST	/api/v3/articles	기사 작성 (임시저장 상태로 생성)	201, 400
POST	/api/v3/articles/with-header	요청 헤더(X-USER-ID, Authorization) 확인	200
PUT	/api/v3/articles/{id}	기사 제목 · 본문 수정	200, 404
PUT	/api/v3/articles/{id}/publish	기사 발행 (임시저장 → 발행)	200, 400, 404
DELETE	/api/v3/articles/{id}	기사 한 건 삭제	200, 404
DELETE	/api/v3/articles/drafts	발행 전(임시저장) 기사 일괄 삭제	200, 404
GET	/api/v3/articles/error-test	500 응답 확인용 테스트 API (서비스 기능 아님)	500
4. 표준 응답 형식 (요구사항 D)
모든 응답은 ApiResponse 클래스로 감싸서 같은 형식으로 반환합니다.

성공

{
  "status": "success",
  "data": { "...": "..." },
  "message": null
}
실패

{
  "status": "error",
  "data": null,
  "message": "기사를 찾을 수 없습니다."
}
필드	설명
status	"success" 또는 "error"
data	성공 시 결과 데이터 (기사, 기사 목록, 메시지 문자열)
message	실패 시 원인 설명
5. 응답 코드 (요구사항 C)
코드	의미	발생 상황
200 OK	성공	조회 · 수정 · 발행 · 삭제 성공
201 Created	생성 성공	기사 작성 성공
400 Bad Request	잘못된 요청	제목 없이 작성, 이미 발행된 기사를 다시 발행, id 자리에 숫자가 아닌 값, 깨진 JSON
404 Not Found	대상 없음	없는 기사 조회 · 수정 · 발행 · 삭제, 지울 임시저장 기사가 없음
405 Method Not Allowed	허용되지 않은 메서드	지원하지 않는 메서드로 요청
500 Internal Server Error	서버 오류	예상하지 못한 예외 발생 (/error-test로 확인)
503 Service Unavailable	서비스 이용 불가	점검 모드가 켜져 있을 때 모든 요청
6. 미들웨어 (요구사항 B)
config/RequestLoggingFilter.java — OncePerRequestFilter를 상속한 필터로, 모든 요청이 컨트롤러에 도착하기 전에 거칩니다.

1) 요청 로그
요청마다 메서드, URL, 응답 코드, 처리 시간을 콘솔에 출력합니다.

[POST] /api/v3/articles -> 201 (133ms)
[POST] /api/v3/articles -> 400 (4ms)
[GET] /api/v3/articles/99 -> 404 (5ms)
[PUT] /api/v3/articles/1/publish -> 200 (4ms)
[GET] /api/v3/articles/error-test -> 500 (5ms)
2) 점검 모드
application.properties에 아래 설정을 넣고 서버를 재시작하면, 컨트롤러로 요청을 보내지 않고 모든 요청에 503을 반환합니다.

app.maintenance=true
{ "status": "error", "data": null, "message": "서비스 점검 중" }
설정이 없거나 false이면 평소처럼 동작합니다.

7. 설계하면서 고민한 점
익명 서비스로 이름을 받지 않음 처음에는 reporter(기자 이름) 필드를 넣으려 했지만, 콘셉트에 맞지 않아 뺐습니다. 기사 데이터는 id, title, content, published만 가집니다.

수정과 발행을 다른 API로 분리 PUT /{id}는 기사 내용(제목, 본문)을, PUT /{id}/publish는 기사 상태(임시저장 → 발행)를 바꿉니다. published 값을 수정 요청 본문에 넣으면 수정 API로도 발행할 수 있게 되므로, 요청 클래스에는 넣지 않고 서버가 관리하도록 했습니다.

"발행 취소"를 DELETE로 볼 수 있는가 처음에는 DELETE /{id}/publish로 발행 취소를 만들었지만, 실제로 데이터를 지우지 않고 상태만 바꾸는 동작이라 DELETE로 보기 애매하다고 판단했습니다. 대신 실제로 데이터를 삭제하는 임시저장 기사 일괄 삭제(DELETE /drafts)로 바꿨습니다.

목록과 검색을 하나의 GET으로 통합 검색 API에서 keyword를 보내지 않으면 전체 목록이 나오므로, 목록 조회와 검색을 GET /api/v3/articles 하나로 합쳤습니다.

제목 없는 기사가 검색 전체를 망가뜨리는 버그 테스트 중 제목 없이 기사를 작성한 뒤 검색하면 NullPointerException으로 500이 나는 것을 발견했습니다. 제목 없는 기사가 하나라도 저장되면 모든 검색이 실패하는 문제였습니다. 작성 단계에서 제목이 비어 있으면 400을 반환하도록 막았고, 이때 검사를 저장 전에 해야 잘못된 데이터가 저장되지 않는다는 것도 확인했습니다.

8. AI 활용
작성한 코드의 리뷰와 버그 확인 (주소 매핑 오류, 저장 순서 문제 등)
개념 학습 (제네릭, static/final, 람다, ResponseEntity, 전역 예외 처리, 필터)
전역 예외 처리와 미들웨어 코드 예시 참고
README 초안 작성
