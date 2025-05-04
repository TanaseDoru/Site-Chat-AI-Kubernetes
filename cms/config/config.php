<?php
return [
    'db_host' => getenv('DB_HOST') ?: 'cms-db',
    'db_name' => getenv('DB_NAME') ?: 'vveb',
    'db_user' => getenv('DB_USER') ?: 'user',
    'db_pass' => getenv('DB_PASS') ?: 'password',
];