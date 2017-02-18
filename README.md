# ekikara2oudia
A converter for OuDia from Ekikara.jp.

## What's this?
This executable jar generates OuDia (.oud) file from the Ekikara.jp URL pages.

## How to build?
```
mvn clean package
```

## How to use?
```
java -jar ekikara2oudia-1.2.10.one-jar.jar [-DprocessTables=1] [-DKitenJikoku=300] URL...
e.g.
java -jar ekikara2oudia-1.2.10.one-jar.jar http://ekikara.jp/newdata/line/1301691/down1_1.htm http://ekikara.jp/newdata/line/1301691/up1_1.htm
java -jar ekikara2oudia-1.2.10.one-jar.jar -DprocessTables=1,2,3,4,6 -DKitenJikoku=530 http://ekikara.jp/newdata/line/0101011/down1_1.htm http://ekikara.jp/newdata/line/0101011/down1_2.htm http://ekikara.jp/newdata/line/0101011/down1_3.htm http://ekikara.jp/newdata/line/0101011/down1_4.htm http://ekikara.jp/newdata/line/0101011/up1_1.htm http://ekikara.jp/newdata/line/0101011/up1_2.htm http://ekikara.jp/newdata/line/0101011/up1_3.htm http://ekikara.jp/newdata/line/0101011/up1_4.htm
```
