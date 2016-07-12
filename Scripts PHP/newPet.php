<?php
mysql_connect("localhost","root","rusifarules");
mysql_select_db("foundapet");

$post = json_decode(file_get_contents("php://input"), true);
$type = $post['type'];
$race = $post['race'];
$desc = $post['desc'];
$lat = $post['lat'];
$long = $post['long'];
$state = $post['state'];
$img = $post['img'];
$iduser = $post['idusr'];
$fol = $post['lost'];

if($fol == ''){
	$fol = 0;
}
$r = true;
// Se da de alta el animal
$r=mysql_query("INSERT INTO PETS_DATA (TYPE, RACE, LATITUDE, LONGITUDE, IMG, STATE, DESCRIPTION, STATUS, ID_USER, FOUND_OR_LOST) VALUES ('" .$type ."', '" .$race ."', '" .$lat ."', '" .$long ."', '" .$img ."', '" .$state ."', '" .$desc ."', 0, " .$iduser .", " .$fol .")");

if($r == FALSE){
	$arr = array(array('result'=>'KO', 'resultCode'=>1));
}else{
	$arr = array(array('result'=>'OK', 'resultCode'=>0));
}

print(json_encode($arr));
mysql_close();
?>