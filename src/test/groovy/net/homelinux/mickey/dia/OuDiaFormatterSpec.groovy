package net.homelinux.mickey.dia

import spock.lang.*
import net.htmlparser.jericho.Source

class OuDiaFormatterSpec extends Specification {
  def formatter = new OuDiaFormatter()
  def output = new StringBuilder()

  def "buildStations駅情報のtest"() {
    setup:
    def stations = [new Station('門司港'),
                    new Station('小森江'),
                    new Station('門司', Station.Type.MAIN),
                    new Station('小倉', Station.Type.MAIN),
                    new Station('西小倉')
                   ]
    formatter.buildStations(stations, output)

    expect:
    output.toString().replaceAll('\r\n', '\n') == '''\
Eki.
Ekimei=門司港
Ekijikokukeisiki=Jikokukeisiki_NoboriChaku
Ekikibo=Ekikibo_Ippan
.
Eki.
Ekimei=小森江
Ekijikokukeisiki=Jikokukeisiki_Hatsu
Ekikibo=Ekikibo_Ippan
.
Eki.
Ekimei=門司
Ekijikokukeisiki=Jikokukeisiki_Hatsuchaku
Ekikibo=Ekikibo_Syuyou
.
Eki.
Ekimei=小倉
Ekijikokukeisiki=Jikokukeisiki_Hatsuchaku
Ekikibo=Ekikibo_Syuyou
.
Eki.
Ekimei=西小倉
Ekijikokukeisiki=Jikokukeisiki_KudariChaku
Ekikibo=Ekikibo_Ippan
.
'''
  }

