
wget -P lib/ http://lyle.smu.edu/~tspell/jaws/jaws-bin.jar

wget -P wordnet/ http://www.cs.cmu.edu/~weizhan1/wordnet/wordnet.tar.gz
tar -zxvf wordnet/wordnet.tar.gz

wget -P res/ http://www.cs.cmu.edu/~weizhan1/res/res.tar.gz
tar -zxvf res/res.tar.gz

wget -P GeoNames/ http://download.geonames.org/export/dump/allCountries.zip
wget -P GeoNames/ http://download.geonames.org/export/dump/alternateNames.zip
wget -P GeoNames/ http://download.geonames.org/export/dump/admin1CodesASCII.txt
wget -P Geonames/ http://download.geonames.org/export/dump/admin2Codes.txt
wget -P GeoNames/ http://download.geonames.org/export/dump/iso-languagecodes.txt
wget -P GeoNames/ http://download.geonames.org/export/dump/timeZones.txt

unzip GeoNames/allCountries.zip 
unzip GeoNames/alternateNames.zip 
mv allCountries.txt GeoNames/
mv alternateNames.txt GeoNames/

rm GeoNames/allCountries.zip
rm GeoNames/alternateNames.zip
