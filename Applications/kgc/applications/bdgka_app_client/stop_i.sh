START_PORT=9091
END_PORT=$1

for ((port=START_PORT; port<=END_PORT; port++)); do
    pid=$(lsof -t -i:$port)
    [ -n "$pid" ] && kill "$pid"
done
