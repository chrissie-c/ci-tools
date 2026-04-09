#!/bin/sh
# Copy the Jenkins jar file to the remote node in $NODE_NAME (provided by Jenkins)
# and run the start_node script

curl http://localhost:8080/jnlpJars/agent.jar | ssh $NODE_NAME "cat > /root/bin/agent.jar"
ssh $NODE_NAME /root/bin/start_node
