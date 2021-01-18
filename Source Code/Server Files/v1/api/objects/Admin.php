<?php

namespace JRLC\DARP\objects;

use \JRLC\DARP\config\Database;
use \JRLC\DARP\core\Security;

/**
 * Handles all CRUD operation for
 * admin accounts.
 * 
 * @author John Roy L. Calimlim
 * @package /api/objects
 */
class Admin {
    private static string $table = "admins";
    private static ?object $result = null;
    private static ?\mysqli $conn = null;

    private function __construct(\mysqli $conn)
    {
        self::$result = (object) [];
        self::$conn = $conn;
    }

    /**
     * Handles initiation of Admin class
     * 
     * @param mysqli $conn The mysqli connection object
     * @return object Admin
     */
    public static function init(\mysqli $conn) : Admin
    {
        return (new Admin($conn));
    }

    /**
     * Checks if the data is existing in the database
     * 
     * @param object $data The data to be verified if it exists in the database
     * @return object
     */
    public static function check(object $data) : object
    {
        $query = "SELECT * FROM " . self::$table. " WHERE ";
        $data = Security::escapeData($data);

        foreach($data as $key => $val)
            $query .= "$key='$val' AND ";
        $query = substr($query, 0, strlen($query) - 5);

        $res = self::$conn->query($query);
        if($res && $res->num_rows > 0)
        {
            $data = [$res->fetch_assoc()];
            self::$result->data = $data;
            self::$result->hasError = false;
        }
        else
        {
            self::$result->message = $res ? "Account does not exist!" : self::$conn->error;
            self::$result->hasError = true;
        }

        return self::$result;
    }

    /**
     * Fetches a data in the database using an id
     * and returns an error message if the data does
     * not exist in the database.
     * 
     * @param int $id The id of the data to be fetched in the database
     * @return object
     */
    public static function get(int $id) : object
    {
        $query = "SELECT * FROM " . self::$table . " WHERE id='$id'";
        $res = self::$conn->query($query);

        if($res && $res->num_rows > 0)
        {
            $data = $res->fetch_assoc();
            self::$result->data = [$data];
            self::$result->hasError = false;
        }
        else
        {
            self::$result->message = $res ? "Account does not exist!" : self::$conn->error;
            self::$result->hasError = true;
        }

        return self::$result;
    }

    /**
     * Handles adding a new data in the database,
     * returns an error message if the data already exists
     * in the database.
     * 
     * @param object $data The data to be added in the database.
     * @return object
     */
    public static function add(object $data) : object
    {
        $check = self::check($data);
        $data = Security::escapeData($data);

        if($check->hasError)
        {
            $query = "INSERT INTO " . self::$table . "(";

            foreach($data as $key => $val)
                $query .= "'$key', ";
            $query = substr($query, 0, strlen($query) - 2);
            $query .= ") VALUES(";

            foreach($data as $key => $val)
                $query .= "'$val', ";
            $query = substr($query, 0, strlen($query) - 2);
            $query .= ")";

            $res = self::$conn->query($query);
            self::$result->message = $res ? "Admin has been successfully created!" : self::$conn->error;
            self::$result->hasError = !$res;
        }
        else
        {
            self::$result->message = "Admin already exist in the database!";
            self::$result->hasError = true;
        }

        return self::$result;
    }

    /**
     * Handles updating of a data in the database
     * using it's id as the reference of which data
     * in the database should be updated. Returns
     * an error message if the reference id does not
     * exist in the database.
     * 
     * @param int $id The id of the data that will be updated
     * @param object $data The new data that will be overriding the existing data in the database
     * @return object
     */
    public static function update(int $id, object $data) : object
    {
        $check = self::check((object) ['id' => $id]);
        $data = Security::escapeData($data);

        if(!$check->hasError)
        {
            $query = "UPDATE " . self::$table . " SET ";

            foreach($data as $key => $val)
                $query .= "$key='$val', ";
            $query = substr($query, 0, strlen($query) - 2);
            $query .= " WHERE id='$id'";

            $res = self::$conn->query($query);
            self::$result->message = $res ? "Admin has been successfully updated!" : self::$conn->error;
            self::$result->hasError = !$res;
        }
        else
        {
            self::$result->message = $check->message;
            self::$result->hasError = $check->hasError;
        }

        return self::$result;
    }

    /**
     * Handles the deletion of a data in the database
     * using it's id as a reference of which data
     * in the database will be deleted. Returns
     * an error message if the reference id does
     * not exist in the database.
     * 
     * @param int $id The id of the data to be deleted
     * @return object
     */
    public static function delete(int $id) : object
    {
        $check = self::check((object) ['id' => $id]);
        $data = Security::escapeData($data);

        if(!$check->hasError)
        {
            $query = "DELETE FROM " . self::$table . " WHERE id='$id'";

            $res = self::$conn->query($query);
            self::$result->message = $res ? "Admin has been successfully deleted!" : self::$conn->error;
            self::$result->hasError = !$res;
        }
        else
        {
            self::$result->message = $check->message;
            self::$result->hasError = $check->hasError;
        }

        return self::$result;
    }
}

?>