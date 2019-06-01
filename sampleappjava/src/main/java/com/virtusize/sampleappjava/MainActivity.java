package com.virtusize.sampleappjava;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.virtusize.libsource.model.VirtusizeButtonStyle;
import com.virtusize.libsource.model.VirtusizeProduct;
import com.virtusize.libsource.ui.FitIllustratorButton;

public class MainActivity extends AppCompatActivity {

    FitIllustratorButton fitIllustratorButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fitIllustratorButton = findViewById(R.id.exampleFitButton);
        App app = (App) getApplication();
        app.Virtusize.setupFitButton(fitIllustratorButton, new VirtusizeProduct("694", "https://www.publicdomainpictures.net/pictures/120000/velka/dress-1950-vintage-style.jpg"));
        fitIllustratorButton.applyStyle(VirtusizeButtonStyle.DEFAULT_STYLE);
    }
}
