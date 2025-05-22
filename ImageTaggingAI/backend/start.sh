#!/bin/bash

# Ensure LD_LIBRARY_PATH is set correctly
export LD_LIBRARY_PATH=/opt/microsoft/msodbcsql18/lib64:/usr/lib

# Verify ODBC setup
echo "[DEBUG] ODBC configuration:" 
odbcinst -j
echo "[DEBUG] ODBC drivers:"
cat /etc/odbcinst.ini
echo "[DEBUG] Checking driver files:"
ls -la /opt/microsoft/msodbcsql18/lib64/

# Start NGINX
/usr/sbin/nginx -g "daemon off;" &

# Wait for NGINX to start
sleep 2

# Start Uvicorn
cd /app && uvicorn main:app --host 0.0.0.0 --port 8000

# Keep the script running
wait