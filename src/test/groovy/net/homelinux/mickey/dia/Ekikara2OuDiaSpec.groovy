package net.homelinux.mickey.dia

import spock.lang.*
import net.htmlparser.jericho.Source

class Ekikara2OuDiaSpec extends Specification {
  def ekikara2OuDia = new Ekikara2OuDia()

  def "titleから偕楽園ruleの設定"() {
    setup:
    ekikara2OuDia.title =
      '[ＪＲ]常磐線 (上野〜仙台) ［路線時刻表］ 土曜日 更新日:2017/2/1'
    ekikara2OuDia.setRuleByTitle()

    expect:
    ekikara2OuDia.rule == Rule.KAIRAKUEN
  }

  def "常磐線以外ではruleが設定されない"() {
    setup:
    ekikara2OuDia.title =
      '[ＪＲ]北陸新幹線 (東京〜金沢) ［路線時刻表］ 土曜日 更新日:2017/2/1'
    ekikara2OuDia.setRuleByTitle()

    expect:
    ekikara2OuDia.rule == null
  }

  def "checkRulesで下り偕楽園の設定"() {
    setup:
    ekikara2OuDia.direction = Direction.DOWN
    ekikara2OuDia.rule = Rule.KAIRAKUEN
    ekikara2OuDia.checkRules('偕楽園', [new Station('友部'),
                                        new Station('内原'),
                                        new Station('赤塚'),
                                        new Station('偕楽園')])
    expect:
    ekikara2OuDia.rule.index == 4
    ekikara2OuDia.rule.direction == Direction.UP
  }

  def "checkRulesで上り偕楽園の設定"() {
    setup:
    ekikara2OuDia.direction = Direction.UP
    ekikara2OuDia.rule = Rule.KAIRAKUEN
    Rule.KAIRAKUEN.setIndex(null)
    Rule.KAIRAKUEN.setDirection(null)
    ekikara2OuDia.checkRules('偕楽園', [new Station('友部'),
                                        new Station('内原'),
                                        new Station('赤塚'),
                                        new Station('偕楽園')])
    expect:
    ekikara2OuDia.rule.index == 3
    ekikara2OuDia.rule.direction == Direction.DOWN
  }

  def "常磐線・偕楽園・下りが先の時のadjustで上り列車に偕楽園通過の付加"() {
    setup:
    ekikara2OuDia.rule = Rule.KAIRAKUEN
    ekikara2OuDia.rule.setIndex(4)
    ekikara2OuDia.rule.setDirection(Direction.UP)
    ekikara2OuDia.allStations = [
      new Station('友部'), new Station('内原'), new Station('赤塚'),
      new Station('偕楽園'), new Station('水戸'), new Station('勝田')
    ]
    ekikara2OuDia.upTrains = [
      new Train('364M'), new Train('2068M'), new Train('1006M') ]
    ekikara2OuDia.upTrains[0].time = [
      [null,'0914'] as String[], ['0920','0930'] as String[],
      [null,'0935'] as String[], [null,'0940'] as String[],
      ['0944','0944'] as String[]]
    ekikara2OuDia.upTrains[1].type = Train.Type.LIMITED_EXPRESS
    ekikara2OuDia.upTrains[1].name = 'ときわ68号'
    ekikara2OuDia.upTrains[1].time = [
      [null,'0904'] as String[], ['0910','0911'] as String[],
      [null,'passage'] as String[], [null,'passage'] as String[],
      ['0921','0921'] as String[]]
    ekikara2OuDia.upTrains[2].type = Train.Type.LIMITED_EXPRESS
    ekikara2OuDia.upTrains[2].name = 'ひたち6号'
    ekikara2OuDia.upTrains[2].time = [
      ['0920','0921'] as String[], ['0926','0927'] as String[],
      [null,'passage'] as String[], [null,'passage'] as String[],
      [null,'passage'] as String[]]
    ekikara2OuDia.adjust()

    expect:
    ekikara2OuDia.upTrains[0].time == [
      [null,'0914'], ['0920','0930'], [null,'passage'], [null,'0935'],
      [null,'0940'], ['0944','0944']]
    ekikara2OuDia.upTrains[1].time == [
      [null,'0904'], ['0910','0911'], [null,'passage'], [null,'passage'],
      [null,'passage'], ['0921','0921']]
    ekikara2OuDia.upTrains[2].time == [
      ['0920','0921'], ['0926','0927'], [null,'passage'], [null,'passage'],
      [null,'passage'], [null,'passage']]
  }

  def "常磐線・偕楽園・上りが先(reverse)の時のadjustで上り列車に偕楽園通過の付加"() {
    setup:
    ekikara2OuDia.rule = Rule.KAIRAKUEN
    ekikara2OuDia.rule.setIndex(3)
    ekikara2OuDia.rule.setDirection(Direction.DOWN)
    ekikara2OuDia.allStations = [
      new Station('勝田'), new Station('水戸'), new Station('赤塚'),
      new Station('内原'), new Station('友部')
    ]
    ekikara2OuDia.downTrains = [
      new Train('364M'), new Train('2068M'), new Train('1006M') ]
    ekikara2OuDia.downTrains[0].time = [
      [null,'0914'] as String[], ['0920','0930'] as String[],
      [null,'0935'] as String[], [null,'0940'] as String[],
      ['0944','0944'] as String[]]
    ekikara2OuDia.downTrains[1].type = Train.Type.LIMITED_EXPRESS
    ekikara2OuDia.downTrains[1].name = 'ときわ68号'
    ekikara2OuDia.downTrains[1].time = [
      [null,'0904'] as String[], ['0910','0911'] as String[],
      [null,'passage'] as String[], [null,'passage'] as String[],
      ['0921','0921'] as String[]]
    ekikara2OuDia.downTrains[2].type = Train.Type.LIMITED_EXPRESS
    ekikara2OuDia.downTrains[2].name = 'ひたち6号'
    ekikara2OuDia.downTrains[2].time = [
      ['0920','0921'] as String[], ['0926','0927'] as String[],
      [null,'passage'] as String[], [null,'passage'] as String[],
      [null,'passage'] as String[]]
    ekikara2OuDia.adjust()

    expect:
    ekikara2OuDia.allStations == [
      new Station('勝田'), new Station('水戸'), new Station('偕楽園'),
      new Station('赤塚'), new Station('内原'), new Station('友部')
    ]
    ekikara2OuDia.downTrains[0].time == [
      [null,'0914'], ['0920','0930'], [null,'passage'], [null,'0935'],
      [null,'0940'], ['0944','0944']]
    ekikara2OuDia.downTrains[1].time == [
      [null,'0904'], ['0910','0911'], [null,'passage'], [null,'passage'],
      [null,'passage'], ['0921','0921']]
    ekikara2OuDia.downTrains[2].time == [
      ['0920','0921'], ['0926','0927'], [null,'passage'], [null,'passage'],
      [null,'passage'], [null,'passage']]
  }

