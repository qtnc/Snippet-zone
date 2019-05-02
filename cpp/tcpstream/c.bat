@echo off
rem g++ -DUNICODE -std=gnu++17 *.cpp -lws2_32 -lcrypt32 -lsecur32 -luser32 -lkernel32
g++ -DUNICODE -std=gnu++17 *.cpp -lws2_32 -lkernel32 -luser32 -ltls-15 -lcrypto-41
