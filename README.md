Run Redis locally on docker with:
docker run --name myredis -p 6379:6379 -d redis

Then enter Redis CLI with
docker exec -it myredis redis-cli