let mysql = require('mysql');

const RDBHOST = 'localhost';
const RDBMYSQLID = 'id';
const RDBMYSQLPW = 'password';
const RDBNAME = 'database';
const RDBPORT = 3306;

let readConn = mysql.createConnection({
    host: RDBHOST,
    user: RDBMYSQLID,
    password: RDBMYSQLPW,
    database: RDBNAME,
    port: RDBPORT,
    multipleStatements: true
});

function handleRDBDisconnect() {
    readConn.connect(function(err) {
        if(err) {
            console.log('error when connecting to db:', err);
            setTimeout(handleRDBDisconnect, 2000);
        }
    });

    readConn.on('error', function(err) {
        console.log('RDB error', err);
        if(err.code === 'PROTOCOL_CONNECTION_LOST') {
            return handleRDBDisconnect();
        } else {
            throw err;
        }
    });
}

handleRDBDisconnect();

module.exports = {
    readConn
}