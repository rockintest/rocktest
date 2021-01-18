FROM adoptopenjdk:11
ENV ROCK=/rock

RUN mkdir -p /rock/sh && mkdir -p /rock/target

COPY sh/rocktest /rock/sh/rocktest
COPY rocktest.jar /rock/target/rocktest.jar

VOLUME /scen
WORKDIR /scen

ENTRYPOINT ["/rock/sh/rocktest"]