  def "buildTrains列車情報"() {
    setup:
    def train323D = new Train('323D')
    train323D.name = ''
    train323D.time = [[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,'0650'] as String[],['0655','0656'] as String[],[null,'0659'] as String[],[null,'0703'] as String[],[null,'0711'] as String[],[null,'0725'] as String[],[null,'0734'] as String[],[null,'0737'] as String[],['0741','0742'] as String[],[null,'0746'] as String[],[null,'0751'] as String[],['0757','0758'] as String[],[null,'0803'] as String[],[null,'0807'] as String[],[null,'0812'] as String[],[null,'0816'] as String[],[null,'0821'] as String[],[null,'0827'] as String[],['0833','0834'] as String[],[null,'0843'] as String[],[null,'0846'] as String[],[null,'0851'] as String[],[null,'0855'] as String[],[null,'0905'] as String[],['0910',null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[]]
    def train375D = new Train('375D')
    train375D.name = ''
    train375D.time = [[null,'||'] as String[],[null,'||'] as String[],[null,'||'] as String[],[null,'||'] as String[],[null,'||'] as String[],['||','||'] as String[],[null,'||'] as String[],[null,'||'] as String[],[null,'||'] as String[],['||','||'] as String[],[null,'||'] as String[],[null,'||'] as String[],['||','||'] as String[],['||','||'] as String[],[null,'||'] as String[],[null,'||'] as String[],[null,'||'] as String[],[null,'||'] as String[],[null,'||'] as String[],[null,'||'] as String[],['||','||'] as String[],[null,'||'] as String[],[null,'||'] as String[],['||','||'] as String[],[null,'||'] as String[],[null,'||'] as String[],[null,'||'] as String[],[null,'||'] as String[],[null,'||'] as String[],[null,'||'] as String[],['||','0818'] as String[],[null,'0824'] as String[],[null,'0827'] as String[],[null,'0838'] as String[],[null,'0841'] as String[],[null,'0846'] as String[],['0851',null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[]]
    def train347D = new Train('347D')
    train347D.name = ''
    train347D.time = [[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,'0813'] as String[],[null,'0821'] as String[],[null,'0826'] as String[],[null,'0834'] as String[],[null,'0841'] as String[],[null,'0846'] as String[],[null,'0852'] as String[],[null,'0905'] as String[],['0912',null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[]]
    def train863D = new Train('863D')
    train863D.name = ''
    train863D.time = [[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,'0908'] as String[],[null,'0912'] as String[],[null,'0916'] as String[],[null,'0921'] as String[],[null,'0926'] as String[],[null,'0931'] as String[],[null,'0935'] as String[],[null,'0939'] as String[],[null,'0942'] as String[],[null,'0949'] as String[],[null,'0954'] as String[]]
    def train125D = new Train('125D')
    train125D.name = ''
    train125D.time = [[null,'0609'] as String[],[null,'0618'] as String[],[null,'0623'] as String[],[null,'0630'] as String[],[null,'0634'] as String[],['0641','0642'] as String[],[null,'0646'] as String[],[null,'0651'] as String[],[null,'0657'] as String[],['0702','0703'] as String[],[null,'0708'] as String[],[null,'0716'] as String[],['0722',null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[]]
    def train3001D = new Train('3001D')
    train3001D.name = 'スーパーおき'
    train3001D.number = 1
    train3001D.type = Train.Type.LIMITED_EXPRESS
    train3001D.time = [[null,'0551'] as String[],[null,'0558'] as String[],[null,'passage'] as String[],[null,'passage'] as String[],[null,'passage'] as String[],['0613','0614'] as String[],[null,'passage'] as String[],[null,'0619'] as String[],[null,'passage'] as String[],['0627','0627'] as String[],[null,'passage'] as String[],[null,'passage'] as String[],['0638','0639'] as String[],['passage','passage'] as String[],[null,'passage'] as String[],[null,'passage'] as String[],[null,'passage'] as String[],[null,'passage'] as String[],[null,'passage'] as String[],[null,'passage'] as String[],['0706','0709'] as String[],[null,'passage'] as String[],[null,'passage'] as String[],['passage','passage'] as String[],[null,'passage'] as String[],[null,'passage'] as String[],[null,'passage'] as String[],[null,'passage'] as String[],[null,'passage'] as String[],[null,'passage'] as String[],['0738','0739'] as String[],[null,'passage'] as String[],[null,'passage'] as String[],[null,'passage'] as String[],[null,'passage'] as String[],[null,'passage'] as String[],['0755','0758'] as String[],[null,'passage'] as String[],[null,'passage'] as String[],[null,'passage'] as String[],[null,'0816'] as String[],[null,'passage'] as String[],[null,'passage'] as String[],[null,'passage'] as String[],['0837','||'] as String[],[null,'||'] as String[],[null,'||'] as String[],[null,'||'] as String[],[null,'||'] as String[],[null,'||'] as String[],[null,'||'] as String[],['||','||'] as String[],[null,'||'] as String[],[null,'||'] as String[],['||','||'] as String[],[null,'||'] as String[],[null,'||'] as String[],[null,'||'] as String[],[null,'||'] as String[],['||','||'] as String[],['||','||'] as String[],[null,'||'] as String[],[null,'||'] as String[],[null,'||'] as String[],[null,'||'] as String[],[null,'||'] as String[],[null,'||'] as String[],[null,'||'] as String[],['||','||'] as String[],[null,'||'] as String[],[null,'||'] as String[],[null,'||'] as String[],['||','||'] as String[],[null,'||'] as String[],[null,'||'] as String[],[null,'||'] as String[],[null,'||'] as String[],[null,'||'] as String[],[null,'||'] as String[],[null,'||'] as String[],[null,'||'] as String[],[null,'||'] as String[],[null,'||'] as String[]]
    train125D.name = ''
    def stations = [new Station('米子'), new Station('安来'), new Station('荒島'), new Station('揖屋'), new Station('東松江'), new Station('松江', Station.Type.MAIN), new Station('乃木'), new Station('玉造温泉'), new Station('来待'), new Station('宍道', Station.Type.MAIN), new Station('荘原'), new Station('直江'), new Station('出雲市', Station.Type.MAIN), new Station('西出雲', Station.Type.MAIN), new Station('出雲神西'), new Station('江南'), new Station('小田'), new Station('田儀'), new Station('波根'), new Station('久手'), new Station('大田市', Station.Type.MAIN), new Station('静間'), new Station('五十猛'), new Station('仁万', Station.Type.MAIN), new Station('馬路'), new Station('湯里'), new Station('温泉津'), new Station('石見福光'), new Station('黒松'), new Station('浅利'), new Station('江津', Station.Type.MAIN), new Station('都野津'), new Station('敬川'), new Station('波子'), new Station('久代'), new Station('下府'), new Station('浜田', Station.Type.MAIN), new Station('西浜田'), new Station('周布'), new Station('折居'), new Station('三保三隅'), new Station('岡見'), new Station('鎌手'), new Station('石見津田'), new Station('益田', Station.Type.MAIN), new Station('戸田小浜'), new Station('飯浦'), new Station('江崎'), new Station('須佐'), new Station('宇田郷'), new Station('木与'), new Station('奈古', Station.Type.MAIN), new Station('長門大井'), new Station('越ヶ浜'), new Station('東萩', Station.Type.MAIN), new Station('萩'), new Station('玉江'), new Station('三見'), new Station('飯井'), new Station('長門三隅', Station.Type.MAIN), new Station('長門市', Station.Type.MAIN), new Station('黄波戸'), new Station('長門古市'), new Station('人丸'), new Station('伊上'), new Station('長門粟野'), new Station('阿川'), new Station('特牛'), new Station('滝部', Station.Type.MAIN), new Station('長門二見'), new Station('宇賀本郷'), new Station('湯玉'), new Station('小串', Station.Type.MAIN), new Station('川棚温泉'), new Station('黒井村'), new Station('梅ヶ峠'), new Station('吉見'), new Station('福江'), new Station('安岡'), new Station('梶栗郷台地'), new Station('綾羅木'), new Station('幡生'), new Station('下関')]
    formatter.buildTrains([train323D, train375D, train347D, train863D,
                           train125D, train3001D] as Set,
                          stations, output, Direction.DOWN)

    expect:
    output.toString().replaceAll('\r\n', '\n') == '''\
Kudari.
Ressya.
Houkou=Kudari
Syubetsu=0
Ressyabangou=323D
EkiJikoku=,,,,,,,,,,,,1;650,1;655/656,1;659,1;703,1;711,1;725,1;734,1;737,1;741/742,1;746,1;751,1;757/758,1;803,1;807,1;812,1;816,1;821,1;827,1;833/834,1;843,1;846,1;851,1;855,1;905,1;910/,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
.
Ressya.
Houkou=Kudari
Syubetsu=0
Ressyabangou=375D
EkiJikoku=3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,1;/818,1;824,1;827,1;838,1;841,1;846,1;851/,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
.
Ressya.
Houkou=Kudari
Syubetsu=0
Ressyabangou=347D
EkiJikoku=,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,1;813,1;821,1;826,1;834,1;841,1;846,1;852,1;905,1;912/,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
.
Ressya.
Houkou=Kudari
Syubetsu=0
Ressyabangou=863D
EkiJikoku=,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,1;908,1;912,1;916,1;921,1;926,1;931,1;935,1;939,1;942,1;949,1;954/
.
Ressya.
Houkou=Kudari
Syubetsu=0
Ressyabangou=125D
EkiJikoku=1;609,1;618,1;623,1;630,1;634,1;641/642,1;646,1;651,1;657,1;702/703,1;708,1;716,1;722/,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
.
Ressya.
Houkou=Kudari
Syubetsu=8
Ressyabangou=3001D
Ressyamei=スーパーおき
Gousuu=1
EkiJikoku=1;551,1;558,2,2,2,1;613/614,2,1;619,2,1;627/627,2,2,1;638/639,2,2,2,2,2,2,2,1;706/709,2,2,2,2,2,2,2,2,2,1;738/739,2,2,2,2,2,1;755/758,2,2,2,1;816,2,2,2,1;837/,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3
.
'''
  }

  def "列車タイプ出力test"() {
    expect:
    formatter.getTrainTypeString(Train.Type.HOME_LINER).replaceAll('\r\n', '\n') == '''\
Ressyasyubetsu.
Syubetsumei=ホームライナー
Ryakusyou=ＨＬ
JikokuhyouMojiColor=00FF0000
JikokuhyouFontIndex=0
DiagramSenColor=00FF0000
DiagramSenStyle=SenStyle_Jissen
DiagramSenIsBold=0
StopMarkDrawType=EStopMarkDrawType_DrawOnStop
.
'''
    formatter.getTrainTypeString(Train.Type.LOCAL).replaceAll('\r\n', '\n') == '''\
Ressyasyubetsu.
Syubetsumei=普通
JikokuhyouMojiColor=00000000
JikokuhyouFontIndex=0
DiagramSenColor=00000000
DiagramSenStyle=SenStyle_Jissen
DiagramSenIsBold=0
StopMarkDrawType=EStopMarkDrawType_Nothing
.
'''
    formatter.getTrainTypeString(Train.Type.BUS).replaceAll('\r\n', '\n') == '''\
Ressyasyubetsu.
Syubetsumei=バス
Ryakusyou=バス
JikokuhyouMojiColor=00000000
JikokuhyouFontIndex=0
DiagramSenColor=00000000
DiagramSenStyle=SenStyle_Jissen
DiagramSenIsBold=0
StopMarkDrawType=EStopMarkDrawType_Nothing
.
'''
    formatter.getTrainTypeString(Train.Type.KODAMA_SUPER_EXPRESS).replaceAll('\r\n', '\n') == '''\
Ressyasyubetsu.
Syubetsumei=こだま型新幹線
Ryakusyou=幹
JikokuhyouMojiColor=00400000
JikokuhyouFontIndex=0
DiagramSenColor=00400000
DiagramSenStyle=SenStyle_Jissen
DiagramSenIsBold=1
StopMarkDrawType=EStopMarkDrawType_DrawOnStop
.
'''
  }
}
