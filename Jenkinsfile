pipeline {
    agent any

    tools {
        jdk 'JDK_17'
        maven 'Maven_3.9'
    }

    parameters {
        choice(name: 'BROWSER', choices: ['chromium', 'firefox', 'webkit', 'chrome', 'msedge'], description: 'Target browser for execution')
        choice(name: 'ENVIRONMENT', choices: ['PROD', 'STAGING', 'QA', 'DEV'], description: 'Target test environment')
        string(name: 'TEST_GROUPS', defaultValue: 'smoke', description: 'TestNG groups to run: smoke, regression, sanity, functional, or empty for all')
        booleanParam(name: 'HEADLESS', defaultValue: true, description: 'Run tests in headless mode')
    }

    environment {
        // Securely retrieve test credentials from Jenkins Credentials Store
        ALBERTA_TEST_USER = credentials('alberta-kiosk-test-user')
        ALBERTA_TEST_PASS = credentials('alberta-kiosk-test-password')
    }

    options {
        timeout(time: 60, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '30'))
        ansiColor('xterm')
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out source repository...'
                checkout scm
            }
        }

        stage('Install Playwright Browsers') {
            steps {
                echo 'Ensuring Playwright browser binaries and OS dependencies are installed...'
                bat '''
                    mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install --with-deps"
                '''
            }
        }

        stage('Execute Tests') {
            steps {
                script {
                    echo "Starting execution: Browser=${params.BROWSER}, Environment=${params.ENVIRONMENT}, Groups=${params.TEST_GROUPS}, Headless=${params.HEADLESS}"
                    
                    def mavenCmd = "mvn clean test -Dbrowser=${params.BROWSER} -Dheadless=${params.HEADLESS} -Denvironment=${params.ENVIRONMENT}"
                    if (params.TEST_GROUPS != null && !params.TEST_GROUPS.trim().isEmpty()) {
                        mavenCmd += " -Dgroups=${params.TEST_GROUPS.trim()}"
                    }

                    // Run tests and capture exit status
                    bat "${mavenCmd}"
                }
            }
        }
    }

    post {
        always {
            echo 'Archiving test results and HTML reports...'
            
            // Publish TestNG XML Results
            testng(testResultsPattern: 'target/surefire-reports/testng-results.xml')

            // Publish Extent Interactive HTML Report
            publishHTML([
                allowMissing: true,
                alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: 'reports',
                reportFiles: 'index.html',
                reportName: 'Alberta Payments Test Automation Report',
                reportTitles: 'Alberta Payments - Extent Execution Report'
            ])

            // Archive Execution Logs
            archiveArtifacts artifacts: 'logs/*.log', allowEmptyArchive: true
        }

        failure {
            echo 'Build failed. Archiving failure screenshots, traces, and video recordings...'
            archiveArtifacts artifacts: 'screenshots/**/*.png, test-results/traces/**/*.zip, test-results/videos/**/*', allowEmptyArchive: true
        }

        success {
            echo 'All test scenarios completed successfully!'
        }
    }
}
