<?php
mysql_connect("quiquevila.es.mysql","quiquevila_es","hNjPnF8Y");
mysql_select_db("quiquevila_es");

$post = json_decode(file_get_contents("php://input"), true);
$user = $post['user'];
$pass = $post['pass'];


$q=mysql_query("SELECT COUNT(*) AS COUNT, ID_USER, USERNAME FROM USERS WHERE USERNAME = '" .$user ."' AND PASSWORD = '" .$pass ."' AND ACTIVE = TRUE");

while($e=mysql_fetch_assoc($q))
        $output[]=$e;
 
print(json_encode($output));
 
mysql_close();
?>