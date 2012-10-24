
#ifndef _WRECKROLL_H
#define _WRECKROLL_H

typedef void (*handler_callback)(char *);

#ifdef __cplusplus
 extern "C" {
#endif

void set_command_handler(handler_callback handler);

#ifdef __cplusplus
 }
#endif

#endif //_WRECKROLL_H
