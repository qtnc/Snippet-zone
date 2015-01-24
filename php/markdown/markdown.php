<?php

class Markdown {
public $referenceHeadingLevel = 2;
public $safe = false;
public $parseTables = true;
private $blocks = array();
private $inlines = array();

public function __construct () {
$this->registerBlockType('``', 'parseLongCode');
$this->registerBlockType('$$', 'parseLongMath');
$this->registerInlineType('`', 'parseShortCode', false);
$this->registerInlineType('$', 'parseShortMath', false);
$this->registerInlineType('**', '<em>$1</em>');
$this->registerInlineType('*', '<strong>$1</strong>');
$this->registerInlineType('--', '<del>$1</del>');
}

public function registerBlockType ($start, $func, $end=null) {
if (!$end) $end = strrev(strtr($start, '([{<', ')]}>'));
$start = preg_quote($start);
$end = preg_quote($end);
$o = new stdClass();
$o->start = $start;
$o->end = $end;
$o->func = null;
$this->findFunc($o,$func);
$this->blocks[] = $o;
}

public function registerInlineType ($start, $func, $wordaware=true, $end=null) {
if (is_object($start) || is_array($start)) { $tmp=$start; $start=$func; $func=$tmp; }
if (!$end) $end = strrev(strtr($start, '([{<', ')]}>'));
$start = preg_quote($start);
$end = preg_quote($end);
$o = new stdClass();
$o->start = $start;
$o->end = $end;
$o->wordaware = $wordaware;
$o->func = null;
$this->findFunc($o,$func);
$this->inlines[] = $o;
}

private function findFunc ($o, $f) {
if (is_string($f) && strlen($f)>0 && $f[0]=='<') { $o->repl=$f; return; }
else if (is_string($f) && method_exists($this,$f)) $f = array($this,$f);
else if (is_array($f) && count($f)==2 && $f[0]==null) $f[0]=$this;
if (is_callable($f)) $o->func = $f;
else $o->repl = $f;
}

private function escapeChar ($ch) {
return ''.ord($ch[1]).'';
}

private function unescapeChar ($ch) {
return chr($ch[1]);
}

public function escape ($str) {
return preg_replace_callback('([][{}()<>	+*!?%@"\'&#|$^~\\/`_-])', array($this,'escapeChar'), $str);
}

public function unescape ($str) {
return preg_replace_callback('/(\d+)/', array($this,'unescapeChar'), $str);
}

protected function parseUnorderedList  ($m) {
$str = $m[0];
$spc = strspn($str, ' ');
if ($spc>0) $str = preg_replace('/^'.str_repeat(' ', $spc).'/m', '', $str);
$str = preg_replace_callback( '/(?:^ +[-+*] .+(?:\n +.+)*(?:\n|\Z)){2,}/m', array($this,'parseUnorderedList'), $str);
$str = preg_replace( '/^[-+*] ([^\n]+)(?:\n|\Z)/m', '<li>$1</li>', $str);
return "</p><ul>$str</ul><p>\n";
}

protected function parseOrderedList ($m) {
$str = $m[0];
$spc = strspn($str, ' ');
if ($spc>0) $str = preg_replace('/^'.str_repeat(' ', $spc).'/m', '', $str);
$num = substr($str, 0, strcspn($str, ' .)-'));
$str = preg_replace_callback( '/(?:^ +(?:\d+|[a-zA-Z]|[IiVvXx]{2,4}) ?[-.)] .+(?:\n +.+)*(?:\n|\Z)){2,}/m', array($this,'parseOrderedList'), $str);
$str = preg_replace( '/^(?:\d+|[a-zA-Z]|[IiVvXx]{2,4}) ?[-.)] ([^\n]+)(?:\n|\Z)/m', '<li>$1</li>', $str);
if (($start=floor($num))>0 || $num=='0') $type = '1';
else if (strlen($num)==1 && ($start=strpos('$abcdefgh$jklmnopqrstuvwxyz', $num))!==false) $type = 'a';
else if (strlen($num)==1 && ($start=strpos('$ABCDEFGH$JKLMNOPQRSTUVWXYZ', $num))!==false) $type = 'A';
else if (strspn($num, 'IiVvXx')>0) {
$type = ctype_lower($num)? 'i' : 'I';
$start = array_search(strtolower($num), array('$', 'i', 'ii', 'iii', 'iv', 'v', 'vi', 'vii', 'viii', 'ix', 'x', 'xi', 'xii', 'xiii', 'xiv', 'xv', 'xvi', 'xvii', 'xviii', 'xix', 'xx'));
}
else $type=$start=1;
return "</p><ol type=\"$type\" start=\"$start\">$str</ol><p>\n";
}

protected function parseQuote ($m) {
$str = $m[0];
$str = preg_replace( '/^&gt;\s?/m', '', $str);
$str = preg_replace_callback( '/^&gt; [^\n]+(?:(?:\n\n&gt; |\n)[^\n]+)*/m', array($this,'parseQuote'), $str);
return "</p><blockquote><p>\n$str\n</p></blockquote><p>";
}

protected function parseHeading ($m) {
$hlvl = $this->referenceHeadingLevel;
$num = strlen($m[1]) + $hlvl -1;
$str = trim($m[2]);
return "</p><h$num role=\"heading\" aria-level=\"$num\">$str</h$num><p>";
}

protected function parseLongCode ($m) {
$cls = ($m[1]? " class=\"$m[1]\"" :'');
$code = $this->escape($m[2]);
return "</p><pre><code$cls>$code</code></pre><p>";
}

protected function parseShortCode ($m) {
$code = $this->escape($m[1]);
return "<code>$code</code>";
}

protected function parseTableWithBars ($m) {
$m[0] = preg_replace('/^\s*\|/m', '', $m[0]);
$m[0] = preg_replace('/\|\s*$/m', '', $m[0]);
$m[0] = trim(preg_replace('/[-+|]+(?:\n|\Z)/m', '', $m[0]));
return $this->parseTable($m, '|');
}

protected function parseTableWithTabs ($m) {
return $this->parseTable($m, "\t");
}

protected function parseTable ($m, $ch) {
$rows = array();
foreach(explode("\n", $m[0]) as $row) {
$row = '<td>'. str_replace($ch, '</td><td>', $row). '</td>';
$row = preg_replace('#<td>\s*=+\s*(.*?)\s*=*\s*</td>#s', '<th>$1</th>', $row);
$row = preg_replace('#(?<=<t[hd]>)\s*(.*?)\s*(?=</t[hd]>)#s', '$1', $row);
$rows[] = $row;
}
return '</p><table><tr>'. implode('</tr><tr>', $rows). '</tr></table><p>';
}

protected function parseShortMath ($m) {
$escaped = $this->escape($m[1]);
return "<img src=\"/cgi-bin/mimetex.cgi?$escaped\" alt=\"$escaped\" />";
}

protected function parseLongMath ($m) {
$escaped = $this->escape(trim(substr($m[0], 2, -2)));
return "</p><p><img src=\"/cgi-bin/mimetex.cgi?$escaped\" alt=\"$escaped\" /></p><p>";
}

public function toHTML ($str) {
$hlvl = $this->referenceHeadingLevel;
$str = htmlspecialchars($str);
$str = str_replace("\r\n", "\n", $str);
//$str = preg_replace_callback('\\\\([][{}()<>	+*?!%@$^~&#\\/|`"\'_-])', array($this,'escapeChar'), $str);
$str = preg_replace_callback( '/^&gt; [^\n]+(?:(?:\n\n&gt; |\n)[^\n]+)*/m', array($this,'parseQuote'), $str);
foreach ($this->blocks as $b) {
$regex = "^{$b->start}([^\r\n]*)\n?(.*?){$b->end}ms";
if ($b->func) $str = preg_replace_callback($regex, $b->func, $str);
else $str = preg_replace($regex, $b->repl, $str);
}
if ($this->parseTables) $str = preg_replace_callback('/(?:^[-+|]+\n)?^\|?[^|\n]+\|[^\n]+(?:\n(?:[-+|]+\n)?\|?[^|\n]+\|[^\n]+)+(?:\n[-+|]+)?(?=\n|\Z)/m', array($this,'parseTableWithBars'), $str);
if ($this->parseTables)  $str = preg_replace_callback('/^[^\t\n]+\t[^\n]+(?:\n[^\t\n]+\t[^\n]+)+(?=\n|\Z)/m', array($this,'parseTableWithTabs'), $str);
if ($hlvl>0) $str = preg_replace_callback( '/^(=+)([^\r\n]+)/m', array($this,'parseHeading'), $str);
$str = preg_replace_callback( '/(?:^[-+*] .+(?:\n.+)*(?:\n|\Z)){2,}/m', array($this,'parseUnorderedList'), $str);
$str = preg_replace_callback( '/(?:^(?:\d+|[a-zA-Z]|[IiVvXx]{2,4}) ?[-.)] .+(?:\n.+)*(?:\n|\Z)){2,}/m', array($this,'parseOrderedList'), $str);
foreach ($this->inlines as $i) {
if ($i->wordaware) $regex = "{$i->start}(?=[\pL\pN])(.*?)(?<=[\pL\pN]){$i->end}su";
else $regex = "{$i->start}(.*?){$i->end}s";
if ($i->func) $str = preg_replace_callback($regex, $i->func, $str);
else $str = preg_replace($regex, $i->repl, $str);
}
$str = preg_replace('/\[(\d+)\]\s?:/', '91<a id="fn$1" href="#fnref$1">$1</a>93:', $str);
$str = preg_replace('/\[(\d+)\]/', '<sub><a id="fnref$1" href="#fn$1">$1</a></sub>', $str);
$str = preg_replace( '@!&quot;(.*?)&quot; \((.*?)\)@', '<img src="$2" alt="$1" />', $str);
if (!$this->safe) {
$str = preg_replace( '@&quot;(.*?)&quot; \((.*?)\)@', '<a href="$2">$1</a>', $str);
$str = preg_replace('@(?<!")(https?://[^\r\n\t "<>]+)@', '<a href="$1">$1</a>', $str);
} else {
$str = preg_replace( '@&quot;(.*?)&quot; \((.*?)\)@', '<a href="$2" rel="nofollow">$1</a>', $str);
$str = preg_replace('@(?<!")(https?://[^\r\n\t "<>]+)@', '<a href="$1" rel="nofollow">$1</a>', $str);
}
$str = $this->unescape($str);
$str = preg_replace('#</li>\s*</p>\s*(?=<[uo]l)#s', '', $str);
$str = preg_replace('#</([uo])l>\s*<p>\s*<li>#s', '</$1l></li><li>', $str);
$str = str_replace("\n", '<br />', $str);
$str = str_replace('<br /><br />', '</p><p>', $str);
$str = str_replace('<br /></p>', '</p>', $str);
$str = str_replace('<p><br />', '<p>', $str);
$str = "<p>$str</p>";
$str = preg_replace('@<p>\s*</p>@s', '', $str);
$str = preg_replace('#</li></p></blockquote><p></([uo])l>#s', '</li></$1l></blockquote>', $str);
return $str;
}

}
?>