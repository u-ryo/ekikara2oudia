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
java -jar ekikara2oudia-1.2.10.one-jar.jar http://ekikara.jp/newdata/line/2301011/down1_1.htm http://ekikara.jp/newdata/line/2301011/down1_2.htm http://ekikara.jp/newdata/line/2301011/down1_3.htm ... http://ekikara.jp/newdata/line/2301011/up1_1.htm http://ekikara.jp/newdata/line/2301011/up1_2.htm http://ekikara.jp/newdata/line/2301011/up1_3.htm ...
```