  def "偕楽園のある常磐線下りでのparseStationsのtest"() {
    setup:
    ekikara2OuDia.direction = Direction.DOWN
    ekikara2OuDia.rule = Rule.KAIRAKUEN
    Rule.KAIRAKUEN.setIndex(null)
    Rule.KAIRAKUEN.direction = null
    def tmpStationList = ekikara2OuDia.parseStations(
      ['上野', '日暮里', '三河島', '南千住', '北千住', '松戸', '柏', '我孫子',
       '〃', '天王台', '取手', '藤代', '佐貫', '牛久', 'ひたち野うしく', '荒川沖',
       '土浦', '〃', '神立', '高浜', '石岡', '羽鳥', '岩間', '友部', '〃', '内原',
       '赤塚', '偕楽園', '水戸', '〃', '勝田', '〃', '佐和', '東海', '大甕',
       '常陸多賀', '日立', '〃', '小木津', '十王', '高萩', '〃', '南中郷', '磯原',
       '大津港', '〃', '勿来', '植田', '泉', '湯本', '内郷', 'いわき', '〃',
       '草野', '四ツ倉', '久ノ浜', '〃', '末続', '広野', '〃', '木戸', '竜田',
       '小高', '磐城太田', '原ノ町', '〃', '鹿島', '日立木', '相馬', '駒ヶ嶺',
       '新地', '〃', '坂元', '山下', '〃', '浜吉田', '亘理', '逢隈', '岩沼',
       '館腰', '名取', '南仙台', '太子堂', '長町', '仙台'] as String[])

    expect:
    tmpStationList == [
      new Station('上野'), new Station('日暮里'), new Station('三河島'),
      new Station('南千住'), new Station('北千住'), new Station('松戸'),
      new Station('柏'), new Station('我孫子', Station.Type.MAIN),
      new Station('天王台'), new Station('取手'), new Station('藤代'),
      new Station('佐貫'), new Station('牛久'), new Station('ひたち野うしく'),
      new Station('荒川沖'), new Station('土浦', Station.Type.MAIN),
      new Station('神立'), new Station('高浜'), new Station('石岡'),
      new Station('羽鳥'), new Station('岩間'),
      new Station('友部', Station.Type.MAIN), new Station('内原'),
      new Station('赤塚'), new Station('偕楽園'),
      new Station('水戸', Station.Type.MAIN),
      new Station('勝田', Station.Type.MAIN), new Station('佐和'),
      new Station('東海'), new Station('大甕'), new Station('常陸多賀'),
      new Station('日立', Station.Type.MAIN), new Station('小木津'),
      new Station('十王'), new Station('高萩', Station.Type.MAIN),
      new Station('南中郷'), new Station('磯原'),
      new Station('大津港', Station.Type.MAIN), new Station('勿来'),
      new Station('植田'), new Station('泉'), new Station('湯本'),
      new Station('内郷'), new Station('いわき', Station.Type.MAIN),
      new Station('草野'), new Station('四ツ倉'),
      new Station('久ノ浜', Station.Type.MAIN), new Station('末続'),
      new Station('広野', Station.Type.MAIN), new Station('木戸'),
      new Station('竜田'), new Station('小高'), new Station('磐城太田'),
      new Station('原ノ町', Station.Type.MAIN), new Station('鹿島'),
      new Station('日立木'), new Station('相馬'), new Station('駒ヶ嶺'),
      new Station('新地', Station.Type.MAIN), new Station('坂元'),
      new Station('山下', Station.Type.MAIN), new Station('浜吉田'),
      new Station('亘理'), new Station('逢隈'), new Station('岩沼'),
      new Station('館腰'), new Station('名取'), new Station('南仙台'),
      new Station('太子堂'), new Station('長町'), new Station('仙台')]
    ekikara2OuDia.rule.index == 25
    ekikara2OuDia.rule.direction == Direction.UP
  }

