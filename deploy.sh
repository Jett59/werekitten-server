echo "Building the deployable artifact ./target/release-directory/werekitten-server.zip"
mvn package
echo "${SERVER_PEM}">server.pem
chmod 400 server.pem
scp -i server.pem -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no target/release-directory/werekitten-server.zip ec2-user@werekitten.mycodefu.com: