var http = require('http'),
    url = require('url'),
    qs = require('querystring');

var ips = {};

var setRegIp = function(req, res){
  console.log(req.connection.remoteAddress);
  var lurl = url.parse(req.url),
      key = lurl.pathname,
      val = req.connection.remoteAddress;
  req.on('data', function(chunk){
    console.log(chunk.toString());
    var data = qs.parse(chunk.toString());
    if (data.ip !== null){
      val = data.ip;
    }
  });
  req.on("end", function(){
    ips[key] = val;
    res.writeHead(200);
    console.log(val);
    res.end(val);
  });
};

var getRegIp = function(req, res){
    lurl = url.parse(req.url);
    if (ips[lurl.pathname]){
      res.writeHead(200);
      res.end(ips[lurl.pathname]);
      return;
    }
    res.writeHead(404);
    res.end('path not found');
};

http.createServer(function(req, res){
  if (req.method.toLowerCase() === "post"){
    return setRegIp(req, res);
  }
  getRegIp(req, res);
}).listen(8001);
