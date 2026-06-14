#!/bin/zsh
join(){
  curl -X POST "http://localhost:$1/peer/join"
}
join 9091
join 9092
join 9093
join 9094
