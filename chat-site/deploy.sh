# 1. Aplici namespace (dacă nu există deja)
microk8s kubectl apply -f k8s/namespace.yaml

# 2. ClusterIP services + deployments (backend, frontend, mongodb)
microk8s kubectl apply -n chat-system -f k8s/mongodb-deployment.yaml \
                                            -f k8s/mongodb-service.yaml \
                                            -f k8s/backend-deployment.yaml \
                                            -f k8s/backend-service.yaml \
                                            -f k8s/frontend-deployment.yaml \
                                            -f k8s/frontend-service.yaml

# 3. Ingress (expune tot pe 80)
microk8s kubectl apply -n chat-system -f k8s/ingress.yaml

# 4. Așteaptă
microk8s kubectl get all -n chat-system
microk8s kubectl get ingress -n chat-system
