<?php

$id = uniqid();
$upload_dir = realpath(__DIR__ . '/../images/');
$upload_prefix = $upload_dir .'/'. $id;

$array = array ("success" => true);
$array['error'] = array();

ini_set("log_errors", 1);
ini_set("error_log", $upload_dir . '/error.log');

if (!isset($_FILES['face']['error']) || is_array($_FILES['face']['error']))
{
    $array['success'] = false;
    $array['error'][] = "No image for face";
    if(error_get_last())
    {
        $array['error'][] = error_get_last();
    }
}
else if (!move_uploaded_file($_FILES['face']['tmp_name'], $upload_prefix . '-face.jpg'))
{
    $array['success'] = false;
    $array['error'][] = "Failed to move face";
    if(error_get_last())
    {
        $array['error'][] = error_get_last();
    }
}

if (!isset($_FILES['foot']['error']) || is_array($_FILES['foot']['error']))
{
    $array['success'] = false;
    $array['error'][] = "No image for foot";
    if(error_get_last())
    {
        $array['error'][] = error_get_last();
    }
}
else if (!move_uploaded_file($_FILES['foot']['tmp_name'], $upload_prefix . '-foot.jpg'))
{
    $array['success'] = false;
    $array['error'][] = "Failed to move foot";
    if(error_get_last())
    {
        $array['error'][] = error_get_last();
    }
}

if (!isset($_FILES['ear']['error']) || is_array($_FILES['ear']['error']))
{
    $array['success'] = false;
    $array['error'][] = "No image for ear";
    if(error_get_last())
    {
        $array['error'][] = error_get_last();
    }
}
else if (!move_uploaded_file($_FILES['ear']['tmp_name'], $upload_prefix . '-ear.jpg'))
{
    $array['success'] = false;
    $array['error'][] = "Failed to move ear";
    if(error_get_last())
    {
        $array['error'][] = error_get_last();
    }
}

if(!$array['success'])
{
    $array['data'] = $_POST;
    $array['files'] = $_FILES;
    $array['request'] = apache_request_headers();
    file_put_contents($upload_prefix . '-error.json', json_encode($array));
    header(':', true, 500);
    header('X-PHP-Response-Code: 500', true, 500);
    echo 'Error';
}
else
{
    ksort($_POST);
    if(!file_put_contents($upload_prefix . '-data.json', json_encode($_POST)))
    {
        $array = array ("success" => false, "message" => "Failed to save data", "error" => error_get_last());
    }

    echo '{"result":"Success"}';
}

?>
