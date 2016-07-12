<?php
mysql_connect("localhost","root","rusifarules");
mysql_select_db("foundapet");

$post = json_decode(file_get_contents("php://input"), true);
$state = $post['state'];

$q=mysql_query("SELECT ID_PET, TYPE, RACE, LATITUDE, LONGITUDE, STATE, DESCRIPTION, ID_USER, FOUND_OR_LOST FROM PETS_DATA WHERE UPPER(STATE) = '" .$state ."' AND STATUS = 0");

while($e=mysql_fetch_assoc($q))
        $output[]=$e;

print(json_encode($output));

mysql_close();
?>