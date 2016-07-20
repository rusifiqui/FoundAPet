<?php
mysql_connect("quiquevila.es.mysql","quiquevila_es","hNjPnF8Y");
mysql_select_db("quiquevila_es");

$q=mysql_query("SELECT MESSAGE FROM MESSAGES WHERE ACTIVE = 1");

while($e=mysql_fetch_assoc($q))
        $output[]=$e;

print(json_encode($output));

mysql_close();
?>