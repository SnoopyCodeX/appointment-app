<?php
require_once('config/Config.php');
require_once(DIR_CONFIG . '/Database.class.php');
require_once(DIR_CORE . '/FileUtil.class.php');
require_once(DIR_CORE . '/Router.class.php');
require_once(DIR_CORE . '/Request.class.php');
require_once(DIR_CORE . '/Security.class.php');
require_once(DIR_OBJECTS . '/Patient.class.php');
require_once(DIR_OBJECTS . '/Doctor.class.php');
require_once(DIR_OBJECTS . '/Appointment.class.php');


Database::init();
Patient::init(Database::$conn);
Doctor::init(Database::$conn);
Appointment::init(Database::$conn);
?>