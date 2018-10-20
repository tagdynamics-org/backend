[![Build Status](https://travis-ci.org/tagdynamics-org/backend.svg?branch=master)](https://travis-ci.org/tagdynamics-org/backend)

# backend
tagdynamics.org backend for serving OpenStreetMap tag analytics

## Cloning and running tests

Note: This project requires Java 8.

```bash
git clone --recurse-submodules https://github.com/tagdynamics-org/backend.git
cd backend
gradle wrapper
./gradlew test
```

## Running

See the [launch.sh](./launch.sh) script.

## License

Copyright 2018 Matias Dahl except as otherwise noted (see in particular the note below).

An initial version of this work was created using sbt and the
[Akka HTTP quickstart template](https://github.com/akka/akka-http-quickstart-scala.g8) by
running `sbt -Dsbt.version=0.13.15 new https://github.com/akka/akka-http-quickstart-scala.g8`.
See [this commit](https://github.com/tagdynamics-org/backend/commit/6bb8726c6688876ed04b2e639379dd9783453d81).
Template license [listed as](https://github.com/akka/akka-http-quickstart-scala.g8/blob/10.1.x/README.md#template-license):

```
Written in 2017 by Lightbend, Inc.

To the extent possible under law, the author(s) have dedicated all copyright and related
and neighboring rights to this template to the public domain worldwide.
This template is distributed without any warranty. See <http://creativecommons.org/publicdomain/zero/1.0/>.
```
