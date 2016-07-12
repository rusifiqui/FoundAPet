<?php
mysql_connect("localhost","root","rusifarules");
mysql_select_db("foundapet");

$q=mysql_query("SELECT USERNAME FROM USERS");

while($e=mysql_fetch_assoc($q))
        $output[]=$e;

print(json_encode($output));

mysql_close();
?>