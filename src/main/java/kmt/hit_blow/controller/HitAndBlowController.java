package kmt.hit_blow.controller;

import java.util.Random;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import kmt.hit_blow.model.HitAndBlow;
import kmt.hit_blow.service.AsyncHitAndBlow;
import kmt.hit_blow.model.User;
import kmt.hit_blow.model.Match;
import kmt.hit_blow.model.MatchInfo;
import kmt.hit_blow.model.MatchUser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.ModelMap;

@Controller
public class HitAndBlowController {

  @Autowired
  private AsyncHitAndBlow HitAndBlow;

  int flag = 0;
  int[] playeranswer = new int[4];// 自分の回答
  int[] rivalanswer = new int[4];// 相手の回答
  String Myanswers; // 自分の回答（文字列）
  String Rivalanswers;// 相手の回答（文字列）
  int battleid = 0; // 対戦相手のidを格納する
  int itemflag = 1;
  List<String> candidates;
  boolean solved = false;

  @GetMapping("/hit-blow") // hit-blow.htmlに遷移する
  public String hit_blow(ModelMap model, Principal prin) {
    // 表示に必要なデータをMapperで格納する
    String loginUser = prin.getName();// ログイン名を取得
    ArrayList<User> users = this.HitAndBlow.asyncSelectExceptByloginUsers(loginUser);
    ArrayList<Match> notactivematches = this.HitAndBlow.asyncSelectAllNotActiveByMatches();// 非アクティブの試合を渡す
    ArrayList<MatchUser> notactivematcheslist = new ArrayList<MatchUser>();// アクティブな試合結果を渡す
    for (int i = 0; i < notactivematches.size(); i++) { // Userid1と2に対応する名前を格納する
      String username1 = this.HitAndBlow.asyncSelectNameByUsers(notactivematches.get(i).getUserid1());
      String username2 = this.HitAndBlow.asyncSelectNameByUsers(notactivematches.get(i).getUserid2());
      notactivematcheslist.add(new MatchUser(notactivematches.get(i), username1, username2));
    }


    ArrayList<Match> activematches = this.HitAndBlow.asyncSelectAllActiveByMatches();// アクティブな試合を取得（観戦者ロール用）

    ArrayList<MatchUser> activematcheslist = new ArrayList<MatchUser>();// アクティブな試合結果を渡す
    for (int i = 0; i < activematches.size(); i++) { // Userid1と2に対応する名前を格納する
      String username1 = this.HitAndBlow.asyncSelectNameByUsers(activematches.get(i).getUserid1());
      String username2 = this.HitAndBlow.asyncSelectNameByUsers(activematches.get(i).getUserid2());
      activematcheslist.add(new MatchUser(activematches.get(i), username1, username2));
    }
    // 次の行にユーザロール用のマッチング待ちのMapper処理を書く

    if (loginUser != "Spectator") {// 観戦者でない場合
      int myid = this.HitAndBlow.asyncSelectIdByName(loginUser);
      ArrayList<Integer> waitmatchesid = this.HitAndBlow.asyncSelectMatchIdByIsActive(myid);
      ArrayList<String> waitmatchesname = new ArrayList<String>();
      ArrayList<User> waitusers = new ArrayList<User>();
      for (int i = 0; i < waitmatchesid.size(); i++) {
        waitmatchesname.add(this.HitAndBlow.asyncSelectNameByUsers(waitmatchesid.get(i)));
        waitusers.add(new User(waitmatchesid.get(i), waitmatchesname.get(i)));
      }
      model.addAttribute("waitmatches", waitusers); // ここにマッチング待ち処理を渡す
    }

    // 表示に必要なデータをmodelに渡す
    model.addAttribute("users", users);
    model.addAttribute("notactivematcheslist", notactivematcheslist);
    model.addAttribute("activematcheslist", activematcheslist);
    return "hitandblow.html";
  }

  @GetMapping("/history") // historyに遷移する
  public String history(@RequestParam("matchid") int matchid, ModelMap model, Principal prin) {
    Match match = this.HitAndBlow.asyncSelectMatchById(matchid);
    ArrayList<MatchInfo> matchInfo = this.HitAndBlow.asyncSelectByMatchId(matchid);
    int myid = this.HitAndBlow.asyncSelectUserId1ByMatchId(matchid);
    int opponentsid = this.HitAndBlow.asyncSelectUserId2ByMatchId(matchid);
    String myname = this.HitAndBlow.asyncSelectNameByUsers(myid);
    String opponentsname = this.HitAndBlow.asyncSelectNameByUsers(opponentsid);

    model.addAttribute("match", match);
    model.addAttribute("matchInfo", matchInfo);
    model.addAttribute("battleid", opponentsid);
    model.addAttribute("loginid", myid);
    model.addAttribute("myname", myname);
    model.addAttribute("opponentsname", opponentsname);
    return "history.html";
  }

