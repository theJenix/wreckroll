
#ifndef _WRECKROLL_H
#define _WRECKROLL_H

typedef int  (*socket_handler)();
typedef void (*handler_callback)(char *);

#define STATE_UNREGISTERED 0
#define STATE_REGISTERING 1
#define STATE_RUNNING 2

#ifdef __cplusplus
 extern "C" {
#endif

void add_socket_handler(socket_handler handler);

int get_run_state();

void register_me();

void set_command_handler(handler_callback handler);

#ifdef __cplusplus
 }
#endif

#endif //_WRECKROLL_H
