#!/bin/bash

# Redis 캐시 초기화 스크립트
# 사용법: ./flush-redis-cache.sh [host] [port]

# 기본값 설정
REDIS_HOST=${1:-"localhost"}
REDIS_PORT=${2:-6379}

echo "Redis 캐시 초기화를 시작합니다..."
echo "호스트: $REDIS_HOST, 포트: $REDIS_PORT"

# Redis 연결 테스트
echo "Redis 연결 테스트 중..."
if ! redis-cli -h $REDIS_HOST -p $REDIS_PORT ping > /dev/null 2>&1; then
    echo "오류: Redis 서버에 연결할 수 없습니다. 호스트와 포트를 확인하세요."
    exit 1
fi

echo "Redis 연결 성공!"

# 캐시 키 패턴 목록
CACHE_PATTERNS=(
    "productDetail:*"
    "categoryProducts:*"
    "mainProducts:*"
    "shared:*"
    "recommend:*"
)

# 각 패턴에 대해 키 삭제
for pattern in "${CACHE_PATTERNS[@]}"; do
    echo "패턴 '$pattern'에 해당하는 키 삭제 중..."
    
    # 키 목록 조회
    KEYS=$(redis-cli -h $REDIS_HOST -p $REDIS_PORT --scan --pattern "$pattern")
    
    # 키가 있는 경우에만 삭제 진행
    if [ -n "$KEYS" ]; then
        COUNT=0
        for key in $KEYS; do
            redis-cli -h $REDIS_HOST -p $REDIS_PORT DEL "$key" > /dev/null
            COUNT=$((COUNT+1))
        done
        echo "  - $COUNT 개의 키 삭제 완료"
    else
        echo "  - 해당 패턴의 키가 없습니다."
    fi
done

echo "캐시 초기화가 완료되었습니다."

# 캐시 통계 출력
echo "현재 Redis 통계:"
redis-cli -h $REDIS_HOST -p $REDIS_PORT info | grep -E "used_memory_human|db0"

echo "완료!"
exit 0
