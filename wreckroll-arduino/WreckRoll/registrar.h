
#ifndef _REGISTRAR_H
#define _REGISTRAR_H

extern unsigned char registrar_ip[];
extern short registrar_port;
extern unsigned char local_ip[];

#ifdef __cplusplus
 extern "C" {
#endif

unsigned char connect_to_registrar();

void post_to_registrar();

void close_registrar();

int get_post_count();
void debug(char *msg);

#ifdef __cplusplus
 }
#endif

#endif //_REGISTRAR_H
