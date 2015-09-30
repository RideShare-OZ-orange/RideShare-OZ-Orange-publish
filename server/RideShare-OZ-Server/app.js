var express = require('express'); var path = require('path');
var favicon = require('serve-favicon');
var logger = require('morgan');
var cookieParser = require('cookie-parser');
var bodyParser = require('body-parser');

// database
/* var mongo = require('mongodb'); */
var mongoose = require('mongoose');

mongoose.connect('mongodb://localhost/RideShare', function(err) {
  if(err) {
    console.log('database connection error', err);
  } else {
    console.log('database connection successful');
  }
});

var auth = require('./authentication.js');
var index = require('./routes/index');
var user = require('./routes/user');
var ride = require('./routes/ride');
var group = require('./routes/group');
var msg = require('./routes/msg');
var test = require('./routes/test');
var events = require('./routes/event');
var credit= require('./routes/credit');
// model
var User = require('./models/User.js');

var app = express();

// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'jade');

// uncomment after placing your favicon in /public
//app.use(favicon(path.join(__dirname, 'public', 'favicon.ico')));
app.use(logger('dev'));
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: false }));
app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'public')));

app.use(function(req,res,next){
  if (req.method === 'POST') {
    auth.auth_token(req.body.token, function(doc){
      if (req.body.username) {
        doc = {'email': req.body.username};
      }
      if (!doc.email) {
        res.end('Invalid Token');
      } else {
        req.userinfo = doc;
        User.find({'username':doc.email},function(err, users){
          console.log(users);
          if (users.length !== 0) {
            req.userinfo._id = users[0]._id;
          }
          next();
        });
      }
    });
  } else {
    next(); }
  // console.log("=========");
  // next();
});

app.use('/', index);
app.use('/user', user);
app.use('/ride', ride);
app.use('/group', group);
app.use('/msg', msg);
app.use('/test', test);
app.use('/event', events);
app.use('/credit', credit);

// catch 404 and forward to error handler
app.use(function(req, res, next) {
  var err = new Error('Not Found');
  err.status = 404;
  next(err);
});

// error handlers

// development error handler
// will print stacktrace
if (app.get('env') === 'development') {
  app.use(function(err, req, res, next) {
    res.status(err.status || 500);
    res.render('error', {
      message: err.message,
      error: err
    });
  });
}

// production error handler
// no stacktraces leaked to user
app.use(function(err, req, res, next) {
  res.status(err.status || 500);
  res.render('error', {
    message: err.message,
    error: {}
  });
});


module.exports = app;
