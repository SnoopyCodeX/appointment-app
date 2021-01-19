<?php
require_once('vendor/autoload.php');

use \JRLC\DARP\config\Config;
use \JRLC\DARP\config\Database;
use \JRLC\DARP\core\Route;
use \JRLC\DARP\core\Security;
use \JRLC\DARP\core\PHPMailer;

use \JRLC\DARP\objects\Admin;
use \JRLC\DARP\objects\Appointment;
use \JRLC\DARP\objects\Doctor;
use \JRLC\DARP\objects\Patient;

use paragraph1\phpFCM\Client;
use paragraph1\phpFCM\Message;
use paragraph1\phpFCM\Recipient\Device;
use paragraph1\phpFCM\Notification;

Config::init();
Database::init();
Admin::init(Database::$conn);
Doctor::init(Database::$conn);
Patient::init(Database::$conn);
Appointment::init(Database::$conn);

/****************************
 * START PUSH NOTIFICATIONS *
 ****************************/

/**
 * Handles registration/reinitialization of 
 * FCM Token map data.
 * 
 * @param int $userId The id of FCM token's owner
 * @return object
 */
Route::add('/fcm/([0-9]+)/token', function(int $userId) {
    $data = file_get_contents("php://input");
    $data = json_decode($data);
    $data = Security::escapeData($data);
    $db = Patient::get($userId);

    if($db->hasError || count($db->data) <= 0)
        $db = Doctor::get($userId);
    
    if(!$db->hasError && count($db->data) > 0)
    {
        $query = "SELECT * FROM fcm_users WHERE owner='$userId'";
        $res = Database::$conn->query($query);

        if($res && $res->num_rows > 0)
            Database::$conn->query("DELETE FROM fcm_users WHERE owner='$userId'");

        if($res && $res->num_rows <= 0)
        {
            $query = "INSERT INTO fcm_users(`token`, `owner`, `active`) VALUES('" . $data->token . "', '$userId', '1')";
            $res = Database::$conn->query($query);
        }
        
        $response = (object) ['message' => '', 'hasError' => false];
        $response->message = $res ? "Token successfully initiated!" : Database::$conn->error;
        $response->hasError = !$res;
        echo json_encode($response);
        return;
    }

    echo json_encode($db);
}, 'POST');

/**
 * Handles logging out of users
 * 
 * @param int $userId The id of FCM token's owner
 * @return object
 */
Route::add('/fcm/([0-9]+)/logout', function(int $userId) {
    $data = file_get_contents("php://input");
    $data = json_decode($data);
    $data = Security::escapeData($data);
    $db = Patient::get($userId);

    if($db->hasError || count($db->data) <= 0)
        $db = Doctor::get($userId);

    if(!$db->hasError && count($db->data) > 0)
    {
        $response = (object) ['message' => '', 'hasError' => false];
        $query = "SELECT * FROM fcm_users WHERE `owner`='$userId' AND token='" . $data->token . "'";
        $res = Database::$conn->query($query);

        if($res && $res->num_rows > 0)
        {
            $query = "DELETE FROM fcm_users WHERE `owner`='$userId' AND token='" . $data->token . "'";
            $res = Database::$conn->query($query);
            
            $response->message = $res ? "" : Database::$conn->error;
            $response->hasError = !$res;
        }
        
        echo json_encode($response);
        return;
    }
    
    echo json_encode($db);
}, 'POST');

Route::add('/debug/fcm/([0-9]+)/notify', function(int $userId) {
    $data = file_get_contents("php://input");
    $data = json_decode($data);
    $data = Security::escapeData($data);

    echo json_encode(['success' => notify($userId, $data->title, $data->body, $data->data)]);
}, 'POST');

Route::add('/debug/phpmailer/send', function() {
    $data = file_get_contents("php://input");
    $data = json_decode($data);
    $data = Security::escapeData($data);

    echo json_encode(['success' => PHPMailer::sendMail($data->to, $data->subject, $data->message, $data->from, $data->isHtml)]);
}, 'POST');

/**
 * Handles sending notification to a specified
 * user id 
 * 
 * @param int $userId The id of the recepient
 * @param string $title The title of the notification
 * @param string $body The content body of the notification
 * @param array $data The extra payload data to be sent along with the notification [OPTIONAL]
 * @return bool
 */
