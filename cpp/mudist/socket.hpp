#ifndef _____SOCKET_HPP_8_____
#define _____SOCKET_HPP_8_____
#include<stdexcept>

struct Socket {
SOCKET sock;
CRITICAL_SECTION cs;

Socket(void);
~Socket();
int open(const char*, int) ;
int recv(void*, int) ;
int send (const void*, int) ;
void close (void) ;
inline operator bool () { return sock && sock!=SOCKET_ERROR; }

static bool initialize (void) ;
static void deinitialize (void) ;
};

struct socket_error: std::runtime_error {
socket_error (const char* what1) : runtime_error(what1) {}
};

#endif
