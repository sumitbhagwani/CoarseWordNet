set size 1.0, 0.6
set terminal postscript portrait enhanced mono dashed lw 1 "Helvetica" 14 
#set output "SVMScaledSupervised0.7.ps"
set output "SVMScaledUnsupervised0.7.ps"
set xtic 0.05                          # set xtics automatically
set ytic 0.05                          # set ytics automatically
set xlabel "Threshold"
set ylabel "FScore"
set xr [0.35:0.75]
#set yr [0.65:1.0]
set yr [0.6:1.0]
#set key box
#plot "SVMScaled0.7" using 1:(($14+$15+$16)/3) title 'Connected Components Clustering', "SVMScaled0.7" using 1:(($6+$7+$8)/3) title 'Random Clustering'
plot "SVMScaled0.7" using 1:17 title 'Connected Components Clustering', "SVMScaled0.7" using 1:9 title 'Random Clustering'  
