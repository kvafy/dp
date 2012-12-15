# makefile pro preklad LaTeX verze Bc. prace
# (c) 2008 Michal Bidlo
# E-mail: bidlom AT fit vutbr cz
#===========================================
# asi budete chtit prejmenovat:
CO=dp

all: $(CO).pdf

pdf: $(CO).pdf

$(CO).ps: $(CO).dvi
	dvips $(CO)

$(CO).pdf: clean obsah.tex
	pdflatex $(CO)
	bibtex $(CO)
	pdflatex $(CO)
	pdflatex $(CO)

$(CO).dvi: $(CO).tex $(CO).bib obsah.tex
	latex $(CO)
	bibtex $(CO)
	latex $(CO)
	latex $(CO)

desky:
#	latex desky
#	dvips desky
#	dvipdf desky
	pdflatex desky

obsah.tex: obsah-gummi.tex
	cp obsah-gummi.tex obsah.tex
	sed -e '0,/%end-of-inserted-header/d' -i obsah.tex
	sed -e '/%begin-of-inserted-footer/,$$d' -i obsah.tex
	vlna -l -v Aa obsah.tex # english vlna

clean:
	rm -f *.dvi *.log $(CO).blg $(CO).bbl $(CO).toc *.aux $(CO).out $(CO).lof
	rm -f $(CO).pdf
	rm -f obsah.tex
	rm -f *~

pack:
	tar czvf bp-xjmeno.tar.gz *.tex *.bib *.bst ./fig/* ./cls/* Makefile Changelog
