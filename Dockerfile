FROM openjdk:17-alpine

ENV ARTIFACT_NAME=printfulShopwareOrderSync

COPY ./target/${ARTIFACT_NAME}.jar  /usr/src/pf/${ARTIFACT_NAME}.jar
WORKDIR /usr/src/pf

RUN echo "0 * * * * java -jar /usr/src/pf/${ARTIFACT_NAME}.jar" >> /var/spool/cron/crontabs/root

CMD crond -f