function notify(int $userId, string $title, string $body, array $data = [])
{
    $query = "SELECT * FROM fcm_users WHERE owner='$userId'";
    $res = Database::$conn->query($query);

    // Don't send notification if user's token is not yet (re)initialized
    if(!$res || $res->num_rows <= 0)
        return false;

    $db = (object) $res->fetch_assoc();

    // Otherwise send notification
    $client = new Client();
    $client->setApiKey(SERVER_KEY);
    $client->injectHttpClient(new \GuzzleHttp\Client());

    $deviceToken = $db->token;
    $notif = new Notification($title, $body);

    $message = new Message();
    $message->addRecipient(new Device($deviceToken));
    $message->setNotification($notif)->setData($data);

    $response = $client->send($message);
}

 /**************************
  * END PUSH NOTIFICATIONS *
  **************************/



/************************
 * START PATIENT ROUTES *
 ************************/

/**
 * Handles login requests
 * 
 * @param object $data The authentication request of the patient
 * @return string $result The json_encoded result of the request
 * @method HTTP_POST
 */
Route::add('/patient/login', function() {
    $data = file_get_contents('php://input');
    $data = json_decode($data);

    $result = Patient::check($data);
    
    if(!$result->hasError && count($result->data) > 0)
    {
        $pass = $data->password;

        if(password_verify($pass, ((object) $result->data[0])->password))
        {
            $result->hasError = false;
        }
        else
        {
            if(isset($result->data))
                unset($result->data);

            $result->message = 'The password you entered is incorrect';
            $result->hasError = true;
        }
    }

    echo json_encode($result);
}, 'POST');

/**
 * Handles adding patient account requests
 * 
 * @param object $data The data of the patient to be added
 * @return string $result The json_encoded result of the request
 * @method HTTP_POST
 */
Route::add('/patient/new', function() {
    $data = file_get_contents('php://input');
    $data = json_decode($data);

    $result = Patient::add($data);
    echo json_encode($result);
}, 'POST');

/**
 * Handles creating new appointment requests
 * 
 * @param object $data The data of the appointment to be created
 * @param int $id The id of the patient that sent the request
 * @return string $result The json_encoded result of the request
 * @method HTTP_POST
 */
Route::add('/patient/([0-9]+)/appointment/new', function(int $id) {
    $data = file_get_contents('php://input');
    $data = json_decode($data);

    $result = Appointment::create($data, $id);
    $bool = $result->hasError;
    $db = Appointment::getFromPatient($id);
    $data->isDoctor = true;

    // Send notification to the doctor if appointment has been successfully created
    if(!$bool)
        notify(
            $data->doctorId, 
            "New Appointment Request", 
            ((object) Patient::get($data->ownerId)->data[0])->fullname . " scheduled an appointment with you and is waiting for your approval.", 
            ['action' => 'patient_newAppointment', 'data' => json_encode($data)]
        );

    $response = (object) ['hasError' => $bool, 'message' => $result->message];
    echo json_encode($response);
}, 'POST');

/**
 * Handles deletion of appointments
 * 
 * @param int $appointmentId The id of the appointment to be POSTd
 * @param int $patientId The id of the patient that sent the request
 * @return string $result The json_encoded result of the request
 * @method HTTP_POST
 */
Route::add('/patient/([0-9]+)/appointment/([0-9]+)/delete', function(int $patientId, int $appointmentId) {
    $db = Patient::get($patientId);

    $saved = Appointment::getFromPatient($patientId, $appointmentId);
    $result = $db->hasError ? $db : Appointment::delete($appointmentId);
    $saved->data[0]['isDoctor'] = true;

    // Send notification to the doctor if appointment has been successfully cancelled
    if(!$result->hasError)
        notify(
            ((object) $saved->data[0])->doctor, 
            "Appointment Cancelled", 
            ((object) Patient::get($patientId)->data[0])->fullname . " cancelled an appointment with you.", 
            ['action' => 'patient_cancelAppointment', 'data' => json_encode($saved->data[0])]
        );

    Request::sendHTTPHeader(201, "");
    $response = (object) ['hasError' => $result->hasError, 'message' => $result->message, 'data' => $saved->data];
    echo json_encode($response);
}, 'POST');

