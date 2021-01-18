<?php
namespace JRLC\DARP\core;

use \JRLC\DARP\core\Security;

class Request {
    private function __construct()
    {}

    /**
     * Fetches POST requests
     * 
     * @param string $key The key of the request
     * @return mixed The value of the key from the POST request
     */
    public static function fetchPost(string $key)
    {
        return (Security::escapeString($_POST[$key] ?? ""));
    }

    /**
     * Fetches requests that are sent using GET request
     * 
     * @param string $key The key of the request
     * @return mixed The value of the key from the GET request
     */
    public static function fetchGet(string $key)
    {
        return (Security::escapeString($_GET[$key] ?? ""));
    }

    /**
     * Gets a value from the HEADER sent with HTTP
     * 
     * @param string $key The key of the HEADER (default: null  ->  get all requested headers)
     * @return string The value of the key from the HTTP header
     */
    public static function getRequestedHeader(string $key = null)
    {
        if(!is_null($key) && !empty($key))
        {
            $header = str_replace('-', '_', $key);
            $header =  strtoupper($header);
            
            if(array_key_exists($header, $_SERVER))
                return $_SERVER[$key];
        }
        else
        {
            $headers = [];

            foreach($_SERVER as $key => $val)
            {
                if(substr($key, 0, 5) <> 'HTTP_')
                    continue;
                
                $header = str_replace(' ', '-', ucwords(str_replace('_', ' ', strtolower(substr($key, 5)))));
                $headers[$header] = $val;
            }

            return $headers;
        }
    }

    /**
     * Sends HTTP response
     * 
     * @param int $httpCode The HTTP code to be sent
     * @param string $httpMessage The HTTP message to be sent
     */
    public static function sendHTTPHeader(int $httpCode, string $httpMessage)
    {
        header("HTTP/1.1 $httpCode $httpMessage");
    }
}

?>