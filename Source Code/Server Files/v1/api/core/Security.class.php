<?php

use Zend\Config\Config;
use Zend\Config\Factory;
use Zend\Http\PhpEnvironment\Request as ZendRequest;
use \Firebase\JWT;

class Security {
    public function __construct()
    {}

    public static function escapeString(string  $str)
    {
        return Database::$conn->real_escape_string($str);
    }

    public static function escapeData(object $data)
    {
        foreach($data as $key => $val)
            if($val instanceof string)
                $data->$key = self::escapeString($val);

        return (object) $data;
    }

    public static function isUserAuthenticated(string $method)
    {
        $request = new ZendRequest();

        if(strtolower($request->getMethod()) <> strtolwer($method))
            return false;

        $authHeader = $request->getHeader('authorization');

        if($authHeader)
        {
            list($jwtToken) = sscanf($authHeader->toString(), 'Authorization: Bearer %s');

            if($jwtToken)
            {
                try {
                    $config = Factory::fromFile(DIR_CONFIG . '/Config.php', true);
                    $secret = base64_decode($config->get('JWTSecret'));
                    $token = JWT::decode($jwtToken, $secret, ['HS512']);
                    return true;
                } catch(Exception $e) {
                    Request::sendHTTPHeader(401, 'Unauthorized');
                }
            }
            else
                Request::sendHTTPHeader(402, 'Bad Request');
        }
        else
        {
            Request::sendHTTPHeader(402, 'Bad Request');
            echo json_encode(['message' => 'Token not found in request', 'hasError' => true]);
        }
    }
}

?>