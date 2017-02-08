
node {

    def buildNumber = this.binding.variables.get('BUILD_NUMBER')
    def sbtFolder   = "${tool name: 'sbt-0.13.13', type: 'org.jvnet.hudson.plugins.SbtPluginBuilder$SbtInstallation'}/bin"

    stage("Checkout"){
        echo "git checkout"
        checkout changelog: false, poll: false, scm: [
            $class: 'GitSCM', 
            branches: [[
                name: 'master'
            ]],
            doGenerateSubmoduleConfigurations: false, 
            extensions: [[
                $class: 'WipeWorkspace'
            ], [
                $class: 'CleanBeforeCheckout'
            ]], 
            submoduleCfg: [], 
            userRemoteConfigs: [[
                credentialsId: 'fe000f7c-4de6-45c7-9097-d1fba24f3cb5', 
                url: 'git@github.com:telegraph/simple-pipeline-test.git'
            ]]
        ]
    }

    stage("Build & Test"){
        echo "sbt build"
        sh """
            ${sbtFolder}/sbt clean test package
        """
    }

    stage("Publish"){
        echo "Run Publish"
        sh """

        """
    }

    stage("Release Notes"){
        echo "Tag Release Candidate"
        echo "Set Release Notes"
    }
}