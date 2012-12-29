#!/bin/bash

PDF="dp.pdf"
BEGIN_RE="Chapter"     # první relevantní řádek práce
END_RE="ISBN" # poslední relevantní řádek
coef=0.95  # kolik procent znaku je odhaden uzitecny text

#########################################################################

chars=$(pdftotext "$PDF" - | sed -n "/$BEGIN_RE/,/$END_RE/p" | grep -v '^[[:space:]]*.\?[[:space:]]*$' | grep -v '^[[:digit:]]\+$' | tee rozsah.out | wc --chars)
normpages=$(python3 -c "print('%.2f' % ($coef * $chars / 1800))")

echo "      Znaků: $chars"
echo " Koeficient: $coef"
echo " Normostran: $normpages"

