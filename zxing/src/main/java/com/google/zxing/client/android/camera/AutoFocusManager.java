/*
 * Copyright (C) 2012 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.client.android.camera;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import com.google.zxing.client.android.PreferencesActivity;

@SuppressWarnings("deprecation") // camera APIs
final class AutoFocusManager implements Camera.AutoFocusCallback {

  private static final String TAG = AutoFocusManager.class.getSimpleName();

  //在自动聚焦的时候会根据该变量设定的时间来睡眠
  private static final long AUTO_FOCUS_INTERVAL_MS = 1500L;
  private static final long AUTO_FOCUS_STOP = 3000L;
  private static final Collection<String> FOCUS_MODES_CALLING_AF;
  static {
    FOCUS_MODES_CALLING_AF = new ArrayList<>(2);
    FOCUS_MODES_CALLING_AF.add(Camera.Parameters.FOCUS_MODE_AUTO);
    FOCUS_MODES_CALLING_AF.add(Camera.Parameters.FOCUS_MODE_MACRO);
  }

  private boolean stopped;
  private boolean focusing;
  private final boolean useAutoFocus;
  private final Camera camera;
  private AsyncTask<?,?,?> outstandingTask;
  private AutoStopTask autoStopTask;

  AutoFocusManager(Context context, Camera camera) {
    this.camera = camera;
    SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    String currentFocusMode = camera.getParameters().getFocusMode();
    useAutoFocus =
        sharedPrefs.getBoolean(PreferencesActivity.KEY_AUTO_FOCUS, true) &&
        FOCUS_MODES_CALLING_AF.contains(currentFocusMode);
    Log.i(TAG, "Current focus mode '" + currentFocusMode + "'; use auto focus? " + useAutoFocus);
    start();
  }

  @Override
  public synchronized void onAutoFocus(boolean success, Camera theCamera) {
      //dzm modify:executeOnExecutor这个函数是API11后提供的新函数，
      //内部传入参数AsyncTask.THREAD_POOL_EXECUTOR是设定线程可以并发，但是不能超过5个，
      //这样会对我们新修改的代码造成线程阻塞，对焦几次后边不会再自动对焦了，所以要做修改，不限制线程的并发
      //目前测试没有问题，不知道会不会因为线程回收不即使导致程序崩溃
      if (focusing) {
          outstandingTask = new AutoFocusTask();
//      outstandingTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
          outstandingTask.executeOnExecutor(Executors.newCachedThreadPool());
          autoStopTask = new AutoStopTask();
//      autoStopTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
          autoStopTask.executeOnExecutor(Executors.newCachedThreadPool());
          focusing = false;
      }
//          focusing = false;
//    autoFocusAgainLater();
  }

  private synchronized void autoFocusAgainLater() {
    if (!stopped && outstandingTask == null) {
      AutoFocusTask newTask = new AutoFocusTask();
      try {
        newTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        outstandingTask = newTask;
      } catch (RejectedExecutionException ree) {
        Log.w(TAG, "Could not request auto focus", ree);
      }
    }
  }

  synchronized void start() {
    if (useAutoFocus) {
      outstandingTask = null;
      //dzm modify
        //if (!stopped && !focusing)
      if (!focusing) {
        try {
          camera.autoFocus(this);
          focusing = true;
        } catch (RuntimeException re) {
          // Have heard RuntimeException reported in Android 4.0.x+; continue?
          Log.w(TAG, "Unexpected exception while focusing", re);
          // Try again later to keep cycle going
          autoFocusAgainLater();
        }
      }
    }
  }

  private synchronized void cancelOutstandingTask() {
    if (outstandingTask != null) {
      if (outstandingTask.getStatus() != AsyncTask.Status.FINISHED) {
        outstandingTask.cancel(true);
      }
      outstandingTask = null;
    }
  }

  private synchronized void cancelStopTask() {
      if(autoStopTask != null){
          autoStopTask.cancel(true);
          autoStopTask = null;
      }
      autoStopTask = null;
  }

  synchronized void stop() {
    stopped = true;
    if (useAutoFocus) {
      cancelOutstandingTask();
      cancelStopTask();
      // Doesn't hurt to call this even if not focusing
      try {
//        camera.cancelAutoFocus();
        start();
      } catch (RuntimeException re) {
        // Have heard RuntimeException reported in Android 4.0.x+; continue?
        Log.w(TAG, "Unexpected exception while cancelling focusing", re);
      }
    }
  }

  private final class AutoFocusTask extends AsyncTask<Object,Object,Object> {
    @Override
    protected Object doInBackground(Object... voids) {
      try {
        Thread.sleep(AUTO_FOCUS_INTERVAL_MS);
      } catch (InterruptedException e) {
        // continue
      }
      start();
      return null;
    }
  }

  //dzm modify:停止变焦
    private final class AutoStopTask extends AsyncTask<Object, Object, Object>{

        @Override
        protected Object doInBackground(Object... params) {
            try {
                Thread.sleep(AUTO_FOCUS_STOP);
            } catch (InterruptedException e) {
                // continue
            }
            synchronized (AutoFocusManager.this) {
                stop();
            }
            return null;
        }

    }

}
