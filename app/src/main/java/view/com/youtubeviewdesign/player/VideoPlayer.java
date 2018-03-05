/*
 * Copyright 2017 Mauricio Colli <mauriciocolli@outlook.com>
 * VideoPlayer.java is part of NewPipe
 *
 * License: GPL-3.0+
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package view.com.youtubeviewdesign.player;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;



import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import view.com.youtubeviewdesign.R;
import view.com.youtubeviewdesign.player.model.AudioStream;
import view.com.youtubeviewdesign.player.model.VideoStream;
import view.com.youtubeviewdesign.player.util.AnimationUtils;
import view.com.youtubeviewdesign.player.util.Utils;

import static view.com.youtubeviewdesign.player.util.AnimationUtils.animateView;


/**
 * Base for <b>video</b> players
 *
 * @author mauriciocolli
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class VideoPlayer extends BasePlayer implements SimpleExoPlayer.VideoListener, SeekBar.OnSeekBarChangeListener, View.OnClickListener, Player.EventListener, PopupMenu.OnMenuItemClickListener, PopupMenu.OnDismissListener {
    public static final boolean DEBUG = BasePlayer.DEBUG;
    public final String TAG;

    /*//////////////////////////////////////////////////////////////////////////
    // Intent
    //////////////////////////////////////////////////////////////////////////*/

    public static final String VIDEO_STREAMS_LIST = "video_streams_list";
    public static final String VIDEO_ONLY_AUDIO_STREAM = "video_only_audio_stream";
    public static final String INDEX_SEL_VIDEO_STREAM = "index_selected_video_stream";
    public static final String STARTED_FROM_NEWPIPE = "started_from_newpipe";

    private int selectedIndexStream;
    private ArrayList<VideoStream> videoStreamsList = new ArrayList<>();
    private AudioStream videoOnlyAudioStream;

    /*//////////////////////////////////////////////////////////////////////////
    // Player
    //////////////////////////////////////////////////////////////////////////*/

    public static final int DEFAULT_CONTROLS_HIDE_TIME = 2000;  // 2 Seconds
    private static final float[] PLAYBACK_SPEEDS = {0.5f, 0.75f, 1f, 1.25f, 1.5f, 1.75f, 2f};

    private boolean startedFromNewPipe = true;
    private boolean wasPlaying = false;

    /*//////////////////////////////////////////////////////////////////////////
    // Views
    //////////////////////////////////////////////////////////////////////////*/

    private View rootView;

    private AspectRatioFrameLayout aspectRatioFrameLayout;
    private TextureView surfaceView;
    private View surfaceForeground;

    private View loadingPanel;
    private ImageView endScreen;
    private ImageView controlAnimationView;

    private View controlsRoot;
    private TextView currentDisplaySeek;

    private View bottomControlsRoot;
    private SeekBar playbackSeekBar;
    private TextView playbackCurrentTime;
    private TextView playbackEndTime;
    private TextView playbackSpeed;

    private View topControlsRoot;
    private TextView qualityTextView;
    private ImageButton fullScreenButton;

    private ValueAnimator controlViewAnimator;
    private Handler controlsVisibilityHandler = new Handler();

    private boolean isSomePopupMenuVisible = false;
    private boolean qualityChanged = false;
    private int qualityPopupMenuGroupId = 69;
    private PopupMenu qualityPopupMenu;

    private int playbackSpeedPopupMenuGroupId = 79;
    private PopupMenu playbackSpeedPopupMenu;

    ///////////////////////////////////////////////////////////////////////////

    public VideoPlayer(String debugTag, Context context) {
        super(context);
        this.TAG = debugTag;
        this.context = context;
    }

    public void setup(View rootView) {
        initViews(rootView);
        setup();
    }

    public void initViews(View rootView)
    {

        this.rootView = rootView;
        this.aspectRatioFrameLayout = (AspectRatioFrameLayout) rootView.findViewById(R.id.aspectRatioLayout);
        this.surfaceView = (TextureView) rootView.findViewById(R.id.surfaceView);
        this.surfaceForeground = rootView.findViewById(R.id.surfaceForeground);
        this.loadingPanel = rootView.findViewById(R.id.loading_panel);
        this.endScreen = (ImageView) rootView.findViewById(R.id.endScreen);
        this.controlAnimationView = (ImageView) rootView.findViewById(R.id.controlAnimationView);
        this.controlsRoot = rootView.findViewById(R.id.playbackControlRoot);
        this.currentDisplaySeek = (TextView) rootView.findViewById(R.id.currentDisplaySeek);
        this.playbackSeekBar = (SeekBar) rootView.findViewById(R.id.playbackSeekBar);
        this.playbackCurrentTime = (TextView) rootView.findViewById(R.id.playbackCurrentTime);
        this.playbackEndTime = (TextView) rootView.findViewById(R.id.playbackEndTime);
        this.playbackSpeed = (TextView) rootView.findViewById(R.id.playbackSpeed);
        this.bottomControlsRoot = rootView.findViewById(R.id.bottomControls);
        this.topControlsRoot = rootView.findViewById(R.id.topControls);
        this.qualityTextView = (TextView) rootView.findViewById(R.id.qualityTextView);
        this.fullScreenButton = (ImageButton) rootView.findViewById(R.id.fullScreenButton);

        //this.aspectRatioFrameLayout.setAspectRatio(16.0f / 9.0f);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            playbackSeekBar.getThumb().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
        this.playbackSeekBar.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);

        this.qualityPopupMenu = new PopupMenu(context, qualityTextView);
        this.playbackSpeedPopupMenu = new PopupMenu(context, playbackSpeed);

        ((ProgressBar) this.loadingPanel.findViewById(R.id.progressBarLoadingPanel)).getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);

    }

    @Override
    public void initListeners() {
        super.initListeners();

        playbackSeekBar.setOnSeekBarChangeListener(this);
        playbackSpeed.setOnClickListener(this);
        fullScreenButton.setOnClickListener(this);
        qualityTextView.setOnClickListener(this);

    }

    @Override
    public void initPlayer() {
        super.initPlayer();
        simpleExoPlayer.setVideoTextureView(surfaceView);
        simpleExoPlayer.setVideoListener(this);
       // surfaceView.getHolder().setFixedSize(surfaceView.getWidth(), surfaceView.getHeight());
    }

    @SuppressWarnings("unchecked")
    public void handleIntent(Bundle intent) {
        super.handleIntent(intent);

        System.out.println("Listing=yesCall");
        if (DEBUG) Log.d(TAG, "handleIntent() called with: intent = [" + intent + "]");
        if (intent == null) return;

        Bundle bundle;


        selectedIndexStream = intent.getInt(INDEX_SEL_VIDEO_STREAM, -1);

        Serializable serializable = intent.getSerializable(VIDEO_STREAMS_LIST);

        if (serializable instanceof ArrayList) videoStreamsList = (ArrayList<VideoStream>) serializable;
        if (serializable instanceof Vector) videoStreamsList = new ArrayList<>((List<VideoStream>) serializable);

        Serializable audioStream = intent.getSerializable(VIDEO_ONLY_AUDIO_STREAM);


        if (audioStream != null) videoOnlyAudioStream = (AudioStream) audioStream;
        startedFromNewPipe = intent.getBoolean(STARTED_FROM_NEWPIPE, true);
        play(true);


    }

    boolean flagAudio_Video=false;
    public void play(boolean autoPlay) {


        //playUrl(getSelectedVideoStream().url, MediaFormat.getSuffixById(getSelectedVideoStream().format), autoPlay);
        playUrl(getSelectedVideoStream().url, Utils.FORMATE, autoPlay);

    }

    @Override
    public void playUrl(String url, String format, boolean autoPlay) {
        if (DEBUG) Log.d(TAG, "play() called with: url = [" + url + "], autoPlay = [" + autoPlay + "]");
        qualityChanged = false;

        if (url == null || simpleExoPlayer == null) {
            RuntimeException runtimeException = new RuntimeException((url == null ? "Url " : "Player ") + " null");
            onError(runtimeException);
            throw runtimeException;
        }

        qualityPopupMenu.getMenu().removeGroup(qualityPopupMenuGroupId);


        buildQualityMenu(qualityPopupMenu);

        playbackSpeedPopupMenu.getMenu().removeGroup(playbackSpeedPopupMenuGroupId);
        buildPlaybackSpeedMenu(playbackSpeedPopupMenu);
        System.out.println("PlayBackUrl="+url + "  , formate="+ format);


        super.playUrl(url, format, autoPlay);
    }

    @Override
    public MediaSource buildMediaSource(String url, String overrideExtension) {
        MediaSource mediaSource = super.buildMediaSource(url, overrideExtension);
        if (!getSelectedVideoStream().isVideoOnly || videoOnlyAudioStream == null) return mediaSource;

        Uri audioUri = Uri.parse(videoOnlyAudioStream.url);
        return new MergingMediaSource(mediaSource, new ExtractorMediaSource(audioUri, cacheDataSourceFactory, extractorsFactory, null, null));
    }
    boolean addAudio=false;
    public void buildQualityMenu(PopupMenu popupMenu) {


        if(!addAudio)
        {
            //Add Audio File manualy by Abhay
            VideoStream audioStreams= new VideoStream(getAudioStream().url,getAudioStream().format,String.valueOf(getAudioStream().average_bitrate));
            videoStreamsList.add(audioStreams);
            //Add Audio File manualy by Abhay  end code
            addAudio=true;
        }

        for (int i = 0; i < videoStreamsList.size(); i++) {

            VideoStream videoStream = videoStreamsList.get(i);

            popupMenu.getMenu().add(qualityPopupMenuGroupId, i, Menu.NONE, Utils.FORMATE + " " + videoStream.resolution);

        }

        //Add For Audio Formate
        // AudioStream audioStream = getAudioStream();
        // popupMenu.getMenu().add(qualityPopupMenuGroupId, 0, Menu.NONE, MediaFormat.getNameById(audioStream.format) + " " + audioStream.average_bitrate);


        qualityTextView.setText(getSelectedVideoStream().resolution);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.setOnDismissListener(this);

    }

    private void buildPlaybackSpeedMenu(PopupMenu popupMenu) {
        for (int i = 0; i < PLAYBACK_SPEEDS.length; i++) {
            popupMenu.getMenu().add(playbackSpeedPopupMenuGroupId, i, Menu.NONE, formatSpeed(PLAYBACK_SPEEDS[i]));
        }
        playbackSpeed.setText(formatSpeed(getPlaybackSpeed()));
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.setOnDismissListener(this);
    }

    /*//////////////////////////////////////////////////////////////////////////
    // States Implementation
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    public void onLoading() {
        if (DEBUG) Log.d(TAG, "onLoading() called");

        if (!isProgressLoopRunning.get()) startProgressLoop();

        controlsVisibilityHandler.removeCallbacksAndMessages(null);
        animateView(controlsRoot, false, 300);

        showAndAnimateControl(-1, true);
        playbackSeekBar.setEnabled(true);
        playbackSeekBar.setProgress(0);

        // Bug on lower api, disabling and enabling the seekBar resets the thumb color -.-, so sets the color again
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            playbackSeekBar.getThumb().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);

        animateView(endScreen, false, 0);
        loadingPanel.setBackgroundColor(Color.BLACK);
        animateView(loadingPanel, true, 0);
        animateView(surfaceForeground, true, 100);
    }

    @Override
    public void onPlaying() {
        if (DEBUG) Log.d(TAG, "onPlaying() called");
        if (!isProgressLoopRunning.get()) startProgressLoop();
        showAndAnimateControl(-1, true);
        loadingPanel.setVisibility(View.GONE);
        showControlsThenHide();
        animateView(currentDisplaySeek, AnimationUtils.Type.SCALE_AND_ALPHA, false, 200);
    }

    @Override
    public void onBuffering() {
        if (DEBUG) Log.d(TAG, "onBuffering() called");
        loadingPanel.setBackgroundColor(Color.TRANSPARENT);
        animateView(loadingPanel, true, 500);
    }

    @Override
    public void onPaused() {
        if (DEBUG) Log.d(TAG, "onPaused() called");
        showControls(400);
        loadingPanel.setVisibility(View.GONE);
    }

    @Override
    public void onPausedSeek() {
        if (DEBUG) Log.d(TAG, "onPausedSeek() called");
        showAndAnimateControl(-1, true);
    }

    @Override
    public void onCompleted() {
        if (DEBUG) Log.d(TAG, "onCompleted() called");

        if (isProgressLoopRunning.get()) stopProgressLoop();

        showControls(500);
        animateView(endScreen, true, 800);
        animateView(currentDisplaySeek, AnimationUtils.Type.SCALE_AND_ALPHA, false, 200);
        loadingPanel.setVisibility(View.GONE);

        playbackSeekBar.setMax((int) simpleExoPlayer.getDuration());
        playbackSeekBar.setProgress(playbackSeekBar.getMax());
        playbackSeekBar.setEnabled(false);
        playbackEndTime.setText(getTimeString(playbackSeekBar.getMax()));
        playbackCurrentTime.setText(playbackEndTime.getText());
        // Bug on lower api, disabling and enabling the seekBar resets the thumb color -.-, so sets the color again
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            playbackSeekBar.getThumb().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);

        animateView(surfaceForeground, true, 100);

        if (currentRepeatMode == RepeatMode.REPEAT_ONE) {
            changeState(STATE_LOADING);
            simpleExoPlayer.seekTo(0);
        }
    }

    /*//////////////////////////////////////////////////////////////////////////
    // ExoPlayer Video Listener
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
        if (DEBUG) {
            Log.d(TAG, "onVideoSizeChanged() called with: width / height = [" + width + " / " + height + " = " + (((float) width) / height) + "], unappliedRotationDegrees = [" + unappliedRotationDegrees + "], pixelWidthHeightRatio = [" + pixelWidthHeightRatio + "]");
        }
        aspectRatioFrameLayout.setAspectRatio(((float) width) / height);
    }

    @Override
    public void onRenderedFirstFrame() {
        animateView(surfaceForeground, false, 100);
    }

    /*//////////////////////////////////////////////////////////////////////////
    // General Player
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    public void onPrepared(boolean playWhenReady) {
        if (DEBUG) Log.d(TAG, "onPrepared() called with: playWhenReady = [" + playWhenReady + "]");

        if (videoStartPos > 0) {
            playbackSeekBar.setProgress((int) videoStartPos);
            playbackCurrentTime.setText(getTimeString((int) videoStartPos));
            videoStartPos = -1;
        }

        playbackSeekBar.setMax((int) simpleExoPlayer.getDuration());
        playbackEndTime.setText(getTimeString((int) simpleExoPlayer.getDuration()));
        playbackSpeed.setText(formatSpeed(getPlaybackSpeed()));
        super.onPrepared(playWhenReady);
    }

    @Override
    public void destroy() {
        super.destroy();
        if (endScreen != null) endScreen.setImageBitmap(null);
    }

    @Override
    public void onUpdateProgress(int currentProgress, int duration, int bufferPercent) {
        if (!isPrepared) return;

        if (currentState != STATE_PAUSED) {
            if (currentState != STATE_PAUSED_SEEK) playbackSeekBar.setProgress(currentProgress);
            playbackCurrentTime.setText(getTimeString(currentProgress));
        }
        if (simpleExoPlayer.isLoading() || bufferPercent > 90) {
            playbackSeekBar.setSecondaryProgress((int) (playbackSeekBar.getMax() * ((float) bufferPercent / 100)));
        }
        if (DEBUG && bufferPercent % 20 == 0) { //Limit log
            Log.d(TAG, "updateProgress() called with: isVisible = " + isControlsVisible() + ", currentProgress = [" + currentProgress + "], duration = [" + duration + "], bufferPercent = [" + bufferPercent + "]");
        }
    }

    @Override
    public void onVideoPlayPauseRepeat() {
        if (DEBUG) Log.d(TAG, "onVideoPlayPauseRepeat() called");
        if (qualityChanged) {
            setVideoStartPos(0);
            play(true);
        } else super.onVideoPlayPauseRepeat();
    }

    @Override
    public void onThumbnailReceived(Bitmap thumbnail) {
        super.onThumbnailReceived(thumbnail);
        if (thumbnail != null) endScreen.setImageBitmap(thumbnail);
    }

    protected abstract void onFullScreenButtonClicked();

    @Override
    public void onFastRewind() {
        super.onFastRewind();
        showAndAnimateControl(R.drawable.ic_action_av_fast_rewind, true);
    }

    @Override
    public void onFastForward() {
        super.onFastForward();
        showAndAnimateControl(R.drawable.ic_action_av_fast_forward, true);
    }

    /*//////////////////////////////////////////////////////////////////////////
    // OnClick related
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    public void onClick(View v) {
        if (DEBUG) Log.d(TAG, "onClick() called with: v = [" + v + "]");
        if (v.getId() == fullScreenButton.getId()) {
            onFullScreenButtonClicked();
        } else if (v.getId() == qualityTextView.getId()) {
            onQualitySelectorClicked();
        } else if (v.getId() == playbackSpeed.getId()) {
            onPlaybackSpeedClicked();
        }
    }

    /**
     * Called when an item of the quality selector or the playback speed selector is selected
     */

    String audioTypeSelection=null;
    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        if (DEBUG)
            Log.d(TAG, "onMenuItemClick() called with: menuItem = [" + menuItem + "], menuItem.getItemId = [" + menuItem.getItemId() + "]");
        System.out.println("MenuItem="+menuItem + " ,"+ menuItem.getGroupId() + " , "+qualityPopupMenuGroupId + " , "+ selectedIndexStream);
        audioTypeSelection=menuItem.toString();
        if (qualityPopupMenuGroupId == menuItem.getGroupId()) {
            if (selectedIndexStream == menuItem.getItemId()) return true;
            setVideoStartPos(simpleExoPlayer.getCurrentPosition());

            selectedIndexStream = menuItem.getItemId();
           System.out.println("SelectedIndex="+selectedIndexStream + " ,"+ videoStreamsList.size());
            if (!(getCurrentState() == STATE_COMPLETED)) play(wasPlaying);
            else qualityChanged = true;

            qualityTextView.setText(menuItem.getTitle());

            return true;
        } else if (playbackSpeedPopupMenuGroupId == menuItem.getGroupId()) {
            int speedIndex = menuItem.getItemId();
            float speed = PLAYBACK_SPEEDS[speedIndex];

            setPlaybackSpeed(speed);
            playbackSpeed.setText(formatSpeed(speed));
        }

        return false;
    }

    /**
     * Called when some popup menu is dismissed
     */
    @Override
    public void onDismiss(PopupMenu menu) {
        if (DEBUG) Log.d(TAG, "onDismiss() called with: menu = [" + menu + "]");
        isSomePopupMenuVisible = false;
        qualityTextView.setText(getSelectedVideoStream().resolution);
    }

    public void onQualitySelectorClicked() {
        if (DEBUG) Log.d(TAG, "onQualitySelectorClicked() called");
        qualityPopupMenu.show();
        isSomePopupMenuVisible = true;
        showControls(300);

        VideoStream videoStream = getSelectedVideoStream();
        qualityTextView.setText(Utils.FORMATE + " " + videoStream.resolution);
        wasPlaying = isPlaying();
    }

    private void onPlaybackSpeedClicked() {
        if (DEBUG) Log.d(TAG, "onPlaybackSpeedClicked() called");
        playbackSpeedPopupMenu.show();
        isSomePopupMenuVisible = true;
        showControls(300);
    }

    /*//////////////////////////////////////////////////////////////////////////
    // SeekBar Listener
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (DEBUG && fromUser) Log.d(TAG, "onProgressChanged() called with: seekBar = [" + seekBar + "], progress = [" + progress + "]");
        //if (fromUser) playbackCurrentTime.setText(getTimeString(progress));
        if (fromUser) currentDisplaySeek.setText(getTimeString(progress));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        if (DEBUG) Log.d(TAG, "onStartTrackingTouch() called with: seekBar = [" + seekBar + "]");
        if (getCurrentState() != STATE_PAUSED_SEEK) changeState(STATE_PAUSED_SEEK);

        wasPlaying = isPlaying();
        if (isPlaying()) simpleExoPlayer.setPlayWhenReady(false);

        showControls(0);
        animateView(currentDisplaySeek, AnimationUtils.Type.SCALE_AND_ALPHA, true, 300);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (DEBUG) Log.d(TAG, "onStopTrackingTouch() called with: seekBar = [" + seekBar + "]");

        simpleExoPlayer.seekTo(seekBar.getProgress());
        if (wasPlaying || simpleExoPlayer.getDuration() == seekBar.getProgress()) simpleExoPlayer.setPlayWhenReady(true);

        playbackCurrentTime.setText(getTimeString(seekBar.getProgress()));
        animateView(currentDisplaySeek, AnimationUtils.Type.SCALE_AND_ALPHA, false, 200);

        if (getCurrentState() == STATE_PAUSED_SEEK) changeState(STATE_BUFFERING);
        if (!isProgressLoopRunning.get()) startProgressLoop();
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Utils
    //////////////////////////////////////////////////////////////////////////*/

    public boolean isControlsVisible() {
        return controlsRoot != null && controlsRoot.getVisibility() == View.VISIBLE;
    }

    /**
     * Show a animation, and depending on goneOnEnd, will stay on the screen or be gone
     *
     * @param drawableId the drawable that will be used to animate, pass -1 to clear any animation that is visible
     * @param goneOnEnd  will set the animation view to GONE on the end of the animation
     */
    public void showAndAnimateControl(final int drawableId, final boolean goneOnEnd) {
        if (DEBUG) Log.d(TAG, "showAndAnimateControl() called with: drawableId = [" + drawableId + "], goneOnEnd = [" + goneOnEnd + "]");
        if (controlViewAnimator != null && controlViewAnimator.isRunning()) {
            if (DEBUG) Log.d(TAG, "showAndAnimateControl: controlViewAnimator.isRunning");
            controlViewAnimator.end();
        }

        if (drawableId == -1) {
            if (controlAnimationView.getVisibility() == View.VISIBLE) {
                controlViewAnimator = ObjectAnimator.ofPropertyValuesHolder(controlAnimationView,
                        PropertyValuesHolder.ofFloat(View.ALPHA, 1f, 0f),
                        PropertyValuesHolder.ofFloat(View.SCALE_X, 1.4f, 1f),
                        PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.4f, 1f)
                ).setDuration(300);
                controlViewAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        controlAnimationView.setVisibility(View.GONE);
                    }
                });
                controlViewAnimator.start();
            }
            return;
        }

        float scaleFrom = goneOnEnd ? 1f : 1f, scaleTo = goneOnEnd ? 1.8f : 1.4f;
        float alphaFrom = goneOnEnd ? 1f : 0f, alphaTo = goneOnEnd ? 0f : 1f;


        controlViewAnimator = ObjectAnimator.ofPropertyValuesHolder(controlAnimationView,
                PropertyValuesHolder.ofFloat(View.ALPHA, alphaFrom, alphaTo),
                PropertyValuesHolder.ofFloat(View.SCALE_X, scaleFrom, scaleTo),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, scaleFrom, scaleTo)
        );
        controlViewAnimator.setDuration(goneOnEnd ? 1000 : 500);
        controlViewAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (goneOnEnd) controlAnimationView.setVisibility(View.GONE);
                else controlAnimationView.setVisibility(View.VISIBLE);
            }
        });


        controlAnimationView.setVisibility(View.VISIBLE);
        controlAnimationView.setImageDrawable(ContextCompat.getDrawable(context, drawableId));
        controlViewAnimator.start();
    }

    public boolean isSomePopupMenuVisible() {
        return isSomePopupMenuVisible;
    }

    public void showControlsThenHide() {
        if (DEBUG) Log.d(TAG, "showControlsThenHide() called");
        animateView(controlsRoot, true, 300, 0, new Runnable() {
            @Override
            public void run() {
                hideControls(300, DEFAULT_CONTROLS_HIDE_TIME);
            }
        });
    }

    public void showControls(long duration) {
        if (DEBUG) Log.d(TAG, "showControls() called");
        controlsVisibilityHandler.removeCallbacksAndMessages(null);
        animateView(controlsRoot, true, duration);
    }

    public void hideControls(final long duration, long delay) {
        if (DEBUG) Log.d(TAG, "hideControls() called with: delay = [" + delay + "]");
        controlsVisibilityHandler.removeCallbacksAndMessages(null);
        controlsVisibilityHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                animateView(controlsRoot, false, duration);
            }
        }, delay);
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Getters and Setters
    //////////////////////////////////////////////////////////////////////////*/

    public AspectRatioFrameLayout getAspectRatioFrameLayout() {
        return aspectRatioFrameLayout;
    }

    public TextureView getSurfaceView() {
        return surfaceView;
    }

    public boolean wasPlaying() {
        return wasPlaying;
    }

    public VideoStream getSelectedVideoStream() {
        return videoStreamsList.get(selectedIndexStream);
    }

    public Uri getSelectedStreamUri() {
        return Uri.parse(getSelectedVideoStream().url);
    }

    public int getQualityPopupMenuGroupId() {
        return qualityPopupMenuGroupId;
    }

    public int getSelectedStreamIndex() {
        return selectedIndexStream;
    }

    public void setSelectedIndexStream(int selectedIndexStream) {
        this.selectedIndexStream = selectedIndexStream;
    }

    public void setAudioStream(AudioStream audioStream) {
        this.videoOnlyAudioStream = audioStream;
    }

    public AudioStream getAudioStream() {
        return videoOnlyAudioStream;
    }

    public ArrayList<VideoStream> getVideoStreamsList() {
        return videoStreamsList;
    }

    public void setVideoStreamsList(ArrayList<VideoStream> videoStreamsList) {
        this.videoStreamsList = videoStreamsList;
    }

    public boolean isStartedFromNewPipe() {
        return startedFromNewPipe;
    }

    public void setStartedFromNewPipe(boolean startedFromNewPipe) {
        this.startedFromNewPipe = startedFromNewPipe;
    }

    public Handler getControlsVisibilityHandler() {
        return controlsVisibilityHandler;
    }

    public View getRootView() {
        return rootView;
    }

    public void setRootView(View rootView) {
        this.rootView = rootView;
    }

    public View getLoadingPanel() {
        return loadingPanel;
    }

    public ImageView getEndScreen() {
        return endScreen;
    }

    public ImageView getControlAnimationView() {
        return controlAnimationView;
    }

    public View getControlsRoot() {
        return controlsRoot;
    }

    public View getBottomControlsRoot() {
        return bottomControlsRoot;
    }

    public SeekBar getPlaybackSeekBar() {
        return playbackSeekBar;
    }

    public TextView getPlaybackCurrentTime() {
        return playbackCurrentTime;
    }

    public TextView getPlaybackEndTime() {
        return playbackEndTime;
    }

    public View getTopControlsRoot() {
        return topControlsRoot;
    }

    public TextView getQualityTextView() {
        return qualityTextView;
    }

    public ImageButton getFullScreenButton() {
        return fullScreenButton;
    }

    public PopupMenu getQualityPopupMenu() {
        return qualityPopupMenu;
    }

    public View getSurfaceForeground() {
        return surfaceForeground;
    }

    public TextView getCurrentDisplaySeek() {
        return currentDisplaySeek;
    }

}
