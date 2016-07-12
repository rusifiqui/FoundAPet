<?php
mysql_connect("localhost","root","rusifarules");
mysql_select_db("foundapet");

$post = json_decode(file_get_contents("php://input"), true);
$state = $post['state'];

$q=mysql_query("SELECT USERNAME, LATITUDE, LONGITUDE FROM USERS WHERE UPPER(STATE) = '" .$state ."' AND ACTIVE = TRUE AND PATROL_STATE = TRUE");

while($e=mysql_fetch_assoc($q))
        $output[]=$e;

print(json_encode($output));

mysql_close();
?>