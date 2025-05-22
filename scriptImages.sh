#!/bin/bash

# Your Docker Hub username
DOCKERHUB_USERNAME="tanase0"
PRIVATE_REGISTRY="localhost:32000"

# Get list of repositories
REPOS=$(curl -s ${PRIVATE_REGISTRY}/v2/_catalog | jq -r '.repositories[]')

for repo in $REPOS; do
    echo "Processing repository: $repo"
    
    # Get tags for this repository
    TAGS=$(curl -s ${PRIVATE_REGISTRY}/v2/${repo}/tags/list | jq -r '.tags[]')
    
    for tag in $TAGS; do
        echo "  Processing tag: $tag"
        
        # Pull from private registry
        docker pull ${PRIVATE_REGISTRY}/${repo}:${tag}
        
        # Tag for Docker Hub
        docker tag ${PRIVATE_REGISTRY}/${repo}:${tag} ${DOCKERHUB_USERNAME}/${repo}:${tag}
        
        # Push to Docker Hub
        docker push ${DOCKERHUB_USERNAME}/${repo}:${tag}
        
        # Clean up local images to save space
        docker rmi ${PRIVATE_REGISTRY}/${repo}:${tag}
        docker rmi ${DOCKERHUB_USERNAME}/${repo}:${tag}
    done
done