  def "偕楽園のある常磐線下り(reverse)でのparseStationsのtest"() {
    setup:
    ekikara2OuDia.direction = Direction.UP
    ekikara2OuDia.rule = Rule.KAIRAKUEN
    Rule.KAIRAKUEN.setIndex(null)
    Rule.KAIRAKUEN.direction = null
    def tmpStationList = ekikara2OuDia.parseStations(
      ['上野', '日暮里', '三河島', '南千住', '北千住', '松戸', '柏', '我孫子',
       '〃', '天王台', '取手', '藤代', '佐貫', '牛久', 'ひたち野うしく', '荒川沖',
       '土浦', '〃', '神立', '高浜', '石岡', '羽鳥', '岩間', '友部', '〃', '内原',
       '赤塚', '偕楽園', '水戸', '〃', '勝田', '〃', '佐和', '東海', '大甕',
       '常陸多賀', '日立', '〃', '小木津', '十王', '高萩', '〃', '南中郷', '磯原',
       '大津港', '〃', '勿来', '植田', '泉', '湯本', '内郷', 'いわき', '〃',
       '草野', '四ツ倉', '久ノ浜', '〃', '末続', '広野', '〃', '木戸', '竜田',
       '小高', '磐城太田', '原ノ町', '〃', '鹿島', '日立木', '相馬', '駒ヶ嶺',
       '新地', '〃', '坂元', '山下', '〃', '浜吉田', '亘理', '逢隈', '岩沼',
       '館腰', '名取', '南仙台', '太子堂', '長町', '仙台'] as String[])

    expect:
    tmpStationList == [
      new Station('上野'), new Station('日暮里'), new Station('三河島'),
      new Station('南千住'), new Station('北千住'), new Station('松戸'),
      new Station('柏'), new Station('我孫子', Station.Type.MAIN),
      new Station('天王台'), new Station('取手'), new Station('藤代'),
      new Station('佐貫'), new Station('牛久'), new Station('ひたち野うしく'),
      new Station('荒川沖'), new Station('土浦', Station.Type.MAIN),
      new Station('神立'), new Station('高浜'), new Station('石岡'),
      new Station('羽鳥'), new Station('岩間'),
      new Station('友部', Station.Type.MAIN), new Station('内原'),
      new Station('赤塚'), new Station('偕楽園'),
      new Station('水戸', Station.Type.MAIN),
      new Station('勝田', Station.Type.MAIN), new Station('佐和'),
      new Station('東海'), new Station('大甕'), new Station('常陸多賀'),
      new Station('日立', Station.Type.MAIN), new Station('小木津'),
      new Station('十王'), new Station('高萩', Station.Type.MAIN),
      new Station('南中郷'), new Station('磯原'),
      new Station('大津港', Station.Type.MAIN), new Station('勿来'),
      new Station('植田'), new Station('泉'), new Station('湯本'),
      new Station('内郷'), new Station('いわき', Station.Type.MAIN),
      new Station('草野'), new Station('四ツ倉'),
      new Station('久ノ浜', Station.Type.MAIN), new Station('末続'),
      new Station('広野', Station.Type.MAIN), new Station('木戸'),
      new Station('竜田'), new Station('小高'), new Station('磐城太田'),
      new Station('原ノ町', Station.Type.MAIN), new Station('鹿島'),
      new Station('日立木'), new Station('相馬'), new Station('駒ヶ嶺'),
      new Station('新地', Station.Type.MAIN), new Station('坂元'),
      new Station('山下', Station.Type.MAIN), new Station('浜吉田'),
      new Station('亘理'), new Station('逢隈'), new Station('岩沼'),
      new Station('館腰'), new Station('名取'), new Station('南仙台'),
      new Station('太子堂'), new Station('長町'), new Station('仙台')]
    ekikara2OuDia.rule.index == 24
    ekikara2OuDia.rule.direction == Direction.DOWN
  }

  def "偕楽園のない常磐線下りでのparseStationsのtest"() {
    setup:
    ekikara2OuDia.direction = Direction.DOWN
    ekikara2OuDia.rule = Rule.KAIRAKUEN
    Rule.KAIRAKUEN.setIndex(null)
    Rule.KAIRAKUEN.direction = null
    def tmpStationList = ekikara2OuDia.parseStations(
      ['上野', '日暮里', '三河島', '南千住', '北千住', '松戸', '柏', '我孫子',
       '〃', '天王台', '取手', '藤代', '佐貫', '牛久', 'ひたち野うしく', '荒川沖',
       '土浦', '〃', '神立', '高浜', '石岡', '羽鳥', '岩間', '友部', '〃', '内原',
       '赤塚', '水戸', '〃', '勝田', '〃', '佐和', '東海', '大甕',
       '常陸多賀', '日立', '〃', '小木津', '十王', '高萩', '〃', '南中郷', '磯原',
       '大津港', '〃', '勿来', '植田', '泉', '湯本', '内郷', 'いわき', '〃',
       '草野', '四ツ倉', '久ノ浜', '〃', '末続', '広野', '〃', '木戸', '竜田',
       '小高', '磐城太田', '原ノ町', '〃', '鹿島', '日立木', '相馬', '駒ヶ嶺',
       '新地', '〃', '坂元', '山下', '〃', '浜吉田', '亘理', '逢隈', '岩沼',
       '館腰', '名取', '南仙台', '太子堂', '長町', '仙台'] as String[])

    expect:
    tmpStationList == [
      new Station('上野'), new Station('日暮里'), new Station('三河島'),
      new Station('南千住'), new Station('北千住'), new Station('松戸'),
      new Station('柏'), new Station('我孫子', Station.Type.MAIN),
      new Station('天王台'), new Station('取手'), new Station('藤代'),
      new Station('佐貫'), new Station('牛久'), new Station('ひたち野うしく'),
      new Station('荒川沖'), new Station('土浦', Station.Type.MAIN),
      new Station('神立'), new Station('高浜'), new Station('石岡'),
      new Station('羽鳥'), new Station('岩間'),
      new Station('友部', Station.Type.MAIN), new Station('内原'),
      new Station('赤塚'),
      new Station('水戸', Station.Type.MAIN),
      new Station('勝田', Station.Type.MAIN), new Station('佐和'),
      new Station('東海'), new Station('大甕'), new Station('常陸多賀'),
      new Station('日立', Station.Type.MAIN), new Station('小木津'),
      new Station('十王'), new Station('高萩', Station.Type.MAIN),
      new Station('南中郷'), new Station('磯原'),
      new Station('大津港', Station.Type.MAIN), new Station('勿来'),
      new Station('植田'), new Station('泉'), new Station('湯本'),
      new Station('内郷'), new Station('いわき', Station.Type.MAIN),
      new Station('草野'), new Station('四ツ倉'),
      new Station('久ノ浜', Station.Type.MAIN), new Station('末続'),
      new Station('広野', Station.Type.MAIN), new Station('木戸'),
      new Station('竜田'), new Station('小高'), new Station('磐城太田'),
      new Station('原ノ町', Station.Type.MAIN), new Station('鹿島'),
      new Station('日立木'), new Station('相馬'), new Station('駒ヶ嶺'),
      new Station('新地', Station.Type.MAIN), new Station('坂元'),
      new Station('山下', Station.Type.MAIN), new Station('浜吉田'),
      new Station('亘理'), new Station('逢隈'), new Station('岩沼'),
      new Station('館腰'), new Station('名取'), new Station('南仙台'),
      new Station('太子堂'), new Station('長町'), new Station('仙台')]
    ekikara2OuDia.rule.index == null
    ekikara2OuDia.rule.direction == null
  }

