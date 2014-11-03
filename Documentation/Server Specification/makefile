DOCUMENT_NAME = Server\ Specification
OUTPUT_DIR    = output
ABGABE_DIR    = ./

# Build the LaTeX document.
all: presentation

presentation: cleanup 
	open -a Preview $(DOCUMENT_NAME).pdf

# cleanup tempfiles
cleanup: report
	rm -f $(DOCUMENT_NAME).bcf rm -f $(DOCUMENT_NAME).run.xml rm -f $(DOCUMENT_NAME).bbl rm -f $(DOCUMENT_NAME).blg rm -f $(DOCUMENT_NAME).aux rm -f $(DOCUMENT_NAME).acn $(DOCUMENT_NAME).glo $(DOCUMENT_NAME).ist $(DOCUMENT_NAME).lof $(DOCUMENT_NAME).log $(DOCUMENT_NAME).lot $(DOCUMENT_NAME).lol $(DOCUMENT_NAME).out $(DOCUMENT_NAME).toc $(DOCUMENT_NAME).alg $(DOCUMENT_NAME).glg $(DOCUMENT_NAME).gls $(DOCUMENT_NAME).acr 

# Generate PDF output from LaTeX input files.
report:
	pdflatex -interaction=errorstopmode $(DOCUMENT_NAME).tex >> log.txt
	makeglossaries -q $(DOCUMENT_NAME).glo >> log.txt
	makeglossaries -q $(DOCUMENT_NAME).acn >> log.txt
	biber $(DOCUMENT_NAME) >> log.txt
	pdflatex -interaction=errorstopmode $(DOCUMENT_NAME).tex >> log.txt
	pdflatex -interaction=errorstopmode $(DOCUMENT_NAME).tex >> log.txt
