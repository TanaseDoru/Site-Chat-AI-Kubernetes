<?php
// Autoload
require __DIR__ . '/../vendor/autoload.php';

// Load configuration
$config = require __DIR__ . '/../config/config.php';

// Simple CMS page using Vvveb.js editor
?><!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>My Vvveb CMS Site</title>
  <link rel="stylesheet" href="https://unpkg.com/vvveb-js@latest/dist/css/vvvebjs.css">
</head>
<body>
  <h1>Welcome to My Vvveb CMS</h1>
  <iframe src="http://4.178.170.54:90" width="50%" height="800px" frameborder="0"></iframe>
    <iframe src="" width="50%" height="800px" frameborder="0"></iframe>
  <div id="vvveb-builder"></div>
  <script src="https://unpkg.com/vvveb-js@latest/dist/js/vvvebjs.js"></script>
  <script>
    Vvveb.Builder.init('#vvveb-builder');
  </script>
</body>
</html>