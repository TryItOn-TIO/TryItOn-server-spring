#!/bin/bash

# 1) 실행 중인 java 프로세스의 PID 목록 조회
PIDS=$(ps -ef | grep '[j]ava -jar' | awk '{print $2}')

# 2) PID가 하나라도 있으면 kill, 없으면 아무 것도 안 함
if [ -n "$PIDS" ]; then
  echo "> Stopping existing Java process(es): $PIDS" >> /home/ec2-user/app/deploy.log
  kill -15 $PIDS
  sleep 5
else
  echo "> No Java process found, skipping stop" >> /home/ec2-user/app/deploy.log
fi

exit 0
