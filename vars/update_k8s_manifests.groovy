#!/usr/bin/env groovy

/**
 * Update Kubernetes manifests with new image tags
 */
def call(Map config = [:]) {
    def imageTag      = config.imageTag ?: error("Image tag is required")
    def manifestsPath = config.manifestsPath ?: 'kubernetes'
    def gitCredentials = config.gitCredentials ?: 'github-credentials'
    def gitUserName   = config.gitUserName ?: 'sureshb987'
    def gitUserEmail  = config.gitUserEmail ?: 'bsuresh8499@gmail.com'
    def gitBranch     = config.gitBranch ?: 'master'

    echo "🔄 Updating Kubernetes manifests with image tag: ${imageTag}"

    withCredentials([usernamePassword(
        credentialsId: gitCredentials,
        usernameVariable: 'GIT_USERNAME',
        passwordVariable: 'GIT_PASSWORD'
    )]) {
        sh """
            set -e

            git config user.name "${gitUserName}"
            git config user.email "${gitUserEmail}"

            # Update deployment image tag
            sed -i "s|image: trainwithshubham/easyshop-app:.*|image: trainwithshubham/easyshop-app:${imageTag}|g" ${manifestsPath}/08-easyshop-deployment.yaml || true

            # Update migration job image tag
            if [ -f "${manifestsPath}/12-migration-job.yaml" ]; then
                sed -i "s|image: trainwithshubham/easyshop-migration:.*|image: trainwithshubham/easyshop-migration:${imageTag}|g" ${manifestsPath}/12-migration-job.yaml
            fi

            # Update ingress domain
            if [ -f "${manifestsPath}/10-ingress.yaml" ]; then
                sed -i "s|host: .*|host: easyshop.letsdeployit.com|g" ${manifestsPath}/10-ingress.yaml
            fi

            if git diff --quiet; then
                echo "✅ No changes to commit."
            else
                git add ${manifestsPath}/*.yaml
                git commit -m "Update image tags to ${imageTag} and domain [ci skip]"

                # Push changes to your GitHub repo
                git remote set-url origin https://${GIT_USERNAME}:${GIT_PASSWORD}@github.com/sureshb987/tws-e-commerce-app_hackathon.git
                git push origin HEAD:${gitBranch}
                echo "✅ Changes pushed to GitHub: sureshb987/tws-e-commerce-app_hackathon"
            fi
        """
    }
}
