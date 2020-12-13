<?php

/**
 * This class handles all the CRUD operations
 * for all of the patient accounts
 * 
 * @author John Roy L. Calimlim
 * @package /api/objects
 */
class Patient {
    private static string $table = 'patients';
    private static object $result;
    private static mysqli $conn;

    private function __construct(mysqli $conn)
    {
        self::$conn = $conn;
        self::$result = ((object) ['message' => "", 'hasError' => false]);
    }

    /**
     * Handles initiation of the Patient class.
     * 
     * @param mysqli $conn The connection to the database
     * @return Patient.class
     */
    public static function init(mysqli $conn)
    {
        return (new Patient($conn));
    }

    /**
     * Gets the information of the patient
     * 
     * @param int $id The id of the patient (optional, default: -1 = means fetch all patient account from the database)
     * @return object $result A result object that contains the error message and data of the request
     */
    public static function get(int $id = -1)
    {
        $query = "SELECT * FROM " . self::$table;

        // Empty the data from the result
        if(isset(self::$result->data))
            unset(self::$result->data);

        if($id > 0)
            $query .= " WHERE id=('$id')";

        $res = self::$conn->query($query);

        if($res && $res->num_rows > 0)
        {
            $data = [];

            while($row = $res->fetch_assoc())
                array_push($data, $row);

            self::$result->data = $data;
            self::$result->hasError = false;
        }
        else
        {
            self::$result->message = $res ? "No records was found in the database" : self::$conn->error;
            self::$result->hasError = true;
        }

        return self::$result;
    }

    /**
     * Checks the information of the patient
     * 
     * @param int $id The data of the patient
     * @return object $result A result object that contains the error message and data of the request
     */
    public static function check(object $data)
    {
        $query = "SELECT * FROM " . self::$table . " WHERE email='" . $data->email . "' AND fullname='" . $data->fullname . "'";
        $res = self::$conn->query($query);

        // Empty the data from the result
        if(isset(self::$result->data))
            unset(self::$result->data);

        $data = Security::escapeData($data);

        if($res && $res->num_rows > 0)
        {
            self::$result->data = [$res->fetch_assoc()];
            self::$result->hasError = false;
        }
        else
        {
            self::$result->message = $res ? "Your account does not appear to be registered in the database" : $conn->error;
            self::$result->hasError = true;
        }

        return self::$result;
    }

    /**
     * Handles adding new Patient account in the database
     * 
     * @param object $data The data of the new Patient account
     * @return object $result A result object that contains the error message and data of the request
     */
    public static function add(object $data)
    {
        $query = "SELECT * FROM " . self::$table . " WHERE fullname=('" . $data->fullname . "') AND email=('" . $data->email . "')";
        $res = self::$conn->query($query);

        // Empty the data from the result
        if(isset(self::$result->data))
            unset(self::$result->data);

        $data = Security::escapeData($data);

        if($res && $res->num_rows <= 0)
        {
            $data->password = password_hash($data->password, PASSWORD_BCRYPT);
            $data->created_at = date('Y-m-d h:i:s', time());

            $query = "INSERT INTO " . self::$table . "(";

            foreach($data as $key => $val)
                $query .= "$key, ";
            $query = substr($query, 0, strlen($query) - 2) . ") VALUES(";

            foreach($data as $key => $val)
                $query .= "'$val', ";
            $query = substr($query, 0, strlen($query) - 2) . ")";

            $res = self::$conn->query($query);
            self::$result->message = $res ? "Account has been registered successfully, you may now log in." : self::$conn->error;
            self::$result->hasError = !$res;
        }
        else
        {
            self::$result->message = $res ? "The name and email you entered appears to be already registered in the database" : self::$conn->error;
            self::$result->hasError = true;
        }

        return self::$result;
    }

    /**
     * Handles deletion of the patient account from the database
     * 
     * @param int $id The id of the patient
     * @return object $result A result object that contains the error message and data of the request
     */
    public static function delete(int $id)
    {
        $db = self::get($id);

        // Empty the data from the result
        if(isset(self::$result->data))
            unset(self::$result->data);

        if(!$db->hasError && count($db->data) > 0)
        {
            $query = "DELETE FROM " . self::$table . " WHERE id=('$id')";
            $res = self::$conn->query($query);

            self::$result->message = $res ? "The account has been successfully deleted!" : self::$conn->error;
            self::$result->hasError = !$res;
        }
        else
        {
            self::$result->message = "The account may have already been deleted.";
            self::$result->hasError = true;
        }

        return self::$result;
    }

    /**
     * Updates the information of the patient
     * 
     * @param object $data The updated information of the patient
     * @param int $id The id of the patient to be updated
     * @return object $result A result object that contains the error message and data of the request
     */
    public static function update(object $data, int $id)
    {
        $db = self::get($id);

        // Empty the data from the result
        if(isset(self::$result->data))
            unset(self::$result->data);

        $data = Security::escapeData($data);

        if(!$db->hasError && count($db->data) > 0)
        {
            $query = "UPDATE " . self::$table . " SET ";

            foreach($data as $key => $val)
                $query .= "$key='$val', ";
            $query = substr($query, 0, strlen($query) - 2) . " WHERE id='$id'";
            
            $res = self::$conn->query($query);
            self::$result->message = $res ? "The account has been successfully updated!" : self::$conn->error;
            self::$result->hasError = !$res;
        }
        else
        {
            self::$result->message = "Cannot update this account due to it's non-existence in the database";
            self::$result->hasError = true;
        }

        return self::$result;
    }
}

?>