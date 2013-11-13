fname="parse_results"
latex_dir="latex"
output_dir="output"
texfile=$fname.tex

cd $latex_dir

printf "\\documentclass{depparse}\n\\\\begin{document}\n\n" > $texfile

for file in `ls $output_dir/*.tex`
do
    printf "\\\\begin{center}\n" >> $texfile
    printf "\\input{$file}\n" >> $texfile
    printf "\\\\end{center}\n" >> $texfile
done

printf "\n\\\\end{document}\n" >> $texfile

pdflatex $fname.tex
cd ..
