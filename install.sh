sudo su
cd /opt
rm -rf werekitten-server
mkdir werekitten-server
cd werekitten-server
unzip /home/ec2-user/werekitten-server.zip
./shutdown.sh
./startup.sh