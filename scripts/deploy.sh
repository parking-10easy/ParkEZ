echo "--------------- 서버 배포 시작 -----------------"
docker stop parkez-server || true
docker rm parkez-server || true
docker pull 529088278674.dkr.ecr.ap-northeast-2.amazonaws.com/parkez-server-ecr:latest
docker run -d --name parkez-server -p 8080:8080 -e SPRING_PROFILES_ACTIVE=prod 529088278674.dkr.ecr.ap-northeast-2.amazonaws.com/parkez-server-ecr:latest
echo "--------------- 서버 배포 끝 -----------------"