  @GetMapping("/match") // 対戦相手を決定する
  public String match(@RequestParam Integer userid, ModelMap model, Principal prin) {
    String loginUser = prin.getName(); // ログイン名を取得
    String rivalname = this.HitAndBlow.asyncSelectNameByUsers(userid);// 対戦相手の名前を取得する変数
    this.itemflag = 1;// ラーの鏡を使用可能にする
    if (userid == 3) {// CPU戦のとき
      String message = loginUser + "の秘密の数字入力を待っています。";// システムメッセージを格納する変数
      model.addAttribute("name", loginUser);
      model.addAttribute("rivalname", rivalname);// Thymeleafで値をHTMLに渡す
      model.addAttribute("message", message);// Thymeleafで値をHTMLに渡す
      model.addAttribute("mysecret", "????");// 自分の秘密の数字は最初????のため
      model.addAttribute("rivalsecret", "????");// 相手の秘密の数字は????のため
      this.battleid = userid;// ここは一度しか経由しないから
      return "match.html";
    }
    if (userid == this.HitAndBlow.asyncSelectIdByName(loginUser)) {// User1がUser1と対戦できないようにする 例外処理
      return this.hit_blow(model, prin);
    }
    // 以降 ログインIDとクリック時のIDは異なる
    int loginid = this.HitAndBlow.asyncSelectIdByName(loginUser);
    int formboolean = 1;
    model.addAttribute("rivalname", rivalname);// Thymeleafで値をHTMLに渡す
    model.addAttribute("myid", loginid);// 自分のid
    model.addAttribute("rivalid", userid);// 相手のid
    model.addAttribute("formboolean", formboolean);// フォーム表示用のboolean
    return "wait.html";
  }