/**
 * Handles updating of appointments
 * 
 * @param object $data The updated data of the appointment
 * @param int $appointmentId The id of the appointment to be updated
 * @param int $patientId The id of the patient that sent the request
 * @return string $result The json_encoded result of the request
 * @method HTTP_POST
 */
Route::add('/patient/([0-9]+)/appointment/([0-9]+)/update', function(int $patientId, int $appointmentId) {
    $data = file_get_contents("php://input");
    $data = json_decode($data);
    $db = Patient::get($patientId);

    $result = $db->hasError ? $db : Appointment::update($appointmentId, $data);
    $bool = $result->hasError;
    $db = Appointment::getFromPatient($patientId, $appointmentId);
    $db->data[0]['isDoctor'] = true;

    // Send notification to the doctor if appointment has been successfully updated
    if(!$bool)
        notify(
            $db->data[0]['doctor'],
            "Appointment Updated", 
            ((object) Patient::get($patientId)->data[0])->fullname . " edited his/her appointment with you and still waiting for your approval.", 
            ['action' => 'patient_updateAppointment', 'data' => json_encode($db->data[0])]
        );

    $response = (object) ['hasError' => $bool, 'message' => $result->message, 'data' => $db->data];
    echo json_encode($response);
}, 'POST');

/**
 * Handles getting information of the appointment
 * 
 * @param int $patientId The id of the patient who sent the request and is the owner of the appointment
 * @param int $appointmentId The id of the appointment that the patient sent
 * @return string $result The json_encoded result of the request
 * @method HTTP_GET
 */
Route::add('/patient/([0-9]+)/appointment/([0-9]+)', function(int $patientId, int $appointmentId) {
    $result = Appointment::getFromPatient($patientId, $appointmentId);
    echo json_encode($result);
}, 'GET');

/**
 * Handles getting approved appointments of the patient
 * 
 * @param int $patientId The id of the patient to who requested/scheduled the appointment
 * @return string $result The json_encoded result of the request
 * @method HTTP_GET
 */
Route::add('/patient/([0-9]+)/appointment/approved', function(int $patientId) {
    $result = Appointment::getFromPatient($patientId, null, Appointment::STATUS_APPROVED);
    echo json_encode($result);
}, 'GET');

/**
 * Handles getting pending appointments of the patient
 * 
 * @param int $patientId The id of the patient to who requested/scheduled the appointment
 * @return string $result The json_encoded result of the request
 * @method HTTP_GET
 */
Route::add('/patient/([0-9]+)/appointment/pending', function(int $patientId) {
    $result = Appointment::getFromPatient($patientId, null, Appointment::STATUS_PENDING);
    echo json_encode($result);
}, 'GET');

/**
 * Handles getting all appointments of the patient
 * 
 * @param int $patientId The id of the patient who requested/scheduled the appointment
 * @return string $result The json_encoded result of the request
 * @method HTTP_GET
 */
Route::add('/patient/([0-9]+)/appointment', function(int $patientId) {
    $result = Appointment::getFromPatient($patientId);
    echo json_encode($result);
}, 'GET');

Route::add('/patient/([0-9]+)/changePassword', function(int $patientId) {
    $data = file_get_contents("php://input");
    $data = json_decode($data);
    $result = Patient::get($patientId);
    
    if(!$result->hasError)
    {
        $oldPassword = $data->old_password;
        $newPassword = password_hash($data->new_password, PASSWORD_BCRYPT);

        if(password_verify($oldPassword, $result->data[0]['password']))
            $result = Patient::update((object) ['password' => $newPassword], $patientId);
        else
        {
            $result->message = "The old password you entered is incorrect, please try again.";
            $result->hasError = true;
        }
    }

    echo json_encode($result);
}, 'POST');

/**********************
 * END PATIENT ROUTES *
 **********************/










/***********************
 * START DOCTOR ROUTES *
 ***********************/

/**
 * Handles login requests of the doctors
 * 
 * @param object $data The data of the doctor
 * @return string $result THe json_encoded result of the request
 * @method HTTP_POST
 */
Route::add('/doctor/login', function() {
    $data = file_get_contents('php://input');
    $data = json_decode($data);

    $result = Doctor::check($data);
    
    if(!$result->hasError && count($result->data) > 0)
    {
        $password = $data->password;
        
        if(password_verify($password, $result->data['password']))
            $result->hasError = false;
        else
        {
            $result->message = 'The password you entered is incorrect';
            $result->hasError = true;
        }
    }
    else
    {
        $result->message = 'The email you entered is not registered in our database';
        $result->hasError = true;
    }

    echo json_encode($result);
}, 'POST');

