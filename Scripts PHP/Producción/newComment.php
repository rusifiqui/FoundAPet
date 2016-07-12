<?php
mysql_connect("quiquevila.es.mysql","quiquevila_es","hNjPnF8Y");
mysql_select_db("quiquevila_es");

$post = json_decode(file_get_contents("php://input"), true);
$idPet = $post['idPet'];
$comment = $post['comment'];
$idUser = $post['idUser'];
$userName = $post['userName'];
$date = date('Y-m-d H:i:s');

$q=mysql_query("INSERT INTO COMMENTS (ID_PET, COMMENT, ID_USER, USER_NAME, COMMENT_DATE) VALUES (" .$idPet .", '" .$comment ."', " .$idUser .", '" .$userName ."', '" .$date ."' )");

if($q == FALSE){
	$arr = array(array('result'=>'KO', 'resultCode'=>1));
}else{
	$arr = array(array('result'=>'OK', 'resultCode'=>0));
}

print(json_encode($arr));
mysql_close();
?>