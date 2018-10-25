package funkymaster.com.rehamkhansbook;


import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnPageChangeListener {

    private NavigationView navigationView;
    private PDFView pdfView;
    private AdView mAdView;
    private InterstitialAd mInterstitialAd;
    private TextView fab_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //full page activity
        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
        this.overridePendingTransition(R.anim.drop_in, R.anim.drop_out);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ini_variables();
        show_book();
        banner_ad();
        interstitial_ad();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Please give us 5 stars.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                search_dialog();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);
    }

    private void ini_variables(){
        pdfView = (PDFView) findViewById(R.id.pdf_view);
        fab_view = (TextView) findViewById(R.id.fab_view);
    }

    private void show_book(){
        pdfView.fromAsset("book.pdf")
                .defaultPage(0)
                .onPageChange(this)
              //  .enableAnnotationRendering(true)
               // .onLoad(this)
              //  .scrollHandle(new DefaultScrollHandle(this))
                .load();
    }

    private void banner_ad(){
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    } //ending of banner ad

    private void interstitial_ad(){
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-1070460350235938/1954288039");
        AdRequest.Builder adRequestBuilder = new AdRequest.Builder();
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
            }
            @Override
            public void onAdClosed() {
                // Proceed to the next level.
                search_dialog();
            }
        });
        mInterstitialAd.loadAd(adRequestBuilder.build());
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public void rate_app()
    {
        try
        {
            Intent rateIntent = rateIntentForUrl("market://details");
            startActivity(rateIntent);
        }
        catch (ActivityNotFoundException e)
        {
            Intent rateIntent = rateIntentForUrl("https://play.google.com/store/apps/details");
            startActivity(rateIntent);
        }
    }

    private Intent rateIntentForUrl(String url)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("%s?id=%s", url, getPackageName())));
        int flags = Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK;
        if (Build.VERSION.SDK_INT >= 21)
        {
            flags |= Intent.FLAG_ACTIVITY_NEW_DOCUMENT;
        }
        else
        {
            //noinspection deprecation
            flags |= Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET;
        }
        intent.addFlags(flags);
        return intent;
    }

    private void my_other_apps(){
        //get user to other apps by Yalmaz Hasan Butt
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/developer?id=Funky+Studios."));
        startActivity(browserIntent);
    }

    private void search_dialog(){
        if(mInterstitialAd.isLoaded()){
            mInterstitialAd.show();
        }else {
            LayoutInflater li = LayoutInflater.from(MainActivity.this);
            View promptsView = li.inflate(R.layout.custom_seach_dialog, null);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    MainActivity.this);
            // set prompts.xml to alertdialog builder
            alertDialogBuilder.setView(promptsView);
            final EditText search_field = (EditText) promptsView.findViewById(R.id.search_field);
            search_field.setFilters(new InputFilter[]{new InputFilterMinMax("0", "364")});
            // set dialog message
            alertDialogBuilder
                    .setCancelable(false)
                    .setPositiveButton("Go",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // get user input and set it to result
                                    // edit text
                                    if (search_field.getText().toString().equalsIgnoreCase("")) {
                                        Toast.makeText(MainActivity.this, "Please input page number!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        goto_page(Integer.parseInt(search_field.getText().toString()));
                                    }
                                }
                            })
                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            // show it
        }
    }

    protected void goto_page(int page_number){
        pdfView.jumpTo(page_number);
        fab_view.setText(pdfView.getCurrentPage()+"");
    }

    public void onResume(){
        super.onResume();
        navigationView.getMenu().getItem(0).setChecked(true);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_book) {
            startActivity(new Intent(MainActivity.this, MainActivity.class));
            finish();
        } else if (id == R.id.nav_search) {
            search_dialog();
        } else if (id == R.id.nav_rate) {
            rate_app();
        } else if (id == R.id.nav_others) {
            my_other_apps();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        fab_view.setText(page+"");
    }
}
