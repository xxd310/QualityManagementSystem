package com.zhihuitech.qualitymanagementsystem.util;

import android.util.Log;
import com.zhihuitech.qualitymanagementsystem.entity.PathUrl;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

/**
 * Created by Administrator on 2017/1/10.
 */
public class ImageDownloader {
    private static String TAG = "ImageDownloader" ;
    public static final int IO_BUFFER_SIZE = 8 * 1024;
    private static final String CACHE_FILENAME_PREFIX = "cache_";
    private static ExecutorService SINGLE_TASK_EXECUTOR = null;
    private static ExecutorService LIMITED_TASK_EXECUTOR = null;
    private static final ExecutorService FULL_TASK_EXECUTOR = null;
    private static final ExecutorService DEFAULT_TASK_EXECUTOR ;
    private static Object lock = new Object();
    static {
        // SINGLE_TASK_EXECUTOR = (ExecutorService)
        // Executors.newSingleThreadExecutor();
        LIMITED_TASK_EXECUTOR = (ExecutorService) Executors
                . newFixedThreadPool(1);
        // FULL_TASK_EXECUTOR = (ExecutorService)
        // Executors.newCachedThreadPool();
        DEFAULT_TASK_EXECUTOR = LIMITED_TASK_EXECUTOR ;
    };
    // 下载状态监听，提供回调
    DownloadStateListener listener;
    DownloadStateListener2 listener2;
    // 下载目录
    private String downloadPath;

    // 下载链接集合
    private List<PathUrl> listURL;
    // 下载个数
    private int size = 0;

    private static ImageDownloader downloader = new ImageDownloader();

    // 下载完成回调接口
    public interface DownloadStateListener {
        public void onFinish();

        public void onFailed();
    }

    // 下载完成回调接口
    public interface DownloadStateListener2 {
        public void onFinish();

        public void onFailed();
    }

    private ImageDownloader() {}


    //静态工厂方法
    public static ImageDownloader getInstance() {
        if (downloader == null) {
            synchronized (ImageDownloader.class) {
                if (downloader == null) {
                    downloader = new ImageDownloader();
                }
            }
        }
        return downloader;
    }

    public ImageDownloader(String downloadPath, List<PathUrl> listURL,
                                DownloadStateListener listener) {
        this.downloadPath = downloadPath;
        this.listURL = listURL;
        this.listener = listener;
    }

    /**
     * 暂未提供设置
     */
    public void setDefaultExecutor() {

    }

    /**
     * 开始下载
     */
    public void startDownload(String downloadPath, List<PathUrl> listURL, DownloadStateListener listener) {
        this.downloadPath = downloadPath;
        this.listURL = listURL;
        this.listener = listener;
        this.size = 0;

        // 首先检测path是否存在
        File downloadDirectory = new File(downloadPath);
        if (!downloadDirectory.exists()) {
            downloadDirectory.mkdirs();
        }

        for (final PathUrl pu : listURL) {
            //捕获线程池拒绝执行异常
            try {
                new Thread() {
                    @Override
                    public void run() {
                        downloadBitmap(pu);
                    }
                }.start();
                // 线程放入线程池
//                DEFAULT_TASK_EXECUTOR.execute(new Runnable() {
//                    @Override
//                    public void run() {
//                        downloadBitmap(url);
//                    }
//                });
            } catch (RejectedExecutionException e) {
                e.printStackTrace();
                Log. e(TAG, "thread pool rejected error");
                listener.onFailed();
            } catch (Exception e) {
                e.printStackTrace();
                listener.onFailed();
            }
        }
    }

    /**
     * 下载图片
     *
     * @param
     * @return
     */
    private File downloadBitmap(PathUrl pu) {
        String fileName = pu.getLocal_url().substring(pu.getLocal_url().lastIndexOf("/"));
//        // 图片命名方式
        final File cacheFile = new File(downloadPath + File.separator + fileName);

        HttpURLConnection urlConnection = null;
        BufferedOutputStream out = null;

        try {
            final URL url = new URL(pu.getUrl());
            urlConnection = (HttpURLConnection) url.openConnection();
            final InputStream in = new BufferedInputStream(
                    urlConnection.getInputStream(), IO_BUFFER_SIZE);
            out = new BufferedOutputStream(new FileOutputStream(cacheFile),
                    IO_BUFFER_SIZE);

            int b;
            while ((b = in.read()) != -1) {
                out.write(b);
            }
            // 每下载成功一个，统计一下图片个数
            statDownloadNum();
            return cacheFile;
        } catch (final IOException e) {
            // 有一个下载失败，则表示批量下载没有成功
            Log. e(TAG, "download " + pu.getUrl() + " error");
            listener.onFailed();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (out != null ) {
                try {
                    out.close();
                } catch (final IOException e) {
                    Log. e(TAG, "Error in downloadBitmap - " + e);
                }
            }
        }
        return null ;
    }

    /**
     * 统计下载个数
     */
    private void statDownloadNum() {
        synchronized (lock) {
            size++;
            if (size == listURL.size()) {
                Log. d(TAG, "download finished total " + size);
                // 释放资源
                DEFAULT_TASK_EXECUTOR.shutdownNow();
                // 如果下载成功的个数与列表中 url个数一致，说明下载成功
                listener.onFinish(); // 下载成功回调
            }
        }
    }

}
