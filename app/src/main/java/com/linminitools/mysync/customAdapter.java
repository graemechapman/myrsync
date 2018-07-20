package com.linminitools.mysync;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;
import static com.linminitools.mysync.MainActivity.configs;


class customAdapter extends BaseAdapter implements ExpandableListAdapter{

        Context context;
        String[] data;
        private static LayoutInflater inflater = null;
        private int fromTab;
        private ArrayList<Scheduler> original_data = new ArrayList<>();



        public customAdapter(Context context, ArrayList<?> data, int request_code) {
        this.context = context;
        this.data=new String[data.size()];
        this.fromTab=request_code;


        if (!data.isEmpty()) {
            for (Object c : data) {

                if (fromTab==2) this.data[data.indexOf(c)] = ((RS_Configuration) c).name;
                else if (fromTab==1 || fromTab==3) {
                    this.data[data.indexOf(c)] = c.getClass().getName();
                    original_data.add(((Scheduler) c));
                }

            }

        }

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
        // TODO Auto-generated method stub
        return data.length;
        }

        @Override
        public Object getItem(int position) {
        // TODO Auto-generated method stub
        return data[position];
        }

        @Override
        public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
        }


        @SuppressLint("InflateParams")
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View vi = convertView;
            if (vi == null)

                if(this.fromTab==1) {

                    vi = inflater.inflate(R.layout.status_row, null);
                    TextView tv_conf_name = vi.findViewById(R.id.tv_get_config_name);
                    TextView tv_sched_name = vi.findViewById(R.id.tv_get_scheduler_name);
                    TextView tv_next_run = vi.findViewById(R.id.tv_get_nextrun);
                    TextView tv_last_run = vi.findViewById(R.id.tv_get_lastrun);
                    TextView tv_result = vi.findViewById(R.id.tv_get_result);
                    ImageView error = vi.findViewById(R.id.img_error_status);
                    error.setVisibility(View.INVISIBLE);

                    Scheduler sched = original_data.get(position);
                    RS_Configuration conf;
                    try{
                        conf= configs.get(sched.config_pos);
                    }catch (IndexOutOfBoundsException e){
                        conf= configs.get(0);
                        sched.config_pos=conf.id;
                    }
                    tv_conf_name.setText(conf.name);
                    tv_sched_name.setText(sched.name);
                    if (context.getSharedPreferences("CMD",MODE_PRIVATE).getBoolean("is_running",false)) tv_next_run.setText("Running");
                    else tv_next_run.setText(sched.getNextAlarm());

                    SharedPreferences result_prefs = context.getSharedPreferences("configs", MODE_PRIVATE);
                    Log.d("SHAREDPREFS", "last_result_" + String.valueOf(conf.id));
                    Log.d("CONTEXT", context.toString());
                    String result = result_prefs.getString("last_result_" + String.valueOf(conf.id), "Never Run");
                    String last_run = result_prefs.getString("last_run_" + String.valueOf(conf.id), "Never Run");
                    tv_last_run.setText(last_run);
                    tv_result.setText(result);

                    if (result.equals("OK")) {
                        vi.findViewById(R.id.img_success_status).setVisibility(View.VISIBLE);
                    } else if (result.equals("Warning! Check Log!")) {
                        Log.d("RESULT_1", result);
                        error.setVisibility(View.VISIBLE);
                    }
                }

                else if (this.fromTab==2) {
                    vi = inflater.inflate(R.layout.row, null);
                    TextView text = vi.findViewById(R.id.list_item_id);
                    text.setText(data[position]);
                    vi.findViewById(R.id.bt_edit).setTag(R.id.bt_edit, position);
                    vi.findViewById(R.id.bt_delete).setTag(R.id.bt_delete, position);
                }
                else if (this.fromTab==3 ) {
                    vi = inflater.inflate(R.layout.row_sched, null);

                    Scheduler sched = original_data.get(position);
                    TextView tv_name = vi.findViewById(R.id.tv_sched_name);
                    tv_name.setText(sched.name);

                    vi.findViewById(R.id.bt_edit_sched).setTag(R.id.bt_edit_sched, position);
                    vi.findViewById(R.id.bt_delete_sched).setTag(R.id.bt_delete_sched, position);

                    TextView tv_time = vi.findViewById(R.id.tv_sched_showtime);
                    tv_time.setText(String.valueOf(sched.hour) + ":" + String.valueOf(sched.min));


                    String days = sched.days;
                    Log.d ("DAYS",days);
                    Log.d ("NAME",sched.name);
                    String[] active_days = days.split("[.]");
                    Log.d("ACTIVE DAYS", String.valueOf(active_days.length));

                    for (int i=0; i<active_days.length;i++) {
                        Log.d("ACTIVE DAYS", String.valueOf(active_days[i]));
                        }

                        for (String d : active_days) {
                        if (!d.isEmpty()) {
                            int rid = context.getResources().getIdentifier("tv_" + d, "id", context.getPackageName());
                            TextView tv_day = vi.findViewById(rid);
                            tv_day.setBackgroundColor(R.drawable.rectangle);

                        }

                    }


                }
                else if(this.fromTab==21){ //tab21 = tab2, 1st Child Listview = Expandable ListView for Daemon Rsync Configuration
                    if (position==0) vi=inflater.inflate(R.layout.add_daemon_config, null);
                    if (position==1) vi=inflater.inflate(R.layout.add_remote_shell_config, null);
                }


        return vi;
        }

    @Override
    public int getGroupCount() {
        Log.d("ADAPTER", "GetGroupCount");
            return 2;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        Log.d("ADAPTER", "getChildrenCount");
            return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        Log.d("ADAPTER", "GetGroup");
            return data[groupPosition];
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        Log.d("ADAPTER", "GET CHILD" + this.data[groupPosition]);
            return this.data[groupPosition] ;
    }

    @Override
    public long getGroupId(int groupPosition) {
        Log.d("ADAPTER", "GetGroupId");
            return 0;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        Log.d("ADAPTER", "GetChildId");
            return 0;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        Log.d("ADAPTER", "getGroupView");
        View vi = convertView;
        if (groupPosition==0) vi=inflater.inflate(android.R.layout.simple_expandable_list_item_2, null);
        if (groupPosition==1) vi=inflater.inflate(android.R.layout.simple_expandable_list_item_2, null);
        return vi;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        Log.d("ADAPTER", "GetChildView");
        View vi = convertView;
        if (childPosition==0) vi=inflater.inflate(R.layout.add_daemon_config, null);
        if (childPosition==1) vi=inflater.inflate(R.layout.add_remote_shell_config, null);
        return vi;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    @Override
    public void onGroupExpanded(int groupPosition) {
        Log.d("ADAPTER", "OnGroupExpanded");
    }

    @Override
    public void onGroupCollapsed(int groupPosition) {
        Log.d("ADAPTER", "onGroupCollapsed");
    }

    @Override
    public long getCombinedChildId(long groupId, long childId) {
        Log.d("ADAPTER", "GetCombinedChildID");
            return 0;
    }

    @Override
    public long getCombinedGroupId(long groupId) {
        Log.d("ADAPTER", "getCombinedGroupId");
        return 0;
    }
}