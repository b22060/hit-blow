package kmt.hit_blow.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import kmt.hit_blow.model.MatchInfoMapper;
import kmt.hit_blow.model.MatchInfo;
import kmt.hit_blow.model.MatchMapper;
import kmt.hit_blow.model.Match;
import kmt.hit_blow.model.UserMapper;
import kmt.hit_blow.model.User;

@Service
public class AsyncHitAndBlow {

  private int customerCount = 1;// customerロール用カウンター
  private int sellerCount = 1;// sellerロール用カウンター
  private final Logger logger = LoggerFactory.getLogger(AsyncHitAndBlow.class);

  private int hogehoge = 0;// ０→1→0と遷移する

  @Autowired
  private UserMapper userMapper;
  @Autowired
  private MatchMapper matchMapper;
  @Autowired
  private MatchInfoMapper matchInfoMapper;

  /* UserMapperを使った処理一覧 */
  public ArrayList<User> asyncSelectAllByUsers() {
    return this.userMapper.selectAllByUsers();
  }

  public String asyncSelectNameByUsers(int userid) {
    return this.userMapper.selectNameByUsers(userid);
  }

  public int asyncSelectIdByName(String userName) {
    return this.userMapper.selectIdByName(userName);
  }

  /* MatchMapperを使った処理一覧 */
  public ArrayList<Match> asyncSelectAllNotActiveByMatches() {
    return this.matchMapper.selectAllNotActiveByMatches();
  }

  public ArrayList<Match> asyncSelectAllActiveByMatches() {
    return this.matchMapper.selectAllActiveByMatches();
  }

  public Match asyncSelectMatchById(int matchid) {
    return this.matchMapper.selectMatchById(matchid);
  }

  public int asyncSelectUserId1ByMatchId(int matchid) {
    return this.matchMapper.selectUserId1ByMatchId(matchid);
  }

  public int asyncSelectUserId2ByMatchId(int matchid) {
    return this.matchMapper.selectUserId2ByMatchId(matchid);
  }

  public int asyncSelectMatchIdByuserId(int userid1, int userid2) {
    return this.matchMapper.selectMatchIdByuserId(userid1, userid2);
  }

  public void asyncInsertMatch(Match match) {
    this.matchMapper.insertMatch(match);
  }

  public void asyncUpdateById(Match match) {
    this.matchMapper.updateById(match);
  }

  public boolean asyncUpdateActive(Match match) {
    return this.matchMapper.updateActive(match);
  }

  public ArrayList<Integer> asyncSelectMatchIdByIsActive(int user2id) {
    return this.matchMapper.selectMatchIdByIsActive(user2id);
  }

  /* MatchInfoMapperを使った処理一覧 */

  public ArrayList<MatchInfo> asyncSelectByMatchId(int matchid) {
    return this.matchInfoMapper.selectByMatchId(matchid);
  }

  public void asyncInsertMatchInfo(MatchInfo matchinfo) {
    this.matchInfoMapper.insertMatchInfo(matchinfo);
  }

  public boolean asyncUpdateActive(MatchInfo matchinfo) {
    return this.matchInfoMapper.updateActive(matchinfo);
  }

  @Async
  public void asyncHitAndBlow(SseEmitter emitter) {

  }

  @Async
  public void count(SseEmitter emitter, String role) throws IOException {
    logger.info("AsyncCount58.count");
    try {
      while (true) {
        int counter = 0;
        // CUSTOMERとSELLERでカウンタを分ける
        // この2つ以外のロール場合は常にcounter=0
        if (role.equals("USER")) {
          counter = customerCount;
          customerCount++;
        } else if (role.equals("USER")) {
          counter = sellerCount;
          sellerCount++;
        }
        // ロールごとのカウンタとロール名を送る
        emitter.send(SseEmitter.event()
            .data(counter)
            .id(role));
        TimeUnit.SECONDS.sleep(1);
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public void samplechange() {
    if (hogehoge == 0) {
      hogehoge = 1;
    } else {
      hogehoge = 0;
    }
  }

  @Async
  public void sample(SseEmitter emitter) {

    logger.info("AsyncCount58.count");
    try {
      while (true) {
        if (hogehoge == 1) {
          TimeUnit.MILLISECONDS.sleep(50);
          continue;
        }
        TimeUnit.MILLISECONDS.sleep(50);
        if (hogehoge == 0) {

          emitter.send("SSEを通信開始");
          System.out.println("確認用！！！！！");
          TimeUnit.MILLISECONDS.sleep(5);
          this.samplechange();
        }

        TimeUnit.SECONDS.sleep(1);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