  def "偕楽園のある(仮想)常磐線上りでのparseStationsのtest"() {
    setup:
    ekikara2OuDia.direction = Direction.UP
    ekikara2OuDia.rule = Rule.KAIRAKUEN
    Rule.KAIRAKUEN.setIndex(25)
    Rule.KAIRAKUEN.direction = Direction.UP
    def tmpStationList = ekikara2OuDia.parseStations(
      ['仙台', '長町', '太子堂', '南仙台', '名取', '館腰', '岩沼', '逢隈', '亘理',
       '浜吉田', '山下', '〃', '坂元', '新地', '〃', '駒ヶ嶺', '相馬', '〃',
       '日立木', '鹿島', '原ノ町', '〃', '磐城太田', '小高', '竜田', '木戸',
       '広野', '末続', '久ノ浜', '〃', '四ツ倉', '草野', '〃', 'いわき', '〃',
       '内郷', '湯本', '泉', '植田', '勿来', '大津港', '磯原', '南中郷', '高萩',
       '〃', '十王', '小木津', '日立', '〃', '常陸多賀', '大甕', '東海', '佐和',
       '勝田', '〃', '水戸', '〃', '偕楽園', '赤塚', '内原', '友部', '〃', '岩間',
       '羽鳥', '石岡', '高浜', '神立', '土浦', '〃', '荒川沖', 'ひたち野うしく',
       '牛久', '佐貫', '藤代', '取手', '天王台', '我孫子', '〃', '柏', '松戸',
       '北千住', '南千住', '三河島', '日暮里', '上野'] as String[])

    expect:
    tmpStationList == [
      new Station('上野'), new Station('日暮里'), new Station('三河島'),
      new Station('南千住'), new Station('北千住'), new Station('松戸'),
      new Station('柏'), new Station('我孫子', Station.Type.MAIN),
      new Station('天王台'), new Station('取手'), new Station('藤代'),
      new Station('佐貫'), new Station('牛久'), new Station('ひたち野うしく'),
      new Station('荒川沖'), new Station('土浦', Station.Type.MAIN),
      new Station('神立'), new Station('高浜'), new Station('石岡'),
      new Station('羽鳥'), new Station('岩間'),
      new Station('友部', Station.Type.MAIN), new Station('内原'),
      new Station('赤塚'), new Station('偕楽園'),
      new Station('水戸', Station.Type.MAIN),
      new Station('勝田', Station.Type.MAIN), new Station('佐和'),
      new Station('東海'), new Station('大甕'), new Station('常陸多賀'),
      new Station('日立', Station.Type.MAIN), new Station('小木津'),
      new Station('十王'), new Station('高萩', Station.Type.MAIN),
      new Station('南中郷'), new Station('磯原'),
      new Station('大津港'), new Station('勿来'),
      new Station('植田'), new Station('泉'), new Station('湯本'),
      new Station('内郷'), new Station('いわき', Station.Type.MAIN),
      new Station('草野', Station.Type.MAIN), new Station('四ツ倉'),
      new Station('久ノ浜', Station.Type.MAIN), new Station('末続'),
      new Station('広野'), new Station('木戸'),
      new Station('竜田'), new Station('小高'), new Station('磐城太田'),
      new Station('原ノ町', Station.Type.MAIN), new Station('鹿島'),
      new Station('日立木'), new Station('相馬', Station.Type.MAIN),
      new Station('駒ヶ嶺'),
      new Station('新地', Station.Type.MAIN), new Station('坂元'),
      new Station('山下', Station.Type.MAIN), new Station('浜吉田'),
      new Station('亘理'), new Station('逢隈'), new Station('岩沼'),
      new Station('館腰'), new Station('名取'), new Station('南仙台'),
      new Station('太子堂'), new Station('長町'), new Station('仙台')].reverse()
    ekikara2OuDia.rule.index == 25
    ekikara2OuDia.rule.direction == Direction.UP
  }

  def "偕楽園のある(仮想)常磐線上り(reverse)でのparseStationsのtest"() {
    setup:
    ekikara2OuDia.direction = Direction.DOWN
    ekikara2OuDia.rule = Rule.KAIRAKUEN
    Rule.KAIRAKUEN.setIndex(null)
    Rule.KAIRAKUEN.direction = null
    def tmpStationList = ekikara2OuDia.parseStations(
      ['仙台', '長町', '太子堂', '南仙台', '名取', '館腰', '岩沼', '逢隈', '亘理',
       '浜吉田', '山下', '〃', '坂元', '新地', '〃', '駒ヶ嶺', '相馬', '〃',
       '日立木', '鹿島', '原ノ町', '〃', '磐城太田', '小高', '竜田', '木戸',
       '広野', '末続', '久ノ浜', '〃', '四ツ倉', '草野', '〃', 'いわき', '〃',
       '内郷', '湯本', '泉', '植田', '勿来', '大津港', '磯原', '南中郷', '高萩',
       '〃', '十王', '小木津', '日立', '〃', '常陸多賀', '大甕', '東海', '佐和',
       '勝田', '〃', '水戸', '〃', '偕楽園', '赤塚', '内原', '友部', '〃', '岩間',
       '羽鳥', '石岡', '高浜', '神立', '土浦', '〃', '荒川沖', 'ひたち野うしく',
       '牛久', '佐貫', '藤代', '取手', '天王台', '我孫子', '〃', '柏', '松戸',
       '北千住', '南千住', '三河島', '日暮里', '上野'] as String[])

    expect:
    tmpStationList == [
      new Station('上野'), new Station('日暮里'), new Station('三河島'),
      new Station('南千住'), new Station('北千住'), new Station('松戸'),
      new Station('柏'), new Station('我孫子', Station.Type.MAIN),
      new Station('天王台'), new Station('取手'), new Station('藤代'),
      new Station('佐貫'), new Station('牛久'), new Station('ひたち野うしく'),
      new Station('荒川沖'), new Station('土浦', Station.Type.MAIN),
      new Station('神立'), new Station('高浜'), new Station('石岡'),
      new Station('羽鳥'), new Station('岩間'),
      new Station('友部', Station.Type.MAIN), new Station('内原'),
      new Station('赤塚'), new Station('偕楽園'),
      new Station('水戸', Station.Type.MAIN),
      new Station('勝田', Station.Type.MAIN), new Station('佐和'),
      new Station('東海'), new Station('大甕'), new Station('常陸多賀'),
      new Station('日立', Station.Type.MAIN), new Station('小木津'),
      new Station('十王'), new Station('高萩', Station.Type.MAIN),
      new Station('南中郷'), new Station('磯原'),
      new Station('大津港'), new Station('勿来'),
      new Station('植田'), new Station('泉'), new Station('湯本'),
      new Station('内郷'), new Station('いわき', Station.Type.MAIN),
      new Station('草野', Station.Type.MAIN), new Station('四ツ倉'),
      new Station('久ノ浜', Station.Type.MAIN), new Station('末続'),
      new Station('広野'), new Station('木戸'),
      new Station('竜田'), new Station('小高'), new Station('磐城太田'),
      new Station('原ノ町', Station.Type.MAIN), new Station('鹿島'),
      new Station('日立木'), new Station('相馬', Station.Type.MAIN),
      new Station('駒ヶ嶺'),
      new Station('新地', Station.Type.MAIN), new Station('坂元'),
      new Station('山下', Station.Type.MAIN), new Station('浜吉田'),
      new Station('亘理'), new Station('逢隈'), new Station('岩沼'),
      new Station('館腰'), new Station('名取'), new Station('南仙台'),
      new Station('太子堂'), new Station('長町'), new Station('仙台')].reverse()
    ekikara2OuDia.rule.index == 47
    ekikara2OuDia.rule.direction == Direction.UP
  }

