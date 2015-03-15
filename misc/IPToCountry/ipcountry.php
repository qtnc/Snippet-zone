<?php
require_once('kernel.php');
ob_start();
$countries = array();
echo <<<END
drop table if exists gsCountries;
drop table if exists gsIPCountries;
create table gsCountries (
code char(2) not null,
code3 char(3) not null,
name varchar(123),
primary key(code));
create table gsIPCountries (
id int unsigned not null auto_increment,
ipFrom int unsigned not null,
ipTo int unsigned not null,
code char(2) not null,
primary key(id),
index ipidx (ipFrom,ipTo));

insert into gsIPCountries (code, ipFrom, ipTo) values
END;
$fd = fopen('private/IpToCountry.csv', 'r');
if (!$fd) die('Failed to read CSV file');
$count = 0;
while ($line = fgets($fd, 4096)) {
if (!$line || strlen($line)<=0 || substr($line,0,1)=='#' || substr($line,0,1)==';') continue;
preg_match(<<<END
@^"(?'from'\d+)","(?'to'\d+)","(?'authority'\w+)","(?'date'\d+)","(?'countryCode'\w{2})","(?'countryCode3'\w{3})","(?'country'[^"]+)"@
END
, $line, $t);
if (!$t) continue;
$from = $t['from'];
$to = $t['to'];
$code = trim($t['countryCode']);
$code3 = trim($t['countryCode3']);
$name = trim($t['country']);
if ($count++>=1) echo ",\r\n";
echo "('$code', $from, $to)";
$countries[$code] = array($code, $code3, $name);
}
fclose($fd);
echo <<<END
;
insert into gsCountries (code, code3, name) values
END;
$count=0;
foreach ($countries as $z) {
list($code, $code3, $name) = $z;
if ($count++>=1) echo ",\r\n";
echo "('$code', '$code3', \"$name\")";
}
echo ';';
$sql = ob_get_contents();
ob_end_clean();
file_put_contents('ipcountry.sql', $sql);
echo 'ok ', date('d.m.Y H:i:s');
?>