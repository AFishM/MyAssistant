package com.name.myassistant;


import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.name.myassistant.util.LogUtil;


/**
 * A simple {@link Fragment} subclass.
 */
public class ContactFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        AdapterView.OnItemClickListener{
    CursorLoader cursorLoader;

    private final static String[] FROM_COLUMNS = {
            Build.VERSION.SDK_INT
                    >= Build.VERSION_CODES.HONEYCOMB ?
                    Contacts.DISPLAY_NAME_PRIMARY :
                    Contacts.DISPLAY_NAME
    };

    private final static int[] TO_IDS = {
            R.id.name
    };

    ListView contactListView;

    long mContactId;

    String mContactKey;

    Uri mContactUri;

    private SimpleCursorAdapter mCursorAdapter;

    private static final String[] PROJECTION = {
            Contacts._ID,
            Contacts.LOOKUP_KEY,
            Build.VERSION.SDK_INT
                    >= Build.VERSION_CODES.HONEYCOMB ?
                    Contacts.DISPLAY_NAME_PRIMARY :
                    Contacts.DISPLAY_NAME
    };

    // The column index for the _ID column
    private static final int CONTACT_ID_INDEX = 0;
    // The column index for the LOOKUP_KEY column
    private static final int LOOKUP_KEY_INDEX = 1;

    private static final String SELECTION =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
                    Contacts.DISPLAY_NAME_PRIMARY + " LIKE ?" :
                    Contacts.DISPLAY_NAME + " LIKE ?";
    // Defines a variable for the search string
    private String mSearchString;
    // Defines the array to hold values that replace the ?
    private String[] mSelectionArgs = { mSearchString };

    public ContactFragment() {
        // Required empty public constructor
    }

    public void setmSearchString(String mSearchString) {
        this.mSearchString = mSearchString;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        cursorLoader=(CursorLoader)getLoaderManager().initLoader(0, null, this);

        View view=inflater.inflate(R.layout.fragment_contact, container, false);
        contactListView=(ListView)view.findViewById(R.id.contact_list);
        mCursorAdapter=new SimpleCursorAdapter(view.getContext(),R.layout.contact_item,null,FROM_COLUMNS,TO_IDS,0);
        contactListView.setAdapter(mCursorAdapter);
        contactListView.setOnItemClickListener(this);

        return view;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        mSelectionArgs[0] = "%" + mSearchString + "%";
        // Starts the query
        return new CursorLoader(
                getActivity(),
                Contacts.CONTENT_URI,
                PROJECTION,
                SELECTION,
                mSelectionArgs,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        LogUtil.d("load");
        mCursorAdapter.swapCursor(null);
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        LogUtil.d("load");
        mCursorAdapter.swapCursor(null);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // Get the Cursor
        Cursor cursor = mCursorAdapter.getCursor();
        // Move to the selected contact
        cursor.moveToPosition(position);
        // Get the _ID value
        mContactId = cursor.getLong(CONTACT_ID_INDEX);
        // Get the selected LOOKUP KEY
        mContactKey = cursor.getString(LOOKUP_KEY_INDEX);
        // Create the contact's content Uri
        mContactUri = Contacts.getLookupUri(mContactId, mContactKey);
    /*
     * You can use mContactUri as the content URI for retrieving
     * the details for a contact.
     */
    }
}
