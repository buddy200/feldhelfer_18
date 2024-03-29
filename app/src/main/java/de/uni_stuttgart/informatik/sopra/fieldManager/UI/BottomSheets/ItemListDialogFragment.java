package de.uni_stuttgart.informatik.sopra.fieldManager.UI.BottomSheets;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import de.uni_stuttgart.informatik.sopra.fieldManager.FragmentInteractionListener;
import de.uni_stuttgart.informatik.sopra.fieldManager.R;
import de.uni_stuttgart.informatik.sopra.fieldManager.data.AgrarianField;
import de.uni_stuttgart.informatik.sopra.fieldManager.data.DamageField;
import de.uni_stuttgart.informatik.sopra.fieldManager.data.Field;

/**
 * sopra_priv
 * Created by Felix B on 03.11.17.
 * Mail: felix.burk@gmail.com
 * <p>
 * A generic List Dialog Fragment containing all Fields on the Map
 */
public class ItemListDialogFragment extends BottomSheetDialogFragment {

    private static final String TAG = "ItemListDialogFragment";

    private FragmentInteractionListener mListener;
    private static List<Field> fieldData;


    /**
     * this factory method is used to generate an instance
     * using the provided parameters
     *
     * @return A new instance of fragment ItemListDialogFragment.
     */
    public static ItemListDialogFragment newInstance(List<Field> fields) {
        final ItemListDialogFragment fragment = new ItemListDialogFragment();
        fieldData = fields;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list_dialog, container, false);
        configureBottomSheetBehavior(view);
        return view;
    }

    /**
     * method to configure the behaviour of the bottom sheet
     *
     * @param view
     */
    private void configureBottomSheetBehavior(View view) {
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        final RecyclerView recyclerView = (RecyclerView) view;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new ItemAdapter());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof FragmentInteractionListener) {
            mListener = (FragmentInteractionListener) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement FragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    /**
     * the ViewHolder holding the Field Objects
     */
    private class ViewHolder extends RecyclerView.ViewHolder {

        final TextView text;
        final ImageView statePattern;
        final TextView stateText;
        final TextView county;
        final LinearLayout layout;

        ViewHolder(LayoutInflater inflater, ViewGroup parent) {
            //add the item layout xml
            super(inflater.inflate(R.layout.fragment_item_list_dialog_item, parent, false));

            //the item is shown as a text view
            text = itemView.findViewById(R.id.item_field_name);
            statePattern = itemView.findViewById(R.id.item_field_state_pattern);
            stateText = itemView.findViewById(R.id.item_field_state);
            county = itemView.findViewById(R.id.item_field_county);
            layout = itemView.findViewById(R.id.ll_item);

            // the on click listener for the item that is being clicked
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onFragmentMessage(TAG, "itemClick", fieldData.get(getAdapterPosition()));
                        dismiss();
                    }
                }
            });
        }
    }

    /**
     * Item Adapter for the different Fields
     */
    private class ItemAdapter extends RecyclerView.Adapter<ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()), parent);
        }

        /**
         * sets the text of our items
         *
         * @param holder
         * @param position
         */
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.text.setText(fieldData.get(position).getName());
            if (fieldData.get(position).getType() != null) {
                Drawable d = getContext().getDrawable(fieldData.get(position).getType().getPattern(getContext()));
                holder.stateText.setText(fieldData.get(position).getType().toString(getContext()));
                if(fieldData.get(position) instanceof AgrarianField){
                    d.setTint(getContext().getResources().getColor(R.color.colorAccentDark));
                } else {
                    d.setTint(getContext().getResources().getColor(R.color.colorPrimary));
                }
                holder.statePattern.setImageDrawable(d);
            } else {
                holder.statePattern.setImageDrawable(null);
            }
            if (fieldData.get(position) instanceof AgrarianField) {
                holder.county.setText(fieldData.get(position).getCounty());
            } else {
                if (fieldData.get(position) instanceof DamageField)
                    holder.county.setText(((DamageField) fieldData.get(position)).getParentField().getName());
            }
        }

        @Override
        public int getItemCount() {
            return fieldData.size();
        }
    }
}