/**
 * Handles adding new doctor requests
 * 
 * @param object $data The data of the doctor to be added
 * @return string $result The json_encoded result of the request
 * @method HTTP_POST
 */
Route::add('/doctor/new', function() {
    $data = file_get_contents('php://input');
    $data = json_decode($data);

    $result = Doctor::add($data);
    echo json_encode($result);
}, 'POST');

/**
 * Handles approval of appointments
 * 
 * @param int $doctorId The id of the doctor that sent the request
 * @param int $appointmentId The id of the appointment to be approved
 * @return string $result The json_encoded result of the request
 * @method HTTP_POST
 */
Route::add('/doctor/([0-9]+)/appointment/([0-9]+)/approve', function(int $doctorId, int $appointmentId) {
    $db = Doctor::get($doctorId);

    $result = $db->hasError ? $db : Appointment::update($appointmentId, ((object) ['status' => Appointment::STATUS_APPROVED]));
    $bool = $result->hasError;
    $db = Appointment::getFromDoctor($doctorId, $appointmentId);

    // Send notification to the patient if appointment has been successfully approved
    if(!$bool)
        notify(
            ((object) $db->data[0])->owner, 
            "Appointment Approved", 
            "Your appointment with Dr. " . ((object) Doctor::get($doctorId)->data[0])->fullname . " has been approved and is now scheduled.", 
            ['action' => 'doctor_approveAppointment', 'data' => json_encode($db->data[0])]
        );

    $response = (object) ['hasError' => $bool, 'message' => $result->message, 'data' => $db->data];
    echo json_encode($response);
}, 'POST');

/**
 * Handles rescheduling of appointments
 * 
 * @param int $doctorId The id of the doctor that sent the request
 * @param int $appointmentId The id of the appointment to be rescheduled
 * @return string $result The json_encoded result of the request
 * @method HTTP_POST
 */
Route::add('/doctor/([0-9]+)/appointment/([0-9]+)/reschedule', function(int $doctorId, int $appointmentId) {
    $data = file_get_contents('php://input');
    $data = json_decode($data);
    $db = Doctor::get($doctorId);

    $data->status = Appointment::STATUS_RESCHEDULED;
    $result = $db->hasError ? $db : Appointment::update($appointmentId, $data);
    $bool = $result->hasError;
    $db = Appointment::getFromDoctor($doctorId, $appointmentId);

    // Send notification to the patient if appointment has been successfully approved
    if(!$bool)
        notify(
            ((object) $db->data[0])->owner, 
            "Appointment Rescheduled", 
            "Your appointment with Dr. " . ((object) Doctor::get($doctorId)->data[0])->fullname . " has been rescheduled.", 
            ['action' => 'doctor_rescheduleAppointment', 'data' => json_encode($db->data[0])]
        );

    $response = (object) ['hasError' => $bool, 'message' => $result->message, 'data' => $db->data];
    echo json_encode($response);
}, 'POST');

/**
 * Handles declining of appointments
 * 
 * @param int $doctorId The id of the doctor that sent the request
 * @param int $appointmentId The id of the appointment to be declined
 * @return string $result The json_encoded result of the request
 * @method HTTP_POST
 */
Route::add('/doctor/([0-9]+)/appointment/([0-9]+)/decline', function(int $doctorId, int $appointmentId) {
    $db = Doctor::get($doctorId);

    $result = $db->hasError ? $db : Appointment::update($appointmentId, ((object) ['status' => Appointment::STATUS_DECLINED]));
    $bool = $result->hasError;
    $db = Appointment::getFromDoctor($doctorId, $appointmentId);

    // Send notification to the patient if appointment has been successfully approved
    if(!$bool)
        notify(
            ((object) $db->data[0])->owner,  
            "Appointment Declined", 
            "Your appointment with Dr. " . ((object) Doctor::get($doctorId)->data[0])->fullname . " has been declined", 
            ['action' => 'doctor_declineAppointment', 'data' => json_encode($db->data[0])]
        );

    $response = (object) ['hasError' => $bool, 'message' => $result->message, 'data' => $db->data];
    echo json_encode($response);
}, 'POST');

