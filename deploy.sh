# 2. ClusterIP services + deployments (backend, frontend, mongodb)
microk8s kubectl apply -f k8s/mongodb-deployment.yaml \
                                            -f k8s/mongodb-service.yaml \
                                            -f k8s/backend-deployment.yaml \
                                            -f k8s/backend-service.yaml \
                                            -f k8s/frontend-deployment.yaml \
                                            -f k8s/frontend-service.yaml \
                                            -f k8s/cms.yaml \
                                            -f k8s/cms-pv.yaml \
                                            -f k8s/ai-backend.yaml \
                                            -f k8s/ai-frontend.yaml

# 3. Ingress (expune tot pe 80)
microk8s kubectl apply -f k8s/ingress.yaml

