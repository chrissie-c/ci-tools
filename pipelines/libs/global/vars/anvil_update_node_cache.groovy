def call(String agentName, Map info)
{
    node("${agentName}") {
	def String[] distros = ['almalinux-9', 'rhel-9']

	for (distro in distros) {
	    stage("Updating ${agentName} ${distro}") {
		try {
		    runWithArtifacts(info, "update_${distro}_cache_${agentName}.log", {
			info['stages_run']++;
			def localinfo = getNodeProperties(agentName)
			def exports = getShellVariables(localinfo)

			sh """
			 $HOME/ci-tools/ci-destroy-anvil-bm-vm
			"""
			sh """
			 $HOME/ci-tools/ci-setup-anvil-bm-vm-cache ${distro} none none
			"""
			sh """
			 $HOME/ci-tools/ci-destroy-anvil-bm-vm
			"""
		    })
		}
		catch (e) {
		    info['stages_fail'] += 1
		    info['stages_fail_nodes'] += "${agentName} ${distro}"
		    catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
			shNoTrace("exit 1", "Marking this stage as a failure")
		    }
		}
	    }
	}
    }
}
