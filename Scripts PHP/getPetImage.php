<?php
mysql_connect("localhost","root","rusifarules");
mysql_select_db("foundapet");

$post = json_decode(file_get_contents("php://input"), true);
$id = $post['id'];

$q=mysql_query("SELECT IMG FROM PETS_DATA WHERE  ID_PET = " .$id);

while($e=mysql_fetch_assoc($q))
        $output[]=$e;

print(json_encode($output));

mysql_close();
?>