package de.uni_stuttgart.informatik.sopra.sopraapp.UI;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import de.uni_stuttgart.informatik.sopra.sopraapp.R;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnMenuFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MenuFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MenuFragment extends Fragment implements View.OnClickListener{
    private static final String TAG = "MenuFragment";
    private OnMenuFragmentInteractionListener mListener;

    public MenuFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MenuFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MenuFragment newInstance() {
        MenuFragment fragment = new MenuFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    EditText input;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_menu, container, false);

        Button list = view.findViewById(R.id.button_list);
        Button loc = view.findViewById(R.id.button_location);
        Button add = view.findViewById(R.id.button_add);
        Button info = view.findViewById(R.id.button_info);
        Button search = view.findViewById(R.id.button_search);
        input = view.findViewById(R.id.edit_text);

        list.setOnClickListener(this);
        loc.setOnClickListener(this);
        info.setOnClickListener(this);
        add.setOnClickListener(this);
        search.setOnClickListener(this);
        input.setOnClickListener(this);

        return view;
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnMenuFragmentInteractionListener) {
            mListener = (OnMenuFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnMenuFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {

        if(mListener != null){
            switch (v.getId()) {
                case R.id.button_list:
                    mListener.onListButtonInteraction();
                    break;
                case R.id.button_location:
                    checkLocPermissions();
                    mListener.onLocationButtonInteraction();
                    break;
                case R.id.button_add:
                    mListener.onAddButtonInteraction();
                    break;
                case R.id.button_info:
                    mListener.onInfoButtonInteraction();
                    break;
                case R.id.edit_text:
                    //remove text if user clicks on search
                    input.setText("");
                    break;
                case R.id.button_search:
                    mListener.onSearchButtonClicked(input.getText().toString());
            }
        }
    }

    private void checkLocPermissions() {
        //TODO check Permissions
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnMenuFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListButtonInteraction();
        void onLocationButtonInteraction();
        void onAddButtonInteraction();
        void onInfoButtonInteraction();
        void onSearchButtonClicked(String text);
    }
}