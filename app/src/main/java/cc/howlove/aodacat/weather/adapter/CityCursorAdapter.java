package cc.howlove.aodacat.weather.adapter;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

/**
 * Created by anymore on 17-4-23.
 */

public class CityCursorAdapter extends CursorAdapter{
    private SQLiteDatabase mSqLiteDatabase;
    public CityCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    public CityCursorAdapter(Context context, Cursor c, int flags, SQLiteDatabase mSqLiteDatabase) {
        super(context, c, flags);
        this.mSqLiteDatabase = mSqLiteDatabase;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        final TextView view = (TextView) inflater.inflate(android.R.layout.simple_dropdown_item_1line, viewGroup, false);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        StringBuilder sb = new StringBuilder();
        sb.append(cursor.getString(cursor.getColumnIndex("province"))+"-");
        sb.append(cursor.getString(cursor.getColumnIndex("city"))+"-");
        sb.append(cursor.getString(cursor.getColumnIndex("district")));
        ((TextView) view).setText(sb.toString());
    }

    @Override
    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
        if (constraint != null) {
            String input = constraint.toString();
            String selection = "district like ? ";
            String[] selectionArgs = new String[]{"%"+input+"%"};
            System.out.println(selection);
            String[] cols = new String[]{"id as _id","province","city","district"};
            return mSqLiteDatabase.query("ALL_CITIES", cols, selection, selectionArgs, null, null, null);
            //从表train查询
        } else {
            return null;
        }
    }

}