  def "parseTrainIdNumbers列車番号取得Test"() {
    setup:
    def html = '''\
<td colspan="2" class="lowBg06"><span class="l"><span class="textBold">列車番号</span></span></td>
<td align="center"  nowrap class="lowBgFFF">
<span class="m"><span style="color:#000000;">521M</span></span></td>
<td align="center"  nowrap class="lowBg12">
<span class="m"><span style="color:#000000;">321M</span></span></td>
<td align="center"  nowrap class="lowBgFFF">
<span class="m"><span style="color:#000000;">661M</span></span></td>
<td align="center"  nowrap class="lowBg12">
<span class="m"><span style="color:#000000;">223M</span></span></td>
<td align="center"  nowrap class="lowBgFFF">
<span class="m"><span style="color:#000000;">221M</span></span></td>
<td align="center"  nowrap class="lowBg12">
<span class="m"><span style="color:#000000;">527M</span></span></td>
<td align="center"  nowrap class="lowBgFFF">
<span class="m"><span style="color:#000000;">663M</span></span></td>
<td align="center"  nowrap class="lowBg12">
<span class="m"><span style="color:#000000;">523M</span></span></td>
<td align="center"  nowrap class="lowBgFFF">
<span class="m"><span style="color:#000000;">225M</span></span></td>
<td align="center"  nowrap class="lowBg12">
<span class="m"><span style="color:#000000;">325M</span></span></td>
<td align="center"  nowrap class="lowBgFFF">
<span class="m"><span style="color:#000000;">669M</span></span></td>
<td align="center"  nowrap class="lowBg12">
<span class="m"><span style="color:#000000;">665M</span></span></td>
<td align="center"  nowrap class="lowBgFFF">
<span class="m"><span style="color:#000000;">227M</span></span></td>
<td align="center"  nowrap class="lowBg12">
<span class="m"><span style="color:#000000;">323M</span></span></td>
<td align="center"  nowrap class="lowBgFFF">
<span class="m"><span style="color:#000000;">525M</span></span></td>'''
    def source = new Source(html)
    source.fullSequentialParse()
    def trains = ekikara2OuDia.parseTrainIdNumbers(source.getAllStartTags('td'))

    expect:
    trains == [new Train('521M'), new Train('321M'), new Train('661M'),
               new Train('223M'), new Train('221M'), new Train('527M'),
               new Train('663M'), new Train('523M'), new Train('225M'),
               new Train('325M'), new Train('669M'), new Train('665M'),
               new Train('227M'), new Train('323M'), new Train('525M')]
  }

  def "parseTrainNames列車名取得test"() {
    setup:
    def html = '''\
<td colspan="2" class="lowBg06"><span class="l"><span class="textBold">列車名</span></span></td>
<td valign="top" align="center" class="lowBgFFF">
<span class="s">[普通]</span><br/><span class="m">&nbsp;</span></td>
<td valign="top" align="center" class="lowBg12">
<span class="s">[普通]</span><br/><span class="m">は<br/>こ<br/>だ<br/>て<br/>ラ<br/>イ<br/>ナ<br/>ー</span></td>
<td valign="top" align="center" class="lowBgFFF">
<span class="s">[特急]</span><br/><span class="m">ス<br/>ー<br/>パ<br/>ー<br/>北<br/>斗<br/>1<br/>号</span></td>
<td valign="top" align="center" class="lowBg12">
<span class="s">[普通]</span><br/><span class="m">&nbsp;</span></td>
<td valign="top" align="center" class="lowBgFFF">
<span class="s">[快速]</span><br/><span class="m">は<br/>こ<br/>だ<br/>て<br/>ラ<br/>イ<br/>ナ<br/>ー</span></td>
<td valign="top" align="center" class="lowBg12">
<span class="s">[普通]</span><br/><span class="m">&nbsp;</span></td>
<td valign="top" align="center" class="lowBgFFF">
<span class="s">[特急]</span><br/><span class="m">北<br/>斗<br/>3<br/>号</span></td>
<td valign="top" align="center" class="lowBg12">
<span class="s">[普通]</span><br/><span class="m">&nbsp;</span></td>
<td valign="top" align="center" class="lowBgFFF">
<span class="s">[普通]</span><br/><span class="m">は<br/>こ<br/>だ<br/>て<br/>ラ<br/>イ<br/>ナ<br/>ー</span></td>
<td valign="top" align="center" class="lowBg12">
<span class="s">[普通]</span><br/><span class="m">&nbsp;</span></td>
<td valign="top" align="center" class="lowBgFFF">
<span class="s">[快速]</span><br/><span class="m">は<br/>こ<br/>だ<br/>て<br/>ラ<br/>イ<br/>ナ<br/>ー</span></td>
<td valign="top" align="center" class="lowBg12">
<span class="s">[特急]</span><br/><span class="m">ス<br/>ー<br/>パ<br/>ー<br/>北<br/>斗<br/>5<br/>号</span></td>
<td valign="top" align="center" class="lowBgFFF">
<span class="s">[普通]</span><br/><span class="m">は<br/>こ<br/>だ<br/>て<br/>ラ<br/>イ<br/>ナ<br/>ー</span></td>
<td valign="top" align="center" class="lowBg12">
<span class="s">[特急]</span><br/><span class="m">ス<br/>ー<br/>パ<br/>ー<br/>北<br/>斗<br/>7<br/>号</span></td>
<td valign="top" align="center" class="lowBgFFF">
<span class="s">[特急]</span><br/><span class="m">ス<br/>ー<br/>パ<br/>ー<br/>北<br/>斗<br/>9<br/>号</span></td>
'''
    def source = new Source(html)
    source.fullSequentialParse()
    def tmpTrainList = [
      new Train('5881D'), new Train('1321M'), new Train('1D'),
      new Train('891D'), new Train('3323M'), new Train('4851D'),
      new Train('3D'), new Train('4831D'), new Train('1325M'),
      new Train('821D'), new Train('3329D'), new Train('5D'),
      new Train('1333M'), new Train('7D'), new Train('9D')
    ]
    ekikara2OuDia.parseTrainNames(source.getAllStartTags('td'), tmpTrainList)

    expect:
    tmpTrainList[0].type == Train.Type.LOCAL
    tmpTrainList[1].name == 'はこだてライナー'
    tmpTrainList[1].type == Train.Type.LOCAL
    tmpTrainList[2].name == 'スーパー北斗'
    tmpTrainList[2].number == '1'
    tmpTrainList[2].type == Train.Type.LIMITED_EXPRESS
    tmpTrainList[3].type == Train.Type.LOCAL
    tmpTrainList[4].name == 'はこだてライナー'
    tmpTrainList[4].type == Train.Type.RAPID
    tmpTrainList[5].type == Train.Type.LOCAL
    tmpTrainList[6].name == '北斗'
    tmpTrainList[6].number == '3'
    tmpTrainList[6].type == Train.Type.LIMITED_EXPRESS
    tmpTrainList[7].type == Train.Type.LOCAL
    tmpTrainList[8].name == 'はこだてライナー'
    tmpTrainList[8].type == Train.Type.LOCAL
    tmpTrainList[9].type == Train.Type.LOCAL
    tmpTrainList[10].name == 'はこだてライナー'
    tmpTrainList[10].type == Train.Type.RAPID
    tmpTrainList[11].name == 'スーパー北斗'
    tmpTrainList[11].number == '5'
    tmpTrainList[11].type == Train.Type.LIMITED_EXPRESS
    tmpTrainList[12].name == 'はこだてライナー'
    tmpTrainList[12].type == Train.Type.LOCAL
    tmpTrainList[13].name == 'スーパー北斗'
    tmpTrainList[13].number == '7'
    tmpTrainList[13].type == Train.Type.LIMITED_EXPRESS
    tmpTrainList[14].name == 'スーパー北斗'
    tmpTrainList[14].number == '9'
    tmpTrainList[14].type == Train.Type.LIMITED_EXPRESS
  }

