<?php
mysql_connect("quiquevila.es.mysql","quiquevila_es","hNjPnF8Y");
mysql_select_db("quiquevila_es");

$post = json_decode(file_get_contents("php://input"), true);
$user = $post['user'];
$pass = $post['pass'];
$fname = $post['fname'];
$lname = $post['lname'];
$email = $post['email'];

$retval = true;
$r = true;

// Se comprueba si existe el usuario
$result=mysql_query("SELECT ID_USER FROM USERS WHERE USERNAME = '" .$user ."'");

if (mysql_num_rows($result)==0){
    // Se da de alta el usuario
	$r=mysql_query("INSERT INTO USERS (USERNAME, PASSWORD, FIRST_NAME, LAST_NAME, EMAIL, ACTIVE) VALUES ('" .$user ."', '" .$pass ."', '" .$fname ."', '" .$lname ."', '" .$email ."', true)");

}else{
	$retval = false;
}

$globalresult = $retval OR $r;

if($globalresult == FALSE){
	if($reval == FALSE){
		// Usuario existente
		$arr = array(array('result'=>'KO', 'resultCode'=>1));
		error_log("foundapet -> Ya existe el usuario " .$user);
	}else{
		$arr = array(array('result'=>'KO', 'resultCode'=>2));
	}
}else{
	$arr = array(array('result'=>'OK', 'resultCode'=>0));
}

print(json_encode($arr));

 
mysql_close();
?>
