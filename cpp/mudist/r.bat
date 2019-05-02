@echo off
del obj\*.o
windres res.rc -o obj\rc.o
for %%i in (*.c) do gcc %%i -o obj\%%~ni.o -std=gnu99 -w -c -s -O3 -Os
for %%i in (*.cpp) do g++ %%i -o obj\%%~ni.o -w -fpermissive -std=gnu++0x -c -O3 -Os -s
g++ obj\*.o -s -O3 -Os -L. -lcomdlg32 -lbass -lws2_32 -liphlpapi -lcomctl32 -lgdi32 -lole32 -loleaut32 -loleacc -luuid -lUniversalSpeech -lluajit -mwindows -o mudist.exe
rem zip -9 -u -q -r salon230.zip qcgc.exe *.dll *.lng images\*.gfx sounds\*
rem "C:\Program Files\Inno Setup 5\ISCC.exe" setup-v2.iss
