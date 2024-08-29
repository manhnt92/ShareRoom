var app = require('http').createServer();
var io = require('socket.io').listen(app, {'pingInterval': 2000, 'pingTimeout': 5000});
var fs = require('fs');
var mysql = require('mysql');

app.listen(3000);

var os = require('os');
var interfaces = os.networkInterfaces();
var addresses = [];
for (var k in interfaces) {
    for (var k2 in interfaces[k]) {
        var address = interfaces[k][k2];
        if (address.family === 'IPv4' && !address.internal) {
            addresses.push(address.address);
        }
    }
}
console.log("Socket Server Running At IP : " + addresses + ":" + app.address().port );


var clients = [];
var db = mysql.createConnection({
    host: 'localhost',
    user: 'root',
	password: '',
    database: 'shareroom',
	dateStrings : 'TIMESTAMP'
});

db.connect(function(err){
    if (err){ 
		console.log(err);
	}
});



io.sockets.on('connection', function (socket) {
	
	socket.on('UserInfo', function(data){
		var SocketID = getSocketid(JSON.parse(data).id);
		if(SocketID == null){
			SocketID = generateSocketID(JSON.parse(data).id);
		}
	});
	
	socket.on('disconnect', function() {
		for (var i= 0 ; i < clients.length ; i++){
			var c = clients[i];
			if(c.SocketID == socket.id){
				clients.splice(i,1);
				break;
			}
		}
	});
	
	/** CHAT API*/
	
	socket.on('UnReadMessage', function(data){
		var SocketID = getSocketid(JSON.parse(data).id);
		if(SocketID == null){
			SocketID = generateSocketID(JSON.parse(data).id);
		}
		var query = db.query("SELECT cm.*, u.first_name, u.last_name, c.user1_id AS from_id "
			+ "FROM chats_message AS cm INNER JOIN chats AS c ON cm.chat_id = c.id AND c.user1_id !=" + JSON.parse(data).id + " "
			+ "INNER JOIN users AS u ON c.user1_id = u.id "
			+ "WHERE cm.to_id =" + JSON.parse(data).id + " AND cm.status < 2 "
			+ "UNION ALL "
			+ "SELECT cm.*, u.first_name, u.last_name, c.user2_id AS from_id "
			+ "FROM chats_message AS cm INNER JOIN chats AS c ON cm.chat_id = c.id AND c.user2_id !=" + JSON.parse(data).id + " "
			+ "INNER JOIN users AS u ON c.user2_id = u.id "
			+ "WHERE cm.to_id =" + JSON.parse(data).id + " AND cm.status < 2 ORDER BY from_id ASC");
		var message_unread = [];
		query.on('result', function(data){
			message_unread.push(data);
		});
		query.on('end', function(){
			if(message_unread.length > 0){
				io.sockets.connected[SocketID].emit("UnReadMessage", JSON.stringify(message_unread));
				for(var i = 0; i < message_unread.length; i++){
					db.query("UPDATE chats_message SET status = 2 WHERE id = " + message_unread[i].id);
				}
			}
		});
	});
	
	socket.on('Conversation', function(data){
		var user1 = JSON.parse(data).User1_ID;
		var SocketID = getSocketid(user1);
		if(SocketID == null){
			SocketID = generateSocketID(user1);
		}
		//console.log("Conversation : " + JSON.stringify(clients));
		var user2 = JSON.parse(data).User2_ID;
		db.query("SELECT count(*) AS count, id FROM chats WHERE (user1_id =" + user1 + " AND user2_id =" + user2 + ") OR (user1_id = " + user2 + " AND user2_id = "+ user1+")").on('result', function(data){
			if(data.count == 0){
				db.query("INSERT INTO chats (user1_id, user2_id) VALUES (" + user1 + "," + user2 + ")").on('result', function(data){
					var rs = new Object();
					rs.message = "Tạo Hội Thoại Thành Công";
					rs.data = false;
					rs.chat_id = data.insertId;
					io.sockets.connected[SocketID].emit("Conversation", JSON.stringify(rs));
				});
			} else {
				var q = db.query("SELECT count(*) as count FROM chats_message WHERE chat_id =" + data.id);
				var count = 0;
				q.on('result', function(datas){
					count = datas.count;
				});
				q.on('end', function(){
					if(count == 0){
						var rs = new Object();
						rs.message = "Không có Nội Dung Hội Thoại";
						rs.data = false;
						rs.chat_id = data.id;
						io.sockets.connected[SocketID].emit("Conversation", JSON.stringify(rs));
					} else {
						var query = db.query("SELECT * FROM chats_message WHERE chat_id =" + data.id + " ORDER BY created ASC");
						var result_chats = new Object();
						result_chats.data = true;
						var rs = [];
						query.on('result', function(data){
							rs.push(data);
						});
						query.on('end', function(){
							result_chats.message = rs;
							io.sockets.connected[SocketID].emit("Conversation", JSON.stringify(result_chats));
						});
					}
				});
			}
		});
	});
	
	socket.on('MyConversations', function(data){
		var SocketID = getSocketid(JSON.parse(data).id);
		if(SocketID == null){
			SocketID = generateSocketID(JSON.parse(data).id);
		}
		var query = db.query("SELECT u.id, u.first_name, u.last_name, u.avatar FROM users AS u INNER JOIN chats AS c ON c.user1_id = u.id WHERE c.user2_id = " + JSON.parse(data).id + " "
			+ "UNION ALL "
			+ "SELECT u.id, u.first_name, u.last_name, u.avatar FROM users AS u INNER JOIN chats AS c ON c.user2_id = u.id WHERE c.user1_id = " + JSON.parse(data).id);
		var my_conversations = [];
		query.on('result', function(data){
			my_conversations.push(data);
		});
		query.on('end', function(){
			if(my_conversations.length > 0){
				io.sockets.connected[SocketID].emit("MyConversations", JSON.stringify(my_conversations));
			}
		});
		
	});
	
	socket.on("Ping",function(data){
		var UserID = JSON.parse(data).id;
		var pingArr = JSON.parse(data).Ping;
		var ping = [];
		for(var i = 0; i < pingArr.length; i++){
			for (var j = 0 ; j < clients.length ;j++){
				var clientInfo = clients[j];
				if(clientInfo.id == pingArr[i].id){
					ping.push({"id" : pingArr[i].id, "status" : 1});
					break;
				}
			}
		}
		if(ping.length > 0){
			var SocketID = getSocketid(UserID);
			if(SocketID != null){
				io.sockets.connected[SocketID].emit("Ping", JSON.stringify(ping));
			} else {
				SocketID = generateSocketID(JSON.parse(data).id);
				io.sockets.connected[SocketID].emit("Ping", JSON.stringify(ping));
			}
		}
	});
	
	socket.on('PrivateMessage', function(data){
		console.log("Private Message : " + JSON.stringify(data));
		var chat_id = JSON.parse(data).chat_id;
		var from_id = JSON.parse(data).from_id;
		var to_id = JSON.parse(data).to_id;
		var message = JSON.parse(data).message;
		var SocketID = getSocketid(from_id);
		if(SocketID == null){
			SocketID = generateSocketID(from_id);
		}
		//console.log("Private Message : " + JSON.stringify(clients));
		db.query("INSERT INTO chats_message (chat_id, to_id, message, status) VALUES (" + chat_id + "," + to_id + ",'" + message + "'," + 1 +")").on('result', function(data){
			var insert_id = data.insertId;
			
			db.query("SELECT cm.*, u.first_name, u.last_name, c.user1_id AS from_id "
					+ "FROM chats_message AS cm "
					+ "INNER JOIN chats AS c ON cm.chat_id = c.id "
					+ "INNER JOIN users AS u ON u.id = c.user1_id "
					+ "WHERE c.user1_id = " + from_id + " AND cm.to_id = " + to_id + " AND cm.id = " + insert_id + " "
					+ "UNION ALL "
					+ "SELECT cm.*, u.first_name, u.last_name, c.user2_id AS from_id "
					+ "FROM chats_message AS cm "
					+ "INNER JOIN chats AS c ON cm.chat_id = c.id "
					+ "INNER JOIN users AS u ON u.id = c.user2_id "
					+ "WHERE c.user2_id = " + from_id + " AND cm.to_id = " + to_id + " AND cm.id = " + insert_id).on('result', function(data){
				io.sockets.connected[SocketID].emit("PrivateMessage", JSON.stringify(data));
				var isSend = false;
				for (var i = 0 ; i < clients.length ; i++){
					var clientInfo = clients[i];
					if(clientInfo.id == to_id){
						io.sockets.connected[clientInfo.SocketID].emit("PrivateMessage", JSON.stringify(data));
						isSend = true;
						break;
					}
				}
				if(isSend){
					db.query("UPDATE chats_message SET status=" + 2 + " WHERE id=" + insert_id);
					db.query("SELECT cm.*, u.first_name, u.last_name, c.user1_id AS from_id "
					+ "FROM chats_message AS cm "
					+ "INNER JOIN chats AS c ON cm.chat_id = c.id "
					+ "INNER JOIN users AS u ON u.id = c.user1_id "
					+ "WHERE c.user1_id = " + from_id + " AND cm.to_id = " + to_id + " AND cm.id = " + insert_id + " "
					+ "UNION ALL "
					+ "SELECT cm.*, u.first_name, u.last_name, c.user2_id AS from_id "
					+ "FROM chats_message AS cm "
					+ "INNER JOIN chats AS c ON cm.chat_id = c.id "
					+ "INNER JOIN users AS u ON u.id = c.user2_id "
					+ "WHERE c.user2_id = " + from_id + " AND cm.to_id = " + to_id + " AND cm.id = " + insert_id).on('result', function(data){
						io.sockets.connected[SocketID].emit("PrivateMessage", JSON.stringify(data));
					});
				}
			});
		});;
	});
	
	socket.on('ReadPrivateMessage', function(data){
		var SocketID = getSocketid(JSON.parse(data).id);//id = user_send_id
		var chats_message_id = JSON.parse(data).chats_message_id;
		var query = db.query("UPDATE chats_message SET status = 3 WHERE id = " + chats_message_id);
		query.on('end', function(){
			if(SocketID != null){
			io.sockets.connected[SocketID].emit("ReadPrivateMessage", JSON.stringify({"chats_message_id" : chats_message_id}));
			}
		});
	});
	
	socket.on('Typing', function(data){
		
	});
	
	socket.on('GetPhoneNumber', function(data){
		var query = db.query("SELECT phonenumber FROM users WHERE id = " + data + ";");
		query.on('result', function(phonenumber){
			io.sockets.connected[socket.id].emit("GetPhoneNumber", phonenumber);
		});
	});
	
	/** END CHAT API*/
	
	/** CALL API */
	
	socket.on('PingBeforeCall',function(data){
		var From_ID = data.id;
		var Call_Type = data.CallType;
		var From_UserName = data.UserName;
		var To_SocketID = getSocketid(data.to_id);
		var otherClient = io.sockets.connected[To_SocketID];
		if(otherClient){
			io.sockets.connected[socket.id].emit('PingBeforeCall', {"success" : true, "ToSocketID" : To_SocketID});
			io.sockets.connected[To_SocketID].emit('InComingCall', {"From" : socket.id, "UserName" : From_UserName, "CallType" : Call_Type});
		} else {
			io.sockets.connected[socket.id].emit('PingBeforeCall', {"success" : false});
		}
	});
	
	socket.on('Ready', function(data){
		var otherClient = io.sockets.connected[data];
		if(!otherClient){
			return;
		}
		io.sockets.connected[data].emit('Ready', "");
	});
	
	socket.on('Reject', function(data){
		var otherClient = io.sockets.connected[data];
		if(!otherClient){
			return;
		}
		io.sockets.connected[data].emit('Reject', "");
	});
	
	socket.on('SocketCallMessage', function(data){
		var otherClient = io.sockets.connected[data.To];
		if (!otherClient) {
			return;
		}
		delete data.To;
        data.From = socket.id;
        otherClient.emit('SocketCallMessage', data);
	});
	
	socket.on('EndCall', function(data){
		var otherClient = io.sockets.connected[data];
		if (!otherClient) {
			return;
		}
        otherClient.emit('EndCall', "");
	})
	/** END CALL API */
	
	function generateSocketID(id){
		var clientInfo = new Object();
		clientInfo.id = id;
		clientInfo.SocketID = socket.id;
		clientInfo.created = new Date().toISOString().replace(/T/, ' ').replace(/\..+/, '');
		clients.push(clientInfo);
		for(var i = 0; i < clients.length;i++){
			var c = clients[i];
			if(c.id == clientInfo.id && c.created != clientInfo.created){
				//console.log("GenerateSocketID Function. Remove : " + JSON.stringify(clients[i]));
				clients.splice(i,1);
				
				//console.log("GenerateSocketID Function. After Remove : " + JSON.stringify(clients));
				break;
			}
		}
		return clientInfo.SocketID;
	}
	
	function getSocketid(id){
		for (var j = 0 ; j < clients.length ;j++){
			var c = clients[j];
			if(c.id == id){
				//console.log("GetSocketID Function : " + JSON.stringify(clients[j]));
				return c.SocketID;
			}
		}
		return null;
	}
	
	function getUserInfo(socketID){
		for (var j = 0 ; j < clients.length ;j++){
			var c = clients[j];
			if(c.id == id){
				//console.log("GetSocketID Function : " + JSON.stringify(clients[j]));
				return c.SocketID;
			}
		}
		return null;
	}

});



