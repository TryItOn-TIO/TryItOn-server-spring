#!/bin/bash
BUILD_JAR=$(ls /home/ec2-user/app/*.jar)
JAR_NAME=$(basename $BUILD_JAR)
echo "> build 파일명: $JAR_NAME" >> /home/ec2-user/app/deploy.log

echo "> build 파일 복사" >> /home/ec2-user/app/deploy.log
DEPLOY_PATH=/home/ec2-user/app/
cp $BUILD_JAR $DEPLOY_PATH

echo "> 현재 실행중인 애플리케이션 pid 확인" >> /home/ec2-user/app/deploy.log
CURRENT_PID=$(pgrep -f $JAR_NAME)

if [ -z $CURRENT_PID ]
then
  echo "> 현재 구동중인 애플리케이션이 없으므로 종료하지 않습니다." >> /home/ec2-user/app/deploy.log
else
  echo "> kill -15 $CURRENT_PID"
  kill -15 $CURRENT_PID
  sleep 5
fi

DEPLOY_JAR=$DEPLOY_PATH$JAR_NAME
echo "> DEPLOY_JAR 배포"    >> /home/ec2-user/app/deploy.log
# 개발 서버에서는 dev 프로파일 사용 (RDS + AWS Secrets Manager)
nohup java --server.address=0.0.0.0 -Dspring.profiles.active=dev -jar $DEPLOY_JAR >> /home/ec2-user/app/deploy.log 2>/home/ec2-user/app/deploy_err.log &