
node {

    def sbtFolder        = "${tool name: 'sbt-0.13.13', type: 'org.jvnet.hudson.plugins.SbtPluginBuilder$SbtInstallation'}/bin"
    def projectName      = "${env.PROJECT_NAME}"
    def github_token     = "3cfa434608e1a81712ece082bfe4a97101be0859"
    def pipeline_version = "1.1.0-b${env.BUILD_NUMBER}"
    def github_commit    = ""

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
                url: "git@github.com:telegraph/${projectName}.git"
            ]]
        ]
    }
    
    stage("Build & Test"){
        sh """
            ${sbtFolder}/sbt clean test package
        """
    }

    stage("Publish"){
        echo "Run Publish"
        sh """
            ${sbtFolder}/sbt publish
        """
    }

    stage("Release Notes"){
        // Possible error if there is a commit different from the trigger commit
        github_commit = sh(returnStdout: true, script: 'git rev-parse HEAD').trim()

        //Realease on Git
        println("\n[TRACE] **** Releasing to github ${github_token}, ${pipeline_version}, ${github_commit} ****")
        sh """#!/bin/bash
            GITHUB_COMMIT_MSG=\$(curl -H "Content-Type: application/json" -H "Authorization: token ${github_token}" https://api.github.com/repos/telegraph/${projectName}/commits/\"${github_commit}\" | /usr/local/bin/jq \'.commit.message\')
            echo "GITHUB_COMMIT_MSG: \${GITHUB_COMMIT_MSG}"
            echo "GITHUB_COMMIT_DONE: DONE"
            C_DATA="{\\\"tag_name\\\": \\\"${pipeline_version}\\\",\\\"target_commitish\\\": \\\"master\\\",\\\"name\\\": \\\"${pipeline_version}\\\",\\\"body\\\": \${GITHUB_COMMIT_MSG},\\\"draft\\\": false,\\\"prerelease\\\": false}"
            echo "C_DATA: \${C_DATA}"
            curl -H "Content-Type: application/json" -H "Authorization: token ${github_token}" -X POST -d "\${C_DATA}" https://api.github.com/repos/telegraph/${projectName}/releases
        """
    }
}
