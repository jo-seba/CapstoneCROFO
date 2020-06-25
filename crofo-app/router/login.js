let express = require('express');
let router = express.Router();
let fs = require('fs');
let mysql = require('mysql');
let session = require('express-session');
let MYSQLStore = require('express-mysql-session')(session);
let bkfd2Password = require('pbkdf2-password');
let hasher = bkfd2Password();
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
        response.redirect('/');
    } else {
        fs.readFile('./webpage/login.html', 'utf8', function(error, data) {
            if(error) {
                console.log(error);
            } else {
                response.writeHead(200, {'Content-Type': 'text/html'});
                response.end(data);
            }
        });
    }
});

router.post('/',
    passport.authenticate('local', {
        successRedirect: '/',
        failureRedirect: '/login'
    })
);

/*
//sing up 
router.post('/reg', function(request, response) {
    hasher({password:request.body.pw}, function(error, pass, salt, hash) {
        let user = {
            id: request.body.id,
            password: hash,
            salt: salt
        };
        let sql = 'insert into login set ?';
        conn.query(sql, user, function(error) {
            if (error) {
                console.log(error);
            } else {
                fs.readFile('./Web_Page.html', function(error, data) {
                    if(error) {
                        console.log(error);
                    } else {
                        response.writeHead(200, {'Content-Type': 'text/html'});
                        response.end(data);
                    }
                });
            }
        });
    });
});
*/

passport.use(new LocalStrategy({
        usernameField: 'id',
        passwordField: 'pw'
    },
    function (username, password, done) {
        let sql = 'select * from login where id=?';
        conn.query(sql, [username], function(error, results) {
            if (error) {
                console.log(error);
            } else {
                if(results.length == 0) 
                    return done('there is no user');
                let user = results[0];
                return hasher({password:password, salt:user.salt}, function(err, pass, salt, hash) {
                    if (hash == user.password) {
                        let today = new Date();
                        console.log(username + ": login success" + "[" + today.getFullYear()  + "/" + (today.getMonth() + 1) + "/" + today.getDate() + " " + today.getHours() + ":" + today.getMinutes() + ":" + today.getSeconds() + "." + today.getMilliseconds() + "]");
                        return done(null, user);
                    } else {
                        return done(null, false);
                    }
                });
            }
        });
    }
));

passport.serializeUser(function(user, done) {
    done(null, user.id);
});

passport.deserializeUser(function(id, done) {
    let sql = 'select * from login where id=?';
    conn.query(sql, [id], function(error, results) {
        if (error) {
            console.log(error);
        } else {
            done(null, results[0]);
        }
    });
});

module.exports = router;