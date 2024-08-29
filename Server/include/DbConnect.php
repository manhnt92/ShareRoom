<?php
	class DbConnect {
		private $conn;
		
		function __construct() {        
		}
		
		function connect() {
			include_once dirname(__FILE__) . '/Config.php';
			$this->conn = new mysqli(DB_HOST, DB_USERNAME, DB_PASSWORD, DB_NAME);
			$this->conn->set_charset("utf8");
			if (mysqli_connect_errno()) {
				echo "Lỗi Kết Nối Tới MySQL: " . mysqli_connect_error();
			}
			return $this->conn;
		}
	}
?>
