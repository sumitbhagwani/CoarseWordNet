a = dlmread('LinearEqualTrainingMinMaxNormalizationWithSynsets');
plot(a(1:850,4),'r*')
hold on
plot(a(851:end,4),'b*')
print('plot.png')

