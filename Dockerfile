FROM openjdk:14
WORKDIR /tmp
RUN yum update -y
RUN yum install -y unzip zip
RUN mkdir -p /opt/werekitten-server
RUN mkdir -p /var/log/werekitten
WORKDIR /opt/werekitten-server
COPY  target/release-directory/werekitten-server.zip /tmp/werekitten-server.zip
RUN unzip /tmp/werekitten-server.zip
RUN chmod +x *.sh
EXPOSE 51273/tcp
ENTRYPOINT java -jar werekitten-server.jar