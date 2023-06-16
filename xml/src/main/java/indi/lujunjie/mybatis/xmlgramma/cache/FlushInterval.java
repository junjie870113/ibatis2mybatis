package indi.lujunjie.mybatis.xmlgramma.cache;

/**
 * @author Lu Jun Jie
 * @date 2021-09-28 09:48
 */
public class FlushInterval {

    private String milliseconds;

    private String seconds;

    private String minutes;

    private String hours;

    public FlushInterval milliseconds(String milliseconds) {
        this.milliseconds = milliseconds;
        return this;
    }

    public FlushInterval seconds(String seconds) {
        this.seconds = seconds;
        return this;
    }

    public FlushInterval minutes(String minutes) {
        this.minutes = minutes;
        return this;
    }

    public FlushInterval hours(String hours) {
        this.hours = hours;
        return this;
    }

    public String getTime() {
        if (milliseconds != null) {
            return milliseconds;
        } else if (seconds != null) {
            return String.valueOf(Integer.parseInt(seconds) * 1000);
        } else if (minutes != null) {
            return String.valueOf(Integer.parseInt(minutes) * 60 * 1000);
        } else {
            return String.valueOf(Integer.parseInt(hours) * 60 * 60 * 1000);
        }
    }
}
