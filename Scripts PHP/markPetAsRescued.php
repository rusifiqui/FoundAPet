<?php
mysql_connect("localhost","root","rusifarules");
mysql_select_db("foundapet");

$post = json_decode(file_get_contents("php://input"), true);
$id = $post['id'];

$q=mysql_query("UPDATE PETS_DATA SET STATUS = 1 WHERE ID_PET = " .$id);

if($q == FALSE){
	$arr = array(array('result'=>'KO', 'resultCode'=>1));
}else{
	$arr = array(array('result'=>'OK', 'resultCode'=>0));
}

print(json_encode($arr));
mysql_close();
?>