package de.uni_stuttgart.informatik.sopra.sopraapp.UI;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

import de.uni_stuttgart.informatik.sopra.sopraapp.FragmentInteractionListener;
import de.uni_stuttgart.informatik.sopra.sopraapp.R;

import de.uni_stuttgart.informatik.sopra.sopraapp.data.AgrarianField;
import de.uni_stuttgart.informatik.sopra.sopraapp.data.DamageField;
import de.uni_stuttgart.informatik.sopra.sopraapp.data.FieldTypes.AgrarianFieldType;
import de.uni_stuttgart.informatik.sopra.sopraapp.data.Field;
import de.uni_stuttgart.informatik.sopra.sopraapp.data.FieldTypes.DamageFieldType;
import de.uni_stuttgart.informatik.sopra.sopraapp.data.FieldTypes.FieldType;


/**
 * sopra_priv
 * Created by Felix B on 20.11.17.
 * Mail: felix.burk@gmail.com
 */

public class BottomSheetDetailDialogFragment extends BottomSheetDialogFragment implements View.OnClickListener {

    private static final String TAG = "BottomSheetDetail";

    private static final String KEY_NAME = "name";
    private static final String KEY_TYPE = "type";
    private static final String KEY_COLOR = "color";
    private static final String KEY_COUNTY = "county";
    private static final String KEY_OWNER = "owner";
    private static final String KEY_SIZE = "size";
    private static final String KEY_DATE = "date";
    private static final String KEY_EVALUATOR = "evaluator";


    private static boolean mEdit = false;

    private FragmentInteractionListener mListener;

    Field changedField;

    public static BottomSheetDialogFragment newInstance(Field field, boolean edit) {
        final BottomSheetDialogFragment fragment = new BottomSheetDetailDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("mField", field);
        fragment.setArguments(args);

        mEdit = edit;
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //prevent cancel by onTab outside of sheet if field is edited
        if(mEdit) this.setCancelable(false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentInteractionListener) {
            mListener = (FragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement FragmentInteractionListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_detail_dialog, container, false);
        if(mEdit){
            view = inflater.inflate(R.layout.fragment_item_detail_dialog_edit, container, false);
        }
        configureBottomSheetBehaviour(view);
        return view;
    }

    private void configureBottomSheetBehaviour(View view) {

    }

    EditText nameEdit;
    EditText countyEdit;
    Spinner type;
    EditText ownerOrEvaluatorEdit;
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        TextView name = (TextView) view.findViewById(R.id.field_detail_name);
        Button editFinish = (Button) view.findViewById(R.id.edit_finish_button);
        editFinish.setOnClickListener(this);

        TextView size = (TextView) view.findViewById(R.id.field_detail_size);

        Field mField = (Field) getArguments().getSerializable("mField");

        size.setText(mField.getSize() + "m" + "\u00B2");


        if(!mEdit) {noEditSetup(view, mField, name, editFinish);}
        else {
            editSetup(view, mField, name, editFinish);
            changeData();
        }



    }

    private void editSetup(View view, Field mField, TextView name, Button editFinish) {
        LinearLayout bottomSheet = (LinearLayout) view.findViewById(R.id.bottomSheet);
        RelativeLayout topPanel = (RelativeLayout) view.findViewById(R.id.topPanel);

        nameEdit = view.findViewById(R.id.field_detail_name_edit);
        nameEdit.setText(mField.getName());

        countyEdit = view.findViewById(R.id.field_detail_region_edit);
        countyEdit.setText(mField.getCounty());

        type = view.findViewById(R.id.field_detail_state_spinner);
        ownerOrEvaluatorEdit = view.findViewById(R.id.field_detail_policyholder_edit);

        if(mField instanceof AgrarianField){
            name.setText("Edit AgrarianField");

            List<AgrarianFieldType> statusCheck;
            statusCheck = Arrays.asList(AgrarianFieldType.values());
            type.setAdapter(new ArrayAdapter<AgrarianFieldType>(getContext(), android.R.layout.simple_spinner_item, AgrarianFieldType.values()));
            type.setSelection(statusCheck.indexOf(mField.getType()));

            ownerOrEvaluatorEdit.setText(((AgrarianField)mField).getOwner());

        }else if(mField instanceof DamageField){
            name.setText("Edit DamageField");

            List<DamageFieldType> statusCheck;
            statusCheck = Arrays.asList(DamageFieldType.values());
            type.setAdapter(new ArrayAdapter<DamageFieldType>(getContext(), android.R.layout.simple_spinner_item, DamageFieldType.values()));
            type.setSelection(statusCheck.indexOf(mField.getType()));

            ownerOrEvaluatorEdit.setText(((DamageField)mField).getEvaluator());

        }

        editFinish.setText(getContext().getResources().getString(R.string.button_finish_name));
    }

    private void noEditSetup(View view, Field mField, TextView name, Button editFinish) {
        TextView state = (TextView) view.findViewById(R.id.field_detail_state);
        TextView county = (TextView) view.findViewById(R.id.field_detail_region);
        TextView ownerOrEvaluator = (TextView) view.findViewById(R.id.field_detail_policyholder);
        TextView date = (TextView) view.findViewById(R.id.field_detail_date);
        Button addDmg = (Button) view.findViewById(R.id.add_damageField_button);

        name.setText(mField.getName());
        county.setText(mField.getCounty());
        editFinish.setText(getContext().getResources().getString(R.string.button_edit_name));

        state.setText(mField.getType().toString());
        state.setTextColor(mField.getColor());

        //is field agrarian?
        if (mField instanceof AgrarianField) {
            ownerOrEvaluator.setText(((AgrarianField)mField).getOwner());
            date.setText("");
        }
        //is field damage?
        if (mField instanceof DamageField) {
            addDmg.setVisibility(View.INVISIBLE);
            date.setText(((DamageField)mField).getParsedDate());
            ownerOrEvaluator.setText(((DamageField)mField).getEvaluator());
        }
    }

    public void setId(int id) {
        this.getView().setId(id);
    }

    @Override
    public void onClick(View v) {
        if(mListener != null) {
            switch (v.getId()) {
                case R.id.edit_finish_button:
                    if(mEdit) mListener.onFragmentMessage(TAG, "finishEdit", changeData());
                    else mListener.onFragmentMessage(TAG, "startEdit", getArguments().getSerializable("mField"));
                    this.dismiss();
                    break;
                case R.id.add_damageField_button:
                    mListener.onFragmentMessage(TAG, "addDmgField", getArguments().getSerializable("mField"));
            }
        }
    }

    public Field changeData(){
        /*Bundle b = new Bundle();
        b.putString("name", nameEdit.getText().toString());
        b.putSerializable("type", (Serializable) type.getSelectedItem());
        if(countyEdit.getText() != null){
            b.putString("address", countyEdit.getText().toString());
        }
        b.putString("ownerOrEvaluator", ownerOrEvaluatorEdit.getText().toString());

        return b;*/

        changedField = (Field) getArguments().getSerializable("mField");
        changedField.setName(nameEdit.getText().toString());
        changedField.setType((FieldType) type.getSelectedItem());

        if(countyEdit.getText() != null) {
            changedField.setCounty(countyEdit.getText().toString());
        }else{
            changedField.setAutomaticCounty();
        }

        if(changedField instanceof AgrarianField){
            ((AgrarianField) changedField).setOwner(ownerOrEvaluatorEdit.getText().toString());
        }else{
            ((DamageField) changedField).setEvaluator(ownerOrEvaluatorEdit.getText().toString());
        }

        return changedField;
    }

}
