<?php
mysql_connect("localhost","root","rusifarules");
mysql_select_db("foundapet");

$post = json_decode(file_get_contents("php://input"), true);
$idUser = $post['idUser'];

$r = true;
$r=mysql_query("UPDATE USERS SET ACTIVE = FALSE WHERE ID_USER = " .$idUser);

if($r == FALSE){
	$arr = array(array('result'=>'KO', 'resultCode'=>1));
}else{
	$arr = array(array('result'=>'OK', 'resultCode'=>0));
}

print(json_encode($arr));
mysql_close();
?>