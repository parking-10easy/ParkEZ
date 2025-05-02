
<div align="center">
<img width="1000" alt="Image" src="https://github.com/user-attachments/assets/c858f5b3-6ea5-4e64-a9ed-9f43c498585e" /> 
</div>

<div align="left">

## 🅿️ ParkEZ: 쉽고 빠른 통합 주차 플랫폼 팀 프로젝트

### 🙋‍♀️ ParkEZ 팀 프로젝트 설명
- **ParkEZ 팀 프로젝트**는 <u>사용자와 주차장 소유자를 위한 예약, 결제, 정산이 가능한 통합 주차 플랫폼을 구현</u>한 팀 프로젝트입니다.
- **팀원 : 조예인, 정준호, 이민정, 전서연, 장윤혁**
- 기간 : 2025.04.01 - 2025.05.06

<br>

📑 팀 노션 : [ParkEZ](https://teamsparta.notion.site/10-1ce2dc3ef514819590ddf6be7e3cbcdd)<p>
📑 팀 브로셔 : [10조 - Park10EZ](https://www.notion.so/teamsparta/10-Park10EZ-1e32dc3ef51480d29d85c60483187dae) <p>
📑 도메인 : [parkez.click](https://parkez.click/swagger-ui/index.html) <p>
📑 시연영상 : [ParkEZ 시연영상](https://youtu.be/VvcWqa1_23U)

<br>

### 🧑‍🧑‍🧒‍🧒 역할 분담
🐣 [조예인](https://github.com/codingTrip-IT) : 주차공간 API, 리뷰 API, Redis Pub/Sub을 이용한 메일 전송 기능 <p>
🦦 [정준호](https://github.com/juno-soodal) : 유저 API, JWT 인증/인가, 카카오 로그인 기능, 프로모션 API + 동시성 제어 적용, AWS 아키텍처 구성, GitHub Actions 활용한 CI/CD <p>
🐶️ [이민정](https://github.com/minjonyyy) : 결제 API, 정산 API, AWS S3 이미지 기능, Toss payments API 연동, Redis를 이용한 예약 대기열 기능  <p>
🗿 [전서연](https://github.com/MythologyDevSeoyeon) : 주차장 API, 네이버 로그인 기능, AWS Lambda를 이용한 공공데이터 저장, 카카오맵 API 연동하여 주소 저장  <p>
🎅 [장윤혁](https://github.com/Jangyounhyuk) : 예약 API + 동시성 제어 적용, 주차장 다건 조회 캐싱, Spring Batch를 이용한 정산 기능 <p>

<br>

## 🛠 목차

1. [🧰 기술스택](#-기술스택)
2. [🗂 시스템 아키텍처](#-시스템-아키텍처)
3. [⚙️ CI/CD](#⚙️-cicd)
4. [🧩 와이어프레임](#-와이어프레임)
5. [🧾 ERD](#-erd)
6. [📡 API 명세](#-api-명세)
7. [🚀 기술 고도화](#-기술-고도화)
8. [🧪 성능 테스트](#-성능-테스트)
9. [📈 테스트 커버리지](#-테스트-커버리지)
10. [🧯 트러블슈팅](#-트러블슈팅)
11. [🔭 향후 발전 방향](#-향후-발전-방향)
12. [💬️ 회고](#💬️-회고)

<br>


## 🧰 기술스택

### 💻 Language / Backend
<img src="https://img.shields.io/badge/java-007396?style=for-the-badge&logo=java&logoColor=white"> <img src="https://img.shields.io/badge/springboot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"> <img src="https://img.shields.io/badge/JPA-59666C?style=for-the-badge&logo=hibernate&logoColor=white"> <img src="https://img.shields.io/badge/QueryDSL-409EFF?style=for-the-badge&logoColor=white"> 

### ⚙️ Database
<img src="https://img.shields.io/badge/mysql-4479A1?style=for-the-badge&logo=mysql&logoColor=white"> <img src="https://img.shields.io/badge/redis-DC382D?style=for-the-badge&logo=redis&logoColor=white">

### 🔍 Test
<img src="https://img.shields.io/badge/k6-7D64FF?style=for-the-badge&logo=k6&logoColor=white"> <img src="https://img.shields.io/badge/JUnit-25A162?style=for-the-badge&logo=junit5&logoColor=white"> <img src="https://img.shields.io/badge/Mockito-8A2BE2?style=for-the-badge&logoColor=white"> <img src="https://img.shields.io/badge/JMeter-D22128?style=for-the-badge&logo=apachejmeter&logoColor=white"> <img src="https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black">

### 🔐 Security
<img src="https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white"> <img src="https://img.shields.io/badge/Spring Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white">

### 🎨 Collaboration Tool
<img src="https://img.shields.io/badge/github-181717?style=for-the-badge&logo=github&logoColor=white"> <img src="https://img.shields.io/badge/notion-000000?style=for-the-badge&logo=notion&logoColor=white">  <img src="https://img.shields.io/badge/slack-4A154B?style=for-the-badge&logo=slack&logoColor=white"> <img src="https://img.shields.io/badge/figma-F24E1E?style=for-the-badge&logo=figma&logoColor=white"> <img src="https://img.shields.io/badge/dbdiagram.io-1B222D?style=for-the-badge&logoColor=white">

### 🛠 Deployment & Distribution
<img src="https://img.shields.io/badge/GitHub%20Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white"> <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white"> <img src="https://img.shields.io/badge/AWS-232F3E?style=for-the-badge&logo=amazonaws&logoColor=white"> <img src="https://img.shields.io/badge/EC2-F58534?style=for-the-badge&logo=amazonec2&logoColor=white"> <img src="https://img.shields.io/badge/ECR-FF6B00?style=for-the-badge&logo=amazonaws&logoColor=white"> <img src="https://img.shields.io/badge/RDS-527FFF?style=for-the-badge&logo=amazonrds&logoColor=white"> <img src="https://img.shields.io/badge/S3-569A31?style=for-the-badge&logo=amazons3&logoColor=white"> <img src="https://img.shields.io/badge/Route%2053-8A63D2?style=for-the-badge&logo=amazonroute53&logoColor=white"> <img src="https://img.shields.io/badge/ALB-FF9900?style=for-the-badge&logo=loadbalancer&logoColor=white"> <img src="https://img.shields.io/badge/ElastiCache-0052CC?style=for-the-badge&logo=amazonaws&logoColor=white"> <img src="https://img.shields.io/badge/SES-FFB300?style=for-the-badge&logo=amazonses&logoColor=white"> <img src="https://img.shields.io/badge/Lambda-FF7F0E?style=for-the-badge&logo=awslambda&logoColor=white">

<br><br>

## 🗂 시스템 아키텍처
<img width="895" alt="ParkEZ 시스템 아키텍처 이미지" src="https://github.com/user-attachments/assets/162c44f3-5042-4a13-b581-2c4a0667fbe0" />

<br>

## ⚙️ CI/CD

<img width="644" alt="ParkEZ CICD" src="https://github.com/user-attachments/assets/ced49e9b-934d-49aa-9c6d-473a01286e52" />

<br>

## 🧩 와이어프레임
<img width="1000" alt="ParkEZ 와이어프레임" src="https://github.com/user-attachments/assets/73c4a748-373e-4f2a-b734-21a06687e219" />

<br>

## 🧾 ERD
<img width="1000" alt="Image" src="https://github.com/user-attachments/assets/c32c3ef5-5723-4b11-b910-448e393177c1" />

<br>

## 📡 API 명세
📑 Swagger 참조 : [parkez.click](https://parkez.click/swagger-ui/index.html) <p>

<br>

## 🚀 기술 고도화
<details>
<summary>☁️ AWS Lambda로 외부 API 호출 자동화</summary>

- **도입 배경**: 하루 1회 실행 작업을 위해 서버를 24시간 유지하는 것은 비효율 → 서버리스 환경으로 전환
- **기술 선택**: `AWS Lambda + EventBridge` 조합으로 서버 관리 부담 없이 스케줄링 자동화

#### ✅ JavaHandler 방식 사용 이유
- 빠른 콜드 스타트 (Spring 초기화 제거)
- 패키지 경량화 → 배포 간편
- 실행 시간 단축 → 비용 절감

#### ✅ 네트워크 구성 요약
- **VPC Peering**: Lambda ↔ RDS 간 통신 위해 VPC 간 연결
- **프라이빗 서브넷**: Lambda 실행 위치, 아웃바운드만 허용
- **퍼블릭 서브넷 + IGW**: 외부 공공 API 호출 가능하게 구성
- **NAT Gateway**: 프라이빗 서브넷에서도 외부 API 접근 가능

</details>

<details>
<summary>💰 Spring Batch 기반 정산 처리</summary>

- **도입 배경**: 수만 건 이상 데이터를 효율적으로 처리하고, 이력 관리와 재처리가 가능한 구조 필요

#### ✅ 기술 선택 이유
- **대용량 처리 최적화**
  - 거래 데이터를 Chunk 단위로 분할 처리 → 메모리 효율성 확보
- **구조적 설계 (Job / Step 분리)**
  - 정산 프로세스를 명확하게 분리하여 유지보수 용이
- **이력 관리 자동화**
  - Job 실행 내역과 상태를 DB에 자동 기록
- **스케줄링 및 재시도 지원**
  - 정해진 시각에 정산 자동 수행
  - 실패한 Step만 재처리 가능
- **Chunk 기반 트랜잭션 제어**
  - 실패한 Chunk만 rollback → 전체 작업에 영향 없이 복구 가능

</details>
<details>
<summary>📧 Redis Pub/Sub + Amazon SES 기반 메일 전송</summary>

#### 📌 기존 구조의 한계점
- 이메일 전송 방식: SMTP + JavaMailSender
  - 인증·전송 신뢰도 낮음
  - 스팸 처리 우려
  - TLS / 포트 제약 존재

- 이벤트 처리 방식: Spring @EventListener
  - 동기 처리 방식 → 비즈니스 로직과 알림 로직 강결합
  - 구조 확장 및 재사용 어려움

#### 🛠 개선 방향

- **이벤트 비동기 처리**:  
  `@EventListener` → `redisTemplate.convertAndSend()`로 전환  
  → Redis Pub/Sub 기반 구조로 분리

- **신뢰성 있는 이메일 전송**:  
  `JavaMailSender` → `SesClient` (AWS SDK v2) 전환
  - 도메인 인증 + DKIM 서명
  - `EmailTemplateService`를 통한 템플릿 기반 메일 구성

#### 🚀 기대 효과

| 항목             | 개선 내용                                       |
|------------------|------------------------------------------------|
| 신뢰도 향상      | SES + DKIM 기반으로 전송률 및 수신 성공률 향상     |
| 확장성 확보      | FCM / SES / Slack 등 다양한 채널 연동 용이         |
| 유지보수 편의성  | 실패 내역 로깅, 통계 분석, 재처리 구조 적용 가능    |

#### 🧾 실행 결과
- 기존 SMTP 기반 구조 대비 **전송 신뢰성** 및 **유지보수 편의성** 대폭 개선
- **알림 로직의 책임 분리**로 구조 유연성과 확장성 확보

</details>

<br>

## 🧪 성능 테스트
### 예약 생성 시 동시성 제어 테스트
- **테스트 시나리오**  
  총 10명의 사용자가 동시에 예약 요청
- **응답 메시지**
  - `"예약 성공"` : 실제 예약 완료
  - `"대기열 등록됨"` : 예약 실패 후 대기열 등록
#### 🔍 테스트 결과
| 구분              | 응답 결과                           |
|-------------------|--------------------------------------|
| ✅ 동시성 제어 이전 | 10명 모두 `"예약 성공"` → 중복 예약 발생 |
| ✅ 동시성 제어 이후 | 1명 `"예약 성공"` + 9명 `"대기열 등록됨"` → 정상 처리됨 |
- **결론**: 동시성 제어를 통해 하나의 주차 공간에 대한 중복 예약을 방지하고, 후순위 사용자를 대기열에 안전하게 등록할 수 있도록 개선되었습니다.
---
### 주차장 조회 성능 테스트
📊 JMeter를 활용한 주차장 조회 성능 테스트

### 🧪 테스트 개요
- **목표**: Redis 캐시 도입 전/후 성능 비교
- **대상**: 약 10만 건의 주차장 데이터
- **도구**: Apache JMeter
- **조건**: 동일한 Thread 수, Ramp-up 시간, Delay 설정


### 📈 테스트 결과

| 항목             | 캐시 적용 전   | 캐시 적용 후   | 변화율         |
|------------------|----------------|----------------|----------------|
| **Throughput**   | 4.2/sec        | 31.9/sec       | 🔼 +659.5% 증가 |
| **평균 응답시간** | 61,641 ms      | 15,678 ms      | 🔽 -74.6% 감소 |


### ✅ 분석
- Redis 캐시 적용으로 **처리량(Throughput)이 6배 이상 증가**
- **응답시간이 1/4 수준으로 단축되어 사용자 경험 개선**
- 대용량 데이터에 대해 캐시 적용 시 확연한 성능 향상 확인


<br>

## 📈 테스트 커버리지
<img width="800" alt="ParkEZ 테스트 커버리지 이미지" src="https://github.com/user-attachments/assets/e3235588-ca93-45e1-98dd-a26a90e2992a" />

## 🧯 트러블슈팅
### 1.  OSIV 설정 차이로 인한 상태 변경 미반영 문제
❗ 문제 상황
- 로컬에서는 정상 동작하던 예약 상태 변경 기능이, 개발 서버에서는 DB에 반영되지 않음
- 예시: `reservation.cancel()` 호출 후에도 `ReservationStatus.CANCELED`가 DB에 반영되지 않음

🔍 원인 분석
- **OSIV 설정 차이**
  - **로컬**: `spring.jpa.open-in-view=true` (기본값)  
    → 트랜잭션 종료 이후에도 영속성 컨텍스트 접근 가능
  - **개발 서버**: `open-in-view=false`  
    → 트랜잭션 종료 시 영속성 컨텍스트도 종료

- **구조적 문제**
  - `@Transactional(readOnly = true)`가 적용된 **Reader**에서 조회한 엔티티는 **Detached 상태**일 수 있음
  - 이후 Writer에서 상태 변경 메서드만 호출하면 **JPA의 dirty checking이 작동하지 않음**

```java
// ReservationService
Reservation reservation = reservationReader.findMyReservation(...); // ReadOnly 트랜잭션
        reservationWriter.cancel(reservation); // 내부에서 reservation.cancel() 호출 → 변경 감지 안 됨
```
✅ 해결 방안

✅ 단기 해결: 명시적 save 호출

```java
public void cancel(Reservation reservation) {
  reservation.cancel();
  reservationRepository.save(reservation); // Detached 객체 merge
}
```

✅ 근본적 해결: 트랜잭션 범위 재설계
```java
// ReservationService
@Transactional
public void cancelReservation(...) {
  Reservation reservation = reservationReader.findMyReservation(...);
  reservation.cancel(); // 영속 상태에서 변경 → dirty checking 작동
}
```
🎯 결과

- 개발 환경에서도 예약 상태 변경이 **정상 반영됨**
- 구조적으로 역할 분리가 명확해짐:
  - **Reader** → 조회 책임
  - **Writer** → 도메인 변경 책임
  - **Service** → 트랜잭션 관리 및 흐름 조율

### 2.  동시성 테스트  - 커넥션 풀 고갈 발생
🔍 문제 발견
- 동시성 테스트 수행 중, **테스트가 끝나지 않고 대기 상태 지속**
- 커넥션 풀 고갈로 인한 **타임아웃 현상 발생**

⚠️ 원인 분석
- 테스트 메서드에 `@Transactional`이 적용되어 **전체 테스트가 하나의 트랜잭션으로 실행됨**
- 내부 메서드도 동일 트랜잭션에 묶여 **커밋/롤백 지연**
- 결과적으로 DB 락이 해제되지 않고, **모든 스레드가 락 대기 상태에 빠짐**
- 커넥션 풀 부족 → **새로운 커넥션 생성 불가 → 테스트 타임아웃**

✅ 해결 방안 및 결과
- `@Transactional` 어노테이션 제거
  - 각 스레드가 **독립된 트랜잭션**으로 실행되어 DB 락 정상 해제
- 테스트 종료 후 `deleteAllInBatch()` 사용
  - 테스트 간 **데이터 잔존 문제 방지**
  - 트랜잭션 제거로 인한 데이터 정리를 명시적으로 수행

<br>

## 🔭 향후 발전 방향
### 🎟️ 프로모션 쿠폰 발급 처리 전략

✅ 현재 구조
DB의 Pessimistic Lock (비관적 락) 을 활용하여 쿠폰 재고를 안전하게 제어하고 있습니다.

- **장점**
  - 다중 사용자 환경에서도 중복 발급 없이 재고 제어 가능

- **단점**
  - 특정 프로모션 발급 요청이 몰리면 해당 레코드에 락이 걸려  
    → **다른 트랜잭션이 지연**되고  
    → **쿠폰 조회 및 관련 로직에 병목**이 발생할 수 있음

🚧 개선 방향

##### 🔐 분산락 적용 예정

- **목표**: 특정 프로모션 단위로 분산락 적용 (ex. Redis 기반)
- **효과**:
  - **발급에만 락을 제한**하고 조회는 락 영향 없이 처리 가능
  - DB 레벨 병목 없이 **확장성 높은 동시성 제어 가능**

##### ⚡ 발급과 관리 분리

- **발급 요청은 동기 처리**, 사용자에게 빠른 응답 제공
- **발급 기록은 비동기 처리**, 시스템 부하 분산
- **예시**: Kafka, SQS 등 메시지 큐를 통해 처리 분리


### 🔔 알림 시스템 구조 개선 방향

✅ 현재 구조
현재 알림 시스템은 Redis Pub/Sub 기반으로, 이벤트 발생 시 알림 메시지를 발행하고, 구독자에서 이메일(SES)을 전송하는 구조입니다.  
이 구조는 간단하고 빠르지만, 다음과 같은 운영상의 한계가 존재합니다.

❗ 문제점

##### 📌 메시지 유실 가능성
- Redis Pub/Sub은 실시간 메시지 전파만 지원하며, 메시지를 저장하지 않음
- 구독자가 다운된 경우 메시지가 유실되어 알림 손실 발생

##### 📌 재처리 불가
- 알림 발송 실패 시 로그만 남고, 별도의 재시도 로직이 없어 운영 신뢰성 부족


✅ 개선 방향

##### 📦 메시지 영속화 기반 구조 고려
- Redis Streams 또는 Kafka 등 **영속 메시징 큐로 교체 또는 보완 시스템 도입** 예정
- 구독자 장애 시에도 **재수신 및 복구 가능**

##### 🔁 실패 내역 저장 및 재처리 도입
- 발송 실패 이력을 **Redis List 또는 DB Table** 등에 저장
- **Scheduled Task 또는 Spring Batch**를 활용한 재처리 구조 적용
- 장기적으로는 **Kafka DLQ(Dead Letter Queue)** 도입 고려

<br>


## 💬️ 회고

🐣 [조예인](https://codingtrip.tistory.com) : 긴 여정을 좋은 튜터님, 팀원분들을 만나 잘 마무리할 수 있었습니다. 정말 감사합니다. 프로젝트 이후에도 같이 만나서복습, 개선 및 발전하고 싶습니다.  <p>
🦦 [정준호](https://jun5-soodal.tistory.com) : 팀원들과의 지속적인 커뮤니케이션과 역할 분담을 통해 점차 안정적인 개발 흐름을 만들어갈 수 있었습니다. 짧은 기간이었지만 기술뿐만 아니라 협업 역량과 책임감까지 함께 키울 수 있었던 값진 시간이었습니다. <p>
🐶️ [이민정](https://velog.io/@minjonyyy/posts) : 처음으로 기획부터 배포까지 해본 프로젝트인 만큼 어려운 점도 많았지만 배운 점도 많았던 시간이었습니다. 프로젝트는 끝났지만, 제가 구현하지 않은 부분에 대해서도꼭 복습하며 공부해야겠다고 생각했습니다. 좋은 팀원들과 튜터님과 소통하며 잘 마무리할 수 있어서 감사했습니다.   <p>
🗿 [전서연](https://dev-leonie.tistory.com) : 한 달 동안 최종 프로젝트를 진행하며 좋은 경험을 하게 되어 뜻 깊은 시간이었습니다. 어려운 부분이 많았지만 좋은 팀원과 튜터님 덕분에 잘 마무리 할 수 있게 되었습니다. 10조 취뽀 화이팅 <p>
🎅 [장윤혁](https://velog.io/@hyuk905/posts) :  최종 프로젝트인 만큼 좋은 팀원들과 함께 여러 가지 시도를 다양하게 해 볼 수 있어서 좋았습니다. 제가 구현하지 못한 부분에 대해서 복습하며 직접 구현해 볼 예정입니다.<p>

</div>
