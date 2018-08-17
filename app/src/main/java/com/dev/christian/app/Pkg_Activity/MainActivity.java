package com.dev.christian.app.Pkg_Activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import com.dev.christian.app.Pkg_Fragment.ConsultFragment;
import com.dev.christian.app.Pkg_Fragment.ProfileFragment;
import com.dev.christian.app.Pkg_Fragment.ScannerFragment;
import com.dev.christian.app.Pkg_Fragment.SesionFragment;
import com.dev.christian.app.R;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private boolean Logeado = false;
    public static String HOST = "http://www.conectatebitel.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        displaySelectedScreen(R.id.nav_home);
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


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        displaySelectedScreen(item.getItemId());
        return true;
    }
    private void displaySelectedScreen(int itemId) {
        Fragment fragment = null;

        switch (itemId) {
            case R.id.nav_home:
                fragment = new ScannerFragment();
                break;
            case R.id.nav_consult:
                fragment = new ConsultFragment();
                break;
            case R.id.nav_account:
                //fragment = new SesionFragment();
                VerificarSesion();
                break;
        }
        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    public void VerificarSesion() {

        SharedPreferences sharedPreferences = getApplication().getSharedPreferences("InicioSesion", Context.MODE_PRIVATE);
        Logeado = sharedPreferences.getBoolean("Logeado", false);

        if (Logeado) {
            ProfileFragment profileFragment = new ProfileFragment();
            FragmentTransaction fragmentTransaction2 = getSupportFragmentManager().beginTransaction();
            fragmentTransaction2.replace(R.id.content_frame, profileFragment, "Mi Perfil");
            fragmentTransaction2.commit();
        } else {
            SesionFragment sesionFragment = new SesionFragment();
            FragmentTransaction fragmentTransaction2 = getSupportFragmentManager().beginTransaction();
            fragmentTransaction2.replace(R.id.content_frame, sesionFragment, "login");
            fragmentTransaction2.commit();
        }

    }
}
