@echo off
del a.exe
rem del obj\*.o
windres res.rc -o obj\rc.o
for %%i in (*.c) do gcc %%i -o obj\%%~ni.o -w -std=gnu99 -c -g -DDEBUG
for %%i in (*.cpp) do g++ %%i -o obj\%%~ni.o -w -fpermissive -std=gnu++0x -c -g -DDEBUG
g++ obj\*.o -L. -lcomdlg32 -lbass -lws2_32 -liphlpapi -lcomctl32 -lgdi32 -lole32 -loleaut32 -loleacc -luuid -lUniversalSpeech -lluajit -g -o a.exe
