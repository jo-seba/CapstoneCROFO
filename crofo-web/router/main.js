let express = require('express');
let router = express.Router();
let fs = require('fs');
let mysql = require('mysql');
let session = require('express-session');
let MYSQLStore = require('express-mysql-session')(session);
let passport = require('passport'),
    LocalStrategy = require('passport-local').Strategy;

const HOST = 'bic4907.diskstation.me'
const MYSQLID = 'capstone';
const MYSQLPW = 'capstone2020';
const DBNAME = 'capstone';
const SESSIONKEY = 'secretkey';

let conn = mysql.createConnection({
    host: HOST,
    user: MYSQLID,
    password: MYSQLPW,
    database: DBNAME
});

router.use(session({
    secret: SESSIONKEY,
    resave: false,
    saveUninitialized: true,
    store: new MYSQLStore({
        host: HOST,
        port: 3306,
        user: MYSQLID,
        password: MYSQLPW,
        database: DBNAME
    })
}));

router.use(passport.initialize());
router.use(passport.session());

router.get('/', function(request, response) {
    if(request.user) {
        fs.readFile('./webpage/main.html', 'utf8', function(error, data) {
            if(error) {
                console.log(error);
            } else {
                response.writeHead(200, {'Content-Type': 'text/html'});
                response.end(data);
            }
        });
    } else {
        response.redirect('/login');
    }
});

router.get('/getVideoAmount', function(request, response) { // 총 비디오의 갯수를 response로 보내줌
    if (!request.user) {
        response.redirect('/login');
    } else {
        let sql = 'select count(*) as cnt from video';
        conn.query(sql, function(error, results) {
            if (error) {
                console.log(error);
            } else {
                let cnt = results[0].cnt;
                response.json({
                    amount: cnt
                });
            }
        });
    }
});

router.get('/getVideoList/:page', function(request, response) { // 현재 페이지에 해당하는 비디오의 목록을 보내줌
    if (!request.user) {
        response.redirect('/login');
    } else {
        let index = request.params.page;
        let sql = 'select name, date from video order by date desc limit ?, ?';
        conn.query(sql, [index * 10, 10], function (error, results) {
            if (error) {
                console.log(error);
            } else {
                if (results.length == 0) {
                    response.json({
                        result: false
                    });
                } else {
                    let arr = [];
                    let len = results.length;
                    let re = {
                        result: true,
                        length: len
                    };
                    arr[0] = re;
                    for(let i = 0; i < len; i++) {
                        let obj = {};
                        obj["name"] = results[i].name;
                        obj["date"] = results[i].date;
                        arr[i + 1] = obj;
                    }
                    let js = JSON.stringify(arr);
                    response.writeHead(200, { 'Content-Type': 'application/json;charset=utf8' });
                    response.end(js);
                }
            }
        });
    }
});

router.post('/loadVideo', function(request, response) { // 선택한 비디오들의 상대경로를 보내줌.(DB와 서버가 같은 하드웨어에 존재)
    if (!request.user) {
        response.redirect('/login');
    } else {
        let videoName = [];
        let body = request.body;
        let cnt = body.cnt;
        for (let i = 0; i < cnt; i++) {
            videoName.push(body.name[i]);
        }
        let arr = [];
        for (let i = 0; i < cnt; i++) {
            let sql = 'select name, path, date from video where name=?';
            conn.query(sql, [videoName[i]], function(error, results) {
                if (error) {
                    console.log(error);
                } else {
                    let obj = {};
                    obj["name"] = results[0].name;
                    obj["date"] = results[0].date;
                    obj["path"] = results[0].path;
                    arr[i] = obj;

                    if (i + 1 == cnt) {
                        let js = JSON.stringify(arr);
                        response.writeHead(200, { 'Content-Type': 'application/json;charset=utf8' });
                        response.end(js);
                    }
                }
            });
        }
    }
});

module.exports = router;