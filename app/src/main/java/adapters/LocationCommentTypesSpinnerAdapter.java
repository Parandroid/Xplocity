package adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.xplocity.xplocity.R;

import java.util.Arrays;

import models.LocationCommentType;

/**
 * Created by dmitry on 15.05.18.
 */

public class LocationCommentTypesSpinnerAdapter extends ArrayAdapter<LocationCommentType> {


    // Your sent context
    private Context mContext;
    // Your custom mValues for the spinner (User)
    private LocationCommentType[] mValues;

    public LocationCommentTypesSpinnerAdapter(Context context, int textViewResourceId,
                       LocationCommentType[] values) {
        super(context, textViewResourceId, values);
        this.mContext = context;
        this.mValues = values;
        this.mValues = Arrays.copyOf(values, values.length + 1); //create new array from old array and allocate one more element
        mValues[mValues.length - 1] = new LocationCommentType(0, "Укажите причину");
    }

    @Override
    public int getCount() {
        return mValues.length - 1;
    }

    @Override
    public LocationCommentType getItem(int position) {
        return mValues[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    // And the "magic" goes here
    // This is for the "passive" state of the spinner
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(mValues[position].name, position, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        return getCustomView(mValues[position].name, position, parent);
    }


    public View getCustomView(String text, int position,  ViewGroup parent) {

        LayoutInflater inflater=(LayoutInflater) mContext.getSystemService(  Context.LAYOUT_INFLATER_SERVICE );
        View row=inflater.inflate(R.layout.spinner_item_location_comment_type, parent, false);
        TextView label=(TextView)row.findViewById(R.id.message_type_name);
        label.setText(text);

        if (position == mValues.length-1) {//Special style for dropdown header
            label.setTextColor(mContext.getResources().getColor(R.color.darkerGray));
        }

        return row;
    }

}
