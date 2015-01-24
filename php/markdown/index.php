<?php
require_once('markdown.php');
$txt = (<<<'END'
Du code !
``cpp
#include<stdio.h>
int main (int argc, char** argv) {
printf("Hello, world !$3$");
return 0;
}
``

ET un peu de maths: 
$$\frac{1}{x^2}$$

Voici deux tableaux :

+-+-+-+
| 1 | 2 | 3 |
+-+-+-+
| 4 | 5 | 6 |
+-+-+-+
| 7 | 8 | 9 |
+-+-+-+

1 | 2 | 3
4 | 5 | 6
7 | 8 | 9
END
);//
$mk = new Markdown();
$mk->registerInlineType(function($m){ return 'OK'; }, '_', null, true);
$txt = $mk->toHTML($txt);
//header('Content-Type: text/html; charset=utf-8');
echo '<html><head><title>Test markdown</title></head><body><h1>Test markdown</h1>';
echo $txt;
echo '<br /><br />', nl2br(htmlspecialchars($txt));
echo '</body<</html>';
?>