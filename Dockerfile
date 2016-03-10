FROM ubuntu:14.04

RUN \
  apt-get install -y --no-install-recommends software-properties-common \
  && add-apt-repository ppa:webupd8team/java \
  && apt-get update \
  && echo debconf shared/accepted-oracle-license-v1-1 select true |  debconf-set-selections \
  && echo debconf shared/accepted-oracle-license-v1-1 seen true |  debconf-set-selections \
  && apt-get install -y --no-install-recommends oracle-java8-installer

EXPOSE 9090

VOLUME /var/log/flipcast

ADD config/application.conf application.conf
ADD config/logback.xml logback.xml

ADD target/scala-2.11/flipcast*.jar flipcast.jar
ADD flipcast-start.sh  /usr/local/bin/flipcast-start.sh

CMD bash -c "flipcast-start.sh ${MONGO_CONNECTION_STRING} ${DATABASE_NAME} ${RMQ_CONNECTION_STRING} ${RMQ_USER} ${RMQ_PASSWORD}"
