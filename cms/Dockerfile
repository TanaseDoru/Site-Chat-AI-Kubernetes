# Use official PHP 8.1 with Apache
FROM php:8.1-apache

# Install extensions and enable modules
RUN apt-get update \
    && apt-get install -y git unzip zip \
    && docker-php-ext-install pdo pdo_mysql \
    && a2enmod rewrite headers

# 1) Suppress the FQDN warning
RUN echo "ServerName localhost" >> /etc/apache2/apache2.conf

# 2) Change DocumentRoot to public/
RUN sed -ri \
    -e 's!DocumentRoot /var/www/html!DocumentRoot /var/www/html/public!g' \
    -e 's!<Directory /var/www/html>!<Directory /var/www/html/public>!g' \
    /etc/apache2/sites-available/*.conf

# Copy application into place
WORKDIR /var/www/html
COPY . /var/www/html

# Install Composer
COPY --from=composer:2 /usr/bin/composer /usr/bin/composer
ENV COMPOSER_ALLOW_SUPERUSER=1
RUN composer install --no-dev --optimize-autoloader

# Ensure entrypoint script is executable
RUN chmod +x /var/www/html/entrypoint.sh

EXPOSE 80
ENTRYPOINT ["/var/www/html/entrypoint.sh"]
