package com.lutshe.wallpaper.live.gifanimated;

import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.Handler;
import android.os.SystemClock;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.io.InputStream;

public class GifAnimationService extends WallpaperService {

    static final Handler handler = new Handler();

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public Engine onCreateEngine() {
        try {
            return new GifEngine();
        } catch (IOException e) {
            stopSelf();
            return null;
        }
    }

    class GifEngine extends Engine {
        private final Movie movie;
        private int duration;
        private final Runnable animate;
        float scaleX;
        float scaleY;
        int when;
        long start;

        GifEngine() throws IOException {
            InputStream is = getResources().openRawResource(R.raw.kisonka);
            if (is != null) {
                try {
                    movie = Movie.decodeStream(is);
                    System.out.println(movie.width()+ " "+ movie.height());
                    duration = movie.duration();
                } finally {
                    is.close();
                }
            } else {
                throw new IOException("Unable to open R.raw.process");
            }

            when = -1;
            animate = new Runnable() {
                public void run() {
                    process();
                }
            };
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            handler.removeCallbacks(animate);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            if (visible) {
                process();
            } else {
                handler.removeCallbacks(animate);
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            scaleX = width / (1f * movie.width());
            scaleY = height / (1f * movie.height());
            process();
        }

        @Override
        public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep,
                                     float yOffsetStep, int xPixelOffset, int yPixelOffset) {
            super.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep, xPixelOffset, yPixelOffset);
            process();
        }

        void process() {
            tick();
            SurfaceHolder surfaceHolder = getSurfaceHolder();
            Canvas canvas = null;
            try {
                canvas = surfaceHolder.lockCanvas();
                if (canvas != null) {
                    onDraw(canvas);
                }
            } finally {
                if (canvas != null) {
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
            handler.removeCallbacks(animate);
            if (isVisible()) {
                handler.postDelayed(animate, 1000L / 25L);
            }
        }

        void tick() {
            if (when == -1L) {
                when = 0;
                start = SystemClock.uptimeMillis();
            } else {
                long mDiff = SystemClock.uptimeMillis() - start;
                when = (int) (mDiff % duration);
            }
        }

        void onDraw(Canvas canvas) {
            canvas.save();
            canvas.scale(scaleX, scaleY);
            movie.setTime(when);
            movie.draw(canvas, 0, 0);
            canvas.restore();
        }
    }
}
