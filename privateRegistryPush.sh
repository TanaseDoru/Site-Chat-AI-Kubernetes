#!/bin/bash

# List of image names (without tags)
FILES=(
    "cms-std"
    "frontend-chat-std"
    "backend-chat-std"
)

# Default tag (modify if needed)
TAG="latest"

# Loop through each file (image name)
for file in "${FILES[@]}"; do
    echo "Processing image: tanase0/$file:$TAG"

    # Pull the image from Docker Hub
    sudo docker image pull tanase0/"$file:$TAG"
    if [ $? -ne 0 ]; then
        echo "Error: Failed to pull tanase0/$file:$TAG"
        exit 1
    fi

    # Tag the image for the local registry
    sudo docker image tag tanase0/"$file:$TAG" localhost:32000/"$file:$TAG"
    if [ $? -ne 0 ]; then
        echo "Error: Failed to tag tanase0/$file:$TAG as localhost:32000/$file:$TAG"
        exit 1
    fi

    # Push the image to the local registry
    sudo docker image push localhost:32000/"$file:$TAG"
    if [ $? -ne 0 ]; then
        echo "Error: Failed to push localhost:32000/$file:$TAG"
        exit 1
    fi

    echo "Successfully processed $file"
done

# Query the local registry catalog
echo "Fetching registry catalog from localhost:32000/v2/_catalog"
curl -s localhost:32000/v2/_catalog | jq .
if [ $? -ne 0 ]; then
    echo "Error: Failed to query registry catalog"
    exit 1
fi

echo "Script completed successfully"