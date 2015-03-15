JFWRunScript(script){
DllCall("jfwapi.dll\JFWRunScript", "str", script)
}

#IfWinActive ahk_class ConsoleWindowClass
^v::Send !{Space}mo
^+c::Send !{Space}mt!{Space}mc
PgDn::JFWRunScript("QC_CMD_PAGEDOWN()")
PgUp::JFWRunScript("QC_CMD_PAGEUP()")
^PgDn::JFWRunScript("QC_CMD_CTRLPAGEDOWN()")
^PgUp::JFWRunScript("QC_CMD_CTRLPAGEUP()")

