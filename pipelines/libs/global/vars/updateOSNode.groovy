// Update BaseOS on one node
//
// This needs to be in the global lib so it can be run from
// a parallel Map
def call(String agentName, Map info)
{
    println("Running updateOSNode on ${agentName}")

    node("${agentName}") {
	try {
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
