/*
 * Copyright 2011 Robert Theis
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
package cl.roblelabs.tools.scans.mrzreader.ocr;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import cl.roblelabs.tools.scans.mrzreader.CaptureActivity;
import cl.roblelabs.tools.scans.mrzreader.R;

/**
 * Installs the language data required for OCR, and initializes the OCR engine using a background 
 * thread.
 */
public final class OcrInitAsyncTask extends AsyncTask<String, String, Boolean> {
  private static final String TAG = OcrInitAsyncTask.class.getSimpleName();

  private CaptureActivity activity;
  private Context context;
  private TessBaseAPI baseApi;
  private ProgressDialog dialog;
  private ProgressDialog indeterminateDialog;
  private final String languageCode;
  private String languageName;
  private int ocrEngineMode;

  /**
   * AsyncTask to asynchronously download data and initialize Tesseract.
   * 
   * @param activity
   *          The calling activity
   * @param baseApi
   *          API to the OCR engine
   * @param dialog
   *          Dialog box with thermometer progress indicator
   * @param indeterminateDialog
   *          Dialog box with indeterminate progress indicator
   * @param languageCode
   *          ISO 639-2 OCR language code
   * @param languageName
   *          Name of the OCR language, for example, "English"
   * @param ocrEngineMode
   *          Whether to use Tesseract, Cube, or both
   */
  public OcrInitAsyncTask(CaptureActivity activity, TessBaseAPI baseApi, ProgressDialog dialog,
                   ProgressDialog indeterminateDialog, String languageCode, String languageName,
                   int ocrEngineMode) {
    this.activity = activity;
    this.context = activity.getBaseContext();
    this.baseApi = baseApi;
    this.dialog = dialog;
    this.indeterminateDialog = indeterminateDialog;
    this.languageCode = languageCode;
    this.languageName = languageName;
    this.ocrEngineMode = ocrEngineMode;
  }

  @Override
  protected void onPreExecute() {
    super.onPreExecute();

    dialog.setMessage("Cargando información...");
    dialog.setIndeterminate(false);
    dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    dialog.setCancelable(false);
    dialog.show();
    activity.setButtonVisibility(false);
  }

  /**
   * In background thread, perform required setup, and request initialization of
   * the OCR engine.
   * 
   * @param params
   *          [0] Pathname for the directory for storing language data files to the SD card
   */
  protected Boolean doInBackground(String... params) {

    Resources res = activity.getResources();
    File folder = new File(this.activity.getCacheDir() + "/tessdata");
    if ( !folder.exists() && !folder.mkdirs()) {
      Log.e(TAG, "Couldn't make directory " + folder);
      return false;
    }
    File f = new File(this.activity.getCacheDir()+"/tessdata/eng.traineddata");
    try{
      InputStream is = res.openRawResource(R.raw.eng_traineddata);
      FileOutputStream fos = new FileOutputStream(f);
      copyFile(is,fos);
      fos.close();
      is.close();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    dialog.setProgress(50);

    File f2 = new File(this.activity.getCacheDir()+"/tessdata/eng.user-patterns");
    try{
      InputStream is = res.openRawResource(R.raw.eng_user_patterns);
      FileOutputStream fos = new FileOutputStream(f2);
      copyFile(is,fos);
      fos.close();
      is.close();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    dialog.setProgress(90);

      // Initialize the OCR engine
    if (baseApi.init(this.activity.getCacheDir() + File.separator, languageCode, ocrEngineMode)) {
      try {
        dialog.dismiss();
      } catch (IllegalArgumentException e) {
        // Catch "View not attached to window manager" error, and continue
      }
      dialog.setProgress(100);
      return true;
    }
    return false;
  }


  private boolean copyFile(InputStream in, OutputStream out) throws IOException {
    byte[] buffer = new byte[1024];
    int read;
    while((read = in.read(buffer)) != -1){
      out.write(buffer, 0, read);
    }
    return true;
  }


  /**
   * Update the dialog box with the latest incremental progress.
   * 
   * @param message
   *          [0] Text to be displayed
   * @param message
   *          [1] Numeric value for the progress
   */
  @Override
  protected void onProgressUpdate(String... message) {
    super.onProgressUpdate(message);
    int percentComplete = 0;
    percentComplete = Integer.parseInt(message[1]);
    dialog.setMessage(message[0]);
    dialog.setProgress(percentComplete);
    dialog.show();
  }

  @Override
  protected void onPostExecute(Boolean result) {
    super.onPostExecute(result);
    
    try {
      indeterminateDialog.dismiss();
    } catch (IllegalArgumentException e) {
      // Catch "View not attached to window manager" error, and continue
    }

    if (result) {
      // Restart recognition
      activity.resumeOCR();

    } else {
      activity.showErrorMessage("Error", "Network is unreachable - cannot download language data. "
          + "Please enable network access and restart this app.");
    }
  }

}