cd ${0%/*}
nohup java -jar werekitten-server.jar >> /var/log/werekitten/console.log &
