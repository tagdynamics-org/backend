#!/usr/bin/env bash

export DATA_DIRECTORY=`pwd`/backend/src/test/resources/testdata/3-aggregates

export OSM_SOURCE_DOWNLOADED="2018.1.20"
export OSM_SOURCE_MD5="1234fffffaabbccdd11aaa1111111123"
export SELECTED_TAGS=amenity,barrier,building,highway,landuse,leisure,man_made,natural,railway,shop,sport,surface,tourism

export HTTP_INTERFACE_NAME=localhost
export HTTP_PORT=8765

./gradlew run
