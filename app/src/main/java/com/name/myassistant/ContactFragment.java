package com.name.myassistant;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.name.myassistant.m.Chat;
import com.name.myassistant.util.LogUtil;

import java.util.ArrayList;
import java.util.List;


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
    ListView phoneListView;

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

    MainActivity activity;


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
        activity=(MainActivity)getActivity();
        cursorLoader=(CursorLoader)getLoaderManager().initLoader(0, null, this);

        View view=inflater.inflate(R.layout.fragment_contact, container, false);
        contactListView=(ListView)view.findViewById(R.id.contact_list);
        phoneListView=(ListView)view.findViewById(R.id.phone_list);
        mCursorAdapter=new SimpleCursorAdapter(view.getContext(),R.layout.contact_item,null,FROM_COLUMNS,TO_IDS,0);
        contactListView.setAdapter(mCursorAdapter);
        contactListView.setOnItemClickListener(this);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
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
//        mCursorAdapter.swapCursor(null);
        mCursorAdapter.swapCursor(data);
        int contactNameCount=mCursorAdapter.getCount();
        LogUtil.d("xzx", "contactNameCount=> " + contactNameCount);
        if(contactNameCount<=0){
            Toast.makeText(activity,getString(R.string.no_this_man),Toast.LENGTH_LONG).show();
//            activity.contactLayout.setVisibility(View.GONE);
//            activity.getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        LogUtil.d("load");
        mCursorAdapter.swapCursor(null);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        LogUtil.d("xzx");
        // Get the Cursor
        Cursor cursor = mCursorAdapter.getCursor();
        // Move to the selected contact
        cursor.moveToPosition(position);
        // Get the _ID value
        mContactId = cursor.getLong(CONTACT_ID_INDEX);
        LogUtil.d("xzx", "mContactId=> " + mContactId);
        int contactIdIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID);
        LogUtil.d("xzx","contactIdIndex=> "+contactIdIndex);
        String contactId=cursor.getString(contactIdIndex);
        LogUtil.d("xzx","contactId=> "+contactId);
        // Get the selected LOOKUP KEY
        mContactKey = cursor.getString(LOOKUP_KEY_INDEX);
        // Create the contact's content Uri
        mContactUri = Contacts.getLookupUri(mContactId, mContactKey);


        Cursor phonesCursor=view.getContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID+"="+contactId,null,null);
        int phoneIndex = 0;
        List<String> phoneList=new ArrayList<>();
        if(phonesCursor!=null&&phonesCursor.getCount() > 0) {
            phoneIndex = phonesCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            while(phonesCursor.moveToNext()) {
                String phoneNumber = phonesCursor.getString(phoneIndex);
                phoneList.add(phoneNumber);
                LogUtil.d("xzx","phoneNumber=> "+phoneNumber);
            }
            LogUtil.d("xzx","phoneList=> "+phoneList.toString());
            phonesCursor.close();
        }
        LogUtil.d("xzx", "phoneList=> " + phoneList.toString());

        phoneListView.setVisibility(View.VISIBLE);
        LogUtil.d("xzx");
        if(phoneList.size()>1){
            phoneListView.setAdapter(new phoneListAdapter(phoneList));
        }else{
            String phoneNum =phoneList.get(0);
            activity.phoneNum=phoneNum;
            activity.contactLayout.setVisibility(View.INVISIBLE);

            if (activity.prepareToSendMessage) {
                activity.robotOutputHandle(getString(R.string.say_something));
            } else {
                callPhone(phoneNum);
            }
        }



    /*
     * You can use mContactUri as the content URI for retrieving
     * the details for a contact.
     */
    }
    class phoneListAdapter extends BaseAdapter{
        public phoneListAdapter(List<String> phoneList) {
            this.phoneList = phoneList;
        }

        List<String> phoneList;
        @Override
        public int getCount() {
            return phoneList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            phoneListViewHolder holder;
            if (null != convertView) {
                view = convertView;
                holder = (phoneListViewHolder) view.getTag();
            } else {
                holder = new phoneListViewHolder();
                view = View.inflate(parent.getContext(), R.layout.phone_item, null);
                holder.phoneTextView=(TextView)view.findViewById(R.id.phone);
                view.setTag(holder);
            }
            final String phoneNum=phoneList.get(position);
            holder.phoneTextView.setText(phoneNum);
            holder.phoneTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LogUtil.d("xzx", "phoneNum=> " + phoneNum);
                    if (activity.prepareToSendMessage) {
                        activity.phoneNum = phoneNum;

                        String robotOutput=getString(R.string.say_something);
                        Chat chat = new Chat(false, robotOutput);
                        activity.chatContentListViewAdapter.chatList.add(chat);
                        activity.chatContentListViewAdapter.notifyDataSetChanged();

                        activity.speechSynthesizer.startSpeaking(getString(R.string.say_something), null);
                    } else {
                        callPhone(phoneNum);
                    }
                    activity.getSupportFragmentManager().popBackStack();
                    activity.contactLayout.setVisibility(View.INVISIBLE);
                }
            });
            return view;
        }

        class phoneListViewHolder{
            TextView phoneTextView;
        }
    }

    void callPhone(String phoneNum) {
        LogUtil.d("xzx");
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNum));
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getActivity(), " 是您拒绝哦，自己打 ", Toast.LENGTH_LONG).show();
            return;
        }
        startActivity(intent);
        activity.onBackPressed();

    }
}
