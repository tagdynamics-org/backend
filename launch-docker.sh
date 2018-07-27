#!/usr/bin/env bash

# https://hub.docker.com/r/matiasdahl/backend/
sudo docker pull matiasdahl/backend:latest

export PORT_INSIDE_DOCKER=8080
export PORT_EXPOSED=80
export DATA_DIR=`pwd`/backend/src/test/resources/testdata/3-aggregates/
#export DATA_DIR=`pwd`/../agg-data/

sudo docker run --rm -it \
  -p $PORT_EXPOSED:$PORT_INSIDE_DOCKER \
  -v $DATA_DIR:/data/ \
  -e CORS_ALLOWED_ORIGINS="*" \
  -e CORS_ALLOW_GENERIC_HTTP_REQUESTS=false \
  -e CORS_ALLOW_CREDENTIALS=false \
  -e DATA_DIRECTORY=/data/ \
  -e OSM_SOURCE_DOWNLOADED="2018.5.31" \
  -e OSM_SOURCE_MD5="1234fffffaabbccdd11aaa1111111123" \
  -e HTTP_PORT=$PORT_INSIDE_DOCKER \
  -e HTTP_INTERFACE_NAME=0.0.0.0 \
  -e SELECTED_TAGS=amenity,barrier,building,highway,landuse,leisure,man_made,natural,railway,shop,sport,surface,tourism \
  matiasdahl/backend:latest
