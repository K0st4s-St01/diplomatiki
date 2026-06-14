echo "p4" > logsgrep.txt;
cat participants_4/* | grep started>> logsgrep.txt; 
cat participants_4/* | grep gk-final>> logsgrep.txt;
cat participants_4/* | grep received>> logsgrep.txt;


echo "p8" >> logsgrep.txt;
cat participants_8/* | grep started>> logsgrep.txt;
cat participants_8/* | grep gk-final>> logsgrep.txt;
cat participants_8/* | grep received>> logsgrep.txt;

echo "p16" >> logsgrep.txt;
cat participants_16/* | grep started>> logsgrep.txt;
cat participants_16/* | grep gk-final>> logsgrep.txt;
cat participants_16/* | grep received>> logsgrep.txt;

echo "p32" >> logsgrep.txt;
cat participants_32/* | grep started>> logsgrep.txt;
cat participants_32/* | grep gk-final>> logsgrep.txt;
cat participants_32/* | grep received>> logsgrep.txt;
