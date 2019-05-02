@echo off
g++ -O3 -s -std=gnu++17 -DRELEASE bass++.cpp -shared -o bass++.dll -Wl,--out-implib,libbass++.a -Wl,--enable-stdcall-fixup -L. -lbass -lbass_fx -lbassmidi -lbassmix
