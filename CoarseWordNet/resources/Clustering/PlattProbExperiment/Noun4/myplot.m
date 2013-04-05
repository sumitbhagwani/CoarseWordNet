a = dlmread('LinearAllTraining0.7MinMaxNormalizationPlattProb');
plot(a(1:850,2),'r*')
hold on
plot(a(851:end,2),'b*')
print('plot.png')

