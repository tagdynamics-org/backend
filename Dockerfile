# https://hub.docker.com/r/hseeberger/scala-sbt/
FROM hseeberger/scala-sbt:8u171_2.12.6_1.1.6

RUN apt-get -y install gradle

WORKDIR /root
RUN git clone --recurse-submodules https://github.com/tagdynamics-org/backend.git

WORKDIR /root/backend
RUN gradle wrapper
RUN ./gradlew test shadowJar

RUN cp /root/backend/backend/build/libs/backend-all.jar /root/backend.jar

WORKDIR /root
RUN rm -rf /root/.gradle
RUN rm -rf /root/backend
RUN rm -rf /root/scala-2.12.6

CMD java -jar /root/backend.jar
