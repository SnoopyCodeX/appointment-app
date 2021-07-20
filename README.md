## :mask: Doctor Appointment App :mask:

### :open_file_folder: Folders in this Project
- Application Source code: "Appointment App" folder
- Server Source code: "Server files" folder

### :page_with_curl: File extensions
- xxx.java & xxx.xml - Used for the android application itself
- xxx.htaccess & xxx.php - Used for the Server side of the application

### :memo: Coding Design Used
- REST (Representational Stateless Transfer) - Server Side
- Structural Design: Application(Client) Side

### :bookmark_tabs: Author's Notice
- Ignore the LICENSE file that is with this file

## :ticket: Server Side
- Link: http://api.doctor-appointment-app.ml/

### :link: Url Endpoints of the REST API
- https://api.doctor-appointment-app.ml/ - The root url of the API (Application Programming Interface)
- /v1/ - Version definition of the API

- /v1/patient/{patientId}/appointments - Returns the list of patient's appointments (Pending and Approved status)
- /v1/patient/{patientId}/appointments/{appointmentId} - Returns a specific appointment that the patient himself/herself has requested

- /v1/doctor/{doctorId}/appointments/pending - Returns the list of doctor's pending appointments which are assigned/sent to him/her
- /v1/doctor/{doctorId}/appointments/approved - Returns the list of doctor's approved appointments which he/she has already approved
- /v1/doctor/{doctorId}/appointments/{appointmentId} - Returns a specific appointment from the doctor's list of appointments

### :trident: Request and Response formats used by both Server and Client Application
- JSON Format (Javascript Object Notation) 
