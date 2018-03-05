package view.com.youtubeviewdesign.player.model;

/**
 * Created by user on 31/10/17.
 */

public class AudioStream extends Stream {
    public int average_bitrate = -1;

    public AudioStream(String url, int format, int averageBitrate) {
        super(url, format);
        this.average_bitrate = averageBitrate;
    }

    public boolean equalStats(Stream cmp) {
        return super.equalStats(cmp) && cmp instanceof AudioStream && this.average_bitrate == ((AudioStream)cmp).average_bitrate;
    }
}
