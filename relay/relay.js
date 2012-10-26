var http = require("http"),
    REGISTRAR_HOST = 'localhost',
    REGISTRAR_ADDRESS = "http://"+ REGISTRAR_HOST +":8001";

var tomer = {
  forward:"f",
  reverse:"v",
  left:"l",
  right:"r",
  stop:"s",
  gun:"g",
  smoke:"m",
  canopy:"e"
};

var ardi = null;

var setrelay = function(){
  var opt = {
    host: REGISTRAR_HOST,
    port: 8001,
    method: "POST",
    path: "/relay"
  };
  console.log('setting request');
  var setreq = http.request(opt, function(res){
    return 5;
  });
  console.log('sending request');
  setreq.end();
};

var getardi = function(){
  var ardreq = http.get(REGISTRAR_ADDRESS + "/ardi", function(res){
    res.setEncoding('utf8');
    res.on('data', function(chunk){
      if (res.statusCode === 200){
        ardi = chunk.toString();
        console.log(ardi);
      }
    });
  });
  ardreq.end();
};

var sendcommand = function(com){
  var mtos = "s";
  if (ardi){
    console.log('sending command',  com);
    if (tomer[com.toLowerCase()]){
      var opts = {
        port: 9000,
        host: ardi
      };
      var client = net.connect(opts, function(){
        client.write(tomer[com.toLowerCase()] + "\n");
        client.end();
      });
    }
  } else {
    console.log('sending command', com);
  }
};

setrelay();
//getardi();

http.createServer(function(req, res){
  console.log('connection received');
  req.on('data', function(chunk){
    try {
      console.log(chunk.toString());
      chdata = JSON.parse(chunk.toString());
      if (chdata && chdata.command){
        sendcommand(chdata.command);
        console.log("yes");
        res.writeHead(200);
        res.end(chdata.command);
      }
    } catch (e){
      console.log("ERROR",chunk.toString());
      console.log(e);
      res.writeHead(500);
      res.end();
    }
  });
}).listen(6696);
