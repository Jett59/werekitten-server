#!/bin/sh
cd ${0%/*}

export JAVA_HOME=/opt/jdk-14
export PATH=$JAVA_HOME/bin:$PATH

nohup java -jar werekitten-server.jar >> /var/log/werekitten/console.log &

