[![Build Status](https://travis-ci.org/tagdynamics-org/backend.svg?branch=master)](https://travis-ci.org/tagdynamics-org/backend)

# backend
Backend for serving tag analytics

## Cloning and running tests

```bash
git clone --recurse-submodules https://github.com/tagdynamics-org/backend.git
cd backend
gradle wrapper
./gradlew test
```

## Running

See the [launch.sh](./launch.sh) script.

## License

The initial version of backend (see [this commit](TODO)) created using sbt and the 
[Akka HTTP quickstart in Scala](https://github.com/akka/akka-http-quickstart-scala.g8) template by
running `sbt -Dsbt.version=0.13.15 new https://github.com/akka/akka-http-quickstart-scala.g8`. Template license [listed as](https://github.com/akka/akka-http-quickstart-scala.g8/blob/10.1.x/README.md#template-license):

```
Written in 2017 by Lightbend, Inc.

To the extent possible under law, the author(s) have dedicated all copyright and related
and neighboring rights to this template to the public domain worldwide.
This template is distributed without any warranty. See <http://creativecommons.org/publicdomain/zero/1.0/>.
```
