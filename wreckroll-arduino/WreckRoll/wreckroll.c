
/******************************************************************************

  Filename:		socketapp.c
  Description:	Simple socket programming example for the WiShield 1.0

 ******************************************************************************

  TCP/IP stack and driver for the WiShield 1.0 wireless devices

  Copyright(c) 2009 Async Labs Inc. All rights reserved.

  This program is free software; you can redistribute it and/or modify it
  under the terms of version 2 of the GNU General Public License as
  published by the Free Software Foundation.

  This program is distributed in the hope that it will be useful, but WITHOUT
  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
  more details.

  You should have received a copy of the GNU General Public License along with
  this program; if not, write to the Free Software Foundation, Inc., 59
  Temple Place - Suite 330, Boston, MA  02111-1307, USA.

  Contact Information:
  <asynclabs@asynclabs.com>

   Author               Date        Comment
  ---------------------------------------------------------------
   AsyncLabs			06/06/2009	Initial version

 *****************************************************************************/

/*
 * This is a short example of how to write uIP applications using
 * protosockets.
 */

/*
 * We define the application state (struct socket_app_state) in the
 * socketapp.h file, so we need to include it here. We also include
 * uip.h (since this cannot be included in socketapp.h) and
 * <string.h>, since we use the memcpy() function in the code.
 */
#include "socketapp.h"
#include "uip.h"
#include <string.h>
#include "wreckroll.h"
#include "registrar.h"
#include "debug.h"

extern unsigned short port;

handler_callback handler;

int run_state = STATE_UNREGISTERED;
/*
 * Declaration of the protosocket function that handles the connection
 * (defined at the end of the code).
 */
int handle_connection(struct socket_app_state *s);
unsigned char check_if_registrar();

socket_handler s_handler = check_if_registrar;

/*---------------------------------------------------------------------------*/

void add_socket_handler(socket_handler s) {
  s_handler = s;
  
}
void set_command_handler(handler_callback h) {
 handler = h;
}

int get_run_state() {
  return run_state;
}

void register_me() {
  run_state = STATE_REGISTERING;
  connect_to_registrar();
}

void finished_register() {
  run_state = STATE_RUNNING;
  close_registrar();
}
/*---------------------------------------------------------------------------*/
/*
 * The initialization function. We must explicitly call this function
 * from the system initialization code, some time after uip_init() is
 * called.
 */
void socket_app_init(void)
{
  debug("in socket_app_init");

  /* We start to listen for connections on TCP port 1000. */
  uip_listen(HTONS(port));
}
/*---------------------------------------------------------------------------*/
/*
 * In socketapp.h we have defined the UIP_APPCALL macro to
 * socket_app_appcall so that this function is uIP's application
 * function. This function is called whenever an uIP event occurs
 * (e.g. when a new connection is established, new data arrives, sent
 * data is acknowledged, data needs to be retransmitted, etc.).
 */
void socket_app_appcall(void)
{
 // debug("in socket_app_appcall");
  
  //check to see if we're working with the registrar
  unsigned char handled = s_handler != NULL ? s_handler() : 0;
  
  //if not, we're working with a client...do that shtuff here
  if (!handled) {
    
    /*
     * The uip_conn structure has a field called "appstate" that holds
     * the application state of the connection. We make a pointer to
     * this to access it easier.
     */
    struct socket_app_state *s = &(uip_conn->appstate);
  
    /*
     * If a new connection was just established, we should initialize
     * the protosocket in our applications' state structure.
     */
    if(uip_connected()) {
      debug("Establishing connection");
      PSOCK_INIT(&s->p, s->inputbuffer, sizeof(s->inputbuffer));
    }
  
    /*
     * Finally, we run the protosocket function that actually handles
     * the communication. We pass it a pointer to the application state
     * of the current connection.
     */
     debug("Handling connection");
     handle_connection(s);
  }
}
/*---------------------------------------------------------------------------*/
/*
 * This is the protosocket function that handles the communication. A
 * protosocket function must always return an int, but must never
 * explicitly return - all return statements are hidden in the PSOCK
 * macros.
 */
int handle_connection(struct socket_app_state *s)
{
  PSOCK_BEGIN(&s->p);

  PSOCK_READTO(&s->p, '\n');
  handler(s->inputbuffer);
  memset(s->inputbuffer, 0x00, sizeof(s->inputbuffer));

  PSOCK_END(&s->p);
}

/*---------------------------------------------------------------------------*/

unsigned char check_if_registrar() {
  unsigned char retval = 0;
  if (run_state == STATE_REGISTERING) {
    retval = 1;
    
//      if(uip_aborted()) {
//    debug("Connection aborted...something's wrong");
//    return;
//  }

//char buf[32];
//sprintf(buf, "%X", uip_flags);
//debug(buf);
//  if (uip_acked()) {
//    return; //nothing to do
//  }
  

    if (uip_aborted()) {
      connect_to_registrar();
    } else if (uip_closed()) {
    } else if (uip_newdata()) {
      if (get_post_count() > 0) {
        finished_register();
      }
    } else if (uip_acked()) {
    } else if (uip_poll()) {
      if (get_post_count() < 3) {
        post_to_registrar();
      }
    } 
  }
  return retval;
}
