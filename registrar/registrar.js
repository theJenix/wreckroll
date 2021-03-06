var http = require('http'),
    url = require('url'),
    qs = require('querystring');

var ips = {};

var setRegIp = function(req, res){
  console.log('received POST from:', req.connection.remoteAddress);
  var lurl = url.parse(req.url),
      key = lurl.pathname,
      val = req.connection.remoteAddress;
  req.on('data', function(chunk){
    console.log('  received POST data:', chunk.toString());
    var data = qs.parse(chunk.toString());
    if (data.ip !== null){
      val = data.ip;
    }
  });
  req.on("end", function(){
    ips[key] = val;
    res.writeHead(200);
    console.log("  set", key, "as", val);
    res.end(val);
  });
};

var getRegIp = function(req, res){
    lurl = url.parse(req.url);
    console.log("received request for:", lurl.pathname);
    if (ips[lurl.pathname]){
      console.log("  returning value:", ips[lurl.pathname]);
      res.writeHead(200);
      res.end(ips[lurl.pathname]);
      return;
    }
    console.log("  request path not in memory");
    res.writeHead(404);
    res.end('path not found');
};

http.createServer(function(req, res){
  console.log('new connection');
  if (req.method.toLowerCase() === "post"){
    console.log('its a post');
    return setRegIp(req, res);
  }
  getRegIp(req, res);
}).listen(8001);
console.log("registrar server started");
