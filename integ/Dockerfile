FROM adoptopenjdk:11
ENV ROCK=/rock

COPY rocktest-bin.tar.gz /rocktest-bin.tar.gz

RUN tar xzvf /rocktest-bin.tar.gz && mv /rocktest-bin /rock

VOLUME /scen
WORKDIR /scen

ENTRYPOINT ["/rock/sh/rocktest"]
