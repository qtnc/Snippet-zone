#include "sslbuf.hpp"
#ifdef USE_SCHANNEL
#include "ssl.h"
#else
#include<tls.h>
#endif
#include<cstring>
using namespace std;

#ifdef USE_SCHANNEL
bool sslstream::initialize (void) { return true; }
void sslstream::deinitialize () {}
#else
static tls_config* ctx = nullptr;

bool sslstream::initialize (void) {
if (ctx) return true;
if (
!tcpstream::initialize()
|| tls_init()
|| !(ctx = tls_config_new())
//|| tls_config_set_ca_mem(ctx, (uint8_t*)cert.data(), cert.size() )
) {
//Beep(1600, 100);
//printf("Config error ! %s\n", tls_config_error(ctx));
}
// As long as we don't know how to load default OS list of trusted certificates, we blindly accept everything by disabling all verifications
// This is of course totally unsafe, but for now we don't have any other possibility
// Please help doing it correctly
tls_config_insecure_noverifycert(ctx);
tls_config_insecure_noverifyname(ctx);
tls_config_insecure_noverifytime(ctx);
return !!ctx;
}

void sslstream::deinitialize (void) {
if (ctx) tls_config_free(ctx);
ctx=nullptr;
}
#endif

int sslstream::open (const char* host, uint16_t port) {
auto buf = new sslbuf();
if (!(errCode = buf->open(host, port))) {
rdbuf(buf);
clear();
}
else {
delete buf;
setstate(badbit);
}
return errCode;
}

int sslbuf::open (const char* host, uint16_t port) {
int result = tcpbuf::open(host, port);
if (result) return result;
#ifdef USE_SCHANNEL
ssl = new SSL_SOCKET(sock, 0);
return ssl->ClientInit();
#else
ssl = tls_client();
tls_configure(ssl, ctx);
int sslre = tls_connect_socket(ssl, sock, host);
//debug << "before handshake TLS error = " << sslre << ": " << protectnull(tls_error(ssl)) << endl; 
sslre = tls_handshake(ssl);
//debug << "after handshake TLS error = " << sslre << ": " << protectnull(tls_error(ssl)) << endl; 
//debug << "tls_peer_cert_provided = " << tls_peer_cert_provided(ssl) << endl;
//debug << "tls_conn_version = " << protectnull(tls_conn_version(ssl)) << endl;
//debug << "tls_conn_cipher = " << protectnull(tls_conn_cipher(ssl)) << endl;
//debug << "tls_peer_cert_subject = " << protectnull(tls_peer_cert_subject(ssl)) << endl;
//debug << "tls_peer_cert_issuer = " << protectnull(tls_peer_cert_issuer(ssl)) << endl;
//debug << "tls_peer_ocsp_cert_status = " << tls_peer_ocsp_cert_status(ssl) << endl;
//debug << "tls_peer_ocsp_crl_reason = " << tls_peer_ocsp_crl_reason(ssl) << endl;
//debug << "tls_peer_ocsp_response_status = " << tls_peer_ocsp_response_status(ssl) << endl;
//debug << "tls_peer_ocsp_result = " << protectnull(tls_peer_ocsp_result(ssl)) << endl;
//debug << "tls_peer_ocsp_url = " << protectnull(tls_peer_ocsp_url(ssl)) << endl;
const char* err = tls_error(ssl);
if (err&&strstr(err, "Broken pipe")) return 10061;
return sslre==-1? 59695 : sslre;
#endif
}

sslbuf::~sslbuf () {
close();
}

void sslbuf::close (void) {
if (ssl) {
#ifdef USE_SCHANNEL
ssl->ClientOff();
delete ssl;
#else
tls_close(ssl);
tls_free(ssl);
#endif
}
ssl=nullptr;
tcpbuf::close();
}

int sslbuf::send (const char* s, int len) {
if (!sock || !ssl) return -1;
int n=0, m=1;
while (sock && n<len && m>0 && m!=SOCKET_ERROR) {
#ifdef USE_SCHANNEL
m = ssl->s_ssend(const_cast<char*>(s)+n, len-n);
#else
m = tls_write(ssl, s+n, len-n);
#endif
if (m==SOCKET_ERROR) { 
close(); 
errCode = WSAGetLastError();
return n; 
}
n+=m;
}
return n;
}

int sslbuf::recv (char* buf, int len) {
if (!sock || !ssl || len<=0) return -1;
#ifdef USE_SCHANNEL
int n = ssl->s_recv(buf, len);
//cout << "Recv(" << len << ") => " << n << endl;
if (n<32) {
int r = ::recv(sock, NULL, 0, 0);
//cout << "SSLStream connection test: r=" << r << ", WSAGetLastError()=" << WSAGetLastError() << ", WSAECONNRESET=" << WSAECONNRESET << endl;
if(r==SOCKET_ERROR && WSAGetLastError() == WSAECONNRESET) {
errCode = WSAGetLastError();
return -1;
}}
#else
int n = tls_read(ssl, buf, len);
#endif
if (n<=0 || n==SOCKET_ERROR) {
errCode = WSAGetLastError();
return -1;
}
return n;
}