  def "parseTrainNotes備考取得test"() {
    setup:
    def html = '''\
<td colspan="2" class="lowBg06"><span class="l"><span class="textBold">運転日注意</span></span></td>
<td align="center" class="lowBgFFF">
<span class="m"><span class="textBold">&nbsp;</span></span></td>
<td align="center" class="lowBg12">
<span class="m"><span class="textBold">&nbsp;</span></span></td>
<td align="center" class="lowBgFFF">
<span class="m"><span class="textBold">&nbsp;</span></span></td>
<td align="center" class="lowBg12">
<span class="m"><span class="textBold">&nbsp;</span></span></td>
<td align="center" class="lowBgFFF">
<span class="m"><span class="textBold">◆</span></span></td>
<td align="center" class="lowBg12">
<span class="m"><span class="textBold">&nbsp;</span></span></td>
<td align="center" class="lowBgFFF">
<span class="m"><span class="textBold">&nbsp;</span></span></td>
<td align="center" class="lowBg12">
<span class="m"><span class="textBold">&nbsp;</span></span></td>
<td align="center" class="lowBgFFF">
<span class="m"><span class="textBold">&nbsp;</span></span></td>
<td align="center" class="lowBg12">
<span class="m"><span class="textBold">&nbsp;</span></span></td>
<td align="center" class="lowBgFFF">
<span class="m"><span class="textBold">&nbsp;</span></span></td>
<td align="center" class="lowBg12">
<span class="m"><span class="textBold">◆</span></span></td>
<td align="center" class="lowBgFFF">
<span class="m"><span class="textBold">&nbsp;</span></span></td>
<td align="center" class="lowBg12">
<span class="m"><span class="textBold">&nbsp;</span></span></td>
<td align="center" class="lowBgFFF">
<span class="m"><span class="textBold">&nbsp;</span></span></td>
'''
    def source = new Source(html)
    source.fullSequentialParse()
    def tmpTrainList = [
      new Train('4833D'), new Train('1339M'), new Train('11D'),
      new Train('4835D'), new Train('8031D'), new Train('1341M'),
      new Train('893D'), new Train('13D'), new Train('3345M'),
      new Train('2841D'), new Train('15D'), new Train('8033D'),
      new Train('3349M'), new Train('1875D'), new Train('17D')
    ]
    ekikara2OuDia.parseTrainNotes(source.getAllStartTags('td'), tmpTrainList)

    expect:
    tmpTrainList[0].note == null
    tmpTrainList[1].note == null
    tmpTrainList[2].note == null
    tmpTrainList[3].note == null
    tmpTrainList[4].note == '運転日注意'
    tmpTrainList[5].note == null
    tmpTrainList[6].note == null
    tmpTrainList[7].note == null
    tmpTrainList[8].note == null
    tmpTrainList[9].note == null
    tmpTrainList[10].note == null
    tmpTrainList[11].note == '運転日注意'
    tmpTrainList[12].note == null
    tmpTrainList[13].note == null
    tmpTrainList[14].note == null
  }

