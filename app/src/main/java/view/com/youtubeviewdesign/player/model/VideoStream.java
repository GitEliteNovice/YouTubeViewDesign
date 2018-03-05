package view.com.youtubeviewdesign.player.model;



/**
 * Created by user on 31/10/17.
 */

public class VideoStream extends Stream {
    public String resolution;
    public boolean isVideoOnly;

    public VideoStream(String url, int format, String res) {
        this(url, format, res, false);
    }

    public VideoStream(String url, int format, String res, boolean isVideoOnly) {
        super(url, format);
        this.resolution = res;
        this.isVideoOnly = isVideoOnly;
    }

    public boolean equalStats(Stream cmp) {
        return super.equalStats(cmp) && cmp instanceof VideoStream && this.resolution.equals(((VideoStream)cmp).resolution) && this.isVideoOnly == ((VideoStream)cmp).isVideoOnly;
    }
}

