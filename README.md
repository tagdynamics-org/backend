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

## Getting certificate for api.tagdynamics.org

The main site tagdynamics.org is hosted using [netlify](https://www.netlify.com). However, we need a separate SSL certificate for the api.tagdynamics.org site.

- Follow [these instructions](https://certbot.eff.org/lets-encrypt/ubuntuxenial-other) and run Let's encrypt `certbot` command on the server connected to `api.tagdynamics.org`. Authenticate with `standalone` mode. Ensure that no other webserver is running on machine (port 80 is free and this port is open for inbound traffic). 
- This will output various keys and certificates to `/etc/letsencrypt/live/api.tagdynamics.org/`, see the README file in that directory. To create a certificate readable by the backend run

```
# in ubuntu root
mkdir certificate
sudo openssl pkcs12 -export -out certificate/certificate.pfx -inkey /etc/letsencrypt/live/api.tagdynamics.org/privkey.pem -in /etc/letsencrypt/live/api.tagdynamics.org/cert.pem -certfile /etc/letsencrypt/live/api.tagdynamics.org/chain.pem
sudo chown ubuntu -R certificate
sudo chgrp ubuntu -R certificate
```

## Running api on aws instance

```
tmux
sudo docker rmi --force api-runner
sudo docker build -t api-runner .
bash launch-docker.sh
```

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
