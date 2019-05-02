@echo off
rem g++ -shared -DUNICODE -O3 -s -std=gnu++17 *.cpp -o netstream.dll -Wl,--out-implib,libnetstream.a -lws2_32 -lcrypt32 -lsecur32 -luser32 -lkernel32
g++ -shared -DUNICODE -s -O3 -std=gnu++17 *.cpp -o netstream.dll -Wl,--out-implib,libnetstream.a -lws2_32 -lkernel32 -luser32 -ltls-15 -lcrypto-41