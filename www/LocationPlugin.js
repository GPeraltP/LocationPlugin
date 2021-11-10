var exec = require('cordova/exec');

exports.enableGPS = function (arg0, success, error) {
    exec(success, error, 'LocationPlugin', 'enableGPS', [arg0]);
};