/**
 * Handles cancellation of appointments
 * 
 * @param int $doctorId The id of the doctor that sent the request
 * @param int $appointmentId The id of the appointment to be declined
 * @return string $result The json_encoded result of the request
 * @method HTTP_POST
 */
Route::add('/doctor/([0-9]+)/appointment/([0-9]+)/cancel', function(int $doctorId, int $appointmentId) {
    $db = Doctor::get($doctorId);

    $result = $db->hasError ? $db : Appointment::update($appointmentId, ((object) ['status' => Appointment::STATUS_CANCELLED]));
    $bool = $result->hasError;
    $db = Appointment::getFromDoctor($doctorId, $appointmentId);

    // Send notification to the patient if appointment has been successfully approved
    if(!$bool)
        notify(
            ((object) $db->data[0])->owner, 
            "Appointment Cancelled", 
            "Your appointment with Dr. " . ((object) Doctor::get($doctorId)->data[0])->fullname . " has been cancelled.", 
            ['action' => 'doctor_cancelAppointment', 'data' => json_encode($db->data[0])]
        );

    $response = (object) ['hasError' => $bool, 'message' => $result->message, 'data' => $db->data];
    echo json_encode($response);
}, 'POST');

/**
 * Handles getting information of the appointment
 * 
 * @param int $doctorId The id of the doctor to whom the appointment was requested/scheduled
 * @param int $appointmentId The id of the appointment that the doctor sent to the doctor
 * @return string $result The json_encoded result of the request
 * @method HTTP_GET
 */
Route::add('/doctor/([0-9]+)/appointment/([0-9]+)', function(int $doctorId, int $appointmentId) {
    $result = Appointment::getFromDoctor($doctorId, $appointmentId);
    echo json_encode($result);
}, 'GET');

/**
 * Handles getting approved appointments of the doctor
 * 
 * @param int $doctorId The id of the doctor to whom the appointment was requested/scheduled to
 * @return string $result The json_encoded result of the request
 * @method HTTP_GET
 */
Route::add('/doctor/([0-9]+)/appointment/approved', function(int $doctorId) {
    $result = Appointment::getFromDoctor($doctorId, null, Appointment::STATUS_APPROVED . " " . Appointment::STATUS_RESCHEDULED);
    echo json_encode($result);
}, 'GET');

/**
 * Handles getting pending appointments of the doctor
 * 
 * @param int $doctorId The id of the doctor to whom the appointment was requested/scheduled to
 * @return string $result The json_encoded result of the request
 * @method HTTP_GET
 */
Route::add('/doctor/([0-9]+)/appointment/pending', function(int $doctorId) {
    $result = Appointment::getFromDoctor($doctorId, null, Appointment::STATUS_PENDING);
    echo json_encode($result);
}, 'GET');

Route::add('/doctor/([0-9]+)/changePassword', function(int $doctorId) {
    $data = file_get_contents("php://input");
    $data = json_decode($data);
    $result = Doctor::get($doctorId);
    
    if(!$result->hasError)
    {
        $oldPassword = $data->old_password;
        $newPassword = password_hash($data->new_password, PASSWORD_BCRYPT);

        if(password_verify($oldPassword, $result->data[0]['password']))
            $result = Doctor::update((object) ['password' => $newPassword], $doctorId);
        else
        {
            $result->message = "The old password you entered is incorrect, please try again.";
            $result->hasError = true;
        }
    }

    echo json_encode($result);
}, 'POST');

/*********************
 * END DOCTOR ROUTES *
 *********************/



/**********************
 * START ADMIN ROUTES *
 **********************/

Route::add('/admin/login', function() {
    $data = file_get_contents("php://input");
    $data = json_decode($data);
    $check = Admin::check((object) ["email_address" => $data->email_address]);
    
    if(!$check->hasError)
    {
        if(password_verify($data->password, $check->data[0]['password']))
            $check->hasError = false;
        else
        {
            if(isset($check->data))
                unset($check->data);

            $check->message = "The password you entered is incorrect, please try again.";
            $check->hasError = true;
        }
    }
    else
    {
        if(isset($check->data))
            unset($check->data);

        $check->message = "The email address you entered does not exist in the database!";
        $check->hasError = true;
    }

    echo json_encode($check);
}, 'POST');

