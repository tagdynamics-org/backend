#!/usr/bin/env bash

# https://hub.docker.com/r/matiasdahl/backend/
#sudo docker pull matiasdahl/backend:latest

export PORT_INSIDE_DOCKER=8080
export PORT_EXPOSED=443

# OSM data
#export DATA_DIR=`pwd`/backend/src/test/resources/testdata/3-aggregates/
export DATA_DIR=`pwd`/../data/aggregates/

# SSL certificate
export CERT_DIR=`pwd`/../certificate/
export KEYSTORE_PASSWORD=abc
export KEYSTORE_FILENAME=certificate.pfx

sudo docker run --rm -it \
  -p $PORT_EXPOSED:$PORT_INSIDE_DOCKER \
  -v $DATA_DIR:/data/ \
  -v $CERT_DIR:/cert/ \
  -e KEYSTORE_PASSWORD=$KEYSTORE_PASSWORD \
  -e KEYSTORE_PATH=/cert/$KEYSTORE_FILENAME \
  -e CORS_ALLOWED_ORIGINS="*" \
  -e CORS_ALLOW_GENERIC_HTTP_REQUESTS=false \
  -e CORS_ALLOW_CREDENTIALS=false \
  -e DATA_DIRECTORY=/data/ \
  -e OSM_SOURCE_DOWNLOADED="2018.5.??" \
  -e OSM_SOURCE_MD5="1234fffffaabbccdd11aaa11111111xx" \
  -e HTTP_PORT=$PORT_INSIDE_DOCKER \
  -e HTTP_INTERFACE_NAME=0.0.0.0 \
  -e SELECTED_TAGS=amenity,barrier,building,highway,landuse,leisure,man_made,natural,railway,shop,sport,surface,tourism \
 api-runner
#matiasdahl/backend:latest
