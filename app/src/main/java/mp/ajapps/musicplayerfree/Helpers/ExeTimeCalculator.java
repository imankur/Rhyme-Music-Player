package mp.ajapps.musicplayerfree.Helpers;

import java.util.ArrayList;

/**
 * Created by Sharing Happiness on 12/11/2015.
 */
public class ExeTimeCalculator {
    public static final int IN_MILLISECONDS = 1;
    public static final int IN_SECONDS = 2;
    StringBuilder mResult = new StringBuilder();

    private ArrayList<TimeFramePojo> mFrames = new ArrayList<TimeFramePojo>();
    private int mType = 1;

    private TimeFramePojo aFrame, bFrame;

    public ExeTimeCalculator() {
    }

    public ExeTimeCalculator(int selection) {
        if (selection == IN_SECONDS)
            this.mType = 1000;
    }

    public void addTimeFrame(String tag) {
        mFrames.add(new TimeFramePojo(tag));
    }

    public void printDifference() {
        for (TimeFramePojo tfp : mFrames) {
            if (aFrame == null) {
                aFrame = tfp;
                continue;
            }
            bFrame = tfp;
            mResult.append("Diff between ").append(aFrame.getMtag()).append(" and ").append(bFrame.getMtag()).append(" is : ");
            mResult.append(bFrame.getmTime() - aFrame.getmTime()).append("\n");
            aFrame = bFrame;
            bFrame = null;
        }
        mResult.append("Total time : ").append(mFrames.get(mFrames.size() - 1).getmTime() - mFrames.get(0).getmTime());
        System.out.println(mResult.toString());
    }

    private class TimeFramePojo {
        String mtag = null;
        long mTime = 0;

        public TimeFramePojo(String tag) {
            this.mtag = tag;
            this.mTime = System.currentTimeMillis() / mType;
        }

        public String getMtag() {
            return mtag;
        }

        public long getmTime() {
            return mTime;
        }
    }
}
