# 네카라쿠배당토야 배치 프로젝트

## 커밋메시지 형식
```bash
🚨 Fix: 수정 내용
✨ Feat: 새로운 기능 추가, 사용자 입장에서 변화가 있을 경우
🎉 Init: 프로젝트 초기 생성
📝 Chore: 그 외 자잘한 수정에 대한 커밋, 주석, 의존성 설치, 리드미 수정
💄 Style: CSS, styled-component 스타일 관련 변경
🔨 Refactor: 코드 리팩토링에 대한 커밋, 사용자 입장에서 변화가 없는 코드, 파일명 폴더명 변경 및 이동
🗑️ Remove: 파일을 삭제하는 작업만 수행하는 경우
```



## Gradle 설정 파일 버전맞추기
```
id 'org.springframework.boot' version '3.5.0' --> id 'org.springframework.boot' version '2.6.4'로 변경
id 'io.spring.dependency-management' version '1.1.4' 추가
```

```
java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17) --> languageVersion = JavaLanguageVersion.of(11)로 변경
	}
}
```

## jdk버전 낮추기
```
jdk 11로 조정
```