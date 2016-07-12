<?php
mysql_connect("quiquevila.es.mysql","quiquevila_es","hNjPnF8Y");
mysql_select_db("quiquevila_es");

$post = json_decode(file_get_contents("php://input"), true);
$state = $post['state'];

$q=mysql_query("SELECT USERNAME, LATITUDE, LONGITUDE FROM USERS WHERE UPPER(STATE) = '" .$state ."' AND ACTIVE = TRUE AND PATROL_STATE = TRUE");

while($e=mysql_fetch_assoc($q))
        $output[]=$e;

print(json_encode($output));

mysql_close();
?>