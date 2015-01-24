<?php
$str = file_get_contents('MidiTextCompiler.java');
$str = preg_replace('/Pattern\.compile\("([^"]+)"\)/', 'getRegex("$1")', $str);
$str = preg_replace('/Pattern\.compile\("([^"]+)", ([^)])\)/', 'getRegex("$1", $2)', $str);
file_put_contents('MidiTextCompiler_2.java', $str);
?>