  @PostMapping("/play") // ここで対戦の処理をする
  public String play(@RequestParam Integer line1, @RequestParam Integer line2, @RequestParam Integer line3,
      @RequestParam Integer line4, ModelMap model, Principal prin) {

    int[] in = { line1, line2, line3, line4 }; // 入力を配列に格納する
    String mysecret = this.Myanswers;// 自分の？？？？と表示されている秘密の数字を格納する変数
    int myHit = 0; // 自分のHitを数える変数
    int myBlow = 0; // 自分のBlowを数える変数
    String rivalsecret = "????";// 相手の？？？？と表示されている秘密の数字を格納する変数
    int rivalHit = 0; // 相手のHitを数える変数
    int rivalBlow = 0; // 相手のBlowを数える変数
    String rivalname = this.HitAndBlow.asyncSelectNameByUsers(battleid);// 対戦相手の名前を取得する変数
    int[] Hit_Blow; // HitとBlowの値を格納する配列
    int goakflag = 0; // 正解かどうかの判定

    HitAndBlow cheak = new HitAndBlow(); // Hit_Blowクラスのメソッドを呼び出す

    String loginUser = prin.getName(); // ログイン名を取得
    String message = loginUser + "の数字入力を待っています。";// システムメッセージを格納する変数
    int loginUser_id = this.HitAndBlow.asyncSelectIdByName(loginUser);// 自分のID取得

    if (cheak.numFormat(in) != true) { // 数値の重複があった場合
      // 重複しているため例外処理を行う。
      message = loginUser + "の数字入力を待っています。" + "エラー：数値を重複させないでください。";
      model.addAttribute("message", message);// Thymeleafで値をHTMLに渡す
      if (this.flag == 0) {// 初回の際に重複した場合、Myanswerがnullのため
        mysecret = "????";
      }
      model.addAttribute("mysecret", mysecret);
      model.addAttribute("rivalsecret", rivalsecret);
      model.addAttribute("rivalname", rivalname);
      model.addAttribute("name", loginUser);
      return "match.html";
    }

    if (this.flag == 0) { // 初回はここに入る
      this.itemflag = 1;// ラーの鏡を使用可能にする
      this.Myanswers = cheak.translateString(in);// ここで自分の答えを4桁の文字列にする
      this.playeranswer = in;// 自分の答えを格納する
      mysecret = this.Myanswers;// 自分の秘密の数字が確定したため更新

      // ここからは相手の答えを生成する
      this.rivalanswer = cheak.generateNumber();// 相手の答えを生成する
      this.Rivalanswers = cheak.translateString(this.rivalanswer); // ここで相手の答えを4桁の文字列にする
      this.candidates = cheak.generateAllCandidates();

      Match match = new Match(loginUser_id, battleid, this.Myanswers, this.Rivalanswers, "", true);// 自分のid,相手のid,自分の答え,相手の答えを格納
      this.HitAndBlow.asyncInsertMatch(match);// 1試合追加(勝敗は不明)
      this.flag = 1; // 生成は1回のみだから

      model.addAttribute("message", message);// Thymeleafで値をHTMLに渡す
      model.addAttribute("mysecret", mysecret);
      model.addAttribute("rivalsecret", rivalsecret);
      model.addAttribute("rivalname", rivalname);
      model.addAttribute("name", loginUser);
      int getanswer = 0;
      model.addAttribute("getanswer", getanswer);//
      model.addAttribute("itemflag", itemflag);
      return "match.html";
    }
    // 以降はflag=1。つまり、秘密の数字決定後の処理を行う

    int matchid = this.HitAndBlow.asyncSelectMatchIdByuserId(loginUser_id, battleid);

    Hit_Blow = cheak.chackHit_Blow(in, this.rivalanswer);// HitとBlowを確認する
    myHit = Hit_Blow[0];
    myBlow = Hit_Blow[1];

    String myguess = cheak.translateString(in); // ここで自分の予想を4桁の文字列にする
    MatchInfo mymatchInfo = new MatchInfo(matchid, loginUser_id, myguess, myHit, myBlow, true); // 情報を格納する
    this.HitAndBlow.asyncInsertMatchInfo(mymatchInfo);

    if (myHit == 4) { // Hitが4だと正解にする
      goakflag = 1;
      this.flag = 0;
      message = loginUser + "の勝利です。";
      this.itemflag = 0;// ゲーム終了時にラーの鏡を押させないようにするため
      rivalsecret = this.Rivalanswers;// 相手の？？？？を開示
      Match match = new Match(matchid, loginUser_id, battleid, this.Myanswers, this.Rivalanswers, loginUser + "の勝利!",
          false);// 勝敗を更新
      this.HitAndBlow.asyncUpdateById(match);
      this.HitAndBlow.asyncUpdateActive(match);
      this.HitAndBlow.asyncUpdateActive(mymatchInfo);
    } else if (battleid == 3) {// battleid =3はCPUである。CPU戦の場合の処理をelse ifで記述している
      // プレイヤーが勝利していないためcpuの手を決める
      //
      Random rnd = new Random();
      int rand = rnd.nextInt(9999);
      String guess = candidates.get(rand % this.candidates.size());
      int[] rivalguess = cheak.changeint(guess);
      Hit_Blow = cheak.chackHit_Blow(rivalguess, this.playeranswer);// HitとBlowの数を計算
      rivalHit = Hit_Blow[0];
      rivalBlow = Hit_Blow[1];

      // 強化学習
      this.candidates = cheak.filterCandidates(this.candidates, guess, rivalHit, rivalBlow);

      String rivalguesshand = cheak.translateString(rivalguess);// ここで相手の予想を4桁の文字列にする
      MatchInfo rivalmatchInfo = new MatchInfo(matchid, battleid, rivalguesshand, rivalHit, rivalBlow, true); // 情報を格納する
      this.HitAndBlow.asyncInsertMatchInfo(rivalmatchInfo);
      //
    }

    if (rivalHit == 4) {// Hitが4だと正解にする
      goakflag = 1;
      this.flag = 0;
      this.itemflag = 0;// ゲーム終了時にラーの鏡を押させないようにするため
      message = rivalname + "の勝利です。";
      Match match = new Match(matchid, loginUser_id, battleid, this.Myanswers, this.Rivalanswers, rivalname + "の勝利!",
          false);// 勝敗を更新
      this.HitAndBlow.asyncUpdateById(match);
      this.HitAndBlow.asyncUpdateActive(match);
      this.HitAndBlow.asyncUpdateActive(mymatchInfo);
    }

    ArrayList<MatchInfo> matchInfo = this.HitAndBlow.asyncSelectByMatchId(matchid);

    model.addAttribute("matchInfo", matchInfo);// Thymeleafで試合情報をHTMLに渡す
    model.addAttribute("message", message);// システムメッセージを表示するために用いる
    model.addAttribute("goalflag", goakflag);// ゲーム終了時用のフラグ
    model.addAttribute("debuganswer", this.rivalanswer);// デバッグ用製品版で削除すること。

    model.addAttribute("name", loginUser);// 自身の名前を表示するために用いる
    model.addAttribute("mysecret", mysecret);// 自身の秘密の数字部分を表示する
    model.addAttribute("loginid", loginUser_id);// matchInfoで自身の試合情報を表示するために用いる

    model.addAttribute("rivalname", rivalname);// 相手の名前を表示するために用いる
    model.addAttribute("rivalsecret", rivalsecret);// 相手の秘密の数字部分を表示する
    model.addAttribute("battleid", battleid);// matchInfoで相手の試合情報を表示するために用いる

    int getanswer = 0;
    model.addAttribute("getanswer", getanswer);//
    model.addAttribute("itemflag", itemflag);

    return "match.html";
  }

