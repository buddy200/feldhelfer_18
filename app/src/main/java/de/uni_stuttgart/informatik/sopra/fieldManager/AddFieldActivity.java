package de.uni_stuttgart.informatik.sopra.fieldManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.Vector;

import de.uni_stuttgart.informatik.sopra.fieldManager.UI.BottomSheets.BSDetailDialogEditAgrField;
import de.uni_stuttgart.informatik.sopra.fieldManager.UI.BottomSheets.BSDetailDialogEditDmgField;
import de.uni_stuttgart.informatik.sopra.fieldManager.UI.BottomSheets.BSEditHandler;
import de.uni_stuttgart.informatik.sopra.fieldManager.UI.BottomSheets.BottomSheetAddPhoto;
import de.uni_stuttgart.informatik.sopra.fieldManager.UI.Map.MapFragment;
import de.uni_stuttgart.informatik.sopra.fieldManager.UI.Map.MapViewHandler;
import de.uni_stuttgart.informatik.sopra.fieldManager.UI.TutorialOverlays;
import de.uni_stuttgart.informatik.sopra.fieldManager.Util.IntersectionCalculator;
import de.uni_stuttgart.informatik.sopra.fieldManager.Util.MYLocationListener;
import de.uni_stuttgart.informatik.sopra.fieldManager.Util.PointOutOfField;
import de.uni_stuttgart.informatik.sopra.fieldManager.data.AgrarianField;
import de.uni_stuttgart.informatik.sopra.fieldManager.data.DamageField;
import de.uni_stuttgart.informatik.sopra.fieldManager.data.Field;
import de.uni_stuttgart.informatik.sopra.fieldManager.data.managers.AppDataManager;


/**
 * sopra_priv
 * Created by Felix B on 10.11.17.
 * Mail: felix.burk@gmail.com
 * <p>
 * this activity lets users add fields depending
 * on their position
 */
public class AddFieldActivity extends AppCompatActivity implements FragmentInteractionListener<Object>, AppDataManager.DataChangeListener {
    private static final String TAG = "AddFieldActivity";

    MapFragment mapFragment;
    MYLocationListener myLocationListener;

    Field fieldToAddFinal;

    BSDetailDialogEditAgrField bottomSheetDialogAF;
    BSDetailDialogEditDmgField bottomSheetDialogDMF;

    ArrayList<GeoPoint> listGeoPoints = new ArrayList<>();

    boolean isDmgField = false;
    AgrarianField parentField;

    private MapViewHandler mMapViewHandler;
    private AppDataManager dataManager;

    private TextView fabLabel;
    private MenuItem menuItemDone;
    private Toolbar toolbar;

    private boolean enoughPoints = false;

    private Polyline polyline;

    private ArrayList<Vector<Double>> linesFromAgrarianField;
    private IntersectionCalculator intersectionCalculator;
    private PointOutOfField pointOutOfField;

