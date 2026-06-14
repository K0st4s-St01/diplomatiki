#!/bin/zsh
join(){
  curl -X POST "http://localhost:$1/peer/join"
}
join 9091
join 9092
join 9093
join 9094
join 9095
join 9096
join 9097
join 9098
