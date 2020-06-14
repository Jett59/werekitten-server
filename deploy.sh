echo "Building the deployable artifact ./target/release-directory/werekitten-server.zip"
mvn package

echo "${SERVER_PEM}" | base64 --decode > server.pem
chmod 400 server.pem

ssh -i server.pem -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no ec2-user@werekitten.mycodefu.com "rm -f werekitten-server.zip"
scp -i server.pem -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no target/release-directory/werekitten-server.zip ec2-user@werekitten.mycodefu.com:

ssh -i server.pem -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no ec2-user@werekitten.mycodefu.com << EOF
  cd /opt
  rm -rf werekitten-server
  mkdir werekitten-server
  cd werekitten-server
  unzip /home/ec2-user/werekitten-server.zip
  ./shutdown.sh
  ./startup.sh
EOF