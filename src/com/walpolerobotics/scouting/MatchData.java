package com.walpolerobotics.scouting;

/**
 * Created by jelmhurst on 5/5/2016.
 */
public class MatchData {
    String startTime;
    Integer matchNumber = 0;
    Integer red1Number = 0;
    Integer red2Number = 0;
    Integer red3Number = 0;
    Integer blue1Number = 0;
    Integer blue2Number = 0;
    Integer blue3Number = 0;

    public MatchData(String startTime,Integer matchNum, Integer red1, Integer red2, Integer red3, Integer blue1, Integer blue2, Integer blue3) {
        this.startTime = new String(startTime);
        this.matchNumber = matchNum;
        this.red1Number = red1;
        this.red2Number = red2;
        this.red3Number = red3;
        this.blue1Number = blue1;
        this.blue2Number = blue2;
        this.blue3Number = blue3;
    }

    public MatchData(String startTime, Integer matchNum) {
        this(startTime,matchNum,0,0,0,0,0,0);
    }
}
