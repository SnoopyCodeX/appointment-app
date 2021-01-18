<?php

namespace JRLC\DARP\config;
/**
 * This class holds the database configuration
 * such as host name, username, password and 
 * database's name.
 * 
 * This also holds the connection variable
 * that is connected to the database.
 * 
 * @author John Roy L. Calimlim
 * @see $host, $username, $password, $db_name  (Change their values if you have your own hosting)
 */
class Database {
    public static \mysqli $conn;
	
	private function __construct()
	{
		self::$conn = new \mysqli(DB_HOST, DB_USER, DB_PASS, DB_NAME);
	}
	
	/**
	 * Handles the initialization of the
	 * connection to the database
	 * 
	 * @return object Database.class
	 */
	public static function init()
	{
		return new Database();
	}
}

?>