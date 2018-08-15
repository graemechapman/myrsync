package com.linminitools.mysync;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;
import static com.linminitools.mysync.MainActivity.appContext;


public class RS_Configuration {

    protected String rs_ip, rs_user, rs_module, rs_options,rs_logfile, local_path,name,rs_dest,arg1,arg2;
    protected String rs_port="873";
    protected int id, mode;
    protected boolean saved;

    RS_Configuration(int id){
        this.id=id;
        this.name="config_"+String.valueOf(id);
        this.saved=false;
    }
    
    protected void saveToDisk(){

            SharedPreferences prefs = appContext.getSharedPreferences("configs", MODE_PRIVATE);
            SharedPreferences.Editor prefseditor = prefs.edit();

        if (mode==0) {
            prefseditor.putString("rs_options_" + String.valueOf(this.id), rs_options);
            prefseditor.putString("rs_user_" + String.valueOf(this.id), rs_user);
            prefseditor.putString("rs_module_" + String.valueOf(this.id), rs_module);
            prefseditor.putString("rs_ip_" + String.valueOf(this.id), rs_ip);
            prefseditor.putString("rs_port_" + String.valueOf(this.id), rs_port);
            prefseditor.putString("local_path_" + String.valueOf(this.id), local_path);
            prefseditor.putString("last_result_" + String.valueOf(this.id), "Never Run");
            prefseditor.putString("last_run_" + String.valueOf(this.id), "Never Run");
            prefseditor.putInt("mode_" + String.valueOf(this.id), mode);
        }
        else if(mode==1){
            prefseditor.putString("rs_options_" + String.valueOf(this.id), rs_options);
            prefseditor.putString("rs_user_" + String.valueOf(this.id), rs_user);
            prefseditor.putString("rs_ip_" + String.valueOf(this.id), rs_ip);
            prefseditor.putString("rs_dest_" + String.valueOf(this.id), rs_dest);
            prefseditor.putString("local_path_" + String.valueOf(this.id), local_path);
            prefseditor.putString("last_result_" + String.valueOf(this.id), "Never Run");
            prefseditor.putString("last_run_" + String.valueOf(this.id), "Never Run");
            prefseditor.putInt("mode_" + String.valueOf(this.id), mode);
        }
        else if(mode==2){
            prefseditor.putString("rs_options_" + String.valueOf(this.id), rs_options);
            prefseditor.putString("arg1_" + String.valueOf(this.id), arg1);
            prefseditor.putString("arg2_" + String.valueOf(this.id), arg2);
            prefseditor.putString("rs_logfile_" + String.valueOf(this.id), rs_logfile);
            prefseditor.putInt("mode_" + String.valueOf(this.id), mode);
            prefseditor.putString("last_result_" + String.valueOf(this.id), "Never Run");
            prefseditor.putString("last_run_" + String.valueOf(this.id), "Never Run");
        }
        prefseditor.putBoolean("saved_"+ String.valueOf(this.id),true);
        prefseditor.apply();
    }
    
    protected void deleteFromDisk(){

            SharedPreferences prefs = appContext.getSharedPreferences("configs", MODE_PRIVATE);
            SharedPreferences.Editor prefseditor = prefs.edit();
        if (mode==0) {
            prefseditor.remove("rs_options_" + String.valueOf(this.id));
            prefseditor.remove("rs_user_" + String.valueOf(this.id));
            prefseditor.remove("rs_module_" + String.valueOf(this.id));
            prefseditor.remove("rs_ip_" + String.valueOf(this.id));
            prefseditor.remove("rs_port_" + String.valueOf(this.id));
            prefseditor.remove("local_path_" + String.valueOf(this.id));
            prefseditor.remove("last_result_" + String.valueOf(this.id));
            prefseditor.remove("last_run_" + String.valueOf(this.id));
        }
        else if (mode==1){
            prefseditor.remove("rs_options_" + String.valueOf(this.id));
            prefseditor.remove("rs_user_" + String.valueOf(this.id));
            prefseditor.remove("mode_" + String.valueOf(this.id));
            prefseditor.remove("rs_ip_" + String.valueOf(this.id));
            prefseditor.remove("rs_dest_" + String.valueOf(this.rs_dest));
            prefseditor.remove("local_path_" + String.valueOf(this.id));
            prefseditor.remove("last_result_" + String.valueOf(this.id));
            prefseditor.remove("last_run_" + String.valueOf(this.id));
        }
        else if (mode==2){
            prefseditor.remove("arg1_"+String.valueOf(this.id));
            prefseditor.remove("arg2_"+String.valueOf(this.id));
            prefseditor.remove("rs_options_"+String.valueOf(this.id));
            prefseditor.remove("rs_logfile_"+String.valueOf(this.id));
            prefseditor.remove("mode_" + String.valueOf(this.id));
            prefseditor.remove("last_result_" + String.valueOf(this.id));
            prefseditor.remove("last_run_" + String.valueOf(this.id));
        }
        prefseditor.remove("saved_"+ String.valueOf(this.id));
        prefseditor.apply();
    }

