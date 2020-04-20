unquoted:SEPARATOR|LINE_FEED|TEXT:unquoted
unquoted:BEGIN_QUOTE:quoted

quoted:QUOTED_TEXT:quoted
quoted:END_QUOTE:unquoted

BEGIN_QUOTE= *"
QUOTED_TEXT=([^"]|"")*
END_QUOTE="(?!") *
SEPARATOR=,
TEXT=[^",\n]+
LINE_FEED=\n