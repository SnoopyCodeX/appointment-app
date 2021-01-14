<?php

/**
 * This class handles all the CRUD operations
 * for all of the appointments
 * 
 * @author John Roy L. Calimlim
 * @package /api/objects
 */
class Appointment {
    const STATUS_APPROVED = "approved";
    const STATUS_PENDING = "pending";
    const STATUS_CANCELLED = "cancelled";
    const STATUS_DECLINED = "declined";
    const STATUS_RESCHEDULED = "rescheduled";

    private static string $table = 'appointments';
    private static object $result;
    private static mysqli $conn;

    private function __construct(mysqli $conn)
    {
        self::$conn = $conn;
        self::$result = ((object) []);
    }

    /**
     * Handles initiation of the Appoointment class.
     * 
     * @param mysqli $conn The connection to the database
     * @return object Appointment.class
     */
    public static function init(mysqli $conn)
    {
        return (new Appointment($conn));
    }

    /**
     * Handles the creation of an appointment
     * 
     * @param object $data The data of the appointment
     * @param int $patientId The id of the patient that sent the request
     * @return object $result A result object that contains the error message and data of the request
     */
    public static function create(object $data, int $patientId)
    {
        $db = Patient::get($patientId);
        $data = Security::escapeData($data);

        if(!$db->hasError && count($db->data) > 0)
        {
            $data->date = date("Y-m-d", strtotime($data->date));
            $data->time = date("H:i:s", strtotime($data->time));
            $query = "SELECT * FROM " . self::$table . " WHERE date='" . $data->date . "' AND time='" . $data->time . "' AND status='" . self::STATUS_APPROVED . "'";
            $res = self::$conn->query($query);

            if($res && $res->num_rows <= 0)
            {
                $data->status = self::STATUS_PENDING;
                $data->created_at = date("Y-m-d H:i:s", time());

                $query = "INSERT INTO " . self::$table . "(";

                foreach($data as $key => $val)
                    if($key == 'ownerId' || $key == 'doctorId')
                    {
                        $key = substr($key, 0, strlen($key) - 2);
                        $query .= "$key, ";
                    }
                    else
                        $query .= "$key, ";
                        
                $query = substr($query, 0, strlen($query) - 2) . ") VALUES(";

                foreach($data as $key => $val)
                    $query .= "'$val', ";
                $query = substr($query, 0, strlen($query) - 2) . ")";

                $res = self::$conn->query($query);
                self::$result->message = $res ? "Your appointment has been successfully scheduled and is now waiting for the approval of the doctor" : self::$conn->error;
                self::$result->hasError = !$res;
            }
            else
            {
                self::$result->message = $res ? "An appointment is already scheduled on this date and time" : self::$conn->error;
                self::$result->hasError = true;
            }
        }
        else
        {
           self::$result->message = $db->message;
           self::$result->hasError = true;
        }

        return self::$result;
    }

    /**
     * Handles the deletion of the appointment
     * 
     * @param int $id The id of the appointment to be deleted
     * @return object $result A result object that contains the error message and data of the request
     */
    public static function delete(int $id)
    {
        $query = "SELECT * FROM " . self::$table . " WHERE id='$id'";
        $res = self::$conn->query($query);

        if($res && $res->num_rows > 0)
        {
            $query = "DELETE FROM " . self::$table . " WHERE id='$id'";
            $res = self::$conn->query($query);

            self::$result->message = $res ? "Appointment has been successfully deleted from the database" : self::$conn->error;
            self::$result->hasError = !$res;
        }
        else
        {
            self::$result->message = $res ? "You are trying to delete an appointment that does not exist in the database" : self::$conn->error;
            self::$result->hasError = true;
        }

        return self::$result;
    }

