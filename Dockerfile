# 빌드 단계: Gradle 컨테이너를 사용해 프로젝트 빌드
FROM gradle:7.5.0-jdk8 AS builder
WORKDIR /app
# 프로젝트 소스 전체를 복사합니다.
COPY --chown=gradle:gradle . .
# Gradle을 이용해 bootJar 실행 (테스트는 생략)
RUN gradle bootJar --no-daemon

# 실행 단계: 생성된 JAR 파일을 OpenJDK 컨테이너에 복사
FROM openjdk:8-jre-slim
# 빌드 시 인자로 활성화할 프로파일을 전달할 수 있음 (기본값은 dev)
ARG ACTIVE_PROFILE=dev
ENV ACTIVE_PROFILE=${ACTIVE_PROFILE}
# 생성된 JAR 파일을 복사합니다.
COPY --from=builder /app/build/libs/*.jar app.jar
# 컨테이너 시작 시, 활성화할 스프링 프로파일을 지정하여 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java -jar -Dspring.profiles.active=${ACTIVE_PROFILE} app.jar"]