    private SharedPreferences prefs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_add_field);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.title_activity_add_field);

        linesFromAgrarianField = new ArrayList<>();
        intersectionCalculator = new IntersectionCalculator(listGeoPoints, linesFromAgrarianField);
        pointOutOfField = new PointOutOfField();
        polyline = new Polyline();
        polyline.setColor(R.color.colorAccent);
        polyline.setWidth(2.0f);

        //back button on toolbar implementation
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //floating action button listener stuff
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFabClick(view);
            }
        });
        fabLabel = (TextView) findViewById(R.id.fab_label);

        //override onClick for toolbar arrow button,
        //to trigger going back to parent activty
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generateSaveDialog().show();
            }
        });

        menuItemDone = toolbar.getMenu().add(Menu.NONE, 1000, Menu.NONE, R.string.done);
        menuItemDone.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        invalidateOptionsMenu();

        mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment_add);

        dataManager = AppDataManager.getInstance(this);
        mMapViewHandler = new MapViewHandler(this, dataManager, mapFragment);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        mapFragment.setPresenter(mMapViewHandler);

    }

    @Override
    public void onStart() {
        super.onStart();

        loadFieldData();
        long i = getIntent().getLongExtra("parentField", -1);
        if (i != -1) {
            parentField = dataManager.getAgrarianFieldMap().get(i);
        } else {
            parentField = null;
        }
        if (parentField != null) {
            //we add a dmg field!
            isDmgField = true;
            //  mMapViewHandler.addField(parentField);
            getSupportActionBar().setTitle(R.string.title_activity_add_fieldDmg);
        }

        myLocationListener = new MYLocationListener();
        myLocationListener.initializeLocationManager(this, mMapViewHandler);
        myLocationListener.setFollow(true);
        Location location = myLocationListener.getLocation();
        if (location != null) {
            mMapViewHandler.animateAndZoomTo(myLocationListener.getLocation().getLatitude(),
                    myLocationListener.getLocation().getLongitude());
            mMapViewHandler.setCurrLocMarker(myLocationListener.getLocation().getLatitude(),
                    myLocationListener.getLocation().getLongitude());
        } else {
            Toast.makeText(this, getResources().getString(R.string.toastmsg_nolocation), Toast.LENGTH_SHORT).show();
        }
        dataManager.readData();

        if(!prefs.getBoolean(this.getResources().getString(R.string.pref_previously_started), false)){
            new TutorialOverlays().addFieldTutorial(this);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        onMapClick();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapViewHandler.saveMapCenter();
        mMapViewHandler.destroy();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_add_field, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_menu_done:
                onDoneButtonClick();
                break;
            case R.id.action_menu_toggleLoc:
                onToggleLocButtonClick();
                break;
            case R.id.action_menu_redo:
                onRedoButtonClick();
                break;
            case R.id.action_menu_tutorial:
                new TutorialOverlays().addFieldTutorial(this);
                break;
        }
        return true;
    }

    private void loadFieldData() {
        String name = prefs.getString("usr", "");
        if (!(prefs.getBoolean("adm", false))) {
            if ((!name.equals(""))) {
                dataManager.loadUserFields(name);
            } else {
                dataManager.clearAllMaps();
            }
        } else {
            dataManager.readData();
        }
        mMapViewHandler.reload();
    }

    /**
     * removes the last added Point from the new field, and redraw the Polyline
     */
    private void onRedoButtonClick() {
        if (listGeoPoints.size() > 0) {
            listGeoPoints.remove(listGeoPoints.size() - 1);
            polyline.setPoints(listGeoPoints);
            mMapViewHandler.deleteLastFieldMarker();
            mMapViewHandler.addPolyline(polyline);
            mMapViewHandler.invalidateMap();
            fabLabel.setText(getResources().getString(R.string.add_Activity_YouNeed) + String.valueOf(3 - listGeoPoints.size()) + " " + getResources().getString(R.string.add_activity_needMore));
            if (listGeoPoints.size() < 3) {
                fabLabel.setVisibility(View.VISIBLE);
                fabLabel.setText(getResources().getString(R.string.add_Activity_YouNeed) + String.valueOf(3 - listGeoPoints.size()) + " " + getResources().getString(R.string.add_activity_needMore));
            }
        } else {
            Toast.makeText(this, getResources().getString(R.string.add_activity_NoMorePoints), Toast.LENGTH_SHORT).show();
        }
        if (!isDmgField && linesFromAgrarianField.size() > 0) {
            linesFromAgrarianField.remove(linesFromAgrarianField.size() - 1);
        }
    }

    private void onToggleLocButtonClick() {
        if (myLocationListener.getFollow()) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.toastmsg_toggle_Loc_Off), Toast.LENGTH_SHORT).show();
            toolbar.getMenu().getItem(2).setTitle(R.string.toastmsg_toggle_Loc_Off);
            myLocationListener.setFollow(false);
        } else {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.toastmsg_toggle_Loc_On), Toast.LENGTH_SHORT).show();
            toolbar.getMenu().getItem(2).setTitle(R.string.toastmsg_toggle_Loc_On);
            myLocationListener.setFollow(true);
        }
    }

    /**
     * adds a point to the field with a click on the map
     */
    public void onMapClick() {
        MapEventsReceiver mReceive = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint point) {
                Log.e(TAG, "single Tab confirmed!");
                Location location = new Location("Loc");
                location.setLatitude(point.getLatitude());
                location.setLongitude(point.getLongitude());
                addPoint(location);
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        };

        MapEventsOverlay overlayEvents = new MapEventsOverlay(mReceive);
        mMapViewHandler.getMap().getOverlayManager().add(overlayEvents);
    }

    /**
     * Handle clicks for the done button on top
     * depending on the current state of our field to add
     */
    private void onDoneButtonClick() {
        if (enoughPoints) {
            Field fieldToAdd;
            if (isDmgField) {
                fieldToAdd = new DamageField(getApplicationContext(), listGeoPoints, parentField);
                parentField.addContainedDamageField((DamageField) fieldToAdd);
                dataManager.changeAgrarianField(parentField);


            } else {
                fieldToAdd = new AgrarianField(getApplicationContext(), listGeoPoints);
                if (fieldToAdd instanceof AgrarianField) {
                    intersectionCalculator.calcLastLine();
                    ((AgrarianField) fieldToAdd).setLinesFormField(linesFromAgrarianField);
                }
            }
            GlobalConstants.setLastLocationOnMap(fieldToAdd.getCentroid());

            if (isDmgField) {
                bottomSheetDialogDMF = BSDetailDialogEditDmgField.newInstance();
                dataManager.addDamageField((DamageField) fieldToAdd);
                BSEditHandler handler = new BSEditHandler(fieldToAdd, dataManager, bottomSheetDialogDMF);
                bottomSheetDialogDMF.setPresenter(handler);
                bottomSheetDialogDMF.show(getSupportFragmentManager(), "EditView");
            } else {
                bottomSheetDialogAF = BSDetailDialogEditAgrField.newInstance();
                dataManager.addAgrarianField((AgrarianField) fieldToAdd);

                BSEditHandler handler = new BSEditHandler(fieldToAdd, dataManager, bottomSheetDialogAF);
                bottomSheetDialogAF.setPresenter(handler);
                bottomSheetDialogAF.show(getSupportFragmentManager(), "EditView");
            }


            enoughPoints = false;
            listGeoPoints.clear();

            fabLabel.setVisibility(View.VISIBLE);
            fabLabel.setText("Add a Corner Point at your current position");

        } else {
            Toast.makeText(getApplicationContext(), R.string.toastmsg_not_enough_points, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * handle Floating Action Button clicks
     * depending on the current state of our field to add
     *
     * @param view
     */
    private void onFabClick(View view) {
        if (fieldToAddFinal == null) {
            Location location = myLocationListener.getLocation();
            if (location != null) {
                addPoint(location);
            } else {
                Snackbar.make(view, R.string.toastmsg_nolocation, Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        } else {
            Snackbar.make(view, R.string.toastmsg_points_already_added, Snackbar.LENGTH_LONG).setAction("Action", null).show();
        }
    }

    /**
     * map callback
     */
    public void onMapFragmentComplete() {

    }


    /**
     * adds a new Point to the Field
     *
     * @param location
     */
    public void addPoint(Location location) {
        GeoPoint g = new GeoPoint(location);
        listGeoPoints.add(g);
        polyline.setPoints(listGeoPoints);
        mMapViewHandler.addPolyline(polyline);
        mMapViewHandler.dropMarker(g.getLatitude(), g.getLongitude());
        mMapViewHandler.invalidateMap();

        if (listGeoPoints.size() > 2) {
            enoughPoints = true;
            fabLabel.setVisibility(View.INVISIBLE);
            menuItemDone.setVisible(true);
            menuItemDone.setTitle(getResources().getString(R.string.done_Button));

        }
        intersectionCalculator.calculateLine(!isDmgField);
        if(!isDmgField){
            fabLabel.setText(getResources().getString(R.string.add_Activity_YouNeed) + String.valueOf(3 - listGeoPoints.size()) + " " + getResources().getString(R.string.add_activity_needMore));
            Snackbar.make(mapFragment.getView(), getResources().getString(R.string.add_activity_pointAt) +
                    location.getLatitude() + " " + location.getLongitude() + getResources().getString(R.string.add_activity_added), Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
        }
        if (isDmgField && pointOutOfField.pointInField(parentField.getLinesFormField(), parentField.getCentroid(), g)) {


            if ((isDmgField && !intersectionCalculator.calcIntersection(parentField))) {
                Toast.makeText(this, this.getResources().getString(R.string.add_activity_outsideOffField), Toast.LENGTH_SHORT).show();
                onRedoButtonClick();
            } else {
                fabLabel.setText(getResources().getString(R.string.add_Activity_YouNeed) + String.valueOf(3 - listGeoPoints.size()) + " " + getResources().getString(R.string.add_activity_needMore));
                Snackbar.make(mapFragment.getView(), getResources().getString(R.string.add_activity_pointAt) +
                        location.getLatitude() + " " + location.getLongitude() + getResources().getString(R.string.add_activity_added), Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            }
        }
        else {
            if (isDmgField) {
                Toast.makeText(this, this.getResources().getString(R.string.add_activity_outsideOffField), Toast.LENGTH_SHORT).show();
                onRedoButtonClick();
            }
            else{

            }
        }
    }
    private AlertDialog.Builder generateSaveDialog() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        onBackPressed();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.dismiss();
                        break;
                }
            }
        };
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.go_back_message)).setPositiveButton(getResources().getString(R.string.word_yes), dialogClickListener)
                .setNegativeButton(getResources().getString(R.string.word_no), dialogClickListener);

        return builder;
    }

    /**
     * called every time a data changes happens from appDataManager instance
     */
    @Override
    public void onDataChange() {
        if (mMapViewHandler != null) {
            mMapViewHandler.reload();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * receive messages from fragments
     *
     * @param Tag    of the fragment
     * @param action the fragment performs
     * @param data   data the fragment sends
     */
    @Override
    public void onFragmentMessage(String Tag, @NonNull String action, @Nullable Object data) {
        Log.d(TAG, "MSG TAG: " + Tag + " ACTION: " + action);
        switch (Tag) {
            case "MapFragment":
                switch (action) {
                    case "complete":
                        onMapFragmentComplete();
                        break;
                }
                break;
            case "BSDetailDialogEditFragmentAgrarianField":
                mMapViewHandler.addField((Field) data);
                mMapViewHandler.invalidateMap();
                mMapViewHandler.getMap().getOverlayManager().remove(polyline);
                Toast.makeText(this, getResources().getString(R.string.toastmsg_anotherAgrarainField), Toast.LENGTH_SHORT).show();
                break;
            case "BSDetailDialogEditFragmentDamageField":
                mMapViewHandler.addField((Field) data);
                mMapViewHandler.invalidateMap();
                Toast.makeText(this, getResources().getString(R.string.toastmsg_anotherDamageField), Toast.LENGTH_SHORT).show();
                switch (action) {
                    case "addPhoto":
                        BottomSheetAddPhoto bottomSheetAddPhoto = BottomSheetAddPhoto.newInstance();
                        new BSEditHandler((Field) data, dataManager, bottomSheetAddPhoto);
                        bottomSheetAddPhoto.show(getSupportFragmentManager(), "test");
                        break;
                }
                break;
        }
    }
}
