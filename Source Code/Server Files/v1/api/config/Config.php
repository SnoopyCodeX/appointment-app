<?php

/**
 * This php file is responsible for
 * declaring the configuration array
 * as a php constant variable
 * 
 * @author John Roy L. Calimlim
 */

$configs = array(
	"DIRECTORIES" => array(
		"DIR_CORE" => $_SERVER['DOCUMENT_ROOT'] . "/v1/api/core",
		"DIR_OBJECTS" => $_SERVER['DOCUMENT_ROOT'] . "/v1/api/objects",
		"DIR_CONFIG" => $_SERVER['DOCUMENT_ROOT'] . "/v1/api/config",
		"DIR_UPLOADS" => $_SERVER['DOCUMENT_ROOT'] . "/v1/api/uploads"
	),
	"DATABASE" => array(
		"DB_NAME" => "id15542625_doctor_appointment_app",
		"DB_USER" => "id15542625_mbc_appointment_app",
		"DB_PASS" => "mia@DARP2020",
		"DB_HOST" => "localhost"
	),
	"FCM" => array (
		"SERVER_KEY" => "AAAAaxK9bKU:APA91bHWtdHVF9AmlyWGnWDQZmbKmhme6jJTrpM4UVyq1tgD_ew2MXTFkLEyxLghLf4k816ZNBKGn90lusIG1QVVO2guJmoWiGc9_lz83OeMQ8gKsuuEYZkajZqEEhdA6087lO8kZYcg"
	)
);

foreach($configs as $config)
	foreach($config as $key => $val)
		define($key, $val);
?>