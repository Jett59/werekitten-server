#!/bin/sh
cd ${0%/*}
java -jar werekitten-server.jar >> /var/log/werekitten/console.log
