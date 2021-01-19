<?php
namespace JRLC\DARP\core;

use PHPMailer\PHPMailer\PHPMailer as _PHPMailer_;
use PHPMailer\PHPMailer\SMTP as _SMTP_;
use PHPMailer\PHPMailer\Exception as _Exception_;

/**
 * Handles sending SMTP Mails
 * 
 * @uses PHPMailer\PHPMailer
 */
class PHPMailer {
    /**
     * True if the mail was sent successfully to the recepient.
     * False if failed.
     * 
     * @var bool
     */
    public static bool $success = false;

    /**
     * Sends email to recepient.
     * 
     * @param string $to Recepient's gmail account
     * @param string $subject Subject of email
     * @param string $message Message to be sent
     * @param string $from Sender's mail
     * @return bool
     */
    public static function sendMail(string $to, string $subject, string $message, string $from, bool $isHtml = false) : bool
    {
        $mail = new _PHPMailer_(true);

        try {
            $mail->isMail();
            $mail->setFrom($from);
            $mail->addAddress($to);
            $mail->addReplyTo($from);
            $mail->isHTML($isHtml);
            $mail->Subject = $subject;
            $mail->Body    = $message;
        
            $mail->send();
            return (self::$success = true);
        } catch (_Exception_ $e) {
            return (self::$success = false);
        }
    }
}

?>