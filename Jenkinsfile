node('local') {
    try {

        stage('Init') {
            env.JAVA_HOME="${tool 'JDK 8u102'}"
            env.PATH="${env.JAVA_HOME}/bin:${env.PATH}"
            sh 'java -version'
            tool 'sbt 0.13.15'
        }

        stage('Checkout') {
            checkout scm
        }

        stage('Build') {
            sh "${tool name: 'sbt 0.13.15', type: 'org.jvnet.hudson.plugins.SbtPluginBuilder$SbtInstallation'}/bin/sbt clean compile"
        }

        stage('Test') {
            sh "${tool name: 'sbt 0.13.15', type: 'org.jvnet.hudson.plugins.SbtPluginBuilder$SbtInstallation'}/bin/sbt coverage test coverageReport"
        }

        stage('Results') {
            junit '**/test-output/junitreports/*.xml'
            publishHTML([allowMissing: true, alwaysLinkToLastBuild: false, keepAll: true, reportDir: 'target/scala-2.11/scoverage-report/', reportFiles: 'index.html', reportName: 'Scoverage Report', reportTitles: ''])
        }

        if (currentBuild.result == null || "SUCCESS" == currentBuild.result) {
            currentBuild.result = "SUCCESS"
            slackSend(color: '#00FF00', message: "${currentBuild.result}: Job '${env.JOB_NAME} #${env.BUILD_NUMBER}' (<${env.BUILD_URL}|Open>)", channel: '#biopet-bot', teamDomain: 'lumc', tokenCredentialId: 'lumc')
        } else {
            slackSend(color: '#FFFF00', message: "${currentBuild.result}: Job '${env.JOB_NAME} #${env.BUILD_NUMBER}' (<${env.BUILD_URL}|Open>)", channel: '#biopet-bot', teamDomain: 'lumc', tokenCredentialId: 'lumc')
        }
    } catch (e) {

        stage('Results') {
            junit '**/test-output/junitreports/*.xml'
            publishHTML([allowMissing: true, alwaysLinkToLastBuild: false, keepAll: true, reportDir: 'target/scala-2.11/scoverage-report/', reportFiles: 'index.html', reportName: 'Scoverage Report', reportTitles: ''])
        }

        if (currentBuild.result == null || "FAILED" == currentBuild.result) {
            currentBuild.result = "FAILED"
            slackSend(color: '#FF0000', message: "${currentBuild.result}: Job '${env.JOB_NAME} #${env.BUILD_NUMBER}' (<${env.BUILD_URL}|Open>)", channel: '#biopet-bot', teamDomain: 'lumc', tokenCredentialId: 'lumc')
        } else {
            slackSend(color: '#FFFF00', message: "${currentBuild.result}: Job '${env.JOB_NAME} #${env.BUILD_NUMBER}' (<${env.BUILD_URL}|Open>)", channel: '#biopet-bot', teamDomain: 'lumc', tokenCredentialId: 'lumc')
        }

        throw e
    }

}