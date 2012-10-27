
#include "socketapp.h"
#include "registrar.h"
#include "uip.h"
#include "debug.h"

unsigned char registrar_ip[]    = {192,168,1,112};	// IP address of WiShield
short registrar_port = 8001;
int post_count = 0;
/*---------------------------------------------------------------------------*/
unsigned char connect_to_registrar() {
   uip_ipaddr_t ipaddr;
   uip_ipaddr(&ipaddr, registrar_ip[0], registrar_ip[1], registrar_ip[2], registrar_ip[3]);
   //connect to the registrar
   struct uip_conn *conn = uip_connect(&ipaddr, htons(registrar_port));
   post_count = 0;
   if (conn != NULL) {
     debug("Connected to registrar");
     return 1;
   } else {
     return 0;
   }
}

void post_to_registrar() {

  debug("Posting to registrar");
  post_count++;
  char *msg = "POST /ardi HTTP/1.1\nUser-Agent: WiShield\nHost: 192.168.1.112:8001\nAccept: */*\nContent-Length: 14\nContent-Type: application/x-www-form-urlencoded\n\nip=192.168.1.2\n";
  debug(msg);
  uip_send(msg, strlen(msg));
}

int get_post_count() {
  return post_count;
}
void close_registrar() {
   uip_close();
}
/*---------------------------------------------------------------------------*/

