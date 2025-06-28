#!/bin/bash

APP_DIR="/home/ec2-user/app"
JAR_NAME="application.jar"
DEPLOY_LOG="$APP_DIR/deploy.log"
ERROR_LOG="$APP_DIR/deploy_err.log"
APP_LOG="$APP_DIR/application.log" # 애플리케이션 자체 로그 파일

echo "### DEPLOYMENT SCRIPT START $(date +%Y-%m-%d-%H:%M:%S) ###" > $DEPLOY_LOG

echo "> 현재 실행중인 애플리케이션 pid 확인" >> $DEPLOY_LOG
# 'application.jar' 라는 이름으로 실행된 프로세스를 찾습니다.
CURRENT_PID=$(pgrep -f "application.jar")

if [ -z "$CURRENT_PID" ]; then
    echo "> 현재 구동중인 애플리케이션이 없으므로 종료하지 않습니다." >> $DEPLOY_LOG
else
    echo "> 실행중인 애플리케이션 종료 (PID: $CURRENT_PID)" >> $DEPLOY_LOG
    kill -15 $CURRENT_PID
    sleep 5
fi

JAR_PATH=$APP_DIR/$JAR_NAME
echo "> 새 애플리케이션 배포: $JAR_PATH" >> $DEPLOY_LOG

# 기존 에러 로그 파일 삭제
rm -f $ERROR_LOG

# Spring Boot 애플리케이션 로그를 별도의 파일로 리다이렉션합니다.
nohup java -Djava.security.egd=file:/dev/./urandom \
           -Dspring.profiles.active=dev \
           -Dserver.address=0.0.0.0 \
           -Djava.net.preferIPv4Stack=true \
           -jar $JAR_PATH > $APP_LOG 2> $ERROR_LOG &

echo "### DEPLOYMENT SCRIPT END $(date +%Y-%m-%d-%H:%M:%S) ###" >> $DEPLOY_LOG