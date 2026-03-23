// Update BaseOS on one node
//
// This needs to be in the global lib so it can be run from
// a parallel Map
// realNode might not match agentName if the node is updated using ansible
//    on built-in, but we still need to know it
def update_node(String agentName, Map info, String realNode)
{
    println("Running updateOSNode on ${agentName}")

    node("${agentName}") {
	try {
	    runWithArtifacts(info, "update_${agentName}.log", {
		info['stages_run']++;

		def localinfo = getNodeProperties(agentName)
		def exports = getShellVariables(localinfo)

		// special case freebsd devel that needs ansible from built-in node
		if (agentName == 'built-in' && info['packager'] == 'freebsd') {
		    sh """
		     cd $HOME/ci-tools/bsd-update
		     ${exports} ./run-update -d
		    """
		} else {
		    sh """
		     ${exports} $HOME/ci-tools/ci-wrap ci-update-${info['packager']}
		    """
		}
	    })
	}
	// Catch any exceptions and record them
	catch (e) {
	    info['stages_fail'] += 1
	    info['stages_fail_nodes'] += "${agentName} "
	    catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
		shNoTrace("exit 1", "Marking this stage as a failure")
	    }
	}
    }
    // Force a re-connect
    disconnect_node(realNode)
}

// Polymorph it until everything is on ansible
def call(String agentName, Map info)
{
    update_node(agentName, info, agentName)
}

def call(String agentName, Map info, String realNode)
{
    update_node(agentName, info, realNode)
}

def disconnect_node(String nodeName)
{
    node('built-in') {
	if (nodeName != 'built-in') {
	    for (aSlave in hudson.model.Hudson.instance.slaves) {
		def computer = aSlave.getComputer();
		if (computer.name == nodeName) {
		    computer.doDoDisconnect("after update")
		}
	    }
	}
    }
}