  def "parseTrainTimes列車時刻取得test"() {
    setup:
    def html = '''\
<td valign="top" class="lowBg06" nowrap><span class="l"><span class="textBold">
  <a href="../../station/01202011.htm">函館</a><br/>
  <a href="../../station/01202021.htm">五稜郭</a><br/>
  <a href="../../station/01202031.htm">桔梗</a><br/>
  <a href="../../station/01337011.htm">大中山</a><br/>
  <a href="../../station/01337021.htm">七飯</a><br/>
  〃<br/>
</span></span></td>
<td valign="top" align="center" class="lowBg06" nowrap><span class="l">
<span class="textBold"><a href="../../ekijikoku/0101011/down1_01202011.htm">発</a></span><br/>
<span class="textBold"><a href="../../ekijikoku/0101011/down1_01202021.htm">発</a></span><br/>
<span class="textBold"><a href="../../ekijikoku/0101011/down1_01202031.htm">発</a></span><br/>
<span class="textBold"><a href="../../ekijikoku/0101011/down1_01337011.htm">発</a></span><br/>
<span class="textBold">着</span><br/>
<span class="textBold"><a href="../../ekijikoku/0101011/down1_01337021.htm">発</a></span><br/>
</span></td>
<td valign="top" align="center" class="lowBgFFF" nowrap>
<span class="l">10:53</span><br/>
<span class="l">10:58</span><br/>
<span class="l">11:05</span><br/>
<span class="l">11:08</span><br/>
<span class="l">11:12</span><br/>
<span class="l">11:13</span><br/>
</span></td>
<td valign="top" align="center" class="lowBg12" nowrap>
<span class="l">11:53</span><br/>
<span class="l">11:58</span><br/>
<span class="l">12:02</span><br/>
<span class="l">12:05</span><br/>
<span class="l">12:09</span><br/>
<span class="l">12:09</span><br/>
</span></td>
<td valign="top" align="center" class="lowBgFFF" nowrap>
<span class="l">12:16</span><br/>
<span class="l">12:21</span><br/>
<span class="l">レ</span><br/>
<span class="l">レ</span><br/>
<span class="l">レ</span><br/>
<span class="l">レ</span><br/>
</span></td>
<td valign="top" align="center" class="lowBg12" nowrap>
<span class="l">12:34</span><br/>
<span class="l">12:40</span><br/>
<span class="l">12:46</span><br/>
<span class="l">12:50</span><br/>
<span class="l">12:54</span><br/>
<span class="l">12:54</span><br/>
</span></td>
<td valign="top" align="center" class="lowBgFFF" nowrap>
<span class="l">12:45</span><br/>
<span class="l">12:51</span><br/>
<span class="l">レ</span><br/>
<span class="l">レ</span><br/>
<span class="l">レ</span><br/>
<span class="l">レ</span><br/>
</span></td>
<td valign="top" align="center" class="lowBg12" nowrap>
<span class="l">13:02</span><br/>
<span class="l">13:07</span><br/>
<span class="l">13:11</span><br/>
<span class="l">13:14</span><br/>
<span class="l">13:18</span><br/>
<span class="l">13:18</span><br/>
</span></td>
<td valign="top" align="center" class="lowBgFFF" nowrap>
<span class="l">&nbsp;</span><br/>
<span class="l">&nbsp;</span><br/>
<span class="l">&nbsp;</span><br/>
<span class="l">&nbsp;</span><br/>
<span class="l">--</span><br/>
<span class="l">--</span><br/>
</span></td>
<td valign="top" align="center" class="lowBg12" nowrap>
<span class="l">13:51</span><br/>
<span class="l">13:56</span><br/>
<span class="l">レ</span><br/>
<span class="l">レ</span><br/>
<span class="l">レ</span><br/>
<span class="l">レ</span><br/>
</span></td>
<td valign="top" align="center" class="lowBgFFF" nowrap>
<span class="l">14:14</span><br/>
<span class="l">14:19</span><br/>
<span class="l">レ</span><br/>
<span class="l">レ</span><br/>
<span class="l">レ</span><br/>
<span class="l">レ</span><br/>
</span></td>
<td valign="top" align="center" class="lowBg12" nowrap>
<span class="l">14:31</span><br/>
<span class="l">14:36</span><br/>
<span class="l">14:43</span><br/>
<span class="l">14:46</span><br/>
<span class="l">14:50</span><br/>
<span class="l">14:51</span><br/>
</span></td>
<td valign="top" align="center" class="lowBgFFF" nowrap>
<span class="l">14:56</span><br/>
<span class="l">15:01</span><br/>
<span class="l">レ</span><br/>
<span class="l">レ</span><br/>
<span class="l">レ</span><br/>
<span class="l">レ</span><br/>
</span></td>
<td valign="top" align="center" class="lowBg12" nowrap>
<span class="l">15:05</span><br/>
<span class="l">15:10</span><br/>
<span class="l">レ</span><br/>
<span class="l">レ</span><br/>
<span class="l">レ</span><br/>
<span class="l">レ</span><br/>
</span></td>
<td valign="top" align="center" class="lowBgFFF" nowrap>
<span class="l">15:45</span><br/>
<span class="l">15:50</span><br/>
<span class="l">レ</span><br/>
<span class="l">レ</span><br/>
<span class="l">レ</span><br/>
<span class="l">レ</span><br/>
</span></td>
<td valign="top" align="center" class="lowBg12" nowrap>
<span class="l">16:14</span><br/>
<span class="l">16:20</span><br/>
<span class="l">16:26</span><br/>
<span class="l">16:30</span><br/>
<span class="l">16:34</span><br/>
<span class="l">==</span><br/>
</span></td>
<td valign="top" align="center" class="lowBgFFF" nowrap>
<span class="l">16:35</span><br/>
<span class="l">16:40</span><br/>
<span class="l">レ</span><br/>
<span class="l">レ</span><br/>
<span class="l">レ</span><br/>
<span class="l">レ</span><br/>
</span></td>
'''
    def source = new Source(html)
    source.fullSequentialParse()
    def tmpStationList = [
      new Station('函館'), new Station('五稜郭'), new Station('桔梗'),
      new Station('大中山'), new Station('七飯', Station.Type.MAIN)
    ]
    def tmpTrainList = [
      new Train('4833D'), new Train('1339M'), new Train('11D'),
      new Train('4835D'), new Train('8031D'), new Train('1341M'),
      new Train('893D'), new Train('13D'), new Train('3345M'),
      new Train('2841D'), new Train('15D'), new Train('8033D'),
      new Train('3349M'), new Train('1875D'), new Train('17D')
    ]
    def stationTimes = source.getAllStartTags('td')
    stationTimes.remove(0)
    stationTimes.remove(0)
    ekikara2OuDia.direction = Direction.DOWN
    ekikara2OuDia.parseTrainTimes(stationTimes, tmpTrainList, tmpStationList)

    expect:
    tmpTrainList[0].time == [[null,'1053'], [null,'1058'], [null,'1105'],
                             [null,'1108'], ['1112','1113']]
    tmpTrainList[1].time == [[null,'1153'], [null,'1158'], [null,'1202'],
                             [null,'1205'], ['1209','1209']]
    tmpTrainList[2].time == [[null,'1216'], [null,'1221'], [null,'passage'],
                             [null,'passage'], ['passage','passage']]
    tmpTrainList[3].time == [[null,'1234'], [null,'1240'], [null,'1246'],
                             [null,'1250'], ['1254','1254']]
    tmpTrainList[4].time == [[null,'1245'], [null,'1251'], [null,'passage'],
                             [null,'passage'], ['passage','passage']]
    tmpTrainList[5].time == [[null,'1302'], [null,'1307'], [null,'1311'],
                             [null,'1314'], ['1318','1318']]
    tmpTrainList[6].time == [[null,''], [null,''], [null,''], [null,''],
                             ['','']]
    tmpTrainList[7].time == [[null,'1351'], [null,'1356'], [null,'passage'],
                             [null,'passage'], ['passage','passage']]
    tmpTrainList[8].time == [[null,'1414'], [null,'1419'], [null,'passage'],
                             [null,'passage'], ['passage','passage']]
    tmpTrainList[9].time == [[null,'1431'], [null,'1436'], [null,'1443'],
                             [null,'1446'], ['1450','1451']]
    tmpTrainList[10].time == [[null,'1456'], [null,'1501'], [null,'passage'],
                              [null,'passage'], ['passage','passage']]
    tmpTrainList[11].time == [[null,'1505'], [null,'1510'], [null,'passage'],
                              [null,'passage'], ['passage','passage']]
    tmpTrainList[12].time == [[null,'1545'], [null,'1550'], [null,'passage'],
                              [null,'passage'], ['passage','passage']]
    tmpTrainList[13].time == [[null,'1614'], [null,'1620'], [null,'1626'],
                              [null,'1630'], ['1634','']]
    tmpTrainList[14].time == [[null,'1635'], [null,'1640'], [null,'passage'],
                              [null,'passage'], ['passage','passage']]
  }

