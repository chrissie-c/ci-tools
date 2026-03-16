// Update BaseOS on one node
//
// This needs to be in the global lib so it can be run from
// a parallel Map
def call(String agentName, Map info)
{
    println("Running updateBSDdevel on ${agentName}")

    node("${agentName}") {
	try {
	    runWithArtifacts(info, "update_${agentName}.log", {
		info['stages_run']++;

		def localinfo = getNodeProperties(agentName)
		def exports = getShellVariables(localinfo)

		sh """
	         ${exports} $HOME/ci-tools/ci-wrap bsd-update/pre-update
	        """
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
}
