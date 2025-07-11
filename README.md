# 🚀 TCP Converter

> **TCP 전문을 JSON으로 변환하는 Spring Boot 기반 API 서비스**

[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-6DB33F?style=for-the-badge&logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=JSON%20web%20tokens&logoColor=white)](https://jwt.io/)

## 📋 목차

- [🎯 프로젝트 개요](#-프로젝트-개요)
- [✨ 주요 기능](#-주요-기능)
- [🏗️ 시스템 아키텍처](#️-시스템-아키텍처)
- [🛠️ 기술 스택](#️-기술-스택)
- [🚀 빠른 시작](#-빠른-시작)
- [📖 API 문서](#-api-문서)
- [🔧 설정](#-설정)
- [🧪 테스트](#-테스트)
- [📁 프로젝트 구조](#-프로젝트-구조)

## 🎯 프로젝트 개요

**TCP Converter**는 레거시 TCP 메시지를 JSON 형식으로 변환하는 Spring Boot 기반의 REST API 서비스입니다. 

### 핵심 가치
- 🔄 **유연한 변환**: 다양한 TCP 메시지 포맷을 동적으로 파싱
- 🔐 **보안 강화**: JWT 기반 인증/인가 시스템
- 📊 **스펙 기반**: 데이터베이스 기반 메시지 스펙 관리
- ⚡ **고성능**: 효율적인 메시지 파싱 알고리즘

## ✨ 주요 기능

### 🔄 TCP 메시지 변환
- **동적 파싱**: API 서비스 코드 기반 스펙 조회
- **중첩 구조 지원**: 객체, 배열, 원시 타입 모두 지원
- **길이 검증**: 메시지 무결성 검증
- **에러 핸들링**: 상세한 예외 처리 및 로깅

### 🔐 인증 & 보안
- **JWT 토큰**: Access/Refresh 토큰 발급
- **토큰 관리**: 토큰 갱신, 폐기, 상태 조회
- **블랙리스트**: 토큰 폐기 시스템
- **클라이언트 인증**: Client ID/Secret 기반 인증

### 📊 스펙 관리
- **동적 스펙**: 데이터베이스 기반 메시지 스펙 관리
- **계층 구조**: 중첩된 객체 및 배열 지원
- **필드 타입**: 다양한 데이터 타입 지원

## 🏗️ 시스템 아키텍처

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Client App    │───▶│  TCP Converter  │───▶│   PostgreSQL    │
│                 │    │   (Spring Boot) │    │   (Database)    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                              │
                              ▼
                       ┌─────────────────┐
                       │  JWT Security   │
                       │   (Auth Layer)  │
                       └─────────────────┘
```

### 처리 흐름

1. **인증**: 클라이언트 ID/Secret으로 JWT 토큰 발급
2. **요청**: TCP 메시지와 함께 변환 요청
3. **파싱**: API 서비스 코드 추출 및 스펙 조회
4. **변환**: 스펙 기반 메시지 파싱 및 JSON 변환
5. **응답**: 변환된 JSON 데이터 반환

## 🛠️ 기술 스택

### Backend
- **Framework**: Spring Boot 3.4.5
- **Language**: Java 17
- **Security**: Spring Security + JWT
- **Database**: PostgreSQL
- **ORM**: Spring Data JPA
- **Build Tool**: Gradle

### Dependencies
- **JWT**: `io.jsonwebtoken:jjwt` 0.11.5
- **Database**: `postgresql` 42.7.3
- **Lombok**
- **Testing**: JUnit 5, Spring Boot Test

## 🚀 빠른 시작

### 1. 사전 요구사항

```bash
# Java 17 이상
java --version

# PostgreSQL 설치 및 실행
psql --version
```

### 2. 데이터베이스 설정

```sql
-- PostgreSQL 데이터베이스 생성
CREATE DATABASE converter;
CREATE USER postgres WITH PASSWORD 'postgres';
GRANT ALL PRIVILEGES ON DATABASE converter TO postgres;
```

### 3. 프로젝트 실행

```bash
# 저장소 클론
git clone <repository-url>
cd tcpconverter

# 빌드 및 실행
./gradlew bootRun
```

### 4. 애플리케이션 확인

```bash
# 헬스체크
curl -X GET http://localhost:8080/actuator/health
```

## 📖 API 문서

### 🔐 인증 API

#### 토큰 발급
```http
POST /api/auth/token
Content-Type: application/json

{
  "client_id": "your-client-id",
  "client_secret": "your-client-secret"
}
```

**응답:**
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

#### 토큰 갱신
```http
POST /api/auth/refresh
Content-Type: application/json

{
  "refresh_token": "your-refresh-token"
}
```

#### 토큰 상태 조회
```http
GET /api/auth/token/status?token=your-token
```

### 🔄 변환 API

#### TCP 메시지 변환
```http
POST /api/convert/parse
Content-Type: text/plain
Authorization: Bearer your-access-token

000657 ISATKKEYEXAMPLE1234ISCD01001APINAMEEXAMPLE...
```

**응답:**
```json
{
  "msgLen": "000657",
  "fintechIsatk": "ISATKKEYEXAMPLE1234",
  "fintechIscd": "ISCD01",
  "apiNm": "APINAMEEXAMPLE",
  "apiSvcCd": "QSD_501",
  "nestedObject": {
    "field1": "value1",
    "field2": "value2"
  },
  "arrayField": [
    {
      "item1": "value1",
      "item2": "value2"
    }
  ]
}
```

## 🔧 설정

### application.properties

```properties
# 애플리케이션 설정
spring.application.name=converter

# 데이터베이스 설정
spring.datasource.url=jdbc:postgresql://localhost:5432/converter
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA 설정
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

### JWT 설정

JWT 토큰 설정은 `JwtUtil` 클래스에서 관리됩니다:
- **Access Token**: 1시간 유효
- **Refresh Token**: 30일 유효
- **알고리즘**: HS256

## 🧪 테스트

### 단위 테스트 실행

```bash
# 모든 테스트 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests ApiConvertControllerTest
```

### 테스트 예제

```java
@Test
@WithMockUser
@DisplayName("TCP 메시지 파싱 테스트")
void parseTcpMessageExample() throws Exception {
    String tcpMsg = "000657ISATKKEYEXAMPLE1234...";
    
    mockMvc.perform(post("/api/convert/parse")
            .contentType(MediaType.TEXT_PLAIN)
            .content(tcpMsg))
            .andExpect(status().isOk());
}
```

## 📁 프로젝트 구조

```
tcpconverter/
├── 🏗️ src/main/java/com/example/tcpconverter/
│   ├── 🔐 auth/                          # 인증 관련
│   │   ├── config/SecurityConfig.java    # Spring Security 설정
│   │   ├── controller/AuthController.java # 인증 API
│   │   ├── service/                      # 인증 서비스
│   │   ├── entity/                       # 인증 엔티티
│   │   └── util/JwtUtil.java            # JWT 유틸리티
│   ├── 🔄 converter/                     # 변환 관련
│   │   ├── controller/ApiConvertController.java # 변환 API
│   │   ├── service/TcpMessageParseService.java  # 파싱 서비스
│   │   ├── parser/TcpMessageParser.java         # 메시지 파서
│   │   ├── entity/                              # 변환 엔티티
│   │   └── repository/                          # 데이터 접근
│   └── 📱 TcpConverterApplication.java   # 메인 애플리케이션
├── 🧪 src/test/                          # 테스트 코드
├── 📄 build.gradle                       # 빌드 설정
└── 📋 README.md                          # 프로젝트 문서
```

### 핵심 컴포넌트

| 컴포넌트 | 역할 | 설명 |
|---------|------|------|
| **AuthController** | 인증 API | JWT 토큰 발급, 갱신, 폐기 |
| **ApiConvertController** | 변환 API | TCP 메시지 변환 요청 처리 |
| **TcpMessageParseService** | 파싱 서비스 | 비즈니스 로직 및 검증 |
| **TcpMessageParser** | 메시지 파서 | 실제 파싱 로직 구현 |
| **SecurityConfig** | 보안 설정 | JWT 기반 인증 설정 |
| **JwtUtil** | JWT 유틸리티 | 토큰 생성, 검증, 파싱 |

## 🤝 기여하기

프로젝트에 기여해 주셔서 감사합니다! 

### 기여 방법

1. **Fork** 프로젝트
2. **Feature Branch** 생성 (`git checkout -b feature/amazing-feature`)
3. **Commit** 변경사항 (`git commit -m 'Add amazing feature'`)
4. **Push** to Branch (`git push origin feature/amazing-feature`)
5. **Pull Request** 생성

### 코딩 컨벤션

- **Java**: Google Java Style Guide 준수
- **Commit**: Conventional Commits 사용
- **Testing**: 모든 새로운 기능에 대한 테스트 코드 작성

---