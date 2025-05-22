FROM python:3.11-slim

# Install system deps, ODBC, and MS driver with dpkg options to handle overwrites
RUN apt-get update \
    && apt-get install -y --no-install-recommends \
       curl \
       gnupg \
       apt-transport-https \
       unixodbc \
       unixodbc-dev \
       nginx \
    && curl https://packages.microsoft.com/keys/microsoft.asc | apt-key add - \
    && curl https://packages.microsoft.com/config/debian/11/prod.list \
        > /etc/apt/sources.list.d/mssql-release.list \
    && apt-get update \
    && ACCEPT_EULA=Y apt-get install -y --allow-downgrades --allow-remove-essential \
       -o Dpkg::Options::="--force-overwrite" \
       msodbcsql18 \
    && ln -sf /opt/microsoft/msodbcsql18/lib64/libmsodbcsql-18.5.so.1.1 \
           /opt/microsoft/msodbcsql18/lib64/libmsodbcsql-18.so \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy and install Python dependencies
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy application code and environment file
# Ensure you include your .env with valid credentials
COPY .env .env
COPY . .

# Create non-root user and adjust ownership
RUN useradd -m appuser && chown -R appuser:appuser /app
USER appuser

# Expose application port (nginx on 80, uvicorn on 8000)
EXPOSE 8000 80

# Entrypoint: set library path, start nginx, then uvicorn
CMD bash -lc "\
    export LD_LIBRARY_PATH=/opt/microsoft/msodbcsql18/lib64:/usr/lib && \
    nginx -g 'daemon off;' & \
    sleep 2 && \
    uvicorn main:app --host 0.0.0.0 --port 8000\
"