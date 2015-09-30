var express = require('express');
var router = express.Router();

router.get('/', function(req, res){
  // console.log(req.query);
  var str = "";
  for(var attr in req.query){
    str += attr + ": " + req.query[attr] + '\n';
  }
  console.log(str);

  res.end("GET request received\n --RideShare-OZ-Server\n" + str);
  // console.log(request.body.user.email);
});

router.post('/', function(req, res){
  var str = "";
  for(var attr in req.body){
    str += attr + ": " + req.body[attr] + '\n';
  }
  console.log(str);

  res.end("POST request received\n --RideShare-OZ-Server\n" + str);
      // console.log(request.body.user.email);
});

router.get('/json', function(req, res){
  console.log(req.query);
  res.end(JSON.stringify(req.query));
  // res.end(req.query);
});

router.post('/json', function(req, res){
  console.log(req.body);
  res.end(JSON.stringify(req.body));
});

module.exports = router;
