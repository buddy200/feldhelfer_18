package de.uni_stuttgart.informatik.sopra.fieldManager.UI.BottomSheets;

import android.support.annotation.NonNull;

import de.uni_stuttgart.informatik.sopra.fieldManager.data.AgrarianField;
import de.uni_stuttgart.informatik.sopra.fieldManager.data.DamageField;
import de.uni_stuttgart.informatik.sopra.fieldManager.data.Field;
import de.uni_stuttgart.informatik.sopra.fieldManager.data.PictureData;
import de.uni_stuttgart.informatik.sopra.fieldManager.data.managers.AppDataManager;

/**
 * sopra_priv
 * Created by Felix B on 15.12.17.
 * Mail: felix.burk@gmail.com
 */

public class BSEditHandler implements BSEditContract.Presenter {

    private static final String TAG = "BSEditHandler";

    @NonNull
    private final AppDataManager mDataManager;

    @NonNull
    private final BSEditContract.BottomSheet mEditFragment;

    private Field mField;

    /**
     * @param field        may be null for a new field
     * @param dataManager
     * @param editFragment
     */
    public BSEditHandler(Field field, @NonNull AppDataManager dataManager, @NonNull BSEditContract.BottomSheet editFragment) {
        mDataManager = dataManager;
        mEditFragment = editFragment;
        mField = field;
        mEditFragment.setPresenter(this);
    }

    @Override
    public void start() {
        if (mField != null) {
            populateBS(mField);
        }
    }

    @Override
    public void populateBS(Field f) {
        mEditFragment.fillData(f);
    }

    @Override
    public void deleteCurrentField() {
        if (mField != null) {
            mDataManager.removeField(mField);
        }
    }

    @Override
    public void changeField(Field f) {
        if (f instanceof AgrarianField) {
            mDataManager.changeAgrarianField((AgrarianField) f);
        } else {
            mDataManager.changeDamageField((DamageField) f);
        }
        mField = f;
    }

    public Field getVisibleField() {
        return mField;
    }

    @Override
    public void addPhotoToDatabase(PictureData pd) {
        mDataManager.addPicture((DamageField) mField, pd);
        ((DamageField) mField).setPath(pd);
        changeField(mField);
    }

    @Override
    public void deletePhotoFromDatabase(PictureData pd) {
        mDataManager.deletePicture((DamageField) mField, pd);
        changeField(mField);
    }
}