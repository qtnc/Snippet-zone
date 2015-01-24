#include <windows.h>
// CPU Usage calculation
// Taken and adapted from : http://www.philosophicalgeek.com/2009/01/03/determine-cpu-usage-of-current-process-c-and-c/
#define GetTickCount64 GetTickCount
BOOL __declspec(dllimport) __stdcall GetSystemTimes (LPFILETIME, LPFILETIME, LPFILETIME ) ;


FILETIME m_ftPrevSysKernel;
FILETIME m_ftPrevSysUser;
FILETIME m_ftPrevProcKernel;
FILETIME m_ftPrevProcUser;
float m_nCpuUsage = -1;
ULONGLONG m_dwLastRun = 0;
CRITICAL_SECTION m_cs;
inline CPU_Initialize () {
ZeroMemory(&m_ftPrevSysKernel, sizeof(FILETIME));
ZeroMemory(&m_ftPrevSysUser, sizeof(FILETIME));
ZeroMemory(&m_ftPrevProcKernel, sizeof(FILETIME));
ZeroMemory(&m_ftPrevProcUser, sizeof(FILETIME));
InitializeCriticalSection(&m_cs);
}
inline void CPU_Release () {
DeleteCriticalSection(&m_cs);
}
inline BOOL CPU_IsFirstRun() { return (m_dwLastRun == 0); }
inline ULONGLONG SubtractTimes(const FILETIME* ftA, const FILETIME* ftB) {
LARGE_INTEGER a, b;
a.LowPart = ftA->dwLowDateTime;
a.HighPart = ftA->dwHighDateTime;

b.LowPart = ftB->dwLowDateTime;
b.HighPart = ftB->dwHighDateTime;

return a.QuadPart - b.QuadPart;
}
inline BOOL CPU_EnoughTimePassed () {
const int minElapsedMS = 250;//milliseconds
ULONGLONG dwCurrentTickCount = GetTickCount64();

return (dwCurrentTickCount - m_dwLastRun) > minElapsedMS;
}
float getCPUUsage () {
if (CPU_IsFirstRun()) CPU_Initialize();
if (!TryEnterCriticalSection(&m_cs)) return m_nCpuUsage;
if (!CPU_EnoughTimePassed()) {
LeaveCriticalSection(&m_cs);
return m_nCpuUsage;
}
FILETIME ftSysIdle, ftSysKernel, ftSysUser;
FILETIME ftProcCreation, ftProcExit, ftProcKernel, ftProcUser;
if (!GetSystemTimes(&ftSysIdle, &ftSysKernel, &ftSysUser) || !GetProcessTimes(GetCurrentProcess(), &ftProcCreation, &ftProcExit, &ftProcKernel, &ftProcUser)) {
LeaveCriticalSection(&m_cs);
return m_nCpuUsage;
}

if (!CPU_IsFirstRun()) {
ULONGLONG ftSysKernelDiff = SubtractTimes(&ftSysKernel, &m_ftPrevSysKernel);
ULONGLONG ftSysUserDiff = SubtractTimes(&ftSysUser, &m_ftPrevSysUser);
ULONGLONG ftProcKernelDiff = SubtractTimes(&ftProcKernel, &m_ftPrevProcKernel);
ULONGLONG ftProcUserDiff = SubtractTimes(&ftProcUser, &m_ftPrevProcUser);
ULONGLONG nTotalSys =  ftSysKernelDiff + ftSysUserDiff;
ULONGLONG nTotalProc = ftProcKernelDiff + ftProcUserDiff;
if (nTotalSys > 0) m_nCpuUsage = ((1.0 * nTotalProc) / nTotalSys);
}
m_ftPrevSysKernel = ftSysKernel;
m_ftPrevSysUser = ftSysUser;
m_ftPrevProcKernel = ftProcKernel;
m_ftPrevProcUser = ftProcUser;
m_dwLastRun = GetTickCount64();
LeaveCriticalSection(&m_cs);
return m_nCpuUsage;
}
