<?php
mysql_connect("quiquevila.es.mysql","quiquevila_es","hNjPnF8Y");
mysql_select_db("quiquevila_es");

$post = json_decode(file_get_contents("php://input"), true);
$idPet = $post['idPet'];

$q=mysql_query("SELECT ID_COMMENT, COMMENT, USER_NAME, COMMENT_DATE, ID_PET FROM COMMENTS WHERE ID_PET = " .$idPet ." ORDER BY ID_COMMENT");

while($e=mysql_fetch_assoc($q))
        $output[]=$e;

print(json_encode($output));

mysql_close();
?>