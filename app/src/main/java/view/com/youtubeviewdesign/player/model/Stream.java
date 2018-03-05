package view.com.youtubeviewdesign.player.model;

/**
 * Created by user on 31/10/17.
 */

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

public abstract class Stream implements Serializable {
    public String url;
    public int format = -1;

    public Stream(String url, int format) {
        this.url = url;
        this.format = format;
    }

    public boolean equalStats(Stream cmp) {
        return cmp != null && this.format == cmp.format;
    }

    public boolean equals(Stream cmp) {
        return this.equalStats(cmp) && this.url.equals(cmp.url);
    }

    public static boolean containSimilarStream(Stream stream, List<? extends Stream> streamList) {
        if(stream != null && streamList != null) {
            Iterator var2 = streamList.iterator();

            Stream cmpStream;
            do {
                if(!var2.hasNext()) {
                    return false;
                }

                cmpStream = (Stream)var2.next();
            } while(!stream.equalStats(cmpStream));

            return true;
        } else {
            return false;
        }
    }
}
