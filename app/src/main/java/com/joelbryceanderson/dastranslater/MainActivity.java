package com.joelbryceanderson.dastranslater;

import android.animation.Animator;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActionBarActivity {

    private FloatingActionButton myFab;
    private Spinner translateTo;
    private Spinner translateFrom;
    private SharedPreferences preferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle("Visual Translator");
        CardView card = (CardView) findViewById(R.id.card_view);
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.fly_in);
        card.startAnimation(anim);
        final ImageView header = (ImageView) findViewById(R.id.header_image);
        List<String> languages = new ArrayList<>();
        languages.add("English");
        languages.add("French");
        languages.add("Spanish");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, languages);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        translateFrom = (Spinner) findViewById(R.id.translate_from);
        translateFrom.setAdapter(adapter);
        translateTo = (Spinner) findViewById(R.id.translate_to);
        translateTo.setAdapter(adapter);
        translateFrom.setSelection(2);
        translateFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Animator reveal =
                            ViewAnimationUtils.createCircularReveal(header,
                                    header.getWidth() / 2,
                                    header.getHeight() / 2,
                                    0,
                                    Math.max(header.getWidth(), header.getHeight()));
                    reveal.setDuration(500);
                    reveal.start();
                }
                if (position == 0) {
                    header.setImageResource(R.drawable.usa);
                } else if (position == 1) {
                    header.setImageResource(R.drawable.par);
                } else if (position == 2) {
                    header.setImageResource(R.drawable.bcn);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        myFab = (FloatingActionButton)  findViewById(R.id.fab);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startCamera();
            }
        });
        Animation anim2 = AnimationUtils.loadAnimation(this, R.anim.scale_up);
        myFab.startAnimation(anim2);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences.getBoolean("first_time", true)) {
            new AlertDialog.Builder(this)
                    .setTitle("Notice")
                    .setMessage("This is your first time using visual translator! " +
                            "Language data needs to be installed. Press okay to continue.")
                    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            new loadLanguageData().execute("");
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:
                new AlertDialog.Builder(this)
                        .setTitle("About")
                        .setMessage(getString(R.string.about_text))
                        .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    void startCamera() {
        if (translateTo.getSelectedItemPosition() == translateFrom.getSelectedItemPosition()) {
            new AlertDialog.Builder(this)
                    .setTitle("Notice")
                    .setMessage("Your original language and " +
                            "desired language are the same. Continue?")
                    .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent =
                                    new Intent(getApplicationContext(), CameraActivity.class);
                            intent.putExtra("toLang", translateTo.getSelectedItemPosition());
                            intent.putExtra("fromLang", translateFrom.getSelectedItemPosition());
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .show();
        } else {
            Intent intent = new Intent(this, CameraActivity.class);
            intent.putExtra("toLang", translateTo.getSelectedItemPosition());
            intent.putExtra("fromLang", translateFrom.getSelectedItemPosition());
            startActivity(intent);
        }
    }


    private class loadLanguageData extends AsyncTask<String, Void, String> {

        private ProgressDialog prog;

        @Override
        protected String doInBackground(String... params) {
            new FileHelper().FileHelper(getApplicationContext());
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            preferences.edit().putBoolean("first_time", false).commit();
            prog.dismiss();
        }

        @Override
        protected void onPreExecute() {
            prog = new ProgressDialog(MainActivity.this);
            prog.setMessage("Loading language data...");
            prog.setIndeterminate(true);
            prog.show();
        }

        @Override
        protected void onProgressUpdate(Void... values) {}
    }
}
