if (process.argv.length > 2 && process.argv[2] == 'localhost'){
  console.log("running assuming registrar is on localhost, port 8001");
  var REGISTRAR_HOST = 'localhost';
} else {
  console.log("running assuming registrar is on 192.168.1.250, port 8001");
  var REGISTRAR_HOST = '192.168.1.250';
}
var http = require("http"),
    verified = false;
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
  var setreq = http.request(opt, function(res){
    return 5;
  });
  setreq.end();
  console.log('sent request to establish ip as /relay');
};

var getardi = function(){
  console.log("attempting to verify existence of '/ardi' on registrar");
  var ardreq = http.get(REGISTRAR_ADDRESS + "/ardi", function(res){
    res.setEncoding('utf8');
    res.on('data', function(chunk){
      if (res.statusCode === 200){
        ardi = chunk.toString();
        console.log("ardi ip is set:", ardi);
      } else {
        console.log("'/ardi' is not defined, retrying");
        setTimeout(getardi, 1000);
      }
    });
  });
  ardreq.end();
};

var sendcommand = function(com){
  var mtos = "s"; //message to send
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
    getardi();
    console.log('would send command', com);
  }
};

var verify = function(){
  console.log("attempting to verify existence of '/relay' on registrar");
  var vreq = http.get(REGISTRAR_ADDRESS + "/relay", function(res){
    res.setEncoding('utf8');
    res.on('data', function(chunk){
      if (res.statusCode === 200){
        console.log("'/relay' is defined!");
      } else {
        console.log("'/relay' is not defined, retrying");
        setrelay();
        setTimeout(verify, 1000);
      }
    });
  });
  vreq.end();
};

verify();
getardi();

http.createServer(function(req, res){
  console.log('connection received');
  req.on('data', function(chunk){
    try {
      chdata = JSON.parse(chunk.toString());
      if (chdata && chdata.command){
        sendcommand(chdata.command);
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
