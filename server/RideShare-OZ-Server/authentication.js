var request = require('request');
var auth = {};

auth.auth_token = function (token, callback) {

  var url = 'https://www.googleapis.com/oauth2/v3/tokeninfo?id_token=' + token;

  console.log(url);
  request(url, function (error, response, body) {
    if (!error && response.statusCode == 200) {
      /* console.log("===body====="); */
      /* console.log(JSON.parse(body)); */
      callback(JSON.parse(body));
    } else {
      callback({"error_description": "Sorry, there is a problem on the server."});
    }
  });

  /* return result; */
}

module.exports = auth;