  @GetMapping("/spect") // 対戦相手を決定する
  public String spect(@RequestParam Integer matchid, ModelMap model, Principal prin) {
    String message = "観戦用まだ未実装だよ";// SSE通信で共有？
    int user1id = this.HitAndBlow.asyncSelectUserId1ByMatchId(matchid);// user1のidを取得
    int user2id = this.HitAndBlow.asyncSelectUserId2ByMatchId(matchid);// user2のidを取得
    String user1name = this.HitAndBlow.asyncSelectNameByUsers(user1id);// user1の名前を取得
    String user2name = this.HitAndBlow.asyncSelectNameByUsers(user2id);// user2の名前を取得
    String user1num = this.HitAndBlow.asyncSelectUserNum1ByMatchId(matchid);// user1の秘密の数字
    String user2num = this.HitAndBlow.asyncSelectUserNum2ByMatchId(matchid);// user2の秘密の数字
    ArrayList<MatchInfo> matchinfo = this.HitAndBlow.asyncSelectByMatchId(matchid);// matchinfoの情報を全て取得
    // ArrayList<MatchInfo> hogehoge; //SSE通信でリアルタイム試合状況を共有

    model.addAttribute("message", message);
    model.addAttribute("user1id", user1id);
    model.addAttribute("user2id", user2id);
    model.addAttribute("name", user1name);
    model.addAttribute("rivalname", user2name);
    model.addAttribute("user1num", user1num);
    model.addAttribute("user2num", user2num);
    model.addAttribute("matchInfo", matchinfo);

    return "spectators.html";
  }

  @PostMapping("/item") // 対戦相手を決定する 2PCでも使用する。
  public String item(@RequestParam Integer useitem, ModelMap model, Principal prin) {
    this.itemflag = 0;
    int getanswer = this.rivalanswer[useitem - 1];
    String loginUser = prin.getName(); // ログイン名を取得
    String message = loginUser + "の数字入力を待っています。";// システムメッセージを格納する変数
    int loginUser_id = this.HitAndBlow.asyncSelectIdByName(loginUser);// 自分のID取得
    int matchid = this.HitAndBlow.asyncSelectMatchIdByuserId(loginUser_id, battleid);
    String mysecret = this.Myanswers;// 自分の？？？？と表示されている秘密の数字を格納する変数
    String rivalname = this.HitAndBlow.asyncSelectNameByUsers(battleid);// 対戦相手の名前を取得する変数
    String rivalsecret = "????";// 相手の？？？？と表示されている秘密の数字を格納する変数
    ArrayList<MatchInfo> matchInfo = this.HitAndBlow.asyncSelectByMatchId(matchid);

    model.addAttribute("getanswer", getanswer);// 相手の答え
    model.addAttribute("itemflag", itemflag);// アイテムを使用したか
    model.addAttribute("index", useitem);// 何番目の数字か
    model.addAttribute("matchInfo", matchInfo);// Thymeleafで試合情報をHTMLに渡す
    model.addAttribute("message", message);// システムメッセージを表示するために用いる

    model.addAttribute("name", loginUser);// 自身の名前を表示するために用いる
    model.addAttribute("mysecret", mysecret);// 自身の秘密の数字部分を表示する
    model.addAttribute("loginid", loginUser_id);// matchInfoで自身の試合情報を表示するために用いる

    model.addAttribute("rivalname", rivalname);// 相手の名前を表示するために用いる
    model.addAttribute("rivalsecret", rivalsecret);// 相手の秘密の数字部分を表示する
    model.addAttribute("battleid", battleid);// matchInfoで相手の試合情報を表示するために用いる

    return "match.html";
  }

}