    protected void executeConfig(final Context context){


        final SharedPreferences prefs =  context.getSharedPreferences("Rsync_Command_build", MODE_PRIVATE);
        final int id = this.id;
        final String options = this.rs_options;
        final String log = prefs.getString("log",context.getApplicationInfo().dataDir+"/logfile.log");
        final String local_path=this.local_path;
        String command="";

        if (this.mode!=2) if(!this.rs_user.isEmpty()) this.rs_user=this.rs_user+"@";

        if (this.mode==0) command = "rsync://" + this.rs_user + this.rs_ip + ":" + this.rs_port + "/" + this.rs_module;
        else if(this.mode==1) command = this.rs_user + ":" + this.rs_ip + "/" + this.rs_dest;


        final String cmd=command;

        Log.d("RSYNC Log Path",log);
        Log.d("RSYNC CMD", cmd);


            Thread t = new Thread(){
                @Override
                public void run() {
                    try {
                        SharedPreferences pref = context.getSharedPreferences("CMD",MODE_PRIVATE);
                        SharedPreferences.Editor pref_Edit= pref.edit();

                        pref_Edit.putBoolean("is_running",true);
                        pref_Edit.commit();
                        String rsync_bin= context.getSharedPreferences("Install",MODE_PRIVATE).getString("rsync_binary",".");

                        ProcessBuilder p;

                        if (mode==0) p = new ProcessBuilder(rsync_bin, options, "--log-file", log, local_path, cmd);
                        else if (mode==1) p = new ProcessBuilder(rsync_bin, options+"e","ssh", "--log-file", log, local_path, cmd);
                        else if (mode==2) p=new ProcessBuilder(rsync_bin,"-"+rs_options,rs_logfile,arg1,arg2);
                        else p = new ProcessBuilder("echo","Wrong Command");

                        Map<String, String> env = p.environment();
                        env.put("PATH", "/su/bin:/sbin:/vendor/bin:/system/sbin:/system/bin:/su/xbin:/system/xbin");
                        p.directory(new File(context.getApplicationInfo().dataDir));
                        Process process=p.start();

                        //BufferedReader std_output = new BufferedReader(new InputStreamReader(process.getInputStream()));
                        BufferedReader std_error = new BufferedReader(new InputStreamReader(process.getErrorStream()));

                        //StringBuilder builder_1 = new StringBuilder();
                        StringBuilder builder_2 = new StringBuilder();
                        //String out_line = null;
                        String err_line = null;

                        while ( (err_line = std_error.readLine()) != null) {
                            builder_2.append(err_line);
                            builder_2.append(System.getProperty("line.separator"));
                        }

                        String result = builder_2.toString();
                        Log.d("CMD_RESULT",result);
                        if (result.equals("")) result="OK";
                        else result="Warning! Check Log!";

                        Log.d("RESULT",result);
                        Log.d("SHAREDPREFS", "last_result_"+String.valueOf(id));

                        SharedPreferences prefs =  context.getSharedPreferences("configs", MODE_PRIVATE);
                        SharedPreferences.Editor prefseditor = prefs.edit();
                        prefseditor.putString("last_result_"+String.valueOf(id),result);


                        Calendar cal = Calendar.getInstance();
                        long time = cal.getTimeInMillis();

                        SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd/MM HH:mm");
                        prefseditor.putString("last_run_"+String.valueOf(id),formatter.format(time));
                        prefseditor.commit();
                        pref_Edit.putBoolean("is_running",false);
                        pref_Edit.commit();

                    }


                    catch (Exception e) {
                        e.printStackTrace();
                        Log.d("EXCEPTION", e.getMessage());
                    }
                }
            };

            t.start();

    }
}
