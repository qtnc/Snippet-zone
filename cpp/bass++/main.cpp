#include "bass.h"
#include "bass_fx.h"
#include "bass++.hpp"
#include<string>
#include<iostream>
#include<cmath>
using namespace std;

bool errorcb (const BASS::Exception& e) {
cout << "BASS ERROR: " << e.GetErrorMessage() << endl;
return true;
}

int main (int argc, char** argv) {
BASS::SetErrorReportCallback(errorcb);
BASS::SetConfig(BASS::CONFIG_DEV_DEFAULT, true);
BASS::SetConfig(BASS::CONFIG_FLOATDSP, true);
BASS::Init();
cout << "List of devices: " << endl;
BASS::DeviceInfo di;
for (int i=1; BASS::GetDeviceInfo(i, di); i++) {
cout << i << ". " << di.name << boolalpha 
<< ", enabled=" << (bool)(di.flags&BASS::DEVICE_ENABLED)
<< ", default=" << (bool)(di.flags&BASS::DEVICE_DEFAULT)
<< ", init=" << (bool)(di.flags&BASS::DEVICE_INIT)
<< endl;
}
auto src = BASS::Stream::CreateFile("C:\\Temp\\loser4h.ogg", BASS::LOOP | BASS::DECODE);
//auto src = BASS::Stream::CreateFile("C:\\Temp\\Remix\\iPhone - Ringtone (MetroGnome Remix).mp3", BASS::LOOP | BASS::PRESCAN | BASS::DECODE);
//auto src = BASS::MusicStream::CreateFile("..\\6player\\Warez_Down - TopStyle 5.xx crk.it", BASS::LOOP | BASS::PRESCAN | BASS::DECODE);
auto stream = BASS::TempoStream(src, BASS::LOOP | BASS::AUTOFREE | BASS::FREESOURCE);
stream.SetVolume(0.4);
stream.SetRate(1.1);
stream.Play();
stream.SetSync(BASS::SYNC_POS, 400000, [&](auto sync, auto ch, auto data){ cout << "Sync!" << data << endl; });
cout << stream.GetLength() << endl;
cout << stream.GetDevice() << endl;

auto sample = BASS::Sample::CreateFile("..\\SalonClient\\Sounds\\quack.ogg");
sample.CreateChannel().Play();
cout << sample.GetAllChannels().size() << endl;

string s0;
getline(cin, s0);
cout << stream.GetPosition() << endl;


auto ch = sample.CreateChannel();
ch.SetRate(1.2);
ch.Play();
auto r = stream.SetFX<BASS_DX8_I3DL2REVERB>(BASS_FX_DX8_I3DL2REVERB);
r.lRoom = -1000;
r.lRoomHF = -100;
r.flDecayTime = 3;
r.flDiffusion = 100;
r.Push();
getline(cin, s0);
r.flDiffusion = 0;
r.Push();
cout << sample.GetAllChannels().size() << endl;

getline(cin, s0);
BASS::Free();
return 0;
}

