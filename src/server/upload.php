<?php

/*
	Response =
	{
		"success": true|false,
		"messages": ["Array of log messages"],
		"error": ["Array of error messages"]
	}
	
	Saves data as:
	{$upload_dir}/
		error.log
		{$record_prefix}/
			data.json|data-error.json
			face/
				face.jpg
				face2.jpg
				face3.jpg
			ear/
				ear.jpg
				ear2.jpg
				ear3.jpg
			foot/
				foot.jpg
				foot2.jpg
				foot3.jpg
*/

// create an id to identify this record (also used a directory name!)
$id = uniqid();
$studyId = "noStudyId";
if (isset($_POST["studyId"])) 
{
	// keep only characters valid in filenames
	$studyId = preg_replace("/[^a-zA-Z0-9_\\-. ]+/", "", $_POST["studyId"]);
}

// paths
$upload_dir = realpath("/babyface-data/test");
$record_prefix = '/'. $id . '-' . $studyId;

$array = array ("success" => true);
$array['error'] = array();
$array['messages'] = array();

// log errors
ini_set("log_errors", 1);
ini_set("error_log", $upload_dir . '/error.log');

// list of expected (required) image fields
$image_keys = array("face", "face2", "face3", "ear", "ear2", "ear3", "foot", "foot2", "foot3");

$array['messages'][] = "Attempting to create '" . $record_prefix . "'";
// make directories
if (mkdir($upload_dir . $record_prefix) && mkdir($upload_dir . $record_prefix . "/face") && mkdir($upload_dir . $record_prefix . "/ear") && mkdir($upload_dir . $record_prefix . "/foot")) 
{
	$array['messages'][] = "Attempting to save images.";
	foreach($image_keys as $image_key) {
		if (!isset($_FILES[$image_key]['error']) || is_array($_FILES[$image_key]['error']) || $_FILES[$image_key]['error'] != 0)
		{
			$array['messages'][] = "No image for {$image_key}.";
			$array['success'] = false;
			$array['error'][] = "No image for {$image_key}";
			if(error_get_last())
			{
				$array['error'][] = error_get_last();
			}
		}
		else 
		{
			$image_path = "/".preg_replace("/[^a-zA-Z]+/", "", $image_key)."/{$image_key}.jpg";
			$array['messages'][] = "Attempting to move '" . $_FILES[$image_key]['tmp_name'] . "' to '" . $record_prefix . $image_path . "'.";
			if (!move_uploaded_file($_FILES[$image_key]['tmp_name'], $upload_dir . $record_prefix . $image_path))
			{
				$array['messages'][] = "Failed to move {$image_key}.";
				$array['success'] = false;
				$array['error'][] = "Failed to move {$image_key}";
				if(error_get_last())
				{
					$array['error'][] = error_get_last();
				}
			} else {
				$array['messages'][] = "{$image_key} moved.";
			}
		}
	}
	
	$array['messages'][] = "Adding server time to data.";
	$date = new DateTime("now", new DateTimeZone( 'UTC' ));
	$_POST["server_received_time"] = $date->format('Y-m-d H:i:s e');
	
	$array['messages'][] = "Attempting to save data.";
	ksort($_POST);
	$data_filename = 'data.json';
	if (!$array['success'])
	{
		// save data even if there was an error (filename makes it clear there was an error)
		$data_filename = 'data-error.json';
	}
	if(file_put_contents($upload_dir . $record_prefix . '/' . $data_filename, json_encode($_POST)))
	{
		$array['messages'][] = "Saved data.";	
	}
	else
	{
		$array['messages'][] = "Failed to save data.";
		$array['success'] = false;
		$array['error'][] = "Failed to save data file.";
		if(error_get_last())
		{
			$array['error'][] = error_get_last();
		}
	}
}
else
{
	$array['messages'][] = "Failed to create '" . $record_prefix . "'.";
	$array['success'] = false;
	$array['error'][] = "Failed to create save directory.";
	if(error_get_last())
	{
		$array['error'][] = error_get_last();
	}
}

echo json_encode($array);

?>
