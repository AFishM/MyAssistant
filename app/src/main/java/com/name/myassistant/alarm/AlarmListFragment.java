package com.name.myassistant.alarm;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.name.myassistant.GlobalVariable;
import com.name.myassistant.R;
import com.name.myassistant.m.Alarm;

import java.util.List;


/**
 * 闹钟列表的fragment
 */
public class AlarmListFragment extends Fragment {
    RecyclerView alarmListView;
    TextView noAlarmNoteTextView;
    AlarmListAdapter alarmListAdapter;

    public AlarmListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_alarm_list, container, false);
        alarmListView=(RecyclerView)view.findViewById(R.id.alarm_list);
        alarmListView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        noAlarmNoteTextView=(TextView)view.findViewById(R.id.no_alarm_note);
        addAlarmData();
        return view;
    }

    public void addAlarmData() {
        List<Alarm> alarmList= GlobalVariable.getInstance().getAlarmList();
        if(alarmList.size()<=0){
            alarmListView.setVisibility(View.GONE);
            noAlarmNoteTextView.setVisibility(View.VISIBLE);
            return;
        }
        noAlarmNoteTextView.setVisibility(View.GONE);
        alarmListView.setVisibility(View.VISIBLE);

        if(alarmListAdapter==null){
            alarmListAdapter=new AlarmListAdapter(alarmList);
            alarmListView.setAdapter(alarmListAdapter);
        }else{
            alarmListAdapter.setAlarmList(alarmList);
            alarmListAdapter.notifyDataSetChanged();
        }
    }

    class AlarmListAdapter extends RecyclerView.Adapter<AlarmListAdapter.ViewHolder>{
        List<Alarm> alarmList;

        public AlarmListAdapter(List<Alarm> alarmList) {
            this.alarmList = alarmList;
        }

        public void setAlarmList(List<Alarm> alarmList) {
            this.alarmList = alarmList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.alarm_item,parent,false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final Alarm alarm=alarmList.get(position);
            String time=alarm.hour+getString(R.string.colon)+alarm.minute;
            holder.timeTextView.setText(time);
            holder.noteTextView.setText(alarm.note);
            holder.weatherAddressTextView.setText(alarm.weatherAddress);
            if(alarm.open){
                holder.onOffTextView.setText(getString(R.string.off));
            }else{
                holder.onOffTextView.setText(getString(R.string.on));
            }
            holder.alarmItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlarmSettingFragment alarmSettingFragment=new AlarmSettingFragment();
                    alarmSettingFragment.alarm=alarm;
                    AlarmSettingActivity activity=(AlarmSettingActivity) getActivity();
                    FragmentTransaction transaction=activity.fragmentManager.beginTransaction();
                    String tag=AlarmSettingFragment.class.toString();
                    transaction.add(activity.fragmentLayoutId,alarmSettingFragment,tag);
                    transaction.addToBackStack(tag);
                    transaction.commit();
                }
            });
            holder.onOffTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alarm.setOpen(v.getContext(),!alarm.open);
                    GlobalVariable.save(v.getContext());
                }
            });
        }

        @Override
        public int getItemCount() {
            return alarmList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder{
            LinearLayout alarmItemView;
            TextView timeTextView;
            TextView noteTextView;
            TextView weatherAddressTextView;
            TextView onOffTextView;
            public ViewHolder(View itemView) {
                super(itemView);
                alarmItemView=(LinearLayout)itemView.findViewById(R.id.alarm_item_view);
                timeTextView=(TextView)itemView.findViewById(R.id.time);
                noteTextView=(TextView)itemView.findViewById(R.id.note);
                weatherAddressTextView=(TextView)itemView.findViewById(R.id.weather_address);
                onOffTextView=(TextView)itemView.findViewById(R.id.on_off);
            }
        }
    }
}