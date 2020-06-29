let express = require('express');
let router = express.Router();
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

// 운전자가 사용하는 어플리케이션에 3km 이내에 있는 교차로들 목록을 보내줌.
router.post('/cross/find', function (request, response) { 
    let data = request.body;
    let lat = data.lat; // 운전자 자동차의 위도
    let lon = data.lon; // 운전자 자동차의 경도
    let arr = []; // 교차로 정보들을 저장할 배열

    let sql = 'select * from intersection';
    conn.query(sql, function(error, results) {
        if (error) {
            console.log(error);
        } else {
            let len = results.length; // DB에 있는 교차로 갯수
            if (len == 0) { // 교차로가 없으면
                response.json({
                    result: false // false를 반환
                });
            } else { // 교차로가 있다면
                for (let i = 0; i < len; i++) { // 갯수만큼 반복문
                    let distance = getDistance(lat, lon, results[i].cent_x, results[i].cent_y); // 운전자 차량위치와 교차로와의 거리 계산
                    if (distance < 3000) { // 3km 이내라면
                        let obj = {
                            id: results[i].id, // 교차로 id
                            cent_x: results[i].cent_x, // 교차로 중앙좌표 lat
                            cent_y: results[i].cent_y, // 교차로 중앙좌표 lon
                            loc_x0: results[i].loc_x0, // 교차로 북동쪽 상단 lat
                            loc_y0: results[i].loc_y0, // 교차로 북동쪽 상단 lon
                            loc_x1: results[i].loc_x1, // 교차로 북서쪽 상단 lat
                            loc_y1: results[i].loc_y1, // 교차로 북서쪽 상단 lon
                            loc_x2: results[i].loc_x2, // 교차로 남서쪽 하단 lat
                            loc_y2: results[i].loc_y2, // 교차로 남서쪽 하단 lon
                            loc_x3: results[i].loc_x3, // 교차로 남동쪽 하단 lat
                            loc_y3: results[i].loc_y3, // 교차로 남동쪽 하단 lon
                            cen_x0: results[i].cen_x0, // 교차로 북쪽 중앙 lat
                            cen_x1: results[i].cen_x1, // 교차로 북쪽 중앙 lon
                            cen_x2: results[i].cen_x2, // 교차로 동쪽 중앙 lat
                            cen_x3: results[i].cen_x3, // 교차로 동쪽 중앙 lon
                            cen_y0: results[i].cen_y0, // 교차로 남쪽 중앙 lat
                            cen_y1: results[i].cen_y1, // 교차로 남쪽 중앙 lon
                            cen_y2: results[i].cen_y2, // 교차로 서쪽 중앙 lat
                            cen_y3: results[i].cen_y3 // 교차로 서쪽 중앙 lon
                        };
                        arr.push(obj);
                    }
                    if (i + 1 == len) { // json형식으로 response 보냄
                        response.json({
                            arr: arr,
                            result: true
                        });
                    }
                }
            }
        }
    });
});

// 위도, 경도 좌표를 이용한 거리 계산
function getDistance(lat1, lon1, lat2, lon2){
    let distance = 0;
    let theta = 0;
    
    theta = lon1 - lon2;
    distance = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
    distance = Math.acos(distance);
    distance = rad2deg(distance);

    distance = distance * 60 * 1.1515;
    distance = distance * 1.609344;    // 단위 mile 에서 km 변환.
    distance = distance * 1000.0;      // 단위  km 에서 m 로 변환

    return distance;
}

// degree를 radian으로 변환
function deg2rad(degree) {
    let pi = Math.PI;
    return degree * pi / 180;
}

// radian을 degree로 변환
function rad2deg(radian) {
    let pi = Math.PI;
    return radian * 180 / pi;   
}

module.exports = router;