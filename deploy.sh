echo "Building the deployable artifact ./target/release-directory/werekitten-server.zip"
mvn package

echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin
docker build --tag luketn/werekitten-server .
docker push luketn/werekitten-server

echo "${SERVER_PEM}" | base64 --decode > server.pem
chmod 400 server.pem

ssh -i server.pem -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no ec2-user@werekitten.mycodefu.com "rm -f werekitten-server.zip"
scp -i server.pem -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no target/release-directory/werekitten-server.zip ec2-user@werekitten.mycodefu.com:
scp -i server.pem -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no install.sh ec2-user@werekitten.mycodefu.com:
ssh -i server.pem -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no ec2-user@werekitten.mycodefu.com "sudo chmod +x install.sh"
ssh -i server.pem -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no ec2-user@werekitten.mycodefu.com "sudo ./install.sh"