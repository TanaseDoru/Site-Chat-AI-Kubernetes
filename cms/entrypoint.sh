#!/usr/bin/env bash
set -e

# If you need to populate environment-based config, ensure config.php exists
if [ ! -f /var/www/html/config/config.php ]; then
  echo "<?php return []; ?>" > /var/www/html/config/config.php
fi

# Optionally perform migrations or cache operations here
# Example: php artisan migrate --force

# Start Apache in the foreground
exec apache2-foreground