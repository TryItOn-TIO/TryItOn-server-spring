#!/bin/bash
# HTTP 200 OK 반환 여부 확인
if curl -f http://localhost:8080/actuator/health; then
  echo "Service is healthy"
  exit 0
else
  echo "Service check failed"
  exit 1
fi
