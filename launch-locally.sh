#!/usr/bin/env bash

export DATA_DIRECTORY=`pwd`/backend/src/test/resources/testdata/3-aggregates

export OSM_SOURCE_DOWNLOADED="2018.1.20"
export OSM_SOURCE_MD5="1234fffffaabbccdd11aaa1111111123"
export SELECTED_TAGS=amenity,barrier,building,highway,landuse,leisure,man_made,natural,railway,shop,sport,surface,tourism

export CORS_ALLOWED_ORIGINS="*" # or eg. "http://tagdynamics.org,http://www.tagdynamics.org"
export CORS_ALLOW_GENERIC_HTTP_REQUESTS=false
export CORS_ALLOW_CREDENTIALS=false

export HTTP_INTERFACE_NAME=0.0.0.0
export HTTP_PORT=8765

./gradlew run
