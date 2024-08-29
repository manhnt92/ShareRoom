<?php
	class DbHandler {

		private $conn;

		function __construct() {
			require_once dirname(__FILE__) . '/DbConnect.php';
			$db = new DbConnect();
			$this->conn = $db->connect();
		}


		/************************************** FUNCTIONS LIÊN QUAN ĐẾN USER **********************************************/

		public function createUserNormal($email, $password, $first_name, $last_name) {
			require_once 'PassHash.php';
			if (!$this->isUserExists($email)) {
				$password_hash = PassHash::hash($password);
				$api_key = $this->generateApiKey();
				$stmt = $this->conn->prepare("INSERT INTO users (email, password, first_name, last_name, api_key,account_type) values (?, ?, ?, ?, ?, 0)");
				$stmt->bind_param("sssss", $email, $password_hash, $first_name, $last_name, $api_key);
				$result = $stmt->execute();
				$stmt->close();
				if ($result) {
					return 0;
				}
				return 1;
			}
			return 2;
		}

		public function createUserFacebook($facebook_id, $access_token, $email, $first_name, $last_name, $gender, $birthday, $avatar, $account_type){
			$api_key = $this->generateApiKey();
			$stmt = $this->conn->prepare("INSERT INTO users (facebook_id, facebook_access_token, email, first_name, last_name, gender, birthday, avatar, api_key, account_type) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			$stmt->bind_param("sssssssssi", $facebook_id, $access_token, $email, $first_name, $last_name, $gender, $birthday, $avatar, $api_key, $account_type);
			$result = $stmt->execute();
			$stmt->close();
			if($result){
				return 0;
			}
			return 1;
		}
		
		public function updateAccount($account_type, $id, $password, $first_name, $last_name, $gender, $birthday, $age, $address, $occupation, $description, $phonenumber, $avatar){
			require_once 'PassHash.php';
			if($account_type == 0){
				$password_hash = PassHash::hash($password);
			}else {
				$password_hash = $password;
			}
			$stmt = $this->conn->prepare("UPDATE users SET first_name = ?, last_name = ?, gender = ?, birthday = ?, age = ?, address = ?, occupation = ?, description = ?, phonenumber = ?, avatar = ?, password = ? WHERE id = ?");
			$stmt->bind_param("ssssissssssi", $first_name, $last_name, $gender, $birthday, $age, $address, $occupation, $description, $phonenumber, $avatar, $password_hash, $id);
			$stmt->execute();
	        $num_affected_rows = $stmt->affected_rows;
	        $stmt->close();
	        return $num_affected_rows > 0;
		}

		public function checkLogin($email, $password) {
			$stmt = $this->conn->prepare("SELECT id, password FROM users WHERE email = ?");
			$stmt->bind_param("s", $email);
			$stmt->execute();
			$stmt->bind_result($id, $password_hash);
			$stmt->store_result();

			if ($stmt->num_rows > 0) {
				$stmt->fetch();
				$stmt->close();
				if (PassHash::check_password($password_hash, $password)) {
					return $id;
				} else {
					return NULL;
				}
			} else {
				$stmt->close();
				return NULL;
			}
		}
		
		public function checkLoginFacebook($facebook_id){
			$stmt = $this->conn->prepare("SELECT id FROM users WHERE facebook_id = ?");
			$stmt->bind_param("s", $facebook_id);
			$stmt->execute();
			$stmt->bind_result($id);
			$stmt->store_result();

			if($stmt->num_rows > 0){
				$stmt->fetch();
				$stmt->close();
				return $id;
			} else {
				$stmt->close();
				return NULL;
			}
		}

		public function updateAccessToken($access_token, $facebook_id, $id){
			$stmt = $this->conn->prepare("UPDATE users set facebook_access_token = ? WHERE id = ? AND facebook_id = ?");
	        $stmt->bind_param("sis", $access_token, $id, $facebook_id);
	        $stmt->execute();
	        $num_affected_rows = $stmt->affected_rows;
	        $stmt->close();
	        return $num_affected_rows > 0;
		}

		public function getUserDataByID($id) {
			$stmt = $this->conn->prepare("SELECT id, email, password, api_key, facebook_id, facebook_access_token, first_name, last_name, gender, birthday, age, address, occupation, description, phonenumber, avatar, account_type FROM users WHERE id = ?");
			$stmt->bind_param("i", $id);
			if ($stmt->execute()) {

				$stmt->bind_result($id, $email, $password, $api_key, $facebook_id, $facebook_access_token, $first_name, $last_name,
					$gender, $birthday, $age, $address, $occupation, $description, $phonenumber, $avatar, $account_type);
				$stmt->fetch();
				$user = array();
				$user["id"] = $id;
				$user["email"] = $email;
				$user["password"] = $password;
				$user["api_key"] = $api_key;
				$user["facebook_id"] = $facebook_id;
				$user["facebook_access_token"] = $facebook_access_token;
				$user["first_name"] = $first_name;
				$user["last_name"] = $last_name;
				$user["gender"] = $gender;
				$user["birthday"] = $birthday;
				$user["age"] = $age;
				$user["address"] = $address;
				$user["occupation"] = $occupation;
				$user["description"] = $description;
				$user["phonenumber"] = $phonenumber;
				$user["avatar"] = $avatar;
				$user["account_type"] = $account_type;
				$stmt->close();
				return $user;
			} else {
				return NULL;
			}
		}

		public function getUserDataByRoomID($room_id){
			$stmt = $this->conn->prepare("SELECT u.id, email, avatar, first_name, last_name, age, gender, birthday, phonenumber, occupation, address, description FROM users AS u, rooms AS r WHERE 
				u.id = r.user_id AND r.id = ?");
			$stmt->bind_param("i", $room_id);
			if ($stmt->execute()) {
				$stmt->bind_result($id, $email, $avatar, $first_name, $last_name, $age, $gender, $birthday, $phonenumber, $occupation, $address, $description);
				$stmt->fetch();
				$user = array();
				if($email != NULL && $first_name != NULL && $last_name != NULL){
					$user["id"] = $id;
					$user["email"] = $email;
					$user["avatar"] = $avatar;
					$user["first_name"] = $first_name;
					$user["last_name"] = $last_name;
					$user["age"] = $age;
					$user["gender"] = $gender;
					$user["birthday"] = $birthday;
					$user["phonenumber"] = $phonenumber;
					$user["occupation"] = $occupation;
					$user["address"] = $address;
					$user["description"] = $description;
					$stmt->close();
					return $user;
				} else {
					return NULL;
				}
			} else {
				return NULL;
			}
		}

		private function isUserExists($email) {
			$stmt = $this->conn->prepare("SELECT id from users WHERE email = ?");
			$stmt->bind_param("s", $email);
			$stmt->execute();
			$stmt->store_result();
			$num_rows = $stmt->num_rows;
			$stmt->close();
			return $num_rows > 0;
		}
		
		private function generateApiKey() {
			return md5(uniqid(rand(), true));
		}

		public function isValidApiKey($api_key) {
	        $stmt = $this->conn->prepare("SELECT id from users WHERE api_key = ?");
	        $stmt->bind_param("s", $api_key);
	        $stmt->execute();
	        $stmt->store_result();
	        $num_rows = $stmt->num_rows;
	        $stmt->close();
	        return $num_rows > 0;
    	}

	    public function getUserId($api_key) {
	        $stmt = $this->conn->prepare("SELECT id FROM users WHERE api_key = ?");
	        $stmt->bind_param("s", $api_key);
	        if ($stmt->execute()) {
	            $stmt->bind_result($user_id);
	            $stmt->fetch();
	            // TODO
	            // $user_id = $stmt->get_result()->fetch_assoc();
	            $stmt->close();
	            return $user_id;
	        } else {
	            return NULL;
	        }
    	}

		/************************************** END FUNCTIONS LIÊN QUAN ĐẾN USER ******************************************/
		
		/************************************** FUNCTIONS LIÊN QUAN ĐẾN ROOM **********************************************/
		
		public function createRoom($user_id, $room_address, $room_properties, $room_images, $room_amenities) {
			$stmt = $this->conn->prepare("INSERT INTO rooms (user_id) values (?)");
			$stmt->bind_param("i", $user_id);
			$result = $stmt->execute();
			$stmt->close();
			if($result){
				$room_id = $this->conn->insert_id;
				//insert room address
				$province_id = $room_address['province_id'];
				$district_id = $room_address['district_id'];
				$ward_id = $room_address['ward_id'];
				$address = $room_address['address'];
				$latitude = $room_address['latitude'];
				$longitude = $room_address['longitude'];
				$stmt = $this->conn->prepare("INSERT INTO room_address (room_id, province_id, district_id, ward_id, address, latitude, longitude) values (?, ?, ?, ?, ?, ?, ?)");
				$stmt->bind_param("issssdd", $room_id, $province_id, $district_id, $ward_id, $address, $latitude, $longitude);
				$result = $stmt->execute();
				$stmt->close();
				if(!$result){
					rollBackRoom($room_id);
					return 0;
				}
				//insert room properties
				$rent_per_month = $room_properties['rent_per_month'];
				$electric = $room_properties['electric'];
				$water = $room_properties['water'];
				$area = $room_properties['area'];
				$number_per_room = $room_properties['number_per_room'];
				$min_stay = $room_properties['min_stay'];
				$available_from = $room_properties['available_from'];
				$room_type = $room_properties['room_type'];
				$room_state = $room_properties['room_state'];
				$stmt = $this->conn->prepare("INSERT INTO room_properties (room_id, rent_per_month, electric, water, area, number_per_room, min_stay, available_from, room_type, room_state) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
				$stmt->bind_param("iddiiiisss", $room_id, $rent_per_month, $electric, $water, $area, $number_per_room, $min_stay, $available_from, $room_type, $room_state);
				$result = $stmt->execute();
				$stmt->close();
				if(!$result){
					$this->rollBackRoom($room_id);
					return 0;
				}
				//insert room images
				if(count($room_images) > 0){
					for($i = 0; $i < count($room_images); $i++){
						$link = $room_images[$i]['link'];
						$note = $room_images[$i]['note'];
						$stmt = $this->conn->prepare("INSERT INTO room_images (room_id, link, note) values (?, ?, ?)");
						$stmt->bind_param("iss", $room_id, $link, $note);
						$result = $stmt->execute();
						$stmt->close();
						if(!$result){
							$this->rollBackRoom($room_id);
							return 0;
						}
					}
				}
				//insert room amenities
				$amenities = json_encode($room_amenities);
				$stmt = $this->conn->prepare("INSERT INTO room_amenities (room_id, amenities) values (?, ?)");
				$stmt->bind_param("is", $room_id, $amenities);
				$result = $stmt->execute();
				$stmt->close();
				if(!$result){
					$this->rollBackRoom($room_id);
					return 0;
				}
				return $room_id;
			} else {
				return 0;
			}
		}

		public function rollBackRoom($room_id){
			$stmt = $this->conn->prepare("DELETE r, ra, ram, ri, rp FROM rooms r INNER JOIN room_address ra ON r.id = ra.room_id 
				INNER JOIN room_amenities ram ON r.id = ram.room_id 
				INNER JOIN room_images ri ON r.id = ri.room_id
				INNER JOIN room_properties rp ON r.id = rp.room_id
				WHERE r.id= ?");
			$stmt->bind_param("i", $room_id);
			$stmt->execute();
			$stmt->close();
		}

		public function getMyRooms($user_id){
			$stmt = $this->conn->prepare("SELECT id FROM rooms WHERE user_id = ?");
			$stmt->bind_param("i", $user_id);
			$stmt->execute();
			$result = $stmt->get_result();
			$stmt->close();
			$room = array();
			if ($result->num_rows > 0) {
				while($row = $result->fetch_assoc()) {
					$room_id = $row['id'];
					$r = array();
					$r["id"] = $room_id;
					$r["room_address"] = $this->getRoomAddress($room_id);
			    	$r["room_properties"] = $this->getRoomProperties($room_id);
					$r["room_amenities"] = $this->getRoomAmenities($room_id);
					$r["room_images"] = $this->getRoomImages($room_id);
					array_push($room, $r);
			    }
			    return $room;
			}
			return $room;
		}

		public function deleteRoom($room_id){
			$stmt = $this->conn->prepare("DELETE FROM room_images WHERE room_id = ?");
			$stmt->bind_param("i", $room_id);
			$stmt->execute();
			$stmt = $this->conn->prepare("DELETE FROM rooms_favorite WHERE room_id = ?");
			$stmt->bind_param("i", $room_id);
			$stmt->execute();
			$stmt = $this->conn->prepare("DELETE r, ra, rp, ram FROM rooms r INNER JOIN room_address ra ON r.id = ra.room_id 
				INNER JOIN room_properties rp ON r.id = rp.room_id 
				INNER JOIN room_amenities ram ON r.id = ram.room_id WHERE r.id = ?");
			$stmt->bind_param("i", $room_id);
			$rs = $stmt->execute();
			$stmt->close();
			return $rs;
		}
		
		/*ẢNH PHÒNG TRỌ*/

		public function deleteRoomImages($delete){
			$image_id = "";
			for($i = 0; $i < count($delete); $i++){
				$id = $delete[$i]['id'];
				if($i < count($delete) - 1){
					$image_id = $image_id.$id.",";
				} else {
					$image_id = $image_id.$id;
				}
			}
			$stmt = $this->conn->prepare("DELETE FROM room_images WHERE id IN (".$image_id.")");
			$stmt->execute();
			$num_affected_rows = $stmt->affected_rows;
			$stmt->close();
			// return ($num_affected_rows == count($delete));
		}

		public function updateRoomImages($update){
			for($i = 0; $i < count($update); $i ++){
				$id = $update[$i]['id'];
				$note = $update[$i]['note'];
				$stmt = $this->conn->prepare("UPDATE room_images SET note = ? WHERE id = ?");
				$stmt->bind_param("si", $note, $id);
				$stmt->execute();
				$stmt->close();	
			}
		}

		public function insertRoomImages($insert, $room_id){
			$query_insert = "";
			for($i = 0; $i < count($insert); $i ++){
				$link = $insert[$i]['link'];
				$note = $insert[$i]['note'];
				if($i < count($insert) - 1){
					$query_insert = $query_insert."('".$link."','".$note."',".$room_id."),";
				}else {
					$query_insert = $query_insert."('".$link."','".$note."',".$room_id.")";
				}
			}
			$stmt = $this->conn->prepare("INSERT INTO room_images (link, note, room_id) VALUES ".$query_insert);
			$stmt->execute();
			// $num_affected_rows = $stmt->affected_rows;
			$stmt->close();
			// return (count($insert) == $num_affected_rows);
		}

		public function getRoomImages($room_id){
			$stmt = $this->conn->prepare("SELECT id, link, note from room_images WHERE room_id = ?");
			$stmt->bind_param("i", $room_id);
			$stmt->execute();
			$result = $stmt->get_result();
			$stmt->close();
			$room_images = array();
			if ($result->num_rows > 0) {
				while($images = $result->fetch_assoc()) {
					$id = $images['id'];
					$link = $images['link'];
					$note = $images['note'];
					$img = array('id' => $id, 'link' => $link, 'note' => $note);
					array_push($room_images, $img);
				}
			}
			return $room_images;
		}
		
		/*THÔNG TIN CHUNG PHÒNG TRỌ*/
		
		public function updateRoomProperties($room_id, $room_properties){
			$rent_per_month = $room_properties['rent_per_month'];
			$electric = $room_properties['electric'];
			$water = $room_properties['water'];
			$area = $room_properties['area'];
			$number_per_room = $room_properties['number_per_room'];
			$min_stay = $room_properties['min_stay'];
			$available_from = $room_properties['available_from'];
			$room_type = $room_properties['room_type'];
			$room_state = $room_properties['room_state'];

			$stmt = $this->conn->prepare("UPDATE room_properties SET rent_per_month = ? , electric = ?, water = ?, area = ?, number_per_room = ?, min_stay = ?, available_from = ?, room_type = ?, room_state = ? WHERE room_id = ?");
			$stmt->bind_param("ddiiiisssi", $rent_per_month, $electric, $water, $area, $number_per_room, $min_stay, $available_from, $room_type, $room_state, $room_id);
			$result = $stmt->execute();
			$stmt->close();
		}

		public function getRoomProperties($room_id){
			$stmt = $this->conn->prepare("SELECT rent_per_month, electric, water, area, number_per_room, min_stay, available_from, room_type, room_state FROM room_properties WHERE room_id = ?");
			$stmt->bind_param("i", $room_id);
			$stmt->execute();
			$stmt->bind_result($rent_per_month, $electric, $water, $area, $number_per_room, $min_stay, $available_from, $room_type, $room_state);
			$stmt->fetch();
			$room_properties = array();
			$room_properties["room_id"] = $room_id;
			$room_properties["rent_per_month"] = $rent_per_month;
			$room_properties["electric"] = $electric;
			$room_properties["water"] = $water;
			$room_properties["area"] = $area;
			$room_properties["number_per_room"] = $number_per_room;
			$room_properties["min_stay"] = $min_stay;
			$room_properties["available_from"] = $available_from;
			$room_properties["room_type"] = $room_type;
			$room_properties["room_state"] = $room_state;
			$stmt->close();
			return $room_properties;
		}
		
		/*TIỆN ÍCH PHÒNG TRỌ*/

		public function updateRoomAmenities($room_id, $room_amenities){
			$amenities = json_encode($room_amenities);
			$stmt = $this->conn->prepare("UPDATE room_amenities SET amenities = ? WHERE room_id = ?");
			$stmt->bind_param("si", $amenities, $room_id);
			$result = $stmt->execute();
			$stmt->close();
		}

		public function getRoomAmenities($room_id){
			$stmt = $this->conn->prepare("SELECT amenities FROM room_amenities WHERE room_id = ?");
			$stmt->bind_param("i", $room_id);
			$stmt->execute();
			$stmt->bind_result($amenities);
			$stmt->fetch();
			$stmt->close();
			return json_decode($amenities,true);
		}
		
		/*ĐỊA CHỈ PHÒNG TRỌ*/

		public function updateRoomAddress($room_id, $room_address){
			$province_id = $room_address['province_id'];
			$district_id = $room_address['district_id'];
			$ward_id = $room_address['ward_id'];
			$address = $room_address['address'];
			$latitude = $room_address['latitude'];
			$longitude = $room_address['longitude'];

			$stmt = $this->conn->prepare("UPDATE room_address SET province_id = ? , district_id = ?, ward_id = ?, address = ?, latitude = ?, longitude = ? WHERE room_id = ?");
			$stmt->bind_param("ssssddi", $province_id, $district_id, $ward_id, $address, $latitude, $longitude, $room_id);
			$result = $stmt->execute();
			$stmt->close();
		}

		public function getRoomAddress($room_id){
			$stmt = $this->conn->prepare("SELECT province_id, district_id, ward_id, address, latitude, longitude FROM room_address WHERE room_id = ?");
			$stmt->bind_param("i", $room_id);
			$stmt->execute();
			$stmt->bind_result($province_id, $district_id, $ward_id, $address, $latitude, $longitude);
			$stmt->fetch();
			$room_address = array();
			$room_address["province_id"] = $province_id;
			$room_address["district_id"] = $district_id;
			$room_address["ward_id"] = $ward_id;
			$room_address["address"] = $address;
			$room_address["latitude"] = $latitude;
			$room_address["longitude"] = $longitude;
			$stmt->close();
			return $room_address;
		}

		/*TÌM KIẾM PHÒNG TRỌ*/

		public function searchByAddress($query){
			$stmt = $this->conn->prepare("SELECT room_id FROM room_address where MATCH(address) AGAINST ('" . $query . "' IN BOOLEAN MODE)");
			$stmt->execute();
			$result = $stmt->get_result();
			$stmt->close();
			$room = array();
			if ($result->num_rows > 0) {
				while($row = $result->fetch_assoc()) {
					$room_id = $row['room_id'];
					$r = array();
					$user_id = $this->getUserIdByRoomID($room_id);
					$r["user_id"] = $user_id;
					$r["id"] = $room_id;
					$r["room_address"] = $this->getRoomAddress($room_id);
			    	$r["room_properties"] = $this->getRoomProperties($room_id);
					$r["room_amenities"] = $this->getRoomAmenities($room_id);
					$r["room_images"] = $this->getRoomImages($room_id);
					array_push($room, $r);
			    }
			    return $room;
			}
			return $room;
		}

		public function searchByRadius($latitude, $longitude, $radius){
			//3959 : dặm, 6371 : kilomet ; 1 dặm = 1.609344 Km
			$stmt = $this->conn->prepare("SELECT room_id, (6371 * acos(cos(radians(?)) * cos(radians(latitude)) * cos(radians(longitude) - radians(?)) + sin( radians(?)) * sin(radians(latitude)))) AS distance 
				FROM room_address 
				HAVING distance < ?");
			$stmt->bind_param('dddd', $latitude, $longitude, $latitude, $radius);
			$stmt->execute();
			$result = $stmt->get_result();
			$stmt->close();
			$room = array();
			if ($result->num_rows > 0) {
				while($row = $result->fetch_assoc()) {
					$room_id = $row['room_id'];
					$r = array();
					$user_id = $this->getUserIdByRoomID($room_id);
					$r["user_id"] = $user_id;
					$r["id"] = $room_id;
					$r["room_address"] = $this->getRoomAddress($room_id);
			    	$r["room_properties"] = $this->getRoomProperties($room_id);
					$r["room_amenities"] = $this->getRoomAmenities($room_id);
					$r["room_images"] = $this->getRoomImages($room_id);
					array_push($room, $r);
			    }
			    return $room;
			}
			return $room;
		}

		public function searchByRectangle($sw_lat, $sw_lng, $ne_lat, $ne_lng) {
			$stmt = $this->conn->prepare("SELECT room_id FROM room_address WHERE latitude >= ? && latitude <= ? && longitude >= ? && longitude <= ?");
			$stmt->bind_param('dddd', $sw_lat, $ne_lat, $sw_lng, $ne_lng);
			$stmt->execute();
			$result = $stmt->get_result();
			$stmt->close();
			$room = array();
			if ($result->num_rows > 0) {
				while($row = $result->fetch_assoc()) {
					$room_id = $row['room_id'];
					$r = array();
					$user_id = $this->getUserIdByRoomID($room_id);
					$r["user_id"] = $user_id;
					$r["id"] = $room_id;
					$r["room_address"] = $this->getRoomAddress($room_id);
			    	$r["room_properties"] = $this->getRoomProperties($room_id);
					$r["room_amenities"] = $this->getRoomAmenities($room_id);
					$r["room_images"] = $this->getRoomImages($room_id);
					array_push($room, $r);
			    }
			    return $room;
			}
			return $room;
		}

		private function getUserIdByRoomID($room_id){
			$stmt = $this->conn->prepare("SELECT user_id FROM rooms WHERE id = ?");
			$stmt->bind_param('i', $room_id);
			if($stmt->execute()){
				$stmt->bind_result($user_id);
	            $stmt->fetch();
	            $stmt->close();
	            return $user_id;
			}
			return NULL;
		}

		/*FAVORITE ROOM*/
		public function isFavoriteRoomExists($room_id, $user_id){
			$stmt = $this->conn->prepare("SELECT * FROM rooms_favorite WHERE user_id = ? AND room_id = ?");
			$stmt->bind_param("ii", $user_id, $room_id);
			$stmt->execute();
			$result = $stmt->get_result();
			if ($result->num_rows > 0) {
				return 1;
			} else {
				return 0;
			}
		}

		public function getMyFavoriteRooms($user_id){
			$stmt = $this->conn->prepare("SELECT r.* FROM rooms_favorite AS rf INNER JOIN rooms AS r ON r.id = rf.room_id WHERE rf.user_id = ?");
			$stmt->bind_param('i', $user_id);
			$stmt->execute();
			$result = $stmt->get_result();
			$stmt->close();
			$room = array();
			if ($result->num_rows > 0) {
				while($row = $result->fetch_assoc()) {
					$room_id = $row['id'];
					$r = array();
					$r["id"] = $room_id;
					$r["room_address"] = $this->getRoomAddress($room_id);
			    	$r["room_properties"] = $this->getRoomProperties($room_id);
					$r["room_amenities"] = $this->getRoomAmenities($room_id);
					$r["room_images"] = $this->getRoomImages($room_id);
					array_push($room, $r);
			    }
			    return $room;
			}
			return $room;
		}

		public function createFavoriteRoom($room_id, $user_id){
			$stmt = $this->conn->prepare("INSERT INTO rooms_favorite (user_id, room_id) VALUES (?, ?)");
			$stmt->bind_param('ii', $user_id, $room_id);
			$result = $stmt->execute();
			$stmt->close();
			if($result){
				return $this->conn->insert_id;
			} else {
				return 0;
			}
		}

		public function deleteFavoriteRoom($room_id, $user_id){
			$stmt = $this->conn->prepare("DELETE FROM rooms_favorite WHERE user_id = ? AND room_id = ?");
			$stmt->bind_param('ii', $user_id, $room_id);
			$stmt->execute();
			$stmt->close();
		}


		/************************************** END FUNCTIONS LIÊN QUAN ĐẾN ROOM ******************************************/

		/************************************** FUNCTIONS LIÊN QUAN ĐẾN COMMENTS ******************************************/

		/*ĐĂNG COMMENT PHÒNG TRỌ*/

		public function createComment($user_id, $room_id, $comment){
			$stmt = $this->conn->prepare("INSERT INTO comments (user_id, room_id, comment) values (?, ?, ?)");
			$stmt->bind_param("iis", $user_id, $room_id, $comment);
			$result = $stmt->execute();
			$comment_id = $stmt->insert_id;
			$stmt->close();
			if($result){
				return $comment_id;
			} else {
				return 0;
			}
		}

		/** SỬA COMMENT PHÒNG TRỌ*/

		public function editComment($user_id, $comment_id, $new_comment){
			$stmt = $this->conn->prepare("UPDATE comments SET comment = ? WHERE user_id = ? AND id = ?");
			$stmt->bind_param("sii", $new_comment, $user_id, $comment_id);
			$stmt->execute();
			$stmt->close();
		}

		/** XOÁ COMMENT PHÒNG TRỌ */

		public function deleteComment($comment_id){
			$stmt = $this->conn->prepare("DELETE FROM comments WHERE id = ?");
			$stmt->bind_param("i", $comment_id);
			$stmt->execute();
			$stmt->close();	
		}

		/*LẤY RA TOÀN BỘ COMMENT CỦA BÀI ĐĂNG*/

		public function getCommentsDataByRoomID($room_id, $page){
			$page_size = 20;
			/** get total rows */
			$r = mysqli_query($this->conn, "SELECT count(*) as total FROM comments WHERE room_id=".$room_id);
			$row = mysqli_fetch_assoc($r);
			$total_rows = $row['total'];
			$total_page = ceil($total_rows / $page_size);

			if($page > $total_page || $page == 0){
				return;
			}

			if($total_rows <= $page_size){
				$start_rows = 0;
				$num = $total_rows;
			} else if ($total_rows > $page_size){
				$start_rows = $total_rows - $page * $page_size;
				if($start_rows < 0){
					$num = $page_size + $start_rows;
					$start_rows = 0;
				} else {
					$num = $page_size;
				}
			}

			$stmt = $this->conn->prepare("SELECT u.first_name AS first_name, u.last_name AS last_name, u.avatar AS avatar, 
				c.id AS comment_id, u.id AS user_id, c.comment AS comment, c.created AS created FROM comments AS c, users AS u WHERE u.id = c.user_id AND c.room_id = ? ORDER BY c.id ASC LIMIT ?,?");
			$stmt->bind_param("iii", $room_id, $start_rows, $num);
			$stmt->execute();
			$result = $stmt->get_result();
			$stmt->close();
			$rs = array();
			if($result->num_rows > 0){
				while($row = $result->fetch_assoc()) {
					$r = array();
					$r["comment_id"] = $row["comment_id"];		$r["user_id"] = $row["user_id"];
					$r["first_name"] = $row["first_name"];		$r["last_name"] = $row["last_name"];
					$r["avatar"] = $row["avatar"];				$r["comment"] = $row["comment"];
					$r["created"] = $row["created"];
					array_push($rs, $r);
				}
			}
			$return["content"] = $rs;
			$return["current_page"] = $page;
			$return["total_page"] = $total_page;
			return $return;
		}

		/*BÌNH CHỌN BÀI ĐĂNG*/

		public function createRating($user_id, $room_id, $rating){
			$stmt = $this->conn->prepare("INSERT INTO ratings (user_id, room_id, rating) values (?, ?, ?)");
			$stmt->bind_param("iii", $user_id, $room_id, $rating);
			$result = $stmt->execute();
			$rating_id = $stmt->insert_id;
			$stmt->close();
			if($result){
				return $rating_id;
			} else {
				return 0;
			}
		}

		public function updateRating($user_id, $room_id, $rating){
			$stmt = $this->conn->prepare("UPDATE ratings SET rating = ? WHERE user_id = ? AND room_id = ?");
			$stmt->bind_param("iii", $rating, $user_id, $room_id);
			$result = $stmt->execute();
			$stmt->close();
			if($result){
				return 1;
			} else {
				return 0;
			}	
		}

		public function isExistsRating($user_id, $room_id){
			$stmt = $this->conn->prepare("SELECT * FROM ratings WHERE user_id = ? AND room_id = ?");
			$stmt->bind_param("ii", $user_id, $room_id);
			$stmt->execute();
			$result = $stmt->get_result();
			if ($result->num_rows > 0) {
				return 1;
			} else {
				return 0;
			}
		}

		public function getUserRating($room_id, $user_id){
			$stmt = $this->conn->prepare("SELECT id, rating FROM ratings WHERE room_id = ? AND user_id = ?");
			$stmt->bind_param("ii", $room_id, $user_id);
			if ($stmt->execute()) {
				$stmt->bind_result($id, $rating);
				$stmt->fetch();
				$rs = array();
				$rs["id"] = $id;
				$rs["user_id"] = $user_id;
				$rs["rating"] = $rating;
				$stmt->close();
				return $rs;
			} else {
				return NULL;
			}
		}

	}

?>
