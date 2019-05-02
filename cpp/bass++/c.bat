@echo off
g++ -g -std=gnu++17 -DDEBUG *.cpp -shared -o bass++.dll -Wl,--out-implib,libbass++.a -Wl,--enable-stdcall-fixup -L. -lbass -lbass_fx -lbassmidi -lbassmix
g++ -g -std=gnu++17 main.cpp -L. -lbass++