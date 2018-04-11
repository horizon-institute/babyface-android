<?php

echo exec('whoami');

$user = posix_getpwuid(posix_geteuid());
echo $user['name'];

$id = uniqid();
echo "id=";
echo $id;
echo "<br>";

$upload_dir = realpath('/babyface-data/data');
echo "upload_dir=";
echo $upload_dir;
echo "<br>";

$upload_prefix = $upload_dir .'/'. $id;
echo "upload_prefix=";
echo $upload_prefix;
echo "<br>";

$array = array ("success" => true);
$array['error'] = array();

ini_set("log_errors", 1);
ini_set("error_log", $upload_dir . '/error.log');

?>