  def "列車のsort_非経由列車"() {
    setup:
    def train323D = new Train('323D')
    train323D.time = [[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,'0650'] as String[],['0655','0656'] as String[],[null,'0659'] as String[],[null,'0703'] as String[],[null,'0711'] as String[],[null,'0725'] as String[],[null,'0734'] as String[],[null,'0737'] as String[],['0741','0742'] as String[],[null,'0746'] as String[],[null,'0751'] as String[],['0757','0758'] as String[],[null,'0803'] as String[],[null,'0807'] as String[],[null,'0812'] as String[],[null,'0816'] as String[],[null,'0821'] as String[],[null,'0827'] as String[],['0833','0834'] as String[],[null,'0843'] as String[],[null,'0846'] as String[],[null,'0851'] as String[],[null,'0855'] as String[],[null,'0905'] as String[],['0910',null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[]]
    def train375D = new Train('375D')
    train375D.time = [[null,'||'] as String[],[null,'||'] as String[],[null,'||'] as String[],[null,'||'] as String[],[null,'||'] as String[],['||','||'] as String[],[null,'||'] as String[],[null,'||'] as String[],[null,'||'] as String[],['||','||'] as String[],[null,'||'] as String[],[null,'||'] as String[],['||','||'] as String[],['||','||'] as String[],[null,'||'] as String[],[null,'||'] as String[],[null,'||'] as String[],[null,'||'] as String[],[null,'||'] as String[],[null,'||'] as String[],['||','||'] as String[],[null,'||'] as String[],[null,'||'] as String[],['||','||'] as String[],[null,'||'] as String[],[null,'||'] as String[],[null,'||'] as String[],[null,'||'] as String[],[null,'||'] as String[],[null,'||'] as String[],['||','0818'] as String[],[null,'0824'] as String[],[null,'0827'] as String[],[null,'0838'] as String[],[null,'0841'] as String[],[null,'0846'] as String[],['0851',null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[]]
    def train347D = new Train('347D')
    train347D.time = [[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,'0813'] as String[],[null,'0821'] as String[],[null,'0826'] as String[],[null,'0834'] as String[],[null,'0841'] as String[],[null,'0846'] as String[],[null,'0852'] as String[],[null,'0905'] as String[],['0912',null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[]]
    def train863D = new Train('863D')
    train863D.time = [[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,'0908'] as String[],[null,'0912'] as String[],[null,'0916'] as String[],[null,'0921'] as String[],[null,'0926'] as String[],[null,'0931'] as String[],[null,'0935'] as String[],[null,'0939'] as String[],[null,'0942'] as String[],[null,'0949'] as String[],[null,'0954'] as String[]]
    def train125D = new Train('125D')
    train125D.time = [[null,'0609'] as String[],[null,'0618'] as String[],[null,'0623'] as String[],[null,'0630'] as String[],[null,'0634'] as String[],['0641','0642'] as String[],[null,'0646'] as String[],[null,'0651'] as String[],[null,'0657'] as String[],['0702','0703'] as String[],[null,'0708'] as String[],[null,'0716'] as String[],['0722',null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[],[null,null] as String[]]
    def trainList = [train323D, train375D, train347D, train863D, train125D]

    expect:
    train323D.compareTo(train375D) > 0 // train323D > train375D
    train375D.compareTo(train323D) < 0 // train375D < train323D

    train323D.compareTo(train347D) > 0 // train323D > train347D
    train347D.compareTo(train323D) < 0 // train347D < train323D

    train375D.compareTo(train347D) > 0 // train375D > train347D
    train347D.compareTo(train375D) < 0 // train347D < train375D

    train323D.compareTo(train863D) > 0 // train323D > train863D
    train863D.compareTo(train323D) < 0 // train863D < train323D

    train347D.compareTo(train863D) > 0 // train347D > train863D
    train863D.compareTo(train347D) < 0 // train863D < train347D

    train375D.compareTo(train863D) > 0 // train375D > train863D
    train863D.compareTo(train375D) < 0 // train863D < train375D

    train125D.compareTo(train323D) > 0 // train125D > train323D
    train323D.compareTo(train125D) < 0 // train323D < train125D

    // trainList.sort() == [train863D, train347D, train375D, train323D, train125D] // 順番をちゃんとするには、各駅間の標準運転時分が必要
  }
}
