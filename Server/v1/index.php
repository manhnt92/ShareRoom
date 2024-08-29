<?php
	require_once '../include/DbHandler.php';
	require_once '../include/PassHash.php';
	require '.././libs/Slim/Slim.php';
	\Slim\Slim::registerAutoloader();
	$app = new \Slim\Slim();

	/************************************** FUNCTIONS LIÊN QUAN ĐẾN USER **********************************************/

	/*ĐĂNG KÍ TÀI KHOẢN BÌNH THƯỜNG*/
	$app->post('/account', function() use ($app) {
		$body = $app->request->getBody();
		$json = json_decode($body, true);
		$db = new DbHandler();
		$response = array();
		$response["success"] = false;
		$response["message"] = "Có Lỗi Xảy Ra. Xin Vui Lòng Thử Lại";
		if(isset($json['email']) && isset($json['password']) && isset($json['first_name']) && isset($json['last_name'])){
			$email = $json['email']; $password = $json['password'];
			$first_name = $json['first_name']; $last_name = $json['last_name'];
			validateEmail("Đăng Kí Tài Khoản Thất Bại. ", $email);
			$res = $db->createUserNormal($email, $password, $first_name, $last_name);
			if ($res == 0) {
				$response["success"] = true;
				$response["message"] = "Đăng Kí Tài Khoản Thành Công";
			} else if ($res == 2) {
				$response["message"] = "Đăng Kí Tài Khoản Thất Bại. Email Đã Tồn Tại";
			}
			echoResponse(200, $response);
			$app->stop();
		}
		$response["message"] = "Đăng Kí Tài Khoản Thất Bại. Yêu Cầu Không Hợp Lệ";
		echoResponse(200, $response);
		$app->stop();
	});
	
	/*ĐĂNG NHẬP*/
	$app->post('/account/login',function() use ($app) {
		$body = $app->request->getBody();
		$json = json_decode($body, true);
		$db = new DbHandler();
		$response = array();
		$response["success"] = false;
        $response["message"] = "Có Lỗi Xảy Ra. Xin Vui Lòng Thử Lại";
		if(isset($json['account_type'])) {
			$account_type = $json['account_type'];
			if($account_type == 0) {
				if(isset($json['email']) && $json['password']){
					$email = $json['email']; $password = $json['password'];
					validateEmail("Đăng Nhập Thất Bại. ",$email);
					$id = $db->checkLogin($email, $password);
					if ($id != NULL) {
		                $user = $db->getUserDataByID($id);
		                if ($user != NULL) {
		                	$user["password"] = $json['password'];
		                    $response["success"] = true;
		                    $response["message"] = "Đăng Nhập Thành Công";
		                    $response["content"] = $user;
		                }
			        } else {
			            $response["message"] = 'Đăng Nhập Thất Bại. Thông Tin Không Chính Xác';
			        }
		        	echoResponse(200, $response);
		        	$app->stop();
		        }
			} else {
				if(isset($json['facebook_id']) && isset($json['facebook_access_token']) && isset($json['email']) && isset($json['first_name']) && isset($json['last_name']) && isset($json['gender']) && isset($json['avatar'])){
					$facebook_id = $json['facebook_id']; $access_token = $json['facebook_access_token'];
					$email = $json['email']; $first_name = $json['first_name'];
					$last_name = $json['last_name']; $gender = $json['gender'];
					if(isset($json['birthday'])){
						$birthday = $json['birthday'];
					} else {
						$birthday = NULL;
					}
					$avatar = $json['avatar'];
					$id = $db->checkLoginFacebook($facebook_id);
					if($id != NULL){
						$db->updateAccessToken($access_token, $facebook_id, $id);
						$user = $db->getUserDataByID($id);
						if ($user != NULL) {
		                    $response["success"] = true;
		                    $response["message"] = "Đăng Nhập Thành Công";
		                    $response["content"] = $user;
		                }
					} else {
						$res = $db->createUserFacebook($facebook_id, $access_token, $email, $first_name, $last_name, $gender, $birthday, $avatar, $account_type);
						if($res == 0) {
							$id = $db->checkLoginFacebook($facebook_id);
							$user = $db->getUserDataByID($id);
			                if ($user != NULL) {
			                    $response["success"] = true;
			                    $response["message"] = "Đăng Kí Tài Khoản Thành Công. Đăng Nhập Thành Công";
			                    $response["content"] = $user;
			                }
						}
					}
					echoResponse(200, $response);
                    $app->stop();
				}
			}
		}
	});

	/*UPDATE TÀI KHOẢN*/
	$app->put('/account', 'authenticate', function() use ($app) {
		$body = $app->request->getBody();
		$json = json_decode($body, true);
		$db = new DbHandler();
		global $user_id;
		$response = array();
		$response["success"] = false;
		if(isset($json['password']) && isset($json['first_name']) && isset($json['last_name']) && isset($json['gender']) && isset($json['birthday']) && isset($json['age']) && isset($json['address']) && isset($json['occupation']) && isset($json['description']) && isset($json['phonenumber']) && isset($json['avatar']) && isset($json['account_type'])){
			$password = $json['password']; $first_name = $json['first_name'];
			$last_name = $json['last_name']; $gender = $json['gender'];
			$birthday = $json['birthday']; $age = $json['age'];
			$address = $json['address']; $occupation = $json['occupation'];
			$description = $json['description']; $phonenumber = $json['phonenumber'];
			$avatar = $json['avatar']; $account_type = $json['account_type'];
			$result = $db->updateAccount($account_type, $user_id, $password, $first_name, $last_name, $gender, $birthday, $age, $address, $occupation, $description, $phonenumber, $avatar);
			if($result){
				$response["success"] = true;
	            $response["message"] = "Update Tài Khoản Thành Công";
			}
			echoResponse(200, $response);
			$app->stop();
		}
		$response["message"] = "Update Tài Khoản Thất Bại. Yêu Cầu Không Hợp Lệ";
		echoResponse(200, $response);
		$app->stop();
	});

	/*TÌM KIẾM TÀI KHOẢN ĐÃ ĐĂNG PHÒNG TRỌ*/
	$app->get('/account/search', function() use ($app){
		$db = new DbHandler();
		$room_id = $app->request->get('room_id');
		$response = array();
		$rs =  $db->getUserDataByRoomID($room_id);
		if($rs != NULL){
			$response["success"] = true;
			$response["content"] = $rs;
			echoResponse(200, $response);
		} else {
			$response["success"] = false;
			$response["message"] = "Không Tìm Thấy Dữ Liệu Người Dùng";
			echoResponse(200, $response);
		}
	});

	/************************************** END FUNCTIONS LIÊN QUAN ĐẾN USER ******************************************/

	/************************************** FUNCTIONS LIÊN QUAN ĐẾN ROOM **********************************************/

	/*ĐĂNG TIN PHÒNG TRỌ*/
	$app->post('/rooms', 'authenticate', function() use ($app){
		global $user_id;
		$db = new DbHandler();
		$body = $app->request->getBody();
		$json = array(json_decode($body, true));
		$response = array();
		$room_address = $json[0]['room_address'];
		$room_properties = $json[0]['room_properties'];
		$room_images = $json[0]['room_images'];
		$room_amenities = $json[0]['room_amenities'];
		$result = $db->createRoom($user_id, $room_address, $room_properties, $room_images, $room_amenities);
		if($result != 0){
			$response["success"] = true;
        	$response["message"] = "Đăng Bài Thành Công";
        	echoResponse(200, $response);	
		}else {
			$response["success"] = false;
        	$response["message"] = "Đăng Bài Thất Bại. Xin Vui Lòng Thử Lại";
        	echoResponse(200, $response);
		}
	});

	/*LẤY VỀ DANH SÁCH PHÒNG USER ĐÃ ĐĂNG*/
	$app->get('/myrooms', 'authenticate', function() use ($app){
		global $user_id;
		$db = new DbHandler();
		$response = array();
		$response["success"] = true;
		$result = $db->getMyRooms($user_id);
		$response["message"] = "Tìm Thấy ".count($result)." Bài Đăng";
		$response["content"] =  $result;
		echoResponse(200, $response);
	});
	
	/*UPDATE BÀI ĐĂNG*/
	$app->put('/rooms/:id', 'authenticate', function($room_id) use ($app){
		// global $user_id;
		$db = new DbHandler();
		$response = array();
		$body = $app->request->getBody();
		$json = array(json_decode($body, true));
		$update_type = $json[0]['update_type'];
		if($update_type == 1){
			//update Room_Address
			$room_address = $json[0]['room_address'];
			$db->updateRoomAddress($room_id, $room_address);
			$rs = $db->getRoomAddress($room_id);
			$response["success"] = true;
			$response["message"] = "Sửa Vị Trí Phòng Trọ Thành Công";
			$response["content"] = $rs;
			echoResponse(200, $response);
		} else if ($update_type == 2){
			//update Room_Properties
			$room_properties = $json[0]['room_properties'];
			$db->updateRoomProperties($room_id, $room_properties);
			$rs = $db->getRoomProperties($room_id);
			$response["success"] = true;
			$response["message"] = "Sửa Thông Tin Chung Phòng Trọ Thành Công";
			$response["content"] = $rs;
			echoResponse(200, $response);
		} else if($update_type == 3){
			//update Room_Images
			$delete = $json[0]['Delete'];
			$insert = $json[0]['Insert'];
			$update = $json[0]['Update'];

			if(count($delete) > 0) {
				$db->deleteRoomImages($delete);
			}
			if(count($update) > 0){
				$db->updateRoomImages($update);
			}
			if(count($insert) > 0){
				$db->insertRoomImages($insert, $room_id);
			}
			$rs = $db->getRoomImages($room_id);
			$response["success"] = true;
			$response["message"] = "Sửa Hình Ảnh Phòng Trọ Thành Công";
	        $response["content"] = $rs;
	        echoResponse(200, $response);
    	} else if($update_type == 4){
    		//update Room_Amenities
    		$room_amenities = $json[0]['room_amenities'];
    		$db->updateRoomAmenities($room_id, $room_amenities);
			$rs = $db->getRoomAmenities($room_id);
			$response["success"] = true;
	        $response["message"] = "Sửa Tiện Ích Phòng Trọ Thành Công";
	        $response["content"] = $rs;
	        echoResponse(200, $response);
    	}
	});

	/*XÓA BÀI ĐĂNG*/
	$app->delete('/rooms/:id', 'authenticate', function($room_id) use ($app){
		$db = new DbHandler();
		$response = array();
		$rs = $db->deleteRoom($room_id);
		if($rs > 0){
			$response["success"] = true;
			$response["message"] = "Xóa Bài Đăng Thành Công";
			echoResponse(200, $response);
		} else {
			$response["success"] = false;
			$response["message"] = "Xóa Bài Đăng Thất Bại";
			echoResponse(200, $response);
		}
	});

	/*TÌM KIẾM BÀI ĐĂNG*/
	$app->get('/rooms/search', function() use ($app){
		$db = new DbHandler();
		$response = array();
		$type = $app->request->get('type');
		if($type == 0){
			//search full text
			$query = $app->request->get('query');
			$rs =  $db->searchByAddress($query);
			$response["success"] = true;
			$response["message"] = "Tìm được ".count($rs)." phòng trọ";
			$response["content"] = $rs;
			echoResponse(200, $response);
		} else if ($type == 1){
			//search radius
			$latitude = $app->request->get('latitude');
			$longitude = $app->request->get('longitude');
			$radius = $app->request->get('radius');
			$rs = $db->searchByRadius($latitude, $longitude, $radius);
			$response["success"] = true;
			$response["message"] = "Tìm được ".count($rs)." phòng trọ";
			$response["content"] = $rs;
			echoResponse(200, $response);
		} else if ($type == 2){
			//search rectangle
			$sw_lat = $app->request->get('sw_lat');
			$sw_lng = $app->request->get('sw_lng');
			$ne_lat = $app->request->get('ne_lat');
			$ne_lng = $app->request->get('ne_lng');
			$rs = $db->searchByRectangle($sw_lat, $sw_lng, $ne_lat, $ne_lng);
			$response["success"] = true;
			$response["message"] = "Tìm được ".count($rs)." phòng trọ";
			$response["content"] = $rs;
			echoResponse(200, $response);
		} else {
			$response["success"] = false;
			$response["message"] = "Tham Số Không Phù Hợp";
			echoResponse(200, $response);
		}
	});

	/*FAVORITE ROOM*/
	$app->get('/favoriteroom', 'authenticate', function() use ($app){
		global $user_id;
		$db = new DbHandler();
		$response = array();
		$response["success"] = true;
		$result = $db->getMyFavoriteRooms($user_id);
		$response["message"] = "Tìm Thấy ".count($result). " Bài Đăng";
		$response["content"] =  $result;
		echoResponse(200, $response);
	});

	$app->get('/favoriteroom/search', 'authenticate', function() use ($app){
		global $user_id;
		$db = new DbHandler();
		$response = array();
		$response["success"] = true;
		$room_id = $app->request->get('room_id');
		$rs = $db->isFavoriteRoomExists($room_id, $user_id);
		if($rs == 1){
			$response["content"] = true;
		} else {
			$response["content"] = false;
		}
		echoResponse(200, $response);
	});

	$app->post('/favoriteroom', 'authenticate', function() use ($app){
		global $user_id;
		$db = new DbHandler();
		$body = $app->request->getBody();
		$json = json_decode($body, true);
		$response = array();
		$room_id = $json['room_id'];
		$result = $db->createFavoriteRoom($room_id, $user_id);
		if($result != 0){
			$response["success"] = true;
        	$response["message"] = "Tạo Mục Ưa Thích Bài Đăng Thành Công";
        	echoResponse(200, $response);	
		}else {
			$response["success"] = false;
        	$response["message"] = "Tạo Mục Ưa Thích Bài Đăng Thất Bại. Xin Vui Lòng Thử Lại";
        	echoResponse(200, $response);
		}
	});

	$app->delete('/favoriteroom/:id', 'authenticate', function($room_id) use ($app){
		global $user_id;
		$db = new DbHandler();
		$response = array();
		$db->deleteFavoriteRoom($room_id, $user_id);
		$response["success"] = true;
		$response["message"] = "Xóa Mục Ưa Thích Bài Đăng Thành Công";
		echoResponse(200, $response);
	});

	/************************************** END FUNCTIONS LIÊN QUAN ĐẾN ROOM ******************************************/

	/************************************** FUNCTIONS LIÊN QUAN ĐẾN COMMENTS ******************************************/

	/*ĐĂNG BÌNH LUẬN*/
	$app->post('/comments','authenticate', function() use ($app){
		global $user_id;
		$db = new DbHandler();
		$body = $app->request->getBody();
		$json = json_decode($body, true);
		$comment = $json['comment'];
		$room_id = $json['room_id'];

		$comment_id = $db->createComment($user_id, $room_id, $comment);
		$response = array();
		if($comment_id != 0){
			$response["success"] = true;
			$response["message"] = "Bình Luận Thành Công";
			$response["content"] = $comment_id;
			echoResponse(200, $response);
		} else {
			$response["success"] = false;
			$response["message"] = "Bình Luận Thất Bại";
			echoResponse(200, $response);
		}
	});

	/*SỬA BÌNH LUẬN*/
	$app->put('/comments/:id', 'authenticate', function($comment_id) use ($app){
		global $user_id;
		$db = new DbHandler();
		$body = $app->request->getBody();
		$json = json_decode($body, true);
		$new_comment = $json['comment'];
		$rs = $db->editComment($user_id, $comment_id, $new_comment);
		$response["success"] = true;
		$response["message"] = "Sửa Bình Luận Thành Công";
		echoResponse(200, $response);
	});

	/*XÓA BÌNH LUẬN*/
	$app->delete('/comments/:id', 'authenticate', function($comment_id) use ($app){
		$db = new DbHandler();
		$rs = $db->deleteComment($comment_id);
		$response["success"] = true;
		$response["message"] = "Xóa Bình Luận Thành Công";
		echoResponse(200, $response);
	});

	/*LẤY VỀ DANH SÁCH BÌNH LUẬN*/
	$app->get('/comments/search', 'authenticate', function() use ($app){
		$db = new DbHandler();
		$room_id = $app->request->get('room_id');
		$page = $app->request->get('page');
		$response = array();
		$rs =  $db->getCommentsDataByRoomID($room_id, $page);
		if($rs != NULL){
			$response["success"] = true;
			$response["content"] = $rs;
			echoResponse(200, $response);
		} else {
			$response["success"] = true;
			if($page > 0){
				$response["message"] = "Không Tìm Thấy Thêm Dữ Liệu Bình Luận Bài Đăng";
			} else {
				$response["message"] = "Không Tìm Thấy Dữ Liệu Bình Luận Bài Đăng";
			}
			$response["content"] = "";
			echoResponse(200, $response);
		}
	});

	/************************************** END FUNCTIONS LIÊN QUAN ĐẾN COMMENTS **************************************/

	$app->post('/ratings', 'authenticate', function() use ($app){
		global $user_id;
		$db = new DbHandler();
		$body = $app->request->getBody();
		$json = json_decode($body, true);
		$rating = $json['rating'];
		$room_id = $json['room_id'];

		$rs = $db->isExistsRating($user_id, $room_id);
		if($rs == 1){
			$rs = $db->updateRating($user_id, $room_id, $rating);
			if($rs > 0){
				$response["success"] = true;
				$response["message"] = "Update Đánh Giá Thành Công";
				echoResponse(200, $response);		
			} else {
				$response["success"] = false;
				$response["message"] = "Update Đánh Giá Thất Bại";
				echoResponse(200, $response);		
			}
		} else {
			$rs = $db->createRating($user_id, $room_id, $rating);
			if($rs > 0){
				$response["success"] = true;
				$response["message"] = "Đánh Giá Thành Công";
				echoResponse(200, $response);		
			} else {
				$response["success"] = false;
				$response["message"] = "Đánh Giá Thất Bại";
				echoResponse(200, $response);		
			}
		}
	});

	$app->get('/ratings/search', 'authenticate', function() use ($app){
		global $user_id;
		$db = new DbHandler();
		$room_id = $app->request->get('room_id');
		$response = array();
		$rs =  $db->getUserRating($room_id, $user_id);
		if($rs != NULL){
			$response["success"] = true;
			$response["content"] = $rs;
			echoResponse(200, $response);
		} else {
			$response["success"] = false;
			$response["message"] = "Không Tìm Thấy Dữ Liệu Đánh Giá Bài Đăng";
			echoResponse(200, $response);
		}
	});

	/************************************** END FUNCTIONS LIÊN QUAN ĐẾN COMMENTS **************************************/

	$app->get('/abc',function() use ($app){
		$arr = array();
		// array_push($arr,"Trường Đại học Bách Khoa - ĐHQG TP.HCM");
		// array_push($arr,"Trường Đại học Khoa học Tự nhiên - ĐHQG TPHCM");
		// array_push($arr,"Trường Đại học Khoa học xã hội và Nhân văn - ĐHQG TP.HCM");
		// array_push($arr,"Trường Đại học Quốc tế - ĐH Quốc gia TP.HCM");
		// array_push($arr,"Trường Đại học Công nghệ Thông tin - ĐH Quốc gia TP.HCM");
		// array_push($arr,"Trường Đại học Kinh tế - Luật (ĐH Quốc gia TP.HCM)");//no result
		// array_push($arr,"Khoa Y - ĐH Quốc gia TP.HCM");
		// array_push($arr,"Học viện Hàng không Việt Nam");
		// array_push($arr,"Trường Đại học Tài chính - Marketing");
		// array_push($arr,"Trường Đại học Tôn Đức Thắng");
		// array_push($arr,"Trường Đại học Công nghiệp TP.HCM");
		// array_push($arr,"Trường Đại học Công nghiệp Thực phẩm TP.HCM");
		// array_push($arr,"Trường Đại học Giao thông vận tải - Cơ sở 2");
		// array_push($arr,"Trường Đại học Giao thông vận tải TP.HCM");
		// array_push($arr,"Trường Đại học Kiến trúc TP.HCM");
		// array_push($arr,"Trường Đại học Kinh tế TP.HCM");
		// array_push($arr,"Trường Đại học Luật TP.HCM");
		// array_push($arr,"Trường Đại học Mỹ thuật TP.HCM");
		// array_push($arr,"Trường Đại học Ngân hàng TP.HCM");
		// array_push($arr,"Nhạc viện TP.HCM");
		// array_push($arr,"Trường Đại học Nông Lâm TP.HCM");
		// array_push($arr,"Trường Đại học Sài Gòn");
		// array_push($arr,"Trường Đại học Sân khấu Điện ảnh TP.HCM");
		// array_push($arr,"Trường Đại học Sư phạm Kỹ thuật TP.HCM");
		// array_push($arr,"Trường Đại học Sư phạm TP.HCM");
		// array_push($arr,"Trường Đại học Sư phạm TDTT TP. HCM");
		// array_push($arr,"Trường Đại học Tài nguyên và Môi trường TP.HCM");
		// array_push($arr,"Trường Đại học Thể dục Thể thao TP.HCM");
		// array_push($arr,"Trường Đại học Văn hóa TP.HCM");
		// array_push($arr,"Trường Đại học Y Dược TP.HCM");
		// array_push($arr,"Trường Đại học Y khoa Phạm Ngọc Thạch");
		// array_push($arr,"Trường Đại học Mở TP.HCM");
		// array_push($arr,"Trường Đại học Công nghệ Sài Gòn");
		// array_push($arr,"Trường Đại học Quốc tế Hồng Bàng");
		// array_push($arr,"Trường Đại học Hùng Vương");
		// array_push($arr,"Trường Đại học Kỹ thuật - Công nghệ TP.HCM");
		// array_push($arr,"Trường ĐH Ngoại ngữ - Tin học TP.HCM");
		// array_push($arr,"Trường Đại học Nguyễn Tất Thành");
		// array_push($arr,"Trường Đại học Văn Hiến");
		// array_push($arr,"Trường Đại học Văn Lang");
		// array_push($arr,"Trường Đại học Kinh tế - Tài chính TP.HCM");
		// array_push($arr,"Trường Đại học Hoa Sen");
		// array_push($arr,"Trường Đại học Công nghệ thông tin Gia Định");
		array_push($arr,"Trường Đại học Quốc tế Sài Gòn");
		array_push($arr,"Trường Đại học Việt - Đức");


		$key_1 = "AIzaSyBWt6qFGhJAS9Z4qXVfTLwFnKlLG1J394M";
		$key_2 = "AIzaSyAnY51CLMsS-_88iQwu-kz96TYb4GJ0vNI";
		$arr_message = array();
		for ($i = 0; $i < 2; $i++){
			$name = urlencode($arr[$i]);
			$url = "https://maps.googleapis.com/maps/api/place/textsearch/json?query=".$name."&key=".$key_2;
			$result = file_get_contents($url);
			$rs = json_decode($result, true);
			if(count($rs['results']) > 0){
				$place_id = $rs['results'][0]['place_id'];
				$r  = $arr[$i]. " - ".$place_id;
				array_push($arr_message, $r);
			}
		}

		$response = array();
		$response["success"] = true;
		$response["message"] = $arr_message;
		echoResponse(200, $response);
	});

	function authenticate(\Slim\Route $route) {
	    $headers = apache_request_headers();
	    $db = new DbHandler();
	    $response = array();
	    $response["success"] = false;
	    $app = \Slim\Slim::getInstance();
	    if (isset($headers['Authorization'])) {
	        $api_key = $headers['Authorization'];
	        if (!$db->isValidApiKey($api_key)) {
	            $response["message"] = "Truy Cập Bị Từ Chối. Api Key Không Hợp Lệ";
	            echoResponse(401, $response);
	            $app->stop();
	        } else {
	            global $user_id;
	            $user_id = $db->getUserId($api_key);
	        }
	    } else {
	        $response["message"] = "Không Có Api Key";
	        echoResponse(200, $response);
	        $app->stop();
	    }
	}

	// function verifyRequiredParams($required_fields) {
	// 	$success = true;
	// 	$error_fields = "";
	// 	$request_params = array();
	// 	$request_params = $_REQUEST;
		
	// 	if ($_SERVER['REQUEST_METHOD'] == 'PUT') {
	// 		$app = \Slim\Slim::getInstance();
	// 		parse_str($app->request()->getBody(), $request_params);
	// 	}
	// 	foreach ($required_fields as $field) {
	// 		if (!isset($request_params[$field]) || strlen(trim($request_params[$field])) <= 0) {
	// 			$success = false;
	// 			$error_fields .= $field . ', ';
	// 		}
	// 	}

	// 	if (!$success) {
	// 		$response = array();
	// 		$app = \Slim\Slim::getInstance();
	// 		$response["success"] = false;
	// 		$response["message"] = 'Required field(s) ' . substr($error_fields, 0, -2) . ' is missing or empty';
	// 		echoResponse(400, $response);
	// 		$app->stop();
	// 	}
	// }

	function validateEmail($string, $email) {
		$app = \Slim\Slim::getInstance();
		if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
			$response["success"] = false;
			$response["message"] = $string.'Địa Chỉ Email Không Hợp Lệ';
			echoResponse(200, $response);
			$app->stop();
		}
	}

	function echoResponse($status_code, $response) {
		$app = \Slim\Slim::getInstance();
		$app->status($status_code);
		$app->contentType('application/json');
		echo json_encode($response);
	}
	
	$app->run();
?>