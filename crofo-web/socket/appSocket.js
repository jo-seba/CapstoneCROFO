module.exports = function (server) {
    let socketio = require('socket.io');
    let mysql = require('mysql');

    const HOST = 'bic4907.diskstation.me'
    const MYSQLID = 'capstone';
    const MYSQLPW = 'capstone2020';
    const DBNAME = 'capstone';

    let conn = mysql.createConnection({
        host: HOST,
        user: MYSQLID,
        password: MYSQLPW,
        database: DBNAME
    });    

    let io = socketio.listen(server);

    io.sockets.on('connection', function (socket) {
        let crosswalk; // 요청을 보낸 소켓의 상대적 횡단보도 위치
        let intersection; // 요청을 보낸 교차로

        socket.on('where', function(data) { // 해당 소켓이 어느 교차로의 어느 횡단보도인지 결정함
            let dir_x = data.x0; // 운전자가 진입하는 횡단보도 위도
            let dir_y = data.y0; // 운전자의 진입하는 횡단보도 경도
            let cro_x = data.x1; // 정보를 받을 교차로 내 횡단보도의 위도
            let cro_y = data.y1; // 정보를 받을 교차로 내 횡단보도의 경도
            let direction; // 진입 방향을 나타낼 변수
            intersection = data.in; // 교차로 id

            let sql = 'select id from crosswalk where cen_x=? and cen_y=? and intersection_id=?';
            conn.query(sql, [dir_x, dir_y, intersection], function(error, results) {
                if (error) {
                    console.log(error);
                } else {
                    direction = results[0].id; // DB에 저장되어 있는 운전자가 진입할 횡단보도의 실제 위치 
                    console.log('direction is ' + direction);
                    conn.query(sql, [cro_x, cro_y, intersection], function (err, result) {
                        if (err) {
                            console.log(err);
                        } else {
                            crosswalk = result[0].id; // DB에 저장되어 있는 정보를 받을 교차로 내 횡단보도의 실제 위치
                            console.log('crosswalk is ' + crosswalk);
                            if (direction == 0) { // 운전자 진입방향에 횡단보도의 따라서 상대적 위치 변경
                                crosswalk = (crosswalk + 2) % 4;
                            } else if (direction == 1) {
                                crosswalk = (crosswalk + 3) % 4;
                            } else if (direction == 3) {
                                crosswalk = (crosswalk + 1) % 4;
                            } 
                
                            console.log(intersection + ", " + crosswalk);
                            socket.join(socket.id); // 소켓의 고유 ID로 join하여 해당 사용자만 정보를 받을 수 있게 지정
                        }
                    });
                }
            });
        });

        socket.on('request', function() { // 해당 횡단보도의 객체 요청
            let sql = 'select content from objhistory where crosswalk_id=? and intersection_id=?';
            conn.query(sql, [crosswalk, intersection], function (error, results) {
                if (error) {
                    console.log(error);
                } else {
                    if (results.length == 0) { // 횡단보도가 DB에 존재하지 않을 때 예외처리
                        console.log(intersection + ", " + crosswalk + ": has no data");
                    } else {
                        let json = JSON.parse(results[0].content); // json 데이터를 파싱
                        let data = {
                            arr: json
                        };
                        console.log("socket.id: " + socket.id + "[ request: " + intersection + ", " + crosswalk + " ]");
                        console.log(data); 
                        io.to(socket.id).emit("object", data); // data를 소켓의 고유 ID로 보냄
                    }
                }
            });
        });
        
        socket.on('disconnect', function() {
            console.log(socket.id + " is disconnect");
        });
        
    });


}