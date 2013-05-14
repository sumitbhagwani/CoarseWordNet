a = dlmread('LinearEqualTrainingMinMaxNormalizationWithSynsets');
plot(a(1:850,4),'r*')
hold on
plot(a(851:end,4),'b*')
legend('{\fontsize{14} Merged Synsets}','Not-Merged Synsets') 
xlabel('Instances')
ylabel('SVM Predictions')
print('plot.png')

