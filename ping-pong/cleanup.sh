rm -rf target/logs
mkdir -p target/logs
docker stop ping-pong || true
docker rm -fv ping-pong || true
