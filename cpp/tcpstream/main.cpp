#include "sslstream.hpp"
#include<string>
#include<iostream>
#include<string>
using namespace std;

int main (int argc, char** argv) {
cout << "Connecting..." << endl;
sslstream serv("localhost", 80);
if (!serv) {
cout << "Failed!" << serv.errcode() << endl;
return 1;
}
cout << "Sending request..." << endl;
serv << "GET / HTTP/1.1\r\nHost: qcsalon.net\r\n\r\n" << flush;
cout << "Reading response..." << endl;
string s;
int count = 0;
while(getline(serv, s)) {
cout << ++count << ". " << s << endl;
}
return 0;
}
