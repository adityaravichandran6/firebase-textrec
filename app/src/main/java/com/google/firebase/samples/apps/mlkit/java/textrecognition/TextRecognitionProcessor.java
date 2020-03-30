// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.firebase.samples.apps.mlkit.java.textrecognition;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.firebase.samples.apps.mlkit.common.CameraImageGraphic;
import com.google.firebase.samples.apps.mlkit.common.FrameMetadata;
import com.google.firebase.samples.apps.mlkit.common.GraphicOverlay;
import com.google.firebase.samples.apps.mlkit.java.ChooserActivity;
import com.google.firebase.samples.apps.mlkit.java.DbHelper;
import com.google.firebase.samples.apps.mlkit.java.LivePreviewActivity;
import com.google.firebase.samples.apps.mlkit.java.VisionProcessorBase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Processor for the text recognition demo.
 */
public class TextRecognitionProcessor extends VisionProcessorBase<FirebaseVisionText> {
    public DbHelper dbHelper;
    private static final String TAG = "TextRecProc";
    public Context context;
    public String data;
    public SQLiteDatabase db;

    Map<String, Boolean> map = new HashMap<String, Boolean>();

    private final FirebaseVisionTextRecognizer detector;

    public TextRecognitionProcessor(Context mContext) {
        detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        context = mContext;

        dbHelper = new DbHelper(context);
        try {
            dbHelper.createDataBase();
        } catch (Exception e) {
            e.printStackTrace();
        }

        dbHelper.openDataBase();
        dbHelper.close();
        db = dbHelper.getReadableDatabase();

        data = "";
    }

    @Override
    public void stop() {
        try {
            detector.close();
        } catch (IOException e) {
            Log.e(TAG, "Exception thrown while trying to close Text Detector: " + e);
        }
    }

    @Override
    protected Task<FirebaseVisionText> detectInImage(FirebaseVisionImage image) {
        return detector.processImage(image);
    }

    @Override
    protected void onSuccess(
            @Nullable Bitmap originalCameraImage,
            @NonNull FirebaseVisionText results,
            @NonNull FrameMetadata frameMetadata,
            @NonNull GraphicOverlay graphicOverlay) {
        graphicOverlay.clear();
        if (data.equals("")) {
            if (originalCameraImage != null) {
                CameraImageGraphic imageGraphic = new CameraImageGraphic(graphicOverlay,
                        originalCameraImage);
                graphicOverlay.add(imageGraphic);
            }
            List<FirebaseVisionText.TextBlock> blocks = results.getTextBlocks();
            for (int i = 0; i < blocks.size(); i++) {
                List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
                for (int j = 0; j < lines.size(); j++) {
                    List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
                    for (int k = 0; k < elements.size(); k++) {
                        GraphicOverlay.Graphic textGraphic = new TextGraphic(graphicOverlay,
                                elements.get(k));
                        graphicOverlay.add(textGraphic);

                        try {
                            if (elements.get(k).getText().length() > 2)
                                checkDatabase(elements.get(k).getText());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Log.d(TAG, data);
                    }
                }
            }
            graphicOverlay.postInvalidate();
        } else {
            Intent intent = new Intent(context, ChooserActivity.class);
            intent.putExtra("Data", data);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }

    }


    public void checkDatabase(String term) throws IOException {

        ArrayList<String> results = new ArrayList<String>();

        if(term.length() < 3 || term.contains("'"))
            return;

        Cursor res = db.rawQuery("SELECT name FROM products WHERE (name like '"+term+" %' OR name like '% "+term+"' OR name like '% "+term+" %')", null);
        res.moveToFirst();

        while (!res.isAfterLast()) {
//            Log.d(TAG, res.getString(0));
            results.add(res.getString(0));
            res.moveToNext();
        }

        res.close();
//        db.close();

        if(results.size()==1) {
            data = results.get(0);
            Log.d(TAG, term);
        }
//        for (int i = 0; i < results.size(); i++) {
//            data += results.get(i) + " : ";
//        }

    }

    @Override
    protected void onFailure(@NonNull Exception e) {
        Log.w(TAG, "Text detection failed." + e);
    }
}
