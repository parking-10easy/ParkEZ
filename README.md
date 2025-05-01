<img width="500" alt="Image" src="https://github.com/user-attachments/assets/c858f5b3-6ea5-4e64-a9ed-9f43c498585e" /> <p>

## 🅿️ ParkEZ: 쉽고 빠른 통합 주차 플랫폼 팀 프로젝트

### 🙋‍♀️ ParkEZ 팀 프로젝트 설명
- **ParkEZ 팀 프로젝트**는 <u>사용자와 주차장 소유자를 위한 예약, 결제, 정산이 가능한 통합 주차 플랫폼을 구현</u>한 팀 프로젝트입니다.
- **팀원 : 조예인, 정준호, 이민정, 전서연, 장윤혁**
- 기간 : 2025.04.01 - 2025.05.06

<br>

### 🧑‍🧑‍🧒‍🧒 역할 분담
🐣 [조예인](https://github.com/codingTrip-IT) : 주차공간 API, 리뷰 API, Redis Pub/Sub을 이용한 메일 전송 기능 <p>
🦦 [정준호](https://github.com/juno-soodal) : 유저 API, JWT 인증/인가, 카카오 로그인 기능, 프로모션 API + 동시성 제어 적용, AWS 아키텍처 구성, GitHub Actions 활용한 CI/CD <p>
🐶️ [이민정](https://github.com/minjonyyy) : 결제 API, 정산 API, AWS S3 이미지 기능, Toss payments API 연동, Redis를 이용한 예약 대기열 기능  <p>
🗿 [전서연](https://github.com/MythologyDevSeoyeon) : 주차장 API, 네이버 로그인 기능, AWS Lambda를 이용한 공공데이터 저장, 카카오맵 API 연동하여 주소 저장  <p>
🎅 [장윤혁](https://github.com/Jangyounhyuk) : 예약 API + 동시성 제어 적용, 주차장 다건 조회 캐싱, Spring Batch를 이용한 정산 기능 <p>


<br>

📑 진행 및 회의 기록 : [ParkEZ](https://www.notion.so/teamsparta/10-1ce2dc3ef514819590ddf6be7e3cbcdd) 노션에서 진행 <p>
📑 팀 브로셔 : [10조 - Park10EZ](https://www.notion.so/teamsparta/10-Park10EZ-1e32dc3ef51480d29d85c60483187dae) <p>
📑 도메인 : [parkez.click](https://parkez.click/swagger-ui/index.html) <p>
📑 시연영상 : [ParkEZ 시연영상](https://youtu.be/eW1-_Kum_6k?si=cDaZrJRaDxDhjrR3)
<br>

## 🛠 목차

1. [📚 STACKS](#-stacks)
2. [👩🏻‍ 시스템 아키텍처](#-시스템-아키텍처)
3. [👩🏻‍ CI/CD](#-cicd)
4. [👩🏻‍ 와이어프레임](#-와이어프레임)
5. [👩 ERD](#-erd)
6. [👩 API 명세](#-api-명세)
7. [👩 기술 고도화](#-기술-고도화)
8. [⚗️ 성능 테스트](#-성능-테스트)
9. [⚗️ 테스트 커버리지](#-테스트-커버리지)
10. [💥 트러블슈팅](#-트러블슈팅)
11. [⚗️ 향후 발전 방향](#-향후-발전-방향)

<br>


## 📚 STACKS

### 💻 Language / Backend
<img src="https://img.shields.io/badge/java-007396?style=for-the-badge&logo=java&logoColor=white"><img src="https://img.shields.io/badge/spring-6DB33F?style=for-the-badge&logo=spring&logoColor=white"><img src="https://img.shields.io/badge/springboot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"><img src="https://img.shields.io/badge/gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white">

### ⚙️ Database
<img src="https://img.shields.io/badge/mysql-4479A1?style=for-the-badge&logo=mysql&logoColor=white"><img src="https://img.shields.io/badge/redis-DC382D?style=for-the-badge&logo=redis&logoColor=white">

### 🔍 Test
<img src="https://img.shields.io/badge/k6-7D64FF?style=for-the-badge&logo=k6&logoColor=white">

### 🔐 Security
<img src="https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white">

### 🔌 Tools
<img src="https://img.shields.io/badge/git-F05032?style=for-the-badge&logo=git&logoColor=white"><img src="https://img.shields.io/badge/github-181717?style=for-the-badge&logo=github&logoColor=white"><img src="https://img.shields.io/badge/intellijidea-000000?style=for-the-badge&logo=intellijidea&logoColor=white">

### 🎨 Communication
<img src="https://img.shields.io/badge/notion-000000?style=for-the-badge&logo=notion&logoColor=white"><img src="https://img.shields.io/badge/slack-4A154B?style=for-the-badge&logo=slack&logoColor=white"><img src="https://img.shields.io/badge/figma-F24E1E?style=for-the-badge&logo=figma&logoColor=white"><img src="https://img.shields.io/badge/dbdiagram.io-1B222D?style=for-the-badge&logoColor=white">

### 🔨 Other Tools
<img src="https://img.shields.io/badge/java mail-007396?style=for-the-badge&logo=openjdk&logoColor=white">

### 🛠 Deployment & Distribution
<img src="https://img.shields.io/badge/github actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white"><img src="https://img.shields.io/badge/docker-2496ED?style=for-the-badge&logo=docker&logoColor=white"><img src="https://img.shields.io/badge/aws-232F3E?style=for-the-badge&logo=amazonaws&logoColor=white"><img src="https://img.shields.io/badge/ec2-FF9900?style=for-the-badge&logo=amazonec2&logoColor=white"><img src="https://img.shields.io/badge/Amazon_ECR-FF9900?style=for-the-badge&logo=amazonaws&logoColor=white"><img src="https://img.shields.io/badge/rds-527FFF?style=for-the-badge&logo=amazonrds&logoColor=white"><img src="https://img.shields.io/badge/s3-569A31?style=for-the-badge&logo=amazons3&logoColor=white"><img src="https://img.shields.io/badge/route 53-8A63D2?style=for-the-badge&logo=amazonroute53&logoColor=white"><img src="https://img.shields.io/badge/application load balancer-FF9900?style=for-the-badge&logo=loadbalancer&logoColor=white"><img src="https://img.shields.io/badge/elasticache-FF4F8B?style=for-the-badge&logo=redis&logoColor=white"><img src="https://img.shields.io/badge/amazon ses-FF9900?style=for-the-badge&logo=amazonses&logoColor=white"><img src="https://img.shields.io/badge/aws lambda-FF9900?style=for-the-badge&logo=awslambda&logoColor=white">

<br><br><br>

## 👩🏻‍ 시스템 아키텍처
<img width="864" alt="ParkEZ 시스템 아키텍처" src="https://github.com/user-attachments/assets/b4079a76-5b7c-4868-ad72-56030e9937d2" />
<br>

## 👩🏻‍ CI/CD
<img width="644" alt="ParkEZ CICD" src="https://github.com/user-attachments/assets/ced49e9b-934d-49aa-9c6d-473a01286e52" />

<br>

## 👩 와이어프레임
<img width="1001" alt="ParkEZ 와이어프레임" src="https://github.com/user-attachments/assets/73c4a748-373e-4f2a-b734-21a06687e219" />

<br>

## 👩 ERD
<img width="552" alt="Image" src="https://github.com/user-attachments/assets/c32c3ef5-5723-4b11-b910-448e393177c1" />

<br>

## 👩🏻‍ API 명세

### 유저 API
<img width="700" alt="ParkEZ 유저 API" src="https://github.com/user-attachments/assets/0e5c9daa-f652-400d-a6d6-02a5fda54072" />

### 주차장 API
<img width="700" alt="ParkEZ 주차장 API" src="https://github.com/user-attachments/assets/ff9e2e0a-7b0c-48b7-a3dc-d4c82906574b" />

### 주차공간 API
<img width="700" alt="ParkEZ 주차공간 API" src="https://github.com/user-attachments/assets/e0289dd8-9ee3-413d-a5c4-e28368fe6ca5" />

### 예약 API
<img width="700" alt="ParkEZ 예약 API" src="https://github.com/user-attachments/assets/a0f4a0b0-af1c-4436-a6a2-d88af8723cfb" />

### 결제 API
<img width="700" alt="ParkEZ 결제 API" src="https://github.com/user-attachments/assets/14d585c4-c58b-4544-a163-3b13bda5ca37" />

### 리뷰 API
<img width="700" alt="ParkEZ 리뷰 API" src="https://github.com/user-attachments/assets/831c0d2f-656a-439e-aad4-50a3da943164" />

### 정산 API
<img width="700" alt="ParkEZ 정산 API" src="https://github.com/user-attachments/assets/8c01a086-da25-4c5b-a134-b21221af8459" />

### 이미지 API
<img width="700" alt="ParkEZ 이미지 API" src="https://github.com/user-attachments/assets/dd0065d2-611e-4114-95df-6056b90214a4" />

### 쿠폰/프로모션/관리자 API
<img width="700" alt="ParkEZ 쿠폰:프로모션 API" src="https://github.com/user-attachments/assets/1c90f02b-aa6d-4c68-a4c9-444aec6ed7b0" />
<img width="700" alt="ParkEZ 프로모션:관리자 API" src="https://github.com/user-attachments/assets/8e02ba25-da60-4021-9156-20636735b50a" />

### 대기열 API
<img width="700" alt="ParkEZ 대기열 API" src="https://github.com/user-attachments/assets/f48e0a14-6976-478e-ba2e-5248b1be7944" />

📑 Swagger 참조 : [parkez.click](https://parkez.click/swagger-ui/index.html) <p>

<br> <br>

## 👩🏻‍ 기술 고도화
- [AWS Lambda로 특정 시간에 외부API 호출하기](https://dev-leonie.tistory.com/78)

- [Spring Batch를 이용한 정산 기능](https://velog.io/@hyuk905/%EC%8A%A4%ED%94%84%EB%A7%81-%EB%B0%B0%EC%B9%98Spring-Batch%EC%97%90-%EB%8C%80%ED%95%B4-%EC%95%8C%EC%95%84%EB%B3%B4%EA%B8%B0)
- 
- [Amazon SES](https://codingtrip.tistory.com/137)
- 
- [EventListener → Redis Pub/Sub](https://codingtrip.tistory.com/141)

## 👩🏻‍ 성능 테스트



## ⚗️ 테스트 커버리지
<img width="812" alt="ParkEZ Test Coverage" src="https://github.com/user-attachments/assets/7b911774-cfb5-490f-9d26-0f40b2165e2d" />

## 💥 트러블슈팅
### 1.  동시성 테스트  - 커넥션 풀 고갈 발생
🔎 상황 <p>
동시성 테스트에서 커넥션 풀 고갈로 인해, 테스트가 끝나지 않는 문제가 발생하였습니다. <br>
💥 문제 <p>
테스트 메서드에 @Transacional이 걸려 있어서 전체 테스트가 하나의 트랜잭션으로 실행되었습니다. <br><br>
🚀 해결 <p>
테스트 메서드에서 @Transactional 제거

<br>



## ⚗️ 향후 발전 방향
### 🎟️ 프로모션 쿠폰 발급 처리 전략

<details><summary>✅ 현재 구조</summary>
> DB의 **Pessimistic Lock (비관적 락)** 을 활용하여 쿠폰 재고를 안전하게 제어하고 있습니다.

- **장점**
  - 다중 사용자 환경에서도 중복 발급 없이 재고 제어 가능

- **단점**
  - 특정 프로모션 발급 요청이 몰리면 해당 레코드에 락이 걸려  
    → **다른 트랜잭션이 지연**되고  
    → **쿠폰 조회 및 관련 로직에 병목**이 발생할 수 있음

</details>
<details><summary>🚧 개선 방향</summary>

##### 🔐 분산락 적용 예정

- **목표**: 특정 프로모션 단위로 분산락 적용 (ex. Redis 기반)
- **효과**:
  - **발급에만 락을 제한**하고 조회는 락 영향 없이 처리 가능
  - DB 레벨 병목 없이 **확장성 높은 동시성 제어 가능**

##### ⚡ 발급과 관리 분리

- **발급 요청은 동기 처리**, 사용자에게 빠른 응답 제공
- **발급 기록은 비동기 처리**, 시스템 부하 분산
- **예시**: Kafka, SQS 등 메시지 큐를 통해 처리 분리
</details>
