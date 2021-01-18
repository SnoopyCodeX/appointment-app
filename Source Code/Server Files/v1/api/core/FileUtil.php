<?php
namespace JRLC\DARP\core;

class FileUtil {
	public function __construct()
	{}
	
	public static function saveFile(string $data, string $name)
	{
		if(!self::isBase64($data))
			return false;
		
		$data = base64_decode($data);
		$dir = DIR_UPLOADS . "/$name";
		$res = file_put_contents($dir, $data);
	}
	
	public static function getImage($dir)
	{
		if(!file_exists($dir))
			return false;

		$file = file_get_contents($dir);
		header('Content-Type: ' . mime_content_type($dir));
		echo $file;
	}
	
	public static function getFileData($dir)
	{
		if(!file_exists($dir))
			return null;

		return (file_get_contents($dir));
	}

	public static function deleteFile($dir)
	{
		if(!file_exists($dir))
			return false;

		return unlink($dir);
	}
	
	public static function isBase64(string $str)
	{
		return (base64_decode($str, true) !== false);
	}

	public static function validateUrl($url) 
	{
		$path = parse_url($url, PHP_URL_PATH);
		$encoded_path = array_map('urlencode', explode('/', $path));
		$url = str_replace($path, implode('/', $encoded_path), $url);
	
		return filter_var($url, FILTER_VALIDATE_URL) ? true : false;
	}
}

?>