Route::add('/admin/([0-9]+)/changePassword', function(int $adminId) {
    $data = file_get_contents("php://input");
    $data = json_decode($data);
    $result = Admin::get($adminId);
    
    if(!$result->hasError)
    {
        $oldPassword = $data->old_password;
        $newPassword = password_hash($data->new_password, PASSWORD_BCRYPT);

        if(password_verify($oldPassword, $result->data[0]['password']))
            $result = Admin::update($adminId, (object) ['password' => $newPassword]);
        else
        {
            $result->message = "The old password you entered is incorrect, please try again.";
            $result->hasError = true;
        }
    }

    echo json_encode($result);
}, 'POST');

Route::add('/admin/([0-9]+)/doctor/new', function(int $adminId) {
    $data = file_get_contents("php://input");
    $data = json_decode($data);
    $result = Admin::get($adminId);

    if(!$result->hasError)
    {
        $password = uniqid('darp_');
        $result = Doctor::add($data);

        if(!$result->hasError)
            $result = (object) [
                'hasError' => !PHPMailer::sendMail(
                    $data->email_address,
                    "Account Registration",
                    "<html lang='en'>
                        <head>
                            <meta charset='utf-8'/>
                            <meta name='viewport' content='width=display-width, initial-scale=1.0'/>
                            <title>Account Registration</title>
                        </head>
                        <body>
                            <p> <strong>IMPORTANT: </strong> <span style='color: red;'>Don't share your password with anyone!</span> </p>
                            <p> <strong>Password: </strong> {$password}</p>
                            <p> You can change your password later when you log in on the app. </p>
                        </body>
                    </html>",
                    "admin@us-imm-node1b.000webhost.io",
                    true
                ), 
                'message' => PHPMailer::$success ? "Successfully sent doctor's password to his/her email address" : "We have failed to send doctor's generated password to his/her email address."
            ];
        else
            $result->message = "Failed to create another account, try again later.\n\nCause: " . !isset(Database::$conn->error) ? $result->message : Database::$conn->error;
    }

    echo json_encode($result);
 }, 'POST');

 Route::add('/admin/([0-9]+)/doctor/([0-9]+)/delete', function(int $adminId, int $doctorId) {
    $result = Admin::get($adminId);

    if(!$result->hasError)
        $result = Doctor::delete($doctorId);
    
    echo json_encode($result);
 }, 'POST');

 Route::add('/admin/([0-9]+)/doctor/([0-9]+)/update', function(int $adminId, int $doctorId) {
    $data = file_get_contents("php://input");
    $data = json_decode($data);
    $result = Admin::get($adminId);

    if(!$result->hasError)
        $result = Doctor::update($data, $doctorId);
    
    echo json_encode($result);
 }, 'POST');

 Route::add('/admin/([0-9]+)/doctor', function(int $adminId) {
    $result = Admin::get($adminId);

    if(!$result->hasError)
        $result = Doctor::get();

    echo json_encode($result);
 }, 'GET');
 
/********************
 * END ADMIN ROUTES *
 ********************/


/******************************
 * START MEDICAL FIELD ROUTES *
 ******************************/

Route::add('/doctor/medical-fields', function() {
    $query = "SELECT * FROM specialties";
    $res = Database::$conn->query($query);

    $data = [];
    while($row = $res->fetch_assoc())
        array_push($data, $row);

    $result = (object) ['hasError' => false];
    $result->data = $data;
    echo json_encode($result);
}, 'GET');

Route::add('/doctor/medical-fields', function() {
    $data = file_get_contents("php://input");
    $data = json_decode($data);
    $name = $data->name;

    $query = "SELECT * FROM doctors WHERE specialty='" . Security::escapeString($name) . "'";
    $res = Database::$conn->query($query);

    if($res && $res->num_rows > 0)
    {
        $data = [];
        while($row = $res->fetch_assoc())
            array_push($data, $row);

        $result = (object) ['hasError' => false];
        $result->data = $data;
        echo json_encode($result);
    }
    else
        echo json_encode(['message' => 'No doctor has that specialty', 'hasError' => true]);
}, 'POST');

 /***************************
 * END MEDICAL FIELD ROUTES *
 ****************************/

/**
 * This method is responsible for running
 * all the routes
 * 
 * @param string Base url
 */
Route::run('/v1');
?>