    /**
     * Handles the update to the appointment
     * 
     * @param int $id The id of the appointment
     * @param object $data The updated data of the appointment
     * @return object $result A result object that contains the error message and data of the request
     */
    public static function update(int $id, object $data)
    {
        $query = "SELECT * FROM " . self::$table . " WHERE id='$id'";
        $res = self::$conn->query($query);

        $data = Security::escapeData($data);

        if($res && $res->num_rows > 0)
        {
            $query = "UPDATE " . self::$table . " SET ";

            if(isset($data->date) || isset($data->time))
            {
                if(isset($data->date))
                    $data->date = date("Y-m-d", strtotime($data->date));
                if(isset($data->time))
                    $data->time = date("H:i:s", strtotime($data->time));

                $check = "SELECT * FROM " . self::$table . " WHERE id <> '$id' AND status='" . self::STATUS_APPROVED . "'";

                if(isset($data->date))
                    $check .= " AND date='" . $data->date . "'";

                if(isset($data->time) && !isset($data->date))
                    $check .= " AND time='" . $data->time . "'";
                else if(isset($data->time) && isset($data->date))
                    $check .= " AND time='" . $data->time . "'";

                $rcheck = self::$conn->query($check);
                if($rcheck && $rcheck->num_rows > 0)
                {
                    self::$result->message = $rcheck ? "The time/date you selected is already occupied" : self::$conn->error;
                    self::$result->hasError = true;
                    return self::$result;
                }
            }

            foreach($data as $key => $val)
            {
                if($key == 'ownerId' || $key == 'doctorId')
                {
                    $key = substr($key, 0, strlen($key) - 2);
                    $query .= "$key='$val', ";
                }
                else
                    $query .= "$key='$val', ";
            }

            $query = substr($query, 0, strlen($query) - 2) . " WHERE id='$id'";

            $res = self::$conn->query($query);
            self::$result->message = $res ? "Appointment has been successfully updated" : self::$conn->error;
            self::$result->hasError = !$res;
        }
        else
        {
            self::$result->message = $res ? "You are trying to update an appointment that does not exist in the database" : self::$conn->error;
            self::$result->hasError = true;
        }
    
        return self::$result;
    }

    /**
     * Fetches the scheduled appointment of the doctor
     * 
     * @param int $doctorId The id of the doctor 
     * @param int $appointmentId The id of the appointment (optional)
     * @param bool $status The status of the appointments (optional)
     * @return object $result A result object that contains the error message and data of the request
     */
    public static function getFromDoctor(int $doctorId, ?int $appointmentId = null, ?string $status = null)
    {
        $db = Doctor::get($doctorId);

        if(!$db->hasError && count($db->data) > 0)
        {
            $query = "SELECT * FROM " . self::$table . " WHERE doctor='$doctorId'";

            if(isset($appointmentId))
                $query .= " AND id='$appointmentId'";
            
            if(isset($status) && !empty($status))
                if(strtolower($status) == (self::STATUS_APPROVED . " " . self::STATUS_RESCHEDULED))
                    $query .= " AND status='" . self::STATUS_RESCHEDULED . "' OR status='" . self::STATUS_APPROVED . "'";
                else
                    $query .= " AND status='$status'";
            
            $res = self::$conn->query($query);
           
            if($res && $res->num_rows > 0)
            {
                    $data = [];

                    while($row = $res->fetch_assoc())
                    {
                        $row['isDoctor'] = true;
                        array_push($data, $row);
                    }

                    self::$result->data = $data;
                    self::$result->hasError = false;
            }
            else
            {
                    self::$result->message = $res ? "No scheduled appointments" : self::$conn->error;
                    self::$result->hasError = true;
            }
        }
        else
        {
            self::$result->message = $db->message;
            self::$result->hasError = true;
        }

        return self::$result;
    }

    /**
     * Fetches the scheduled appointment of the patient
     * 
     * @param int $patientId The id of the patient 
     * @param int $appointmentId The id of the appointment (optional)
     * @param bool $status The status of the appointments (optional)
     * @return object $result A result object that contains the error message and data of the request
     */
    public static function getFromPatient(int $patientId, ?int $appointmentId = null, ?string $status = null)
    {
        $db = Patient::get($patientId);

        if(!$db->hasError && count($db->data) > 0)
        {
            $query = "SELECT * FROM " . self::$table . " WHERE owner='$patientId'";

            if(isset($appointmentId))
                $query .= " AND id='$appointmentId'";
            
                if(isset($status) && !empty($status))
                    if(strtolower($status) == (self::STATUS_APPROVED . " " . self::STATUS_RESCHEDULED))
                        $query .= " AND status='" . self::STATUS_RESCHEDULED . "' OR status='" . self::STATUS_APPROVED . "'";
                    else
                        $query .= " AND status='$status'";


            $res = self::$conn->query($query);

            if($res && $res->num_rows > 0)
            {
                $data = array();

                while($row = $res->fetch_assoc())
                    array_push($data, $row);
                
                self::$result->data = $data;
                self::$result->hasError = false;
            }
            else
            {
                self::$result->message = $res ? "No scheduled appointments" : self::$conn->error;
                self::$result->hasError = true;
            }
        }
        else
        {
            self::$result->message = $db->message;
            self::$result->hasError = true;
        }

        return self::$result;
    